---
name: dejaview-auth
description: Implement or review DejaView cookie sessions, login, CAPTCHA, logout and mobile identity flows. Use for authentication and account isolation changes.
---

# Auth

Read session contract and current backend evidence. Browser OIDC exists; native exchange is a separate contract. Browser cookies do not automatically reach OkHttp. Reuse established security/platform libraries, never implement cryptographic primitives.

Use one CookieStore/SessionReader with host/domain/path/secure/expiry scope. Keystore protects encryption keys; plain DataStore is not encrypted storage. Do not persist passwords. Restore checks session; network failure does not authenticate an unknown session. Loss of key/corrupted storage must fail safely.

Protected 401 invalidates session once; login 400/403 may mean credentials, CAPTCHA or unconfirmed account. Interceptor never navigates. Logout clears local personal state even if server logout fails and reports that failure. Session generation prevents late responses/Set-Cookie from restoring the old account.

Current session JSON lacks stable user_id; do not use mutable username as permanent history identity. Respect agreed session namespace. PendingAction records desired state, executes once after success and is removed on cancel. Media requires reconfirmation. No secrets in routes/SavedState/logs.

CAPTCHA uses a fresh token, allowlisted origin and minimal bridge. Do not bypass it for demo. Reset/registration links handle expiry/reuse/cancel. Native OIDC requires agreed state/PKCE, callback allowlist and one-time native exchange; provider secret stays server-side.

Automate concurrent 401, late Set-Cookie, expiry, corrupted storage, logout during request, account switching and pending action duplication. Check platform storage and real auth flow separately when environment permits. Missing live credentials leaves that check NOT RUN, not a fake successful login.
