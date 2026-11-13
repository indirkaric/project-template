# Platform registry

The only source for other services' addresses. Never read a sibling repo to find them.
`scripts/new_service.py` reads this table to reject a taken port or name, and appends
every service it creates.

`Unknown caller` = what the callee's `SqaClientIdHeaderAuthenticationFilter` grants a
`Sqa-Client-Id` it does not know. `nothing` means 403 until that repo adds the caller
to its allowlist — an edit in that repo, which needs the user's yes.

| Service | Host alias | Port | Base URI | Unknown caller | Notes |
|---|---|---|---|---|---|
| identity | identity.nsight.local | 50002 | http://identity.nsight.local:50002/identity/ | default authorities | PAT validation: `GET /identity/v1/personal-access-tokens/authenticate` |
| artifact | artefact.nsight.local | 50031 | http://artefact.nsight.local:50031/artefact/ | nothing | prefix spelled `artefact` |
| data-storage | data-storage.nsight.local | 50032 | http://data-storage.nsight.local:50032/data-storage/ | nothing | |
| integration | integration.nsight.local | 50033 | http://integration.nsight.local:50033/integration/ | nothing | |
| genie-ui-service | ui.nsight.local | 50034 | http://ui.nsight.local:50034/ui/ | nothing | BFF; normally a caller, not a callee |
| chat | chat.nsight.local | 50035 | http://chat.nsight.local:50035/chat/ | nothing | |
| settings | — | 50037 | http://localhost:50037/settings/ | nothing | no compose alias yet; `chat` calls it on localhost |
| pmga-llm | pmga-llm.nsight.local | 50018 | http://pmga-llm.nsight.local:50018/v3/ | — | Python, read-only; compose alias is `pmga.nsight.local` |
| pmga-llm-proxy | pmga-llm-proxy.nsight.local | 50024 | — | — | Python, read-only, internal to pmga-llm |
| organization | organization.nsight.local | 50001 | http://organization.nsight.local:50001/organization/ | — | `other/`, out of scope |
| notification | notification.nsight.local | 50003 | http://notification.nsight.local:50003/system/ | — | `other/`, out of scope |
| profile | profile.nsight.local | 50004 | http://profile.nsight.local:50004/team/ | — | `other/`, out of scope |
| calendar | calendar.nsight.local | 50011 | http://calendar.nsight.local:50011/calendar/ | — | `other/`, out of scope |
| timesheet | timesheet.nsight.local | 50012 | http://timesheet.nsight.local:50012/timesheet/ | — | `other/`, out of scope |

Other aliases already taken in `development/nSight/docker-compose.yml` (no port listed here, so
not a valid target, but the name is taken): analytics, assets, audit, codebook, data-watcher, dms,
document, eliminator, filter, hr, hr-management, jira, name-origin, pmga, project.

New services start at port 50036 and take the lowest free port at or above it.
