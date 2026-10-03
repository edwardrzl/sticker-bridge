# .aidlc — AI-DLC framework

Installed by the AI-DLC installer. This directory is **framework**: an update
replaces it. Project data lives in `aidlc-docs/` and is never touched by
updates.

| Path | What |
|---|---|
| `workflow.json` | Stages, phases and scopes (the workflow graph) |
| `defaults.json` | Default languages and guard mode (copied to `aidlc-docs/config.json` on first use) |
| `method/` | Protocols the conductor follows (questions, gates, construction) |
| `stages/<phase>/<stage>.md` | Instructions for each stage |
| `agents/` | Expert personas that lead the stages |
| `knowledge/` | Reference guides the personas load on demand |
| `templates/` | Document templates |
| `bin/aidlc.mjs` | CLI that owns workflow state, gates and the audit trail |
| `hooks/` | Claude Code hooks: code-write guard, human-turn recorder, session resume |
| `lib/core.mjs` | Shared logic for the CLI and hooks |

Run `node .aidlc/bin/aidlc.mjs help` for the CLI and
`node .aidlc/bin/aidlc.mjs doctor` to check the installation.

To customize a stage for one project, edit its file here and keep a note: the
next update overwrites it. For changes you want everywhere, change the source
repository instead.
