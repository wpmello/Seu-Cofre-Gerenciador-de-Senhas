---
name: branch-and-commit-hygiene
description: Use when the task is ending and Codex must organize changes into logical commits and branches, verify the correct base branch, avoid accidentally stacked branches, or repair a branch layout so each branch targets the intended integration branch cleanly.
---

# Branch And Commit Hygiene

Use this skill at the end of implementation work when the user asks to:
- create commits;
- create branches;
- prepare pull-request-ready history;
- reorganize branches that were created on the wrong base;
- decide whether one task should stay in one branch or be split into multiple branches.

## Required project context

Read:
- `AGENTS.md`
- `docs/branching-and-commit-strategy.md`

## Default rule

Assume the merge target is `master` unless the user explicitly says otherwise.

Do not stack branches by default.

## Decision rule

Before creating branches or commits, decide:

### Case 1 — Single cohesive delivery
Keep everything in one branch.

Examples:
- one feature that also needs dependency changes;
- one bugfix that also needs tests and docs;
- one chore that also needs small config updates.

In this case:
- create one branch for the delivery;
- split by logical commits only.

### Case 2 — Multiple independent deliveries
Create multiple branches only if each delivery:
- has its own purpose;
- can be merged independently;
- does not need another branch's commit to make sense.

Each branch must start from the integration target branch.

## Workflow

### 1. Inspect the current git state
Check:
- current branch;
- merge target;
- unstaged/staged changes;
- recent graph;
- whether the worktree is dirty.

### 2. Group changes by objective
Identify groups such as:
- main implementation;
- tests;
- docs;
- tooling.

### 3. Decide branch strategy
Choose one of:
- one branch with multiple commits;
- multiple branches from the target base.

Do not create extra branches only because files are of different types.

### 4. Validate the base before branch creation
Before creating a branch, confirm it starts from the intended merge target.

If the branch already exists, confirm it does not accidentally include unrelated commits.

### 5. Create logical commits
Use commits that each explain one purpose.

Good examples:
- `feat: add splash flow and home navigation`
- `docs: capture splash ui implementation guidance`
- `chore: stabilize android build environment`

### 6. Repair wrong branch ancestry safely
If branches were created on the wrong base:

1. create backup branches;
2. stash or otherwise preserve local dirty state if needed;
3. recreate each branch from the correct base;
4. `cherry-pick` only the relevant commit(s);
5. verify the graph and merge-base;
6. only then update the remote branch.

## Mandatory checks before finishing

Verify:
- each branch contains only the commits intended for its PR;
- each branch merge-base matches the intended target base;
- backup refs exist before any rewrite;
- the user is told if force-push will be required after local history rewrite.

## Do not do

- do not split one cohesive feature into multiple branches only because it touched dependencies, docs, or tests;
- do not rebase or rewrite history without preserving backup refs;
- do not assume the local `master` is current if the real merge target may differ and you have not verified it;
- do not leave the user unaware that a remote PR will need `push --force-with-lease` after branch recreation.
