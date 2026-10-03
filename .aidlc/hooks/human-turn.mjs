#!/usr/bin/env node
// UserPromptSubmit + PostToolUse(AskUserQuestion) hook.
//
// Records the moment a person last answered. The CLI refuses to approve a gate
// or a plan unless a human turn happened after that gate or plan was
// presented, so the agent cannot approve its own work.
//
// Set AIDLC_UNATTENDED=1 for scripted/unattended runs: presence is then NOT
// recorded, so nothing can be approved without a person.

import { existsSync, readFileSync } from "node:fs";
import { findRoot, paths, recordHumanTurn } from "../lib/core.mjs";

try {
  const raw = readFileSync(0, "utf8");
  const input = raw.trim() ? JSON.parse(raw) : {};
  if (process.env.AIDLC_UNATTENDED !== "1") {
    const root = findRoot(process.env.CLAUDE_PROJECT_DIR || input.cwd || process.cwd());
    if (root && existsSync(paths(root).docs)) {
      recordHumanTurn(root, input.hook_event_name === "PostToolUse" ? "question-answer" : "prompt");
    }
  }
} catch {
  // Never block the person's turn.
}
