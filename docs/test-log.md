# Test log

Emulator: Medium_Phone_API_37.0 (API 37), headless, dark mode unless noted. Date: 2026-11-23. Branch: m3-screens.

## Automated

| Suite | Command | Result |
|---|---|---|
| Unit (RulesTest) | `./gradlew testDebugUnitTest` | 15 of 15 passed |
| Repository on emulator (RecRepositoryTest) | `./gradlew connectedDebugAndroidTest` | 7 of 7 passed |
| Lint | `./gradlew lintDebug` | 0 errors, 8 notes (min SDK attributes, Gradle version, merge tag, button bar style, overdraw on row ripple) |

## Planned cases

| ID | Result | Notes |
|---|---|---|
| T01 | Pass | Severance from Ana saved with Done, row at top, "Saved. Ana's pick is on your list." |
| T02 | Pass | Empty title shows "Add a title.", nothing saved |
| T03 | Pass | Empty person shows "Add who told you.", nothing saved |
| T04 | Pass | Title of three spaces shows "Add a title." |
| T05 | Pass | "ana" linked to existing Ana; typing "an" suggests Ana as a chip |
| T06 | Pass | Edited title to "Dune Messiah" and type to Book, "Changes saved." |
| T07 | Pass | Delete, Cancel keeps it; Back closes only the panel; Delete removes it with "Deleted "Dune Messiah"." |
| T08 | Partial | Rating shows "Marked as liked. Ana is now 1 of 1." People screen comes in M4 |
| T09 | Not yet | Search and filter is M4 |
| T10 | Not yet | Pick for me is M4 |
| T11 | Partial | First launch empty state passes; filter and People empty states come in M4 |
| T12 | Pass | Force stop and reopen, all picks still listed |

Also checked: Share to Quick Add fills the Link field and closes after save; "Discard changes?" panel on Back with edits; light mode and 1.3x font size on List and Detail with no cut-off text.

## Defects found and fixed

| ID | Defect | Cause | Fix | Retest |
|---|---|---|---|---|
| D01 | Repository tests reported "0 tests" with no error | `androidx.test:runner` missing, so `AndroidJUnitRunner` was not on the test classpath | Added `androidTestImplementation("androidx.test:runner:1.7.0")` | 7 of 7 passed |
| D02 | Keyboard did not open when Add opened | `show(ime())` posted before the window had focus | `SOFT_INPUT_STATE_VISIBLE` in add mode | Keyboard shown on open (`mInputShown=true`) |
| D03 | Check icon in the success panel almost invisible in dark mode | Circle used `quiet`, which turns Earth-muted at night, under an Olive check | Circle uses fixed Sand in both themes | Check visible in dark screenshot |
| D04 | Message said "ana's pick" after saving as "ana" | Message used the typed name, not the stored one | Message reads the saved person name | Confirmed with "kuya jon" and "Mika" saves |
| D05 | Saving from Share created two identical rows | Screen waits 1.2s before closing and the keyboard Enter fired save again; only the Save button was disabled | One `saved` guard in `save()` | "Cafe Test" saved once |
| D06 | Success panel clipped on the right | Snackbar layout forces children to full width and ignores margins | Spacing moved to Snackbar padding | Panel edges even in screenshot |

Found in code review and fixed (not seen on screen): AddEdit accepted `rec_id` from other apps (Share now uses an exported alias that ignores it, AddEdit is not exported); a rotation during the Share save window allowed a second save.

## Improvements from test evidence (dark-mode design pass, 2026-11-23)

| ID | Evidence | Change | Result |
|---|---|---|---|
| I01 | Dark screenshots: success panel was the same Earth as the screen and its shadow did not show, so the pop-in barely registered | Raised panels use #54443A with a 1dp #6E5F55 edge in dark mode | Panel and pop-out read as lifted cards in dark screenshots |
| I02 | Held-press screenshots looked the same as rest in dark mode (shadow drop invisible, 97% scale too small) | Press now shifts the fill (Olive lighter in dark, darker in light; secondary fills) and scales to 95% | Held "Not for me" visibly fills and shrinks |
| I03 | Selected type chip (Olive on Earth, about 2:1) looked weaker than the outlined chips next to it | Selected chip is Sand with Earth text in dark mode (8.0:1) | Selected chip is the most visible one |
| I04 | Pop-out dim stopped at the status bar and the panel stopped above the navigation bar | Only the content is padded for system bars; the panel adds the navigation bar to its own bottom padding | Dim covers the full screen, panel reaches the bottom edge |
| I05 | EB Garamond's short x-height made 14sp meta and 12sp labels look thin | Body 17sp, meta 15sp, labels 13sp | Meta lines readable at a glance |

Also fixed: dark error color measured 4.46:1 on the raised panel, now #EBAE98 (4.9:1).
