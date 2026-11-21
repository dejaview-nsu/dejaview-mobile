import hashlib
import importlib.util
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('privacy_check', Path(__file__).parents[1] / 'privacy_check.py')
privacy = importlib.util.module_from_spec(spec)
spec.loader.exec_module(privacy)


class PrivacyChecks(unittest.TestCase):
    def test_wrapper_exception_requires_both_exact_path_and_digest(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            subprocess.run(['git', 'init', '-q', str(root)], check=True)
            wrapper = root / 'android/gradle/wrapper/gradle-wrapper.jar'
            legacy = root / 'gradle/wrapper/gradle-wrapper.jar'
            expected = b'\xffsynthetic wrapper'
            for artifact in (wrapper, legacy):
                artifact.parent.mkdir(parents=True)
                artifact.write_bytes(expected)
            with patch.object(privacy, 'GRADLE_WRAPPER_SHA256', hashlib.sha256(expected).hexdigest()):
                self.assertEqual([('gradle/wrapper/gradle-wrapper.jar', 0, 'uninspected-binary')], privacy.scan(root))
                subprocess.run(['git', '-C', str(root), 'add', str(wrapper)], check=True)
                self.assertEqual([], privacy.scan(root, staged=True))
                wrapper.write_bytes(b'modified')
                subprocess.run(['git', '-C', str(root), 'add', str(wrapper)], check=True)
                wrapper.write_bytes(expected)
                self.assertEqual(
                    [('android/gradle/wrapper/gradle-wrapper.jar', 0, 'unverified-gradle-wrapper')],
                    privacy.scan(root, staged=True),
                )

    def test_detects_private_paths_without_flagging_api_or_url(self):
        drive = 'Z' + ':/' + 'Users/example/private'
        self.assertIn((1, 'absolute-local-path'), privacy.inspect_text(drive))
        self.assertEqual([], privacy.inspect_text('https://example.org/api /users/me/watchlist'))

    def test_email_and_private_names(self):
        self.assertTrue(privacy.inspect_text('sample' + '@' + 'mail.testdomain.org'))
        self.assertEqual([], privacy.inspect_text('user@example.invalid'))
        self.assertIn((1, 'local-private-term'), privacy.inspect_text('Example Person', ['Example Person']))

    def test_staged_bytes_are_scanned_even_if_worktree_is_cleaned(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            subprocess.run(['git', 'init', '-q', str(root)], check=True)
            file = root / 'doc.md'
            file.write_text('Z' + ':/' + 'Users/example/private', encoding='utf-8')
            subprocess.run(['git', '-C', str(root), 'add', 'doc.md'], check=True)
            file.write_text('clean', encoding='utf-8')
            self.assertEqual([], privacy.scan(root))
            self.assertTrue(privacy.scan(root, staged=True))
            file.unlink()
            self.assertTrue(privacy.scan(root, staged=True))

    def test_ignored_private_input_is_excluded_but_forced_tracked_input_is_not(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            subprocess.run(['git', 'init', '-q', str(root)], check=True)
            (root / '.gitignore').write_text('.local/\n', encoding='utf-8')
            (root / '.local').mkdir()
            (root / '.local' / 'input.fig').write_bytes(b'private')
            self.assertEqual([], privacy.scan(root))
            subprocess.run(['git', '-C', str(root), 'add', '-f', '.local/input.fig'], check=True)
            self.assertTrue(privacy.scan(root))

    def test_forced_private_text_is_blocked_even_without_recognizable_pii(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            subprocess.run(['git', 'init', '-q', str(root)], check=True)
            (root / '.local').mkdir()
            (root / '.local' / 'raw.json').write_text('{"name":"unrecognized"}')
            self.assertTrue(privacy.scan(root))


if __name__ == '__main__':
    unittest.main()
