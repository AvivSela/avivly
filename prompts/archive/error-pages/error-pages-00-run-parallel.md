# Orchestrator: Error Pages — Run All Tasks in Parallel

Read the three task files below, then spawn one subagent per task **in a single message** (all three Agent tool calls in the same response) so they execute in parallel. Do not run them sequentially.

## Task files

- `prompts/error-pages-01-backend-custom-error-controller.md`
- `prompts/error-pages-02-frontend-not-found.md`
- `prompts/error-pages-03-frontend-link-expired-redesign.md`

## Instructions for each subagent

Pass each task file's full contents as the subagent prompt. The tasks are fully independent (no shared files, no ordering dependency), so all three can run simultaneously.

Each subagent should:
1. Read its task file for the complete spec.
2. Implement exactly what is described — no more, no less.
3. Run the verification steps listed at the bottom of its task file.
4. Report back: what it created/changed and whether verification passed.

## After all three subagents complete

Summarise results in a table:

| Task | Files changed | Verification |
|------|--------------|--------------|
| Backend CustomErrorController | … | pass / fail |
| Frontend NotFound + App.jsx route | … | pass / fail |
| Frontend LinkExpired redesign | … | pass / fail |

If any subagent reports a failure, describe what went wrong.
