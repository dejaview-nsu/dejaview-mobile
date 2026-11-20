---
name: dejaview-api
description: Implement or verify DejaView HTTP adapters, DTO mapping and contract tests against actual backend evidence. Use for data-layer API changes.
---

# Api

Read docs/planning/backend-evidence.md and the selected baseline operation. Distinguish specification, handler, source test, executed test and live deployment evidence. An open PR is not a deployed capability. Refresh stale evidence before relying on it.

Record method/path, auth, request, statuses and response fields. Existing lists use /users/me/lists/{list_type} and membership uses /users/me/movies/{movie_id}; target mobile paths/DTOs differ. Select an explicit adapter, never probe URLs until one returns 200. Missing required data is a schema error, not a default zero.

Keep Retrofit/DTO in data and map into domain. Handle required/nullable, numeric IDs, timestamps, pagination and unknown additional fields according to schema. 204 has no JSON. Multipart uses one file field with validated MIME and a stream, not a full video ByteArray. Use the shared cookie/error transport.

Differentiate transport failure, malformed response, 401 protected, 403 validation, 429 Retry-After and server failures. Preserve cancellation. No automatic retry of ambiguous search/auth POSTs. Desired-state list operations are not arbitrary toggles.

Test through MockWebServer or equivalent real HTTP boundary: valid/null/schema mismatch, empty, 204, HTML error, pagination, auth and cancel. Live credentials stay local. Report exact mismatches and continue independent UI on explicit demo; never synthesize prod success for a missing rating endpoint.
