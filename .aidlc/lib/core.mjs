// Shared core for the AI-DLC CLI and hooks.
//
// Everything that decides workflow state lives here so the CLI (bin/aidlc.mjs)
// and the hooks (hooks/*.mjs) never disagree. No third-party dependencies:
// Node >= 18 only.

import { createHash } from "node:crypto";
import { existsSync, mkdirSync, readFileSync, readdirSync, renameSync, statSync, writeFileSync, appendFileSync } from "node:fs";
import { dirname, join, relative, resolve, sep } from "node:path";

export const STATE_VERSION = 1;

export const STATUS = {
  PENDING: "pending",
  IN_PROGRESS: "in-progress",
  AWAITING: "awaiting-approval",
  REVISING: "revising",
  COMPLETED: "completed",
  SKIPPED: "skipped",
};

const DONE = new Set([STATUS.COMPLETED, STATUS.SKIPPED]);

export const AUDIT_EVENTS = new Set([
  "WORKFLOW_CREATED",
  "WORKFLOW_PARKED",
  "WORKFLOW_RESUMED",
  "WORKFLOW_COMPLETED",
  "SCOPE_CHANGED",
  "DEPTH_CHANGED",
  "STAGE_STARTED",
  "STAGE_COMPLETED",
  "STAGE_SKIPPED",
  "GATE_OPENED",
  "GATE_APPROVED",
  "GATE_REJECTED",
  "STAGE_REVISED",
  "PLAN_PRESENTED",
  "PLAN_APPROVED",
  "PLAN_REJECTED",
  "UNITS_REGISTERED",
  "UNITS_ORDERED",
  "MEMORY_PROMOTED",
  // Free-form events the agent may log through `aidlc log`:
  "QUESTIONS_CREATED",
  "ANSWERS_RECORDED",
  "DECISION",
  "REVIEW",
  "NOTE",
]);

export const LOGGABLE_EVENTS = new Set(["QUESTIONS_CREATED", "ANSWERS_RECORDED", "DECISION", "REVIEW", "NOTE", "MEMORY_PROMOTED"]);

export class AidlcError extends Error {}

// ---------------------------------------------------------------------------
// Paths

export function findRoot(start = process.env.CLAUDE_PROJECT_DIR || process.cwd()) {
  let dir = resolve(start);
  for (;;) {
    if (existsSync(join(dir, ".aidlc", "workflow.json"))) return dir;
    const parent = dirname(dir);
    if (parent === dir) return null;
    dir = parent;
  }
}

export function paths(root) {
  const docs = join(root, "aidlc-docs");
  return {
    root,
    framework: join(root, ".aidlc"),
    graph: join(root, ".aidlc", "workflow.json"),
    defaults: join(root, ".aidlc", "defaults.json"),
    docs,
    config: join(docs, "config.json"),
    active: join(docs, "active.json"),
    runtime: join(docs, ".runtime"),
    humanTurn: join(docs, ".runtime", "human-turn.json"),
    workflows: join(docs, "workflows"),
    memory: join(docs, "memory"),
    codebase: join(docs, "codebase"),
  };
}

export function workflowPaths(root, id) {
  const dir = join(paths(root).workflows, id);
  return {
    dir,
    state: join(dir, "state.json"),
    stateMd: join(dir, "aidlc-state.md"),
    audit: join(dir, "audit.md"),
    request: join(dir, "request.md"),
  };
}

export function rel(root, p) {
  return relative(root, p).split(sep).join("/");
}

// ---------------------------------------------------------------------------
// Small IO helpers

export function readJson(file, fallback = undefined) {
  if (!existsSync(file)) {
    if (fallback !== undefined) return fallback;
    throw new AidlcError(`Missing file: ${file}`);
  }
  const text = readFileSync(file, "utf8").replace(/^﻿/, "");
  try {
    return JSON.parse(text);
  } catch (err) {
    throw new AidlcError(`Invalid JSON in ${file}: ${err.message}`);
  }
}

export function writeJsonAtomic(file, data) {
  mkdirSync(dirname(file), { recursive: true });
  const tmp = `${file}.${process.pid}.tmp`;
  writeFileSync(tmp, JSON.stringify(data, null, 2) + "\n", "utf8");
  renameSync(tmp, file);
}

export function now() {
  return new Date().toISOString();
}

export function sha256File(file) {
  return createHash("sha256").update(readFileSync(file)).digest("hex");
}

// ---------------------------------------------------------------------------
// Graph and config

export function loadGraph(root) {
  const graph = readJson(paths(root).graph);
  graph.bySlug = Object.fromEntries(graph.stages.map((s) => [s.slug, s]));
  return graph;
}

export const DEFAULT_CONFIG = {
  interaction_language: "es",
  artifact_language: "es",
  code_language: "en",
  guard: "strict",
};

export function loadConfig(root) {
  const p = paths(root);
  const defaults = readJson(p.defaults, DEFAULT_CONFIG);
  const project = readJson(p.config, {});
  return { ...DEFAULT_CONFIG, ...defaults, ...project };
}

// ---------------------------------------------------------------------------
// Active workflow and state

export function loadActiveId(root) {
  const active = readJson(paths(root).active, null);
  return active && active.workflow ? active.workflow : null;
}

export function setActiveId(root, id) {
  writeJsonAtomic(paths(root).active, { workflow: id, updated_at: now() });
}

export function loadState(root, id = loadActiveId(root)) {
  if (!id) return null;
  const file = workflowPaths(root, id).state;
  if (!existsSync(file)) return null;
  return readJson(file);
}

export function listWorkflows(root) {
  const dir = paths(root).workflows;
  if (!existsSync(dir)) return [];
  return readdirSync(dir)
    .filter((name) => existsSync(join(dir, name, "state.json")))
    .sort()
    .map((name) => {
      const s = readJson(join(dir, name, "state.json"));
      return { id: s.id, status: s.status, scope: s.scope, request: s.request, created_at: s.created_at };
    });
}

export function stageKey(slug, unit) {
  return unit ? `${slug}@${unit}` : slug;
}

export function parseKey(key) {
  const [slug, unit] = key.split("@");
  return { slug, unit: unit || null };
}

export function stageInScope(stage, scope) {
  return stage.scopes.includes(scope);
}

// The ordered list of stage keys the workflow will run, given what is known
// now. Per-unit stages expand once units are registered; before that they are
// represented by their bare slug (one iteration) unless the scope includes a
// unit-registering stage, in which case they wait for registration.
export function plannedKeys(graph, state) {
  const keys = [];
  const inScope = graph.stages.filter((s) => stageInScope(s, state.scope));
  const perUnit = inScope.filter((s) => s.per_unit);
  let perUnitEmitted = false;
  for (const stage of inScope) {
    if (!stage.per_unit) {
      keys.push({ key: stage.slug, stage, unit: null });
      continue;
    }
    if (perUnitEmitted) continue;
    perUnitEmitted = true;
    const units = orderedUnits(state);
    if (units && units.length) {
      for (const unit of units) for (const s of perUnit) keys.push({ key: stageKey(s.slug, unit.id), stage: s, unit: unit.id });
    } else {
      for (const s of perUnit) keys.push({ key: s.slug, stage: s, unit: null });
    }
  }
  return keys;
}

export function scopeRegistersUnits(graph, scope) {
  return graph.stages.some((s) => s.registers_units && stageInScope(s, scope));
}

export function orderedUnits(state) {
  if (!state.units || !state.units.length) return null;
  const byId = Object.fromEntries(state.units.map((u) => [u.id, u]));
  if (state.unit_order && state.unit_order.length) return state.unit_order.map((id) => byId[id]);
  return topoSort(state.units);
}

export function topoSort(units) {
  const byId = Object.fromEntries(units.map((u) => [u.id, u]));
  const out = [];
  const mark = {};
  const visit = (id, trail) => {
    if (mark[id] === 2) return;
    if (mark[id] === 1) throw new AidlcError(`Unit dependency cycle: ${[...trail, id].join(" -> ")}`);
    const unit = byId[id];
    if (!unit) throw new AidlcError(`Unknown unit in depends_on: ${id}`);
    mark[id] = 1;
    for (const dep of unit.depends_on || []) visit(dep, [...trail, id]);
    mark[id] = 2;
    out.push(unit);
  };
  for (const u of units) visit(u.id, []);
  return out;
}

export function stageRecord(state, key) {
  return state.stages[key] || { status: STATUS.PENDING, revisions: 0 };
}

// Make sure every planned key has a record, and drop pending records that are
// no longer planned (after a scope change or unit registration).
export function syncStageMap(graph, state) {
  const planned = plannedKeys(graph, state);
  const plannedSet = new Set(planned.map((p) => p.key));
  for (const { key, stage } of planned) {
    if (!state.stages[key]) {
      const rec = { status: STATUS.PENDING, revisions: 0 };
      if (stage.condition === "brownfield" && state.type !== "brownfield") {
        rec.status = STATUS.SKIPPED;
        rec.reason = "Greenfield project: there is no existing code to analyze.";
        rec.reason_code = "greenfield";
        rec.completed_at = now();
      }
      state.stages[key] = rec;
    }
  }
  for (const [key, rec] of Object.entries(state.stages)) {
    if (!plannedSet.has(key) && rec.status === STATUS.PENDING) delete state.stages[key];
  }
  return planned;
}

// Read-only: what should happen next.
export function nextStep(graph, state) {
  if (state.status === "parked") return { kind: "parked" };
  if (state.status === "completed") return { kind: "done" };
  const planned = plannedKeys(graph, state);
  for (const item of planned) {
    const rec = stageRecord(state, item.key);
    if (DONE.has(rec.status)) continue;
    if (item.stage.per_unit && !item.unit && scopeRegistersUnits(graph, state.scope)) {
      return {
        kind: "error",
        message:
          "Construction needs registered units, but none are registered. Re-open Units Generation or run `aidlc units set --file <unit-of-work.md>`.",
      };
    }
    return { kind: "run-stage", ...item, status: rec.status };
  }
  return { kind: "done" };
}

export function progress(graph, state) {
  const planned = plannedKeys(graph, state);
  const done = planned.filter((p) => DONE.has(stageRecord(state, p.key).status)).length;
  return { done, total: planned.length };
}

export function artifactDir(root, state, stage, unit) {
  const wf = workflowPaths(root, state.id).dir;
  if (stage.per_unit && unit) return join(wf, stage.phase, unit, stage.slug);
  return join(wf, stage.phase, stage.slug);
}

// ---------------------------------------------------------------------------
// Persistence with audit trail

export function appendAudit(root, state, event, key, details) {
  if (!AUDIT_EVENTS.has(event)) throw new AidlcError(`Unknown audit event: ${event}`);
  const file = workflowPaths(root, state.id).audit;
  mkdirSync(dirname(file), { recursive: true });
  if (!existsSync(file)) writeFileSync(file, `# Audit Log — ${state.id}\n\nAppend-only. Written by the aidlc CLI; do not edit by hand.\n`, "utf8");
  const lines = [`\n## ${now()} · ${event}${key ? ` · ${key}` : ""}`];
  if (details) {
    for (const [label, value] of Object.entries(details)) {
      if (value === undefined || value === null || value === "") continue;
      lines.push(`- **${label}**: ${String(value).replace(/\r?\n/g, " ")}`);
    }
  }
  appendFileSync(file, lines.join("\n") + "\n", "utf8");
}

export function saveState(root, graph, state) {
  state.updated_at = now();
  const wp = workflowPaths(root, state.id);
  writeJsonAtomic(wp.state, state);
  writeFileSync(wp.stateMd, renderStateMd(root, graph, state), "utf8");
}

// ---------------------------------------------------------------------------
// Human presence

export function lastHumanTurn(root) {
  const data = readJson(paths(root).humanTurn, null);
  return data && data.at ? data.at : null;
}

export function recordHumanTurn(root, source) {
  writeJsonAtomic(paths(root).humanTurn, { at: now(), source });
}

export function humanActedSince(root, iso) {
  if (loadConfig(root).approvals === "delegated") return true;
  const at = lastHumanTurn(root);
  return Boolean(at && iso && at > iso);
}

// ---------------------------------------------------------------------------
// Questions files

// Returns the list of unanswered `[Answer]:` tags in a questions file, with the
// nearest preceding heading so the message can name the question.
export function unansweredQuestions(file) {
  const lines = readFileSync(file, "utf8").split(/\r?\n/);
  const missing = [];
  let heading = null;
  lines.forEach((line, i) => {
    const h = line.match(/^#{2,4}\s+(.*)$/);
    if (h) heading = h[1].trim();
    const m = line.match(/^\s*\[Answer\]:\s*(.*)$/i);
    if (m && /^[\s_]*$/.test(m[1])) missing.push({ line: i + 1, question: heading });
  });
  return missing;
}

export function questionFilesIn(dir) {
  if (!existsSync(dir)) return [];
  const out = [];
  const walk = (d) => {
    for (const name of readdirSync(d)) {
      const p = join(d, name);
      if (statSync(p).isDirectory()) walk(p);
      else if (/-questions\.md$/.test(name)) out.push(p);
    }
  };
  walk(dir);
  return out;
}

export function markdownFilesIn(dir) {
  if (!existsSync(dir)) return [];
  const out = [];
  const walk = (d) => {
    for (const name of readdirSync(d)) {
      const p = join(d, name);
      if (statSync(p).isDirectory()) walk(p);
      else if (name.endsWith(".md")) out.push(p);
    }
  };
  walk(dir);
  return out;
}

// ---------------------------------------------------------------------------
// Units

// Units are declared in a fenced ```json block holding {"units": [...]}.
export function parseUnitsFile(file) {
  const text = readFileSync(file, "utf8");
  const blocks = [...text.matchAll(/```json\s*\n([\s\S]*?)```/g)].map((m) => m[1]);
  for (const block of blocks) {
    let data;
    try {
      data = JSON.parse(block);
    } catch {
      continue;
    }
    if (data && Array.isArray(data.units)) return validateUnits(data.units);
  }
  throw new AidlcError(`No fenced \`\`\`json block with a "units" array found in ${file}`);
}

export function validateUnits(units) {
  if (!units.length) throw new AidlcError("The units list is empty.");
  const seen = new Set();
  const clean = units.map((u) => {
    if (!u || typeof u.id !== "string" || !/^[a-z][a-z0-9-]{0,63}$/.test(u.id)) {
      throw new AidlcError(`Invalid unit id ${JSON.stringify(u && u.id)}: use lowercase letters, digits and hyphens (e.g. u1-auth).`);
    }
    if (seen.has(u.id)) throw new AidlcError(`Duplicate unit id: ${u.id}`);
    seen.add(u.id);
    return { id: u.id, name: u.name || u.id, kind: u.kind || null, depends_on: Array.isArray(u.depends_on) ? u.depends_on : [] };
  });
  for (const u of clean) {
    for (const dep of u.depends_on) {
      if (dep === u.id) throw new AidlcError(`Unit ${u.id} depends on itself.`);
      if (!seen.has(dep)) throw new AidlcError(`Unit ${u.id} depends on unknown unit ${dep}.`);
    }
  }
  topoSort(clean);
  return clean;
}

export function validateOrder(units, order) {
  const ids = new Set(units.map((u) => u.id));
  if (order.length !== ids.size || !order.every((id) => ids.has(id)) || new Set(order).size !== order.length) {
    throw new AidlcError(`The order must list every unit exactly once: ${[...ids].join(", ")}`);
  }
  const pos = Object.fromEntries(order.map((id, i) => [id, i]));
  for (const u of units) {
    for (const dep of u.depends_on) {
      if (pos[dep] > pos[u.id]) throw new AidlcError(`Unit ${u.id} depends on ${dep}, so ${dep} must come first.`);
    }
  }
}

// ---------------------------------------------------------------------------
// Rendering aidlc-state.md (human view, generated)

const LABELS = {
  es: {
    title: "Estado del workflow AI-DLC",
    generated: "Archivo generado por el CLI de AI-DLC. No lo edites a mano.",
    request: "Solicitud",
    type: "Tipo de proyecto",
    scope: "Alcance (scope)",
    depth: "Profundidad",
    tests: "Estrategia de pruebas",
    status: "Estado",
    progress: "Progreso",
    current: "Etapa actual",
    next: "Siguiente",
    units: "Unidades de trabajo",
    stages: "Etapas",
    none: "ninguna",
    statuses: {
      pending: "pendiente",
      "in-progress": "en curso",
      "awaiting-approval": "esperando aprobación",
      revising: "en revisión",
      completed: "completada",
      skipped: "omitida",
    },
    wf: { active: "activo", parked: "pausado", completed: "completado" },
    reasons: { greenfield: "proyecto nuevo: no hay código existente que analizar" },
  },
  en: {
    title: "AI-DLC Workflow State",
    generated: "Generated by the AI-DLC CLI. Do not edit by hand.",
    request: "Request",
    type: "Project type",
    scope: "Scope",
    depth: "Depth",
    tests: "Test strategy",
    status: "Status",
    progress: "Progress",
    current: "Current stage",
    next: "Next",
    units: "Units of work",
    stages: "Stages",
    none: "none",
    statuses: {
      pending: "pending",
      "in-progress": "in progress",
      "awaiting-approval": "awaiting approval",
      revising: "revising",
      completed: "completed",
      skipped: "skipped",
    },
    wf: { active: "active", parked: "parked", completed: "completed" },
    reasons: { greenfield: "greenfield project: there is no existing code to analyze" },
  },
};

const MARK = {
  pending: "[ ]",
  "in-progress": "[-]",
  "awaiting-approval": "[?]",
  revising: "[R]",
  completed: "[x]",
  skipped: "[S]",
};

export function renderStateMd(root, graph, state) {
  const config = loadConfig(root);
  const L = LABELS[config.artifact_language] || LABELS.en;
  const prog = progress(graph, state);
  const nxt = nextStep(graph, state);
  const out = [];
  out.push(`# ${L.title} — ${state.id}`, "", `> ${L.generated}`, "");
  out.push(`- **${L.request}**: ${state.request.split(/\r?\n/)[0].slice(0, 200)}`);
  out.push(`- **${L.type}**: ${state.type}`);
  out.push(`- **${L.scope}**: ${state.scope}`);
  out.push(`- **${L.depth}**: ${state.depth}`);
  out.push(`- **${L.tests}**: ${state.test_strategy}`);
  out.push(`- **${L.status}**: ${L.wf[state.status] || state.status}`);
  out.push(`- **${L.progress}**: ${prog.done}/${prog.total}`);
  out.push(`- **${L.current}**: ${state.current || "—"}`);
  out.push(`- **${L.next}**: ${nxt.kind === "run-stage" ? nxt.key : nxt.kind}`);
  out.push("");
  if (state.units && state.units.length) {
    out.push(`## ${L.units}`, "");
    for (const u of orderedUnits(state)) {
      out.push(`- \`${u.id}\` — ${u.name}${u.depends_on.length ? ` (← ${u.depends_on.join(", ")})` : ""}`);
    }
    out.push("");
  }
  out.push(`## ${L.stages}`, "");
  const planned = plannedKeys(graph, state);
  for (const phase of graph.phases) {
    const items = planned.filter((p) => p.stage.phase === phase);
    if (!items.length) continue;
    out.push(`### ${phase.toUpperCase()}`, "");
    for (const item of items) {
      const rec = stageRecord(state, item.key);
      const reasonText = (rec.reason_code && L.reasons[rec.reason_code]) || rec.reason;
      const reason = rec.status === STATUS.SKIPPED && reasonText ? ` — ${reasonText}` : "";
      out.push(`- ${MARK[rec.status]} ${item.stage.name}${item.unit ? ` · ${item.unit}` : ""} (${L.statuses[rec.status]})${reason}`);
    }
    out.push("");
  }
  return out.join("\n");
}

// ---------------------------------------------------------------------------
// Workspace detection (heuristic, read-only)

const IGNORED_DIRS = new Set([
  ".git", ".aidlc", ".claude", "aidlc-docs", "node_modules", "dist", "build", "out", "target", "bin", "obj",
  ".venv", "venv", "__pycache__", ".next", ".nuxt", "vendor", "coverage", ".idea", ".vscode",
]);

const LANG_BY_EXT = {
  ".ts": "TypeScript", ".tsx": "TypeScript", ".js": "JavaScript", ".jsx": "JavaScript", ".mjs": "JavaScript", ".cjs": "JavaScript",
  ".py": "Python", ".java": "Java", ".kt": "Kotlin", ".go": "Go", ".rs": "Rust", ".cs": "C#", ".php": "PHP",
  ".rb": "Ruby", ".swift": "Swift", ".dart": "Dart", ".scala": "Scala", ".c": "C", ".cpp": "C++", ".h": "C/C++",
  ".vue": "Vue", ".svelte": "Svelte", ".sql": "SQL", ".tf": "Terraform",
};

const MANIFESTS = {
  "package.json": "npm/Node.js", "pnpm-lock.yaml": "pnpm", "yarn.lock": "Yarn", "bun.lockb": "Bun", "bun.lock": "Bun",
  "pyproject.toml": "Python (pyproject)", "requirements.txt": "Python (pip)", "Pipfile": "Pipenv", "go.mod": "Go modules",
  "Cargo.toml": "Cargo", "pom.xml": "Maven", "build.gradle": "Gradle", "build.gradle.kts": "Gradle (Kotlin)",
  "composer.json": "Composer", "Gemfile": "Bundler", "pubspec.yaml": "Dart/Flutter", "Dockerfile": "Docker",
  "docker-compose.yml": "Docker Compose", "docker-compose.yaml": "Docker Compose", "serverless.yml": "Serverless Framework",
  "cdk.json": "AWS CDK", "angular.json": "Angular", "next.config.js": "Next.js", "next.config.mjs": "Next.js",
  "vite.config.ts": "Vite", "vite.config.js": "Vite", "tsconfig.json": "TypeScript config",
};

export function detectWorkspace(root, limit = 5000) {
  const languages = {};
  const manifests = new Set();
  let sourceFiles = 0;
  let scanned = 0;
  const walk = (dir, depth) => {
    if (scanned > limit || depth > 8) return;
    let entries;
    try {
      entries = readdirSync(dir, { withFileTypes: true });
    } catch {
      return;
    }
    for (const e of entries) {
      if (scanned > limit) return;
      if (e.isDirectory()) {
        if (!IGNORED_DIRS.has(e.name) && !e.name.startsWith(".")) walk(join(dir, e.name), depth + 1);
        continue;
      }
      scanned++;
      if (MANIFESTS[e.name]) manifests.add(`${MANIFESTS[e.name]} (${rel(root, join(dir, e.name))})`);
      if (/\.(csproj|sln)$/.test(e.name)) manifests.add(`.NET (${rel(root, join(dir, e.name))})`);
      const ext = e.name.includes(".") ? e.name.slice(e.name.lastIndexOf(".")) : "";
      const lang = LANG_BY_EXT[ext];
      if (lang) {
        languages[lang] = (languages[lang] || 0) + 1;
        sourceFiles++;
      }
    }
  };
  walk(root, 0);
  return {
    type: sourceFiles > 0 || manifests.size > 0 ? "brownfield" : "greenfield",
    source_files: sourceFiles,
    languages: Object.entries(languages).sort((a, b) => b[1] - a[1]).map(([name, files]) => ({ name, files })),
    manifests: [...manifests].sort(),
    truncated: scanned > limit,
  };
}

function normalizeText(text) {
  return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
}

export function suggestScope(graph, request, type) {
  const text = normalizeText(request || "");
  for (const name of ["bugfix", "refactor", "poc", "mvp", "enterprise"]) {
    const scope = graph.scopes[name];
    if (!scope) continue;
    for (const kw of scope.keywords) {
      const re = new RegExp(`(^|[^a-z0-9])${normalizeText(kw).replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}([^a-z0-9]|$)`);
      if (re.test(text)) {
        // bug/refactor words only make sense on existing code.
        if ((name === "bugfix" || name === "refactor") && type === "greenfield") continue;
        return { scope: name, matched: kw };
      }
    }
  }
  return { scope: "feature", matched: null };
}

export function slugify(text, max = 40) {
  const s = normalizeText(text)
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, max)
    .replace(/-+$/g, "");
  return s || "workflow";
}
