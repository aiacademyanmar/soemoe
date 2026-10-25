# AAII Learning V4.9 — Visible Functions Repair

## Why V4.8 controls were difficult to find
V4.8 hid certificate action in Wallet and Profile, while app navigation immediately destroyed the tapped View so animations were effectively invisible. Debug APK signing uses ephemeral runner keys; old app may remain when update fails.

## Changes
A Home "NEW FUNCTIONS" hero is always visible, with direct buttons to Coursera organization My Learning certificates, Profile Bottom Popup, tap+sound test, sound settings. The top app header displays V4.9. Tap callback delays navigation 145ms so scaling is visible; audio volume boosted. Course cards, program cards, learning tiles, and Home section cards now clickable with feedback. Sound toggle moved into a separate interactive settings section. GitHub workflow asserts V4.9 controls exist before APK compilation/sign verification. Person-specific certificate URLs still stay private; import in Wallet, public organization certificate URL requires account login.

## Test
Look for `AAII Learning V4.9` in header and `NEW FUNCTIONS` at top of Home. If absent, the device is running a prior version. Debug signing keys may change between GitHub Actions builds; Android may refuse updates. Back up personal learning progress before uninstalling older apps. No device-level test performed by CI.
