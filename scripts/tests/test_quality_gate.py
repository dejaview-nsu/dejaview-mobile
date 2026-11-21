import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
from types import SimpleNamespace
import time

sys.path.insert(0, str(Path(__file__).parents[1]))
spec = importlib.util.spec_from_file_location('quality_gate', Path(__file__).parents[1] / 'quality_gate.py')
gate = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gate)


class QualityGateTests(unittest.TestCase):
    def test_android_cannot_pass_without_real_configuration(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with self.assertRaises(ValueError):
                gate.android_commands(root, {})
            for name in ['gradlew', 'gradlew.bat']:
                (root / name).touch()
            with self.assertRaises(ValueError):
                gate.android_commands(root, {'commands': [], 'junit_reports': ['results/*.xml']})

    def test_reports_reject_missing_empty_skipped_and_failed(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with self.assertRaises(ValueError):
                gate.check_reports(root, ['test.xml'])
            for body in ['', '<testcase><skipped/></testcase>', '<testcase><failure/></testcase>']:
                (root / 'test.xml').write_text('<testsuite>' + body + '</testsuite>')
                with self.assertRaises(ValueError):
                    gate.check_reports(root, ['test.xml'])
            (root / 'test.xml').write_text('<testsuite><testcase name="real"/></testsuite>')
            gate.check_reports(root, ['test.xml'])
            with self.assertRaises(ValueError):
                gate.check_reports(root, ['test.xml'], not_before=time.time() + 1)

    def test_failed_command_stops_pipeline_and_records_failure(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with patch.object(gate.subprocess, 'run', return_value=SimpleNamespace(returncode=7)) as process:
                self.assertEqual(1, gate.run(root, scope='repository'))
                self.assertEqual(1, process.call_count)
            report = json.loads((root / '.local/quality/last-run.json').read_text())
            self.assertEqual('FAILED', report['status'])
            self.assertEqual(7, report['checks'][0]['exit_code'])

    def test_ui_requires_its_own_command_and_results(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in ['gradlew', 'gradlew.bat']:
                (root / name).touch()
            config = {'commands': [{'group': group, 'argv': ['gradlew', 'check']} for group in gate.REQUIRED], 'junit_reports': ['result.xml']}
            gate.android_commands(root, config)
            with self.assertRaises(ValueError):
                gate.android_commands(root, config, ui=True)

    def test_android_gate_uses_project_directory_for_wrapper_and_fresh_reports(self):
        for project_path in ('.', 'android'):
            with self.subTest(project=project_path), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                (root / 'android').mkdir()
                project = root / project_path
                project.mkdir(exist_ok=True)
                for name in gate.WRAPPER_SCRIPTS:
                    (project / name).touch()
                groups = gate.REQUIRED | {'ui'}
                config = {
                    'commands': [{'group': group, 'argv': ['gradlew', group]} for group in sorted(groups)],
                    'junit_reports': ['reports/unit.xml'],
                    'ui_junit_reports': ['reports/ui.xml'],
                }
                (root / 'config').mkdir()
                (root / 'config/quality-gate.json').write_text(json.dumps({'android': config}))

                def run_command(argv, *, cwd, check):
                    if argv[0] == str(project / ('gradlew.bat' if gate.os.name == 'nt' else 'gradlew')):
                        self.assertEqual(project, cwd)
                        if argv[1] in {'unit', 'ui'}:
                            (cwd / 'reports').mkdir(exist_ok=True)
                            (cwd / 'reports' / (argv[1] + '.xml')).write_text('<testsuite><testcase/></testsuite>')
                    else:
                        self.assertEqual(root, cwd)
                    return SimpleNamespace(returncode=0)

                with patch.object(gate.subprocess, 'run', side_effect=run_command) as process:
                    self.assertEqual(0, gate.run(root, ui=True))
                    self.assertEqual(4 + len(groups), process.call_count)
                report = json.loads((root / '.local/quality/last-run.json').read_text())
                self.assertEqual('android', report['scope'])
                self.assertEqual('PASSED', report['status'])

    def test_partial_nested_wrapper_cannot_fall_back_to_repository_scope(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            project = root / 'android'
            project.mkdir()
            (project / 'gradlew').touch()
            (root / 'config').mkdir()
            (root / 'config/quality-gate.json').write_text('{"android": {}}')
            self.assertEqual(1, gate.run(root))
            report = json.loads((root / '.local/quality/last-run.json').read_text())
            self.assertEqual('android', report['scope'])
            self.assertIn('Android wrapper is missing', report['reason'])

    def test_android_source_triggers_android_scope_without_wrapper(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with patch.object(gate, 'candidates', return_value=['android/app/src/main/Sample.kt']):
                self.assertTrue(gate.android_present(root))
