# Changelog

All notable changes to Aurora. Versions follow `versionName` in
`app/build.gradle.kts`; release tags are `v<versionName>`.

## [Unreleased]

### Added

- **Self-update from GitHub Releases.** A background check runs at most once a
  day, posts a notification when a new version exists, and downloads the APK on
  request before opening Android's installer. Manual check in
  Profile → About Aurora. New strings in English and Arabic, plus a separate
  `aurora_updates_channel` notification channel.
- **Project icon and colour picker** in the New project dialog (7 icons,
  5 accents) instead of random assignment.
- **Conversation search** — instant filter over titles, previews, member names
  and message text, with an empty state.

### Changed

- Group avatars in the chat list take their colour from the conversation id, not
  from the row position, so filtering no longer repaints them.
- The empty description of a new project reads "No description yet." instead of
  the seeded tagline.
- The project "Add" button uses a visible label rather than a screen-reader
  string.

### Fixed

- `DateText` no longer renders "28 November 1970" for dates without a year.
- Misc: tab transition animation, unified 1.8 stroke icon set, GlassCard shadow,
  monochrome launcher icon layer.

## [1.0.0]

First complete build: auth screen, four tabs (Home / Projects / Chat / Profile),
five themes, Arabic + English with RTL, tasks with priorities and tags, project
detail, conversations, local JSON persistence, notifications channel, tablet
layout from 600dp.
