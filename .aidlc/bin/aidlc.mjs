#!/usr/bin/env node
// AI-DLC workflow CLI.
//
// This tool owns every state transition, gate and audit entry. The agent
// never edits state.json, aidlc-state.md or audit.md by hand: it calls these
// commands, and the guard hook refuses direct edits to those files.
//
// Usage: node .aidlc/bin/aidlc.mjs <command> [args]   (run `help` for the list)

import { copyFileSync, existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { join, resolve } from "node:path";
import {
  AidlcError,
  LOGGABLE_EVENTS,
  STATE_VERSION,
  STATUS,
  appendAudit,
  artifactDir,
  detectWorkspace,
  findRoot,
  humanActedSince,
  listWorkflows,
  loadActiveId,
  loadConfig,
  loadGraph,
  loadState,
  markdownFilesIn,
  nextStep,
  now,
  parseKey,
  parseUnitsFile,
  paths,
  plannedKeys,
  progress,
  questionFilesIn,
  rel,
  saveState,
  setActiveId,
  sha256File,
  slugify,
  stageRecord,
  suggestScope,
  syncStageMap,
  unansweredQuestions,
  validateOrder,
  workflowPaths,
  writeJsonAtomic,
} from "../lib/core.mjs";

const HELP = `AI-DLC CLI — node .aidlc/bin/aidlc.mjs <command>

Workspace
  detect [--request "<text>"]         Detect greenfield/brownfield and suggest a scope (JSON)
  doctor                              Check the installation
  workflows                           List workflows in this project
  switch <workflow-id>                Make another workflow the active one

Workflow lifecycle
  init --request "<text>" | --request-file <path>  [--scope <s>] [--type greenfield|brownfield] [--slug <s>]
  status [--json]                     Show the active workflow
  next                                Print the next step as JSON (read-only)
  park | resume                       Pause / resume the active workflow
  scope <name>                        Change the scope of the active workflow
  depth <minimal|standard|comprehensive> [--tests <same>]

Stage lifecycle (key = <stage-slug> or <stage-slug>@<unit-id>)
  start <key>                         Mark the next stage in progress
  complete <key>                      Complete a stage that has no approval gate
  gate <key>                          Open the approval gate (checks answers and artifacts)
  approve <key> --choice "<label>"    Record the human's approval (needs a human turn after the gate)
  reject <key> --reason "<feedback>"  Record requested changes (needs a human turn after the gate)
  revised <key>                       Re-open the gate after a revision
  skip <key> --reason "<why>"         Skip the next stage when its condition does not apply

Code generation plan
  plan-request <key> --plan <path>    Record that the plan was presented for approval
  plan-approve <key> --choice "<label>"  Record the human's plan approval
  plan-reject <key> --reason "<feedback>"

Units of work
  units set --file <path>             Register units from a fenced \`\`\`json {"units": [...]} block
  units order <id,id,...>             Set the build order (must respect dependencies)

Records
  log --stage <key> --event <EVENT> --details "<text>"   Events: ${[...LOGGABLE_EVENTS].join(", ")}
  questions <file-or-dir>             List unanswered [Answer]: tags
  path <key>                          Print the artifact directory for a stage
`;

// ---------------------------------------------------------------------------

function parseArgs(argv) {
  const positional = [];
  const flags = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a.startsWith("--")) {
      const name = a.slice(2);
      const next = argv[i + 1];
      if (next === undefined || next.startsWith("--")) flags[name] = true;
      else {
        flags[name] = next;
        i++;
      }
    } else positional.push(a);
  }
  return { positional, flags };
}

function out(obj) {
  process.stdout.write(typeof obj === "string" ? obj + "\n" : JSON.stringify(obj, null, 2) + "\n");
}

function requireRoot() {
  const root = findRoot();
  if (!root) throw new AidlcError("AI-DLC is not installed here: no .aidlc/workflow.json found in this directory or its parents.");
  return root;
}

function requireState(root, graph) {
  const state = loadState(root);
  if (!state) throw new AidlcError("There is no active workflow. Start one with `init --request \"...\"`.");
  if (state.version !== STATE_VERSION) throw new AidlcError(`Unsupported state version ${state.version} (expected ${STATE_VERSION}).`);
  return state;
}

function requireFlag(flags, name, hint) {
  const v = flags[name];
  if (v === undefined || v === true || String(v).trim() === "") throw new AidlcError(`Missing --${name}${hint ? ` (${hint})` : ""}.`);
  return String(v);
}

function stageOf(graph, key) {
  const { slug, unit } = parseKey(key);
  const stage = graph.bySlug[slug];
  if (!stage) throw new AidlcError(`Unknown stage: ${slug}`);
  return { stage, unit };
}

function assertIsNext(graph, state, key) {
  const nxt = nextStep(graph, state);
  if (nxt.kind !== "run-stage") throw new AidlcError(`No stage is runnable right now (${nxt.kind}${nxt.message ? `: ${nxt.message}` : ""}).`);
  if (nxt.key !== key) throw new AidlcError(`${key} is not the current step. The workflow is at ${nxt.key} (${nxt.status}).`);
  return nxt;
}

function assertStatus(state, key, allowed) {
  const rec = stageRecord(state, key);
  if (!allowed.includes(rec.status)) {
    throw new AidlcError(`${key} is ${rec.status}; this command needs it to be ${allowed.join(" or ")}.`);
  }
  return rec;
}

function followingStep(graph, state, key) {
  const planned = plannedKeys(graph, state);
  const i = planned.findIndex((p) => p.key === key);
  const after = planned.slice(i + 1).find((p) => {
    const rec = stageRecord(state, p.key);
    return rec.status !== STATUS.COMPLETED && rec.status !== STATUS.SKIPPED;
  });
  if (!after) return null;
  return { key: after.key, name: after.stage.name + (after.unit ? ` (${after.unit})` : "") };
}

function describe(root, graph, state, nxt) {
  if (nxt.kind !== "run-stage") return nxt;
  const { stage, unit, key } = nxt;
  const config = loadConfig(root);
  const rec = stageRecord(state, key);
  const planned = plannedKeys(graph, state);
  const idx = planned.findIndex((p) => p.key === key);
  const prev = planned[idx - 1];
  const plan = state.plans[key] || null;
  return {
    kind: "run-stage",
    key,
    slug: stage.slug,
    name: stage.name,
    unit,
    phase: stage.phase,
    status: rec.status,
    revisions: rec.revisions || 0,
    gate: stage.gate,
    plan_approval: Boolean(stage.plan_approval),
    plan: plan ? { path: plan.path, presented: Boolean(plan.requested_at), approved: Boolean(plan.approved_at) } : null,
    writes_code: Boolean(stage.writes_code),
    registers_units: Boolean(stage.registers_units),
    condition: stage.condition || null,
    first_in_phase: !prev || prev.stage.phase !== stage.phase,
    stage_file: `.aidlc/stages/${stage.phase}/${stage.slug}.md`,
    persona_file: `.aidlc/agents/${stage.lead}.md`,
    artifact_dir: rel(root, artifactDir(root, state, stage, unit)),
    workflow_dir: rel(root, workflowPaths(root, state.id).dir),
    next_after: followingStep(graph, state, key),
    project: { workflow: state.id, type: state.type, scope: state.scope, depth: state.depth, test_strategy: state.test_strategy },
    languages: { interaction: config.interaction_language, artifacts: config.artifact_language, code: config.code_language },
    guard: config.guard,
    progress: progress(graph, state),
  };
}

// ---------------------------------------------------------------------------
// Commands

const commands = {
  help() {
    out(HELP);
  },

  detect(root, { flags }) {
    const graph = loadGraph(root);
    const ws = detectWorkspace(root);
    const request = flags.request ? String(flags.request) : "";
    out({ ...ws, suggested_scope: suggestScope(graph, request, ws.type), scopes: graph.scopes });
  },

  doctor(root) {
    const p = paths(root);
    const checks = [];
    const check = (ok, what) => checks.push(`${ok ? "OK  " : "FAIL"} ${what}`);
    const graph = loadGraph(root);
    check(true, `workflow graph: ${graph.stages.length} stages, ${Object.keys(graph.scopes).length} scopes`);
    for (const s of graph.stages) check(existsSync(join(root, ".aidlc", "stages", s.phase, `${s.slug}.md`)), `stage file ${s.phase}/${s.slug}.md`);
    for (const lead of new Set(graph.stages.map((s) => s.lead))) check(existsSync(join(root, ".aidlc", "agents", `${lead}.md`)), `persona ${lead}.md`);
    check(existsSync(join(root, ".claude", "skills", "aidlc", "SKILL.md")), "skill .claude/skills/aidlc/SKILL.md");
    const settingsFile = join(root, ".claude", "settings.json");
    let hooksOk = false;
    if (existsSync(settingsFile)) {
      try {
        hooksOk = readFileSync(settingsFile, "utf8").includes(".aidlc/hooks/guard.mjs");
      } catch {}
    }
    check(hooksOk, "hooks registered in .claude/settings.json");
    check(existsSync(p.docs), "aidlc-docs/ exists");
    const major = Number(process.versions.node.split(".")[0]);
    check(major >= 18, `Node.js ${process.versions.node} (>= 18 required)`);
    out(checks.join("\n"));
    if (checks.some((c) => c.startsWith("FAIL"))) process.exitCode = 1;
  },

  workflows(root) {
    const active = loadActiveId(root);
    const list = listWorkflows(root);
    if (!list.length) return out("No workflows yet.");
    for (const w of list) out(`${w.id === active ? "*" : " "} ${w.id}  [${w.status}, ${w.scope}]  ${w.request.split(/\r?\n/)[0].slice(0, 80)}`);
  },

  switch(root, { positional }) {
    const id = positional[0];
    if (!id) throw new AidlcError("Usage: switch <workflow-id>");
    if (!existsSync(workflowPaths(root, id).state)) throw new AidlcError(`Unknown workflow: ${id}`);
    const current = loadState(root);
    if (current && current.id !== id && current.status === "active") {
      throw new AidlcError(`Workflow ${current.id} is still active. Park it first with \`park\`.`);
    }
    setActiveId(root, id);
    out(`Active workflow: ${id}`);
  },

  init(root, { flags }) {
    const graph = loadGraph(root);
    const p = paths(root);
    let request = flags["request-file"] ? readFileSync(resolve(String(flags["request-file"])), "utf8") : flags.request;
    if (!request || request === true || !String(request).trim()) throw new AidlcError('Missing --request "<what to build>" (or --request-file <path>).');
    request = String(request).trim();

    const current = loadState(root);
    if (current && current.status === "active") {
      throw new AidlcError(`Workflow ${current.id} is still active. Finish it, or park it with \`park\` before starting a new one.`);
    }

    const ws = detectWorkspace(root);
    const type = flags.type ? String(flags.type) : ws.type;
    if (!["greenfield", "brownfield"].includes(type)) throw new AidlcError("--type must be greenfield or brownfield.");
    const scope = flags.scope ? String(flags.scope) : suggestScope(graph, request, type).scope;
    if (!graph.scopes[scope]) throw new AidlcError(`Unknown scope "${scope}". Available: ${Object.keys(graph.scopes).join(", ")}`);

    const n = String(listWorkflows(root).length + 1).padStart(3, "0");
    const id = `${n}-${flags.slug ? slugify(String(flags.slug)) : slugify(request)}`;
    const wp = workflowPaths(root, id);
    if (existsSync(wp.state)) throw new AidlcError(`Workflow ${id} already exists.`);

    // Project-level files are created once and never overwritten.
    mkdirSync(p.memory, { recursive: true });
    if (!existsSync(p.config)) writeJsonAtomic(p.config, loadConfig(root));
    const memoryTemplate = join(p.framework, "templates", "memory-project.md");
    const memoryFile = join(p.memory, "project.md");
    if (!existsSync(memoryFile) && existsSync(memoryTemplate)) copyFileSync(memoryTemplate, memoryFile);
    const gitignore = join(p.docs, ".gitignore");
    if (!existsSync(gitignore)) writeFileSync(gitignore, ".runtime/\n", "utf8");

    const s = graph.scopes[scope];
    const state = {
      version: STATE_VERSION,
      id,
      request: request.split(/\r?\n/)[0].slice(0, 300),
      type,
      scope,
      depth: s.depth,
      test_strategy: s.test_strategy,
      status: "active",
      created_at: now(),
      updated_at: now(),
      current: null,
      stages: {},
      units: null,
      unit_order: null,
      plans: {},
      workspace: { languages: ws.languages.map((l) => l.name), manifests: ws.manifests },
    };
    mkdirSync(wp.dir, { recursive: true });
    writeFileSync(wp.request, `# Request\n\n${request}\n`, "utf8");
    syncStageMap(graph, state);
    appendAudit(root, state, "WORKFLOW_CREATED", null, { Scope: scope, Type: type, Depth: state.depth, Request: state.request });
    setActiveId(root, id);
    saveState(root, graph, state);
    out({ created: id, type, scope, depth: state.depth, test_strategy: state.test_strategy, workflow_dir: rel(root, wp.dir) });
  },

  status(root, { flags }) {
    const graph = loadGraph(root);
    const state = loadState(root);
    if (!state) {
      if (flags.json) return out({ active: null, workflows: listWorkflows(root) });
      return out("No active workflow.");
    }
    const nxt = describe(root, graph, state, nextStep(graph, state));
    if (flags.json) {
      return out({
        active: state.id,
        status: state.status,
        request: state.request,
        type: state.type,
        scope: state.scope,
        depth: state.depth,
        test_strategy: state.test_strategy,
        units: state.units,
        progress: progress(graph, state),
        next: nxt,
      });
    }
    out(readFileSync(workflowPaths(root, state.id).stateMd, "utf8"));
  },

  next(root) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    out(describe(root, graph, state, nextStep(graph, state)));
  },

  park(root) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    if (state.status !== "active") throw new AidlcError(`Workflow is ${state.status}, not active.`);
    state.status = "parked";
    appendAudit(root, state, "WORKFLOW_PARKED", state.current, {});
    saveState(root, graph, state);
    out(`Parked ${state.id}. Resume with \`resume\`.`);
  },

  resume(root) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    if (state.status !== "parked") throw new AidlcError(`Workflow is ${state.status}, not parked.`);
    state.status = "active";
    appendAudit(root, state, "WORKFLOW_RESUMED", state.current, {});
    saveState(root, graph, state);
    out(describe(root, graph, state, nextStep(graph, state)));
  },

  scope(root, { positional }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const scope = positional[0];
    if (!scope || !graph.scopes[scope]) throw new AidlcError(`Usage: scope <${Object.keys(graph.scopes).join("|")}>`);
    const from = state.scope;
    state.scope = scope;
    state.depth = graph.scopes[scope].depth;
    state.test_strategy = graph.scopes[scope].test_strategy;
    syncStageMap(graph, state);
    appendAudit(root, state, "SCOPE_CHANGED", null, { From: from, To: scope, Depth: state.depth });
    saveState(root, graph, state);
    out({ scope, depth: state.depth, test_strategy: state.test_strategy, progress: progress(graph, state) });
  },

  depth(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const levels = ["minimal", "standard", "comprehensive"];
    const depth = positional[0];
    if (!levels.includes(depth)) throw new AidlcError(`Usage: depth <${levels.join("|")}> [--tests <${levels.join("|")}>]`);
    if (flags.tests && !levels.includes(flags.tests)) throw new AidlcError(`--tests must be one of ${levels.join(", ")}`);
    state.depth = depth;
    if (flags.tests) state.test_strategy = flags.tests;
    appendAudit(root, state, "DEPTH_CHANGED", null, { Depth: depth, "Test strategy": state.test_strategy });
    saveState(root, graph, state);
    out({ depth: state.depth, test_strategy: state.test_strategy });
  },

  start(root, { positional }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError("Usage: start <key>");
    const nxt = assertIsNext(graph, state, key);
    const rec = stageRecord(state, key);
    if (rec.status === STATUS.IN_PROGRESS || rec.status === STATUS.REVISING) return out(describe(root, graph, state, nxt));
    assertStatus(state, key, [STATUS.PENDING]);
    state.stages[key] = { ...rec, status: STATUS.IN_PROGRESS, started_at: now() };
    state.current = key;
    const { stage } = stageOf(graph, key);
    mkdirSync(artifactDir(root, state, stage, parseKey(key).unit), { recursive: true });
    appendAudit(root, state, "STAGE_STARTED", key, {});
    saveState(root, graph, state);
    out(describe(root, graph, state, nextStep(graph, state)));
  },

  complete(root, { positional }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError("Usage: complete <key>");
    const { stage } = stageOf(graph, key);
    if (stage.gate) throw new AidlcError(`${stage.name} has an approval gate: use \`gate\` and then \`approve\`.`);
    assertIsNext(graph, state, key);
    const rec = assertStatus(state, key, [STATUS.IN_PROGRESS]);
    state.stages[key] = { ...rec, status: STATUS.COMPLETED, completed_at: now() };
    appendAudit(root, state, "STAGE_COMPLETED", key, {});
    finishIfDone(root, graph, state);
    saveState(root, graph, state);
    out(describe(root, graph, state, nextStep(graph, state)));
  },

  gate(root, { positional }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError("Usage: gate <key>");
    const { stage, unit } = stageOf(graph, key);
    if (!stage.gate) throw new AidlcError(`${stage.name} has no approval gate: use \`complete\`.`);
    assertIsNext(graph, state, key);
    const rec = assertStatus(state, key, [STATUS.IN_PROGRESS, STATUS.REVISING]);

    const dir = artifactDir(root, state, stage, unit);
    const docs = markdownFilesIn(dir).filter((f) => !/-questions\.md$/.test(f));
    if (!docs.length) throw new AidlcError(`No artifacts found in ${rel(root, dir)}. Write the stage's documents before opening the gate.`);
    const open = questionFilesIn(dir).flatMap((f) => unansweredQuestions(f).map((q) => `${rel(root, f)}:${q.line}${q.question ? ` (${q.question})` : ""}`));
    if (open.length) throw new AidlcError(`Unanswered questions remain:\n  ${open.join("\n  ")}\nResolve them before opening the gate.`);
    if (stage.plan_approval) {
      const plan = state.plans[key];
      if (!plan || !plan.approved_at) throw new AidlcError(`The code generation plan for ${key} has not been approved yet.`);
    }
    if (stage.registers_units && (!state.units || !state.units.length)) {
      throw new AidlcError("Register the units before opening the gate: `units set --file <unit-of-work.md>`.");
    }
    const wasRevising = rec.status === STATUS.REVISING;
    state.stages[key] = { ...rec, status: STATUS.AWAITING, gate_opened_at: now() };
    appendAudit(root, state, wasRevising ? "STAGE_REVISED" : "GATE_OPENED", key, { Revision: rec.revisions || 0 });
    saveState(root, graph, state);
    out({ key, status: STATUS.AWAITING, revisions: rec.revisions || 0, next_after: followingStep(graph, state, key) });
  },

  approve(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError('Usage: approve <key> --choice "<exact label>"');
    const choice = requireFlag(flags, "choice", "the exact option the human chose");
    const rec = assertStatus(state, key, [STATUS.AWAITING]);
    if (!humanActedSince(root, rec.gate_opened_at)) {
      throw new AidlcError(
        "No human response has been recorded since this gate opened. Present the approval question, end your turn, and approve only after the person answers.",
      );
    }
    state.stages[key] = { ...rec, status: STATUS.COMPLETED, completed_at: now(), choice };
    appendAudit(root, state, "GATE_APPROVED", key, { Choice: choice, Revisions: rec.revisions || 0 });
    appendAudit(root, state, "STAGE_COMPLETED", key, {});
    finishIfDone(root, graph, state);
    saveState(root, graph, state);
    out(describe(root, graph, state, nextStep(graph, state)));
  },

  reject(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError('Usage: reject <key> --reason "<feedback>"');
    const reason = requireFlag(flags, "reason", "the person's feedback");
    const rec = assertStatus(state, key, [STATUS.AWAITING]);
    if (!humanActedSince(root, rec.gate_opened_at)) {
      throw new AidlcError("No human response has been recorded since this gate opened. Wait for the person's answer.");
    }
    const revisions = (rec.revisions || 0) + 1;
    state.stages[key] = { ...rec, status: STATUS.REVISING, revisions };
    appendAudit(root, state, "GATE_REJECTED", key, { Feedback: reason, Revision: revisions });
    saveState(root, graph, state);
    out({ key, status: STATUS.REVISING, revisions, accept_as_is_available: revisions >= 3 });
  },

  revised(root, args) {
    // Same checks as opening the gate the first time.
    commands.gate(root, args);
  },

  skip(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError('Usage: skip <key> --reason "<why>"');
    const reason = requireFlag(flags, "reason", "why the stage does not apply");
    assertIsNext(graph, state, key);
    const rec = assertStatus(state, key, [STATUS.PENDING, STATUS.IN_PROGRESS]);
    const { stage } = stageOf(graph, key);
    if (stage.slug === "code-generation" || stage.slug === "requirements-analysis") {
      throw new AidlcError(`${stage.name} cannot be skipped: it is the core of every workflow.`);
    }
    state.stages[key] = { ...rec, status: STATUS.SKIPPED, reason, completed_at: now() };
    appendAudit(root, state, "STAGE_SKIPPED", key, { Reason: reason });
    finishIfDone(root, graph, state);
    saveState(root, graph, state);
    out(describe(root, graph, state, nextStep(graph, state)));
  },

  "plan-request"(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError("Usage: plan-request <key> --plan <path>");
    const { stage } = stageOf(graph, key);
    if (!stage.plan_approval) throw new AidlcError(`${stage.name} has no plan approval step.`);
    assertIsNext(graph, state, key);
    assertStatus(state, key, [STATUS.IN_PROGRESS, STATUS.REVISING]);
    const planPath = resolve(root, requireFlag(flags, "plan", "path to the plan file"));
    if (!existsSync(planPath)) throw new AidlcError(`Plan file not found: ${rel(root, planPath)}`);
    state.plans[key] = { path: rel(root, planPath), sha256: sha256File(planPath), requested_at: now() };
    appendAudit(root, state, "PLAN_PRESENTED", key, { Plan: rel(root, planPath) });
    saveState(root, graph, state);
    out({ key, plan: rel(root, planPath), status: "awaiting-plan-approval" });
  },

  "plan-approve"(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError('Usage: plan-approve <key> --choice "<exact label>"');
    const choice = requireFlag(flags, "choice", "the exact option the human chose");
    const plan = state.plans[key];
    if (!plan || !plan.requested_at) throw new AidlcError(`No plan has been presented for ${key}. Run \`plan-request\` first.`);
    if (!humanActedSince(root, plan.requested_at)) {
      throw new AidlcError("No human response has been recorded since the plan was presented. Wait for the person's answer.");
    }
    const file = resolve(root, plan.path);
    if (!existsSync(file) || sha256File(file) !== plan.sha256) {
      throw new AidlcError("The plan changed after it was presented. Present it again with `plan-request` so the person approves the current version.");
    }
    state.plans[key] = { ...plan, approved_at: now(), choice };
    appendAudit(root, state, "PLAN_APPROVED", key, { Plan: plan.path, Choice: choice });
    saveState(root, graph, state);
    out({ key, plan: plan.path, status: "plan-approved" });
  },

  "plan-reject"(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError('Usage: plan-reject <key> --reason "<feedback>"');
    const reason = requireFlag(flags, "reason", "the person's feedback");
    const plan = state.plans[key];
    if (!plan) throw new AidlcError(`No plan has been presented for ${key}.`);
    if (!humanActedSince(root, plan.requested_at)) throw new AidlcError("No human response has been recorded since the plan was presented.");
    state.plans[key] = { path: plan.path };
    appendAudit(root, state, "PLAN_REJECTED", key, { Feedback: reason });
    saveState(root, graph, state);
    out({ key, status: "plan-rejected" });
  },

  units(root, { positional, flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const sub = positional[0];
    const constructionStarted = Object.entries(state.stages).some(
      ([key, rec]) => graph.bySlug[parseKey(key).slug].phase === "construction" && rec.status !== STATUS.PENDING && rec.status !== STATUS.SKIPPED,
    );
    if (sub === "set") {
      if (constructionStarted) throw new AidlcError("Construction has already started; units can no longer be redefined in this workflow.");
      const file = resolve(root, requireFlag(flags, "file", "unit-of-work document"));
      const units = parseUnitsFile(file);
      state.units = units;
      state.unit_order = null;
      syncStageMap(graph, state);
      appendAudit(root, state, "UNITS_REGISTERED", state.current, { Units: units.map((u) => u.id).join(", "), Source: rel(root, file) });
      saveState(root, graph, state);
      return out({ units: units.map((u) => ({ id: u.id, depends_on: u.depends_on })), progress: progress(graph, state) });
    }
    if (sub === "order") {
      if (!state.units) throw new AidlcError("No units registered yet.");
      if (constructionStarted) throw new AidlcError("Construction has already started; the order can no longer change.");
      const order = String(positional[1] || "").split(",").map((s) => s.trim()).filter(Boolean);
      validateOrder(state.units, order);
      state.unit_order = order;
      syncStageMap(graph, state);
      appendAudit(root, state, "UNITS_ORDERED", state.current, { Order: order.join(" → ") });
      saveState(root, graph, state);
      return out({ order });
    }
    throw new AidlcError("Usage: units set --file <path> | units order <id,id,...>");
  },

  log(root, { flags }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const event = requireFlag(flags, "event");
    if (!LOGGABLE_EVENTS.has(event)) throw new AidlcError(`Event must be one of: ${[...LOGGABLE_EVENTS].join(", ")}`);
    const key = flags.stage && flags.stage !== true ? String(flags.stage) : state.current;
    appendAudit(root, state, event, key, { Details: requireFlag(flags, "details") });
    out(`Logged ${event}${key ? ` for ${key}` : ""}.`);
  },

  questions(root, { positional }) {
    const target = positional[0];
    if (!target) throw new AidlcError("Usage: questions <file-or-dir>");
    const p = resolve(root, target);
    if (!existsSync(p)) throw new AidlcError(`Not found: ${target}`);
    const files = p.endsWith(".md") ? [p] : questionFilesIn(p);
    const result = files.map((f) => ({ file: rel(root, f), unanswered: unansweredQuestions(f) }));
    out({ complete: result.every((r) => !r.unanswered.length), files: result });
  },

  path(root, { positional }) {
    const graph = loadGraph(root);
    const state = requireState(root, graph);
    const key = positional[0];
    if (!key) throw new AidlcError("Usage: path <key>");
    const { stage, unit } = stageOf(graph, key);
    out(rel(root, artifactDir(root, state, stage, unit)));
  },
};

function finishIfDone(root, graph, state) {
  if (nextStep(graph, state).kind === "done") {
    state.status = "completed";
    state.current = null;
    appendAudit(root, state, "WORKFLOW_COMPLETED", null, { Progress: `${progress(graph, state).done}/${progress(graph, state).total}` });
  }
}

// ---------------------------------------------------------------------------

function main() {
  const [cmd, ...rest] = process.argv.slice(2);
  if (!cmd || cmd === "help" || cmd === "--help" || cmd === "-h") return commands.help();
  const fn = commands[cmd];
  if (!fn) throw new AidlcError(`Unknown command "${cmd}". Run \`help\` for the list.`);
  fn(requireRoot(), parseArgs(rest));
}

try {
  main();
} catch (err) {
  if (err instanceof AidlcError) {
    process.stderr.write(`ERROR: ${err.message}\n`);
    process.exit(1);
  }
  throw err;
}
