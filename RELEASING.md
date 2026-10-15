# Releasing

## Branches

- **`develop`** — integration branch, default branch for PRs. Every feature/fix branch targets this.
- **`main`** — release branch. Only ever receives merges from `develop`, never direct commits or feature-branch merges. Each merge to `main` is a release.

```
feat/123-something ──┐
fix/456-something   ──┼──► develop ──► main (tagged vX.Y.Z)
chore/789-something ──┘
```

## When to release

Release `develop` → `main` when a coherent, working set of changes has accumulated — not on every single merge to `develop`. In practice: after a milestone/epic's tickets are done and CI is green on `develop`, or when there's a concrete reason to ship (e.g. a bug fix users are waiting on).

## How to release

1. Confirm `develop` is green: `gh pr checks` on the most recent merge, or check the Actions tab for the latest `develop` push.
2. Open a PR from `develop` into `main`:
   ```bash
   gh pr create --base main --head develop --title "Release vX.Y.Z"
   ```
3. Once merged, tag the resulting commit on `main`:
   ```bash
   git checkout main && git pull
   git tag -a vX.Y.Z -m "vX.Y.Z"
   git push origin vX.Y.Z
   ```

## Version numbers

Follow [Semantic Versioning](https://semver.org/) (`vMAJOR.MINOR.PATCH`), derived from the Conventional Commits merged since the last release:

- Any `feat:` commit → bump **MINOR** (e.g. `v1.2.0` → `v1.3.0`).
- Only `fix:`/`chore:`/`docs:`/etc., no `feat:` → bump **PATCH** (e.g. `v1.2.0` → `v1.2.1`).
- A commit with a `BREAKING CHANGE:` footer, or `!` after the type (e.g. `feat!:`) → bump **MAJOR** (e.g. `v1.2.0` → `v2.0.0`).

Before `v1.0.0` (current state — no release has shipped yet), treat every release as a MINOR bump (`v0.1.0`, `v0.2.0`, ...) regardless of the above, per semver's pre-1.0 convention that the public API isn't stable yet.

## Branch protection

Both `develop` and `main` require:
- The `backend-ci` and `frontend-ci` (`build-test-lint` + `e2e`) status checks to pass.
- Changes via pull request — no direct pushes.

No minimum approving-review count is enforced yet — this is currently a single-contributor repo, and requiring a review would block all merges. Revisit this once there's more than one contributor.
