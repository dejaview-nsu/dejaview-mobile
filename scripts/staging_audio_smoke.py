"""Run the opt-in prodDebug Android test against an explicitly selected HTTPS origin."""

import argparse
import getpass
import json
import os
from pathlib import Path
import secrets
import shutil
import socket
import subprocess
import time
from urllib.parse import urlsplit


def run_smoke(serial, origin):
    endpoint = urlsplit(origin)
    if (endpoint.scheme != 'https' or not endpoint.hostname or endpoint.username
            or endpoint.password or endpoint.path or endpoint.query or endpoint.fragment):
        raise ValueError('Expected an HTTPS origin without a path or credentials')
    sdk = os.environ.get('ANDROID_HOME')
    adb = shutil.which('adb') or shutil.which('adb', path=str(Path(sdk or '') / 'platform-tools'))
    if not adb:
        raise RuntimeError('Set ANDROID_HOME or add adb to PATH')
    command = [adb, '-s', serial]

    def run(*args):
        return subprocess.check_output(command + list(args), text=True, timeout=120).strip()

    root = Path(__file__).resolve().parents[1] / 'android/app/build/outputs/apk'
    for apk in (root / 'prod/debug/app-prod-debug.apk',
                root / 'androidTest/prod/debug/app-prod-debug-androidTest.apk'):
        if run('install', '-r', str(apk)).splitlines()[-1] != 'Success':
            raise RuntimeError('Could not install the smoke test or application')

    login = input('Test account login: ').strip()
    password = getpass.getpass('Test account password: ')
    channel = 'dv-smoke-' + secrets.token_hex(16)
    port = run('forward', 'tcp:0', 'localabstract:' + channel)
    try:
        with subprocess.Popen(command + [
            'shell', 'am', 'instrument', '-w', '-e', 'class', 'ru.dejaview.app.StagingAudioSmoke',
            '-e', 'smokeSocket', channel, 'ru.dejaview.mobile.test/androidx.test.runner.AndroidJUnitRunner',
        ], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True) as test:
            try:
                send_credentials(test, int(port), origin, login, password)
                output, _ = test.communicate(timeout=150)
                if test.returncode or 'OK (1 test)' not in output:
                    errors = [line for line in output.splitlines() if 'Live login rejected' in line
                              or 'Server logout was not confirmed' in line or 'StagingAudioSmoke.kt:' in line]
                    raise RuntimeError('Live Android smoke failed. ' + ' '.join(errors))
            finally:
                if test.poll() is None:
                    test.kill()
    finally:
        run('forward', '--remove', 'tcp:' + port)
    print('PASS: Android login, session restore, audio search and logout')


def send_credentials(test, port, origin, login, password):
    deadline = time.monotonic() + 45
    while time.monotonic() < deadline and test.poll() is None:
        try:
            with socket.create_connection(('127.0.0.1', port), timeout=2) as connection:
                with connection.makefile('rb') as reader:
                    configured = reader.readline(512).decode().strip()
                if configured:
                    if configured != origin:
                        raise ValueError('APK origin differs from the requested smoke origin')
                    payload = json.dumps({'login': login, 'password': password}) + '\n'
                    connection.sendall(payload.encode())
                    return
        except (ConnectionError, TimeoutError):
            pass
        time.sleep(.3)
    raise RuntimeError('The test did not open its adb input channel')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--serial', required=True, help='Dedicated Android test device from adb devices')
    parser.add_argument('--origin', required=True, help='Expected HTTPS staging origin')
    args = parser.parse_args()
    run_smoke(args.serial, args.origin)
