#!/usr/bin/env python3
"""Validate local document links, project skill metadata and design catalog."""
import json
import re
import sys
from pathlib import Path
from urllib.parse import unquote
from privacy_check import candidates


def validate(root):
    errors = []
    for name in candidates(root):
        file = root / name
        if file.suffix != '.md' or not file.exists():
            continue
        text = file.read_text(encoding='utf-8')
        clean = re.sub(r'```.*?```', '', text, flags=re.S)
        for target in re.findall(r'\]\(([^)]+)\)', clean):
            target = target.strip('<>').split('#')[0]
            if not target or re.match(r'[a-z]+:', target):
                continue
            if not (file.parent / unquote(target)).exists():
                errors.append(f'{name}: broken local link {target}')
        if file.name == 'SKILL.md':
            header = text.split('---', 2)
            if len(header) != 3 or header[0].strip():
                errors.append(f'{name}: missing frontmatter')
                continue
            skill_name = re.search(r'^name: ([a-z0-9-]+)$', header[1], re.M)
            if not skill_name or skill_name[1] != file.parent.name:
                errors.append(f'{name}: name does not match directory')
            if not re.search(r'^description: .{20,}$', header[1], re.M):
                errors.append(f'{name}: missing usable description')
    skills = list((root / '.agents/skills').glob('*/SKILL.md'))
    if len(skills) != 13:
        errors.append('Expected thirteen project skills; update this invariant when the set changes')
    catalog = json.loads((root / 'docs/design/screens.json').read_text(encoding='utf-8'))['frames']
    ids = [frame['nodeId'] for frame in catalog]
    if len(ids) != len(set(ids)):
        errors.append('Duplicate design node IDs')
    if any(frame['width'] <= 0 or frame['height'] <= 0 for frame in catalog):
        errors.append('Invalid design dimensions')
    return errors


if __name__ == '__main__':
    result = validate(Path(__file__).resolve().parents[1])
    print('\n'.join(result) if result else 'Repository links, skills and catalog: OK')
    sys.exit(bool(result))
