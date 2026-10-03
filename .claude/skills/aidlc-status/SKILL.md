---
name: aidlc-status
description: Show where the AI-DLC workflow of this project stands — active workflow, progress, current and next stage. Use when the person asks for the AI-DLC status or runs /aidlc-status.
user-invocable: true
---

Run `node .aidlc/bin/aidlc.mjs status` from the project root and show the
result to the person, in the language configured in `aidlc-docs/config.json`
(`interaction_language`, default Spanish). Add one line saying what the next
step is and that `/aidlc` continues from there. If there is no workflow, say
so and suggest `/aidlc <qué quieres construir>`.

Also run `node .aidlc/bin/aidlc.mjs workflows` when there is more than one
workflow, and list them briefly. Do not change anything.
