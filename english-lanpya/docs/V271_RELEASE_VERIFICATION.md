# V2.7.1 Release Verification

Status: GitHub source uploaded, workflow file verified, actual GitHub Actions run result **not verified**.

## Repository isolation
- Existing `main` contains the AAII Kid Creator Academy build validation project.
- English LanPya files were committed to `english-lanpya-v271` only, under `english-lanpya/`.
- Build workflow lives at `.github/workflows/english-lanpya-v271.yml`.

## Scope limitation
This branch currently holds a public-safe English LanPya **build QA bootstrap**, not the full V2.7 source package. It implements a Compose UI for original A1 demo lessons, optional HTTPS activation request QA, local speaking recorder and exam/certificate release-state screens. It does not implement production certificate issuance, graded speaking upload, official exam engine, complete Google Drive streaming or paid learning logic. Do not use test activation codes or real student data with public demo builds.

## Verification checklist
- [x] Connected GitHub repository identified and branch created.
- [x] Project configuration, native UI entry point, manifest, Gradle and CI workflow committed.
- [x] GitHub fetch confirmed workflow, Gradle config and activity contents on branch.
- [ ] Actions run completion verified.
- [ ] APK artifact checksum and debug signature verified.
- [ ] Android device installation and UI smoke tests.
- [ ] Staging Cloud API integration and authorized course playback.
- [ ] Speaking upload / teacher review / exam / PDF verification integration.

No signed release should be published before remaining gates pass.
