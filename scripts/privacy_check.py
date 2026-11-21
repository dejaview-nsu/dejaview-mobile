#!/usr/bin/env python3
"""Check publishable files without echoing matched private values. Stdlib only."""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path

PATTERNS = {
    'absolute-local-path': re.compile(r'(?<![\w])(?:[A-Za-z]:[\\/](?!/)|/(?:home|Users)/[^/\s]+|file:' + r'//|\\\\[\w.-]+\\)'),
    'private-key': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'),
    'access-token': re.compile(r'\b(?:gh[pousr]_[A-Za-z0-9]{25,}|github_pat_[A-Za-z0-9_]{30,}|AKIA[A-Z0-9]{16}|figd_[A-Za-z0-9_-]{20,})\b'),
    'private-figma-reference': re.compile(r'https?://(?:www\.)?figma\.com/(?:design|file|api/mcp/asset)/[^\s)"<>]+'),
    'personal-phone': re.compile(r'(?<!\w)\+\d[\d ()-]{8,}\d'),
}
EMAIL = re.compile(r'[A-Za-z0-9_.+%-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})')
GRADLE_WRAPPER_PATH = 'android/gradle/wrapper/gradle-wrapper.jar'
# Gradle 8.12.1: https://gradle.org/release-checksums/
GRADLE_WRAPPER_SHA256 = '2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046'
PRIVATE_EXTENSIONS = {'.fig', '.pdf', '.docx', '.xlsx', '.zip', '.jks', '.keystore', '.p12', '.pem'}
MEDIA_EXTENSIONS = {'.png', '.jpg', '.jpeg', '.webp', '.gif', '.mp4', '.mp3', '.wav'}


def candidates(root, staged=False):
    cmd = ['git', '-C', str(root), 'diff', '--cached', '--name-only', '--diff-filter=ACMR', '-z'] if staged else [
        'git', '-C', str(root), 'ls-files', '-z', '--cached', '--others', '--exclude-standard']
    result = subprocess.run(cmd, capture_output=True)
    if result.returncode:
        raise RuntimeError('Run from a Git repository; file enumeration failed')
    return sorted(set(p for p in result.stdout.decode('utf-8').split('\0') if p))


def inspect_text(text, deny_terms=()):
    findings = []
    for line_no, line in enumerate(text.splitlines(), 1):
        for rule, pattern in PATTERNS.items():
            if pattern.search(line):
                findings.append((line_no, rule))
        for match in EMAIL.finditer(line):
            domain = match.group(1).lower()
            if not (domain.endswith(('.invalid', '.test')) or domain in {'example.com', 'example.org', 'example.net'}):
                findings.append((line_no, 'non-synthetic-email'))
        if any(term.casefold() in line.casefold() for term in deny_terms if term):
            findings.append((line_no, 'local-private-term'))
    return sorted(set(findings))


def scan(root, staged=False, deny_terms=()):
    findings = []
    for rel in candidates(root, staged):
        path = root / rel
        if rel.split('/')[0] in {'.local', 'tmp'} or (path.name.startswith('.env') and path.name != '.env.example') or path.name == 'local.properties':
            findings.append((rel, 0, 'private-location-in-public-tree'))
            continue
        if path.is_symlink():
            findings.append((rel, 0, 'symlink-review-required'))
            continue
        if not staged and not path.is_file():
            continue
        if staged:
            result = subprocess.run(['git', '-C', str(root), 'show', ':' + rel], capture_output=True)
            if result.returncode:
                findings.append((rel, 0, 'index-read-failed'))
                continue
            raw = result.stdout
        else:
            raw = path.read_bytes()
        if rel == GRADLE_WRAPPER_PATH:
            if hashlib.sha256(raw).hexdigest() != GRADLE_WRAPPER_SHA256:
                findings.append((rel, 0, 'unverified-gradle-wrapper'))
            continue
        if path.suffix.lower() in PRIVATE_EXTENSIONS:
            findings.append((rel, 0, 'private-artifact-in-public-tree'))
            continue
        if path.suffix.lower() in MEDIA_EXTENSIONS:
            findings.append((rel, 0, 'media-needs-private-review'))
            continue
        try:
            text = raw.decode('utf-16') if raw.startswith((b'\xff\xfe', b'\xfe\xff')) else raw.decode('utf-8-sig')
        except UnicodeError:
            # Unknown binaries require review, not a silent pass.
            findings.append((rel, 0, 'uninspected-binary'))
            continue
        findings.extend((rel, line, rule) for line, rule in inspect_text(text, deny_terms))
    return findings


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--staged', action='store_true', help='Inspect the index bytes, not working copies')
    parser.add_argument('--private-terms', type=Path, help='Ignored local JSON array of names/identifiers; never echoed')
    args = parser.parse_args()
    terms = json.loads(args.private_terms.read_text(encoding='utf-8')) if args.private_terms else []
    if not isinstance(terms, list) or not all(isinstance(t, str) for t in terms):
        parser.error('private terms must be a JSON array of strings')
    findings = scan(args.root.resolve(), args.staged, terms)
    for path, line, rule in findings:
        print(f'{path}:{line}: {rule}')
    print(f'Privacy check: {len(findings)} finding(s). Values are not printed.')
    return 1 if findings else 0


if __name__ == '__main__':
    raise SystemExit(main())
