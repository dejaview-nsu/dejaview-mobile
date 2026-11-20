---
name: dejaview-privacy
description: Sanitize and verify DejaView documents, fixtures, logs and design exports before Git or sharing. Use for public artifacts and sensitive data handling.
---

# Privacy

Read docs/privacy.md. Determine the actual publication set from status/diff; private input does not become public because it was processed locally. Keep originals, raw FIG/PDF/JSON/renders, signed URLs and account responses in ignored .local. Verify ignore state; tracked files remain tracked despite ignore rules.

Replace personal values with synthetic data while preserving API field names and requirements. Use Dev 1–4, sample_user, user@example.invalid. Sanitize copies, retain private originals and label derived snapshots. For OpenAPI preserve structural fields and check equivalence excluding intentionally sanitized examples/descriptions.

Run python scripts/privacy_check.py; if available also --private-terms .local/private-terms.json. After staging run --staged to inspect index bytes. Report repository-relative path/line/category, never the matched secret or local private dictionary. Scan images visually before publishing; metadata removal does not erase text in pixels. Do not solve a finding with blanket allowlists.

Distinguish current files from published history, CI logs and caches. Correct authorized local files; do not claim history deletion or change Git author, force-push or revoke credentials without the corresponding task authorization. Notify the user of an exposed secret without repeating it.

Report checked scope, actual results and remaining limits. Scanner cannot identify every name or perform complete OCR. Keep the small remaining manual checks targeted to publishable imagery and ambiguous personal values.
