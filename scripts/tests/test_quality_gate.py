import importlib.util
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
            import json
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
