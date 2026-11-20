#!/usr/bin/env python3
"""Mirror project skills locally; never overwrite an edited/unmanaged target."""
import hashlib
import json
from pathlib import Path


def digest(data):
    return hashlib.sha256(data).hexdigest()


def sync(root):
    root = root.resolve()
    source = root / '.agents/skills'
    target = root / '.claude/skills'
    manifest = target / '.managed.json'
    for parent in [root / '.claude', target, manifest, root / '.agents', source]:
        if parent.is_symlink():
            raise ValueError('Refusing symlink in skill directories')
    old = json.loads(manifest.read_text(encoding='utf-8')) if manifest.exists() else {}
    planned = []
    for file in sorted(source.glob('*/SKILL.md')):
        if file.is_symlink() or file.parent.is_symlink():
            raise ValueError('Refusing symlink source')
        key = file.relative_to(source).as_posix()
        dest = target / key
        if dest.is_symlink() or dest.parent.is_symlink():
            raise ValueError('Refusing symlink target')
        data = file.read_bytes()
        if dest.exists() and dest.read_bytes() != data:
            if digest(dest.read_bytes()) != old.get(key):
                raise ValueError('Local skill has unmanaged changes: ' + key)
        planned.append((key, dest, data))
    # Validate all targets before writing any; stale local copies are reported, not deleted.
    stale = sorted(set(old) - {key for key, _, _ in planned})
    if stale:
        raise ValueError('Stale managed skills require explicit review: ' + ', '.join(stale))
    for _, dest, data in planned:
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)
    target.mkdir(parents=True, exist_ok=True)
    manifest.write_text(json.dumps({key: digest(data) for key, _, data in planned}, indent=2) + '\n', encoding='utf-8')
    return len(planned)


if __name__ == '__main__':
    try:
        print('Synced project skills:', sync(Path(__file__).resolve().parents[1]))
    except (ValueError, OSError) as error:
        print(str(error) if isinstance(error, ValueError) else 'Cannot read/write local skill copies')
        raise SystemExit(1)
