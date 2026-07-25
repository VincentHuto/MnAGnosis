# MnAGnosis Lore and Design Bible Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a maintainable Markdown bible that records MnAGnosis lore, creative identity, current implementation, and future technical direction.

**Architecture:** One Markdown file will contain two books: a lore-facing creative bible and a production-facing technical reference. Explicit maturity and status labels will prevent canon, current code, and speculative ideas from being confused.

**Tech Stack:** CommonMark/GitHub-flavored Markdown, repository source inspection, Git.

## Global Constraints

- Target Minecraft version: 1.20.1.
- MnAGnosis is a late-game addon to Mana and Artifice.
- Reality is real but low-order, contingent, and reshapeable.
- The player is becoming an emerging peer of higher powers.
- Use selected Gnostic terms inside an original cosmology.
- Preserve the current Tier 6, Truth, tesseract, and offering flow as canon.
- Record all five faction bosses and a Broker exchange as the future ascension gate.
- Do not modify unrelated dirty-worktree files.

---

### Task 1: Author the Living Bible

**Files:**
- Create: `docs/mnagnosis-lore-and-design-bible.md`

**Interfaces:**
- Consumes: approved document design and current repository state.
- Produces: the single living Markdown reference used for future lore and feature work.

- [ ] **Step 1: Create the document shell**

Add metadata, status keys, a linked table of contents, Book I, Book II, reusable templates, and a revision log.

- [ ] **Step 2: Author Book I**

Record the core thesis, player arc, cosmology, Truth, tesseract, Pentad of Authorities, Broker, offerings, sensory language, writing voice, lore fragments, terminology, and bounded idea garden.

- [ ] **Step 3: Author Book II**

Record the implementation snapshot, target ascension flow, prerequisite matrix, design principles, feature cards, roadmap, decision log, and open questions.

### Task 2: Validate the Artifact

**Files:**
- Verify: `docs/mnagnosis-lore-and-design-bible.md`

**Interfaces:**
- Consumes: the completed living bible.
- Produces: a structurally sound document with accurate status distinctions.

- [ ] **Step 1: Check Markdown structure**

Confirm heading levels are coherent, table rows are valid, internal links resolve to real headings, and reusable templates render as intended.

- [ ] **Step 2: Check content integrity**

Scan for placeholders, contradictions, accidental historical-religion claims, and speculative features presented as implemented.

- [ ] **Step 3: Check repository accuracy**

Compare implemented claims against Tier 6 progression, Truth, tesseract, shader, resource, and registry files currently present in the repository.

### Task 3: Record the Deliverable

**Files:**
- Commit: `docs/mnagnosis-lore-and-design-bible.md`
- Commit: `docs/superpowers/specs/2026-07-25-mnagnosis-lore-design-bible-design.md`
- Commit: `docs/superpowers/plans/2026-07-25-mnagnosis-lore-design-bible.md`

**Interfaces:**
- Consumes: the validated artifact and updated Markdown-specific spec.
- Produces: a focused documentation commit that excludes unrelated staged or working-tree changes.

- [ ] **Step 1: Review the focused diff**

Run `git diff --check` and inspect only the three documentation paths.

- [ ] **Step 2: Commit only the documentation paths**

Use path-limited staging and commit commands so pre-existing source and texture changes remain untouched.
