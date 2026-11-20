#!/usr/bin/env python3
"""Run explicit checks; an unconfigured Android gate never passes."""
import argparse
import glob
import json
import os
from pathlib import Path
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from privacy_check import candidates

REQUIRED = {'format', 'static', 'architecture', 'unit', 'coverage', 'build-demo', 'build-prod'}


def android_present(root):
    return (root / 'gradlew').exists() or (root / 'gradlew.bat').exists() or any(
        Path(p).suffix in {'.kt', '.java'} for p in candidates(root))


def android_commands(root, config, ui=False):
    if not (root / 'gradlew').exists() or not (root / 'gradlew.bat').exists():
        raise ValueError('Android wrapper is missing; Android checks NOT RUN')
    commands = config.get('commands', [])
    needed = REQUIRED | ({'ui'} if ui else set())
    if not needed.issubset({c.get('group') for c in commands}):
        raise ValueError('Android quality groups are not configured')
    if not config.get('junit_reports') or (ui and not config.get('ui_junit_reports')):
        raise ValueError('JUnit report paths are not configured')
    result = []
    for command in commands:
        if command['group'] == 'ui' and not ui:
            continue
        argv = command.get('argv')
        if not isinstance(argv, list) or not argv or not all(isinstance(v, str) and v for v in argv):
            raise ValueError('Each command requires a nonempty argv string list')
        argv = argv.copy()
        if argv[0] == 'gradlew':
            argv[0] = str(root / ('gradlew.bat' if os.name == 'nt' else 'gradlew'))
        result.append((command['group'], argv))
    return result


def check_reports(root, patterns, not_before=None):
    for pattern in patterns:
        if Path(pattern).is_absolute() or '..' in Path(pattern).parts:
            raise ValueError('Report paths must remain inside the repository')
        files = glob.glob(str(root / pattern), recursive=True)
        if not files:
            raise ValueError('Required JUnit report pattern matched no files')
        executed = 0
        for filename in files:
            if not_before is not None and Path(filename).stat().st_mtime < not_before:
                raise ValueError('JUnit report is stale; rerun the actual test task')
            tree = ET.parse(filename)
            for suite in tree.iter('testsuite'):
                if int(suite.get('failures', '0')) or int(suite.get('errors', '0')):
                    raise ValueError('JUnit suite reports failures/errors')
            for case in tree.iter('testcase'):
                if case.find('failure') is not None or case.find('error') is not None:
                    raise ValueError('JUnit reports contain failures/errors')
                if case.find('skipped') is None:
                    executed += 1
        if not executed:
            raise ValueError('Required JUnit reports contain zero executed tests')


def run(root, scope='auto', ui=False):
    if scope == 'auto':
        scope = 'android' if android_present(root) else 'repository'
    report = {'scope': scope, 'status': 'FAILED', 'checks': []}
    started = time.time()
    commands = [
        ('documents', [sys.executable, 'scripts/validate_repo.py']),
        ('privacy', [sys.executable, 'scripts/privacy_check.py']),
        ('tools-python', [sys.executable, '-m', 'unittest', 'discover', '-s', 'scripts/tests']),
        ('tools-node', ['node', '--test', 'tools/design/extract-fig.test.mjs', 'tools/figma-export/export.test.mjs']),
    ]
    try:
        if ui and scope != 'android':
            raise ValueError('--ui requires Android scope')
        if scope == 'android':
            config = json.loads((root / 'config/quality-gate.json').read_text(encoding='utf-8'))['android']
            commands += android_commands(root, config, ui)
        for group, argv in commands:
            print('CHECK:', group, flush=True)
            result = subprocess.run(argv, cwd=root, check=False)
            report['checks'].append({'group': group, 'exit_code': result.returncode})
            if result.returncode:
                raise ValueError('Failed check: ' + group)
        if scope == 'android':
            check_reports(root, config['junit_reports'], started)
            if ui:
                check_reports(root, config['ui_junit_reports'], started)
        report['status'] = 'PASSED'
    except (ValueError, OSError, KeyError, ET.ParseError) as error:
        report['reason'] = str(error) if isinstance(error, ValueError) else 'Configuration, tool or report could not be read/executed'
        print(report['reason'])
    out = root / '.local/quality'
    out.mkdir(parents=True, exist_ok=True)
    (out / 'last-run.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print(scope.upper(), report['status'])
    return 0 if report['status'] == 'PASSED' else 1


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--scope', choices=['auto', 'repository', 'android'], default='auto')
    parser.add_argument('--ui', action='store_true')
    args = parser.parse_args()
    sys.exit(run(Path(__file__).resolve().parents[1], args.scope, args.ui))
