# Question rendering (Claude Code)

How the protocol's questions map to Claude Code's `AskUserQuestion` tool.

## Limits of the tool

- 1–4 questions per call; 2–4 options per question; the tool always adds an
  "Other" choice by itself.
- `header` is a chip of at most 12 characters.
- `multiSelect: true` for "elige todas las que apliquen" questions.

## Mapping a questions-file entry

| File | AskUserQuestion |
|---|---|
| `## Q3 — Base de datos` | `header: "Q3 Base datos"` (≤12 chars) |
| context line + question | `question` (one or two sentences) |
| `A.`–`E.` options | `options[].label` = the option text without the letter; `description` = the trade-off in one line |
| `X. Otra (especifica)` | not rendered — the tool's own "Other" covers it |
| `(recomendada)` | put that option first and append " (recomendada)" to its label |

A question with **more than 4 real options** (A–E): do not drop any. Either
split into two calls ("¿Alguna de estas…?" with the first 3 + "Ninguna de
estas", then the rest), or present it as a numbered prose list in chat and let
the person reply with the letter. The file keeps the full list either way.

## Batches in "Guíame" mode

- Up to 4 questions per AskUserQuestion call, in file order.
- Before the first batch, tell the person once: "Si eliges «Other» en alguna,
  la conversamos antes de fijar la respuesta."
- After each batch, write the answers into the `[Answer]:` tags immediately
  (letter + text, e.g. `[Answer]: B — PostgreSQL`), then continue.
- "Other" on a question → discuss it, then ask for the final answer before
  moving on.

## Gates and checkpoints

Approval gates, plan approval, summary confirmation and the answering-mode
question are single-question calls:

```
question: "Requisitos completados. ¿Cómo quieres continuar?"
header: "Aprobación"
options:
  - label: "Aprobar"            description: "Continuar con Tech Stack Definition"
  - label: "Solicitar cambios"  description: "Indicar qué revisar"
```

The tool's "Other" on a gate is never an approval: discuss, then ask again.

## Numbered-prose fallback

If `AskUserQuestion` is unavailable or fails, present the same options as a
numbered list (each question numbered from 1 on its own) plus a final
"Otra" line, end the turn, and map the reply back to the option.
