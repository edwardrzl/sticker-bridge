#!/usr/bin/env node
// SessionStart hook: when a workflow is in flight, put a one-paragraph resume
// point into the session context so a new or compacted session picks up where
// the last one stopped.

import { existsSync, readFileSync } from "node:fs";
import { findRoot, loadGraph, loadState, nextStep, paths, progress } from "../lib/core.mjs";

try {
  const raw = readFileSync(0, "utf8");
  const input = raw.trim() ? JSON.parse(raw) : {};
  const root = findRoot(process.env.CLAUDE_PROJECT_DIR || input.cwd || process.cwd());
  if (root && existsSync(paths(root).docs)) {
    const state = loadState(root);
    if (state && state.status !== "completed") {
      const graph = loadGraph(root);
      const nxt = nextStep(graph, state);
      const prog = progress(graph, state);
      const where = nxt.kind === "run-stage" ? `${nxt.key} (${nxt.status})` : nxt.kind;
      process.stdout.write(
        [
          `AI-DLC: workflow "${state.id}" is ${state.status} — scope ${state.scope}, ${prog.done}/${prog.total} stages done, next: ${where}.`,
          `Request: ${state.request}`,
          state.status === "parked"
            ? "It is parked; the person can continue it with /aidlc."
            : "To continue the workflow, use the /aidlc skill (it resumes from this point). Do not write application code outside the workflow's Code Generation stage.",
        ].join("\n") + "\n",
      );
    }
  }
} catch {
  // Never block session start.
}
