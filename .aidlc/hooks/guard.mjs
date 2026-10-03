#!/usr/bin/env node
// PreToolUse hook (Write|Edit|MultiEdit|NotebookEdit|Bash).
//
// Two rules, only while a workflow is active:
//   1. Workflow bookkeeping (state.json, aidlc-state.md, audit.md, active.json,
//      .runtime/) is owned by the CLI. Direct edits are refused.
//   2. Application source can only be written during a stage that writes code,
//      and for Code Generation only after the person approved the plan. Under
//      guard "strict" the approved plan must also be unchanged since approval.
//
// Anything unexpected fails open: a broken hook must never wedge the session.

import { existsSync, readFileSync } from "node:fs";
import { isAbsolute, join, relative, resolve, sep } from "node:path";
import { findRoot, loadConfig, loadGraph, loadState, parseKey, paths, sha256File, stageRecord } from "../lib/core.mjs";

const WRITE_TOOLS = new Set(["Write", "Edit", "MultiEdit", "NotebookEdit"]);
const TOOL_OWNED = /(^|\/)aidlc-docs\/(active\.json|\.runtime\/.*|workflows\/[^/]+\/(state\.json|aidlc-state\.md|audit\.md))$/;

function deny(reason) {
  process.stdout.write(
    JSON.stringify({
      hookSpecificOutput: { hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: reason },
    }),
  );
  process.exit(0);
}

function relPath(root, file) {
  const abs = isAbsolute(file) ? file : resolve(root, file);
  return relative(root, abs).split(sep).join("/");
}

function isOutside(relp) {
  return relp.startsWith("../") || relp === ".." || isAbsolute(relp);
}

// Paths the workflow always allows: its own documents, the framework, Claude
// configuration and the project's CLAUDE.md files.
function isWorkflowSpace(relp) {
  return (
    relp.startsWith("aidlc-docs/") ||
    relp.startsWith(".aidlc/") ||
    relp.startsWith(".claude/") ||
    relp === "CLAUDE.md" ||
    relp === "CLAUDE.local.md"
  );
}

function codeWriteVerdict(root, state, graph, config) {
  const key = state.current;
  if (!key) return "No stage is in progress. Application code is written during Code Generation, after the plan is approved.";
  const { slug } = parseKey(key);
  const stage = graph.bySlug[slug];
  const rec = stageRecord(state, key);
  const active = rec.status === "in-progress" || rec.status === "revising";
  if (!stage || !stage.writes_code || !active) {
    return `The workflow is in "${stage ? stage.name : slug}" (${rec.status}), which does not write application code. Code is written during Code Generation, after the plan is approved.`;
  }
  if (stage.plan_approval) {
    const plan = state.plans[key];
    if (!plan || !plan.approved_at) {
      return `The code generation plan for ${key} is not approved yet. Present the plan and wait for the person's approval before writing code.`;
    }
    if (config.guard === "strict") {
      const file = resolve(root, plan.path);
      if (!existsSync(file) || sha256File(file) !== plan.sha256) {
        return `The approved plan (${plan.path}) changed after approval. Present it again for approval before writing more code.`;
      }
    }
  }
  return null;
}

function main() {
  const raw = readFileSync(0, "utf8");
  if (!raw.trim()) return;
  const input = JSON.parse(raw);
  const root = findRoot(process.env.CLAUDE_PROJECT_DIR || input.cwd || process.cwd());
  if (!root || !existsSync(paths(root).docs)) return;
  const state = loadState(root);
  if (!state || state.status !== "active") return;
  const config = loadConfig(root);
  if (config.guard === "off") return;
  const graph = loadGraph(root);
  const tool = input.tool_name;
  const ti = input.tool_input || {};

  if (tool === "Bash") {
    const cmd = String(ti.command || "");
    if (/human-turn\.mjs/.test(cmd)) {
      deny("Human presence is recorded only when the person answers; it cannot be recorded from a command.");
    }
    const touchesOwned = /aidlc-docs[\\/](active\.json|\.runtime|workflows[\\/][^\s'"]+[\\/](state\.json|aidlc-state\.md|audit\.md))/.test(cmd);
    const writes = /(>|\bsed\s+-i|\bSet-Content\b|\bAdd-Content\b|\bOut-File\b|\btee\b|\bmv\b|\bcp\b|\brm\b|Remove-Item|Move-Item|Copy-Item|writeFile)/.test(cmd);
    if (touchesOwned && writes) {
      deny("Workflow state files are maintained by the AI-DLC CLI (node .aidlc/bin/aidlc.mjs). Use its commands instead of editing them.");
    }
    return;
  }

  if (!WRITE_TOOLS.has(tool)) return;
  const file = ti.file_path || ti.notebook_path;
  if (!file) return;
  const relp = relPath(root, file);
  if (isOutside(relp)) return;
  if (TOOL_OWNED.test(relp)) {
    deny("Workflow state files are maintained by the AI-DLC CLI (node .aidlc/bin/aidlc.mjs). Use its commands instead of editing them.");
  }
  if (isWorkflowSpace(relp)) return;
  const verdict = codeWriteVerdict(root, state, graph, config);
  if (verdict) deny(`${verdict} (blocked write: ${relp})`);
}

try {
  main();
} catch {
  // Fail open.
}
