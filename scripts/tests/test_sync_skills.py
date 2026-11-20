import importlib.util
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location('sync_skills', Path(__file__).parents[1] / 'sync_skills.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class SkillSync(unittest.TestCase):
    def test_updates_managed_but_preserves_user_changes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / '.agents/skills/example/SKILL.md'
            source.parent.mkdir(parents=True)
            source.write_text('v1')
            self.assertEqual(1, module.sync(root))
            dest = root / '.claude/skills/example/SKILL.md'
            source.write_text('v2')
            module.sync(root)
            self.assertEqual('v2', dest.read_text())
            dest.write_text('user edit')
            source.write_text('v3')
            with self.assertRaises(ValueError):
                module.sync(root)
            self.assertEqual('user edit', dest.read_text())

    def test_refuses_unmanaged_target(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for folder, content in [('.agents', 'canonical'), ('.claude', 'unmanaged')]:
                file = root / folder / 'skills/example/SKILL.md'
                file.parent.mkdir(parents=True)
                file.write_text(content)
            with self.assertRaises(ValueError):
                module.sync(root)
