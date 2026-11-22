# Test log

Emulator: Medium_Phone_API_37.0 (API 37), headless, dark mode unless noted. Date: 2026-11-23. Branches: m3-screens (T01-T07, T12), m4-extras (T08-T11).

## Automated

| Suite | Command | Result |
|---|---|---|
| Unit (RulesTest) | `./gradlew testDebugUnitTest` | 15 of 15 passed |
| Repository on emulator (RecRepositoryTest) | `./gradlew connectedDebugAndroidTest` | 12 of 12 passed (M4 added search, wildcard, combined filter, untried and people ranking tests) |
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
| T08 | Pass | Repository test rates Ana's picks liked, liked, not for me and gets 2 of 3. People screen shows "1 of 2 liked" for Ana's real data, best scores first, "Nothing tried yet" last |
| T09 | Pass | "sev" shows Seven Nation Army and Severance; adding Song leaves only Seven Nation Army |
| T10 | Pass | Place filter picks only untried places; Pick again switches to the other one; Book with one untried shows "No other untried picks here."; Show with none untried shows "You've tried everything here. Add a new pick." |
| T11 | Pass | First launch hides search and filters and shows the empty message; Book + Liked shows "No picks match these filters." with Clear filters; People with no data shows "No people yet." |
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
| D07 | Keyboard kept popping up on the List after using search, and once covered the "You've tried everything" panel | Search field kept focus, so Android reopened the keyboard each time the screen came back | `stateAlwaysHidden` on List; Search key and filter chips clear focus and hide the keyboard | Keyboard stays closed in all M4 captures; panel visible |

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

## Improvements from the all-states design pass (light, dark, 2x font, landscape; 2026-11-23)

| ID | Evidence | Change | Result |
|---|---|---|---|
| I06 | Light success panel was Ivory on Ivory with a faint shadow, read as floating text | Light raised panels get a 1dp Sand edge | Panel reads as a card in light screenshots |
| I07 | Floating field labels and error text were 12sp Garamond, hard to read | Field label and error text 15sp | "Add a title." and labels readable at 1x |
| I08 | After rating, both buttons looked the same; only the status line changed | Chosen rating button shows a check icon | "✓ Liked it" visible on Detail |
| I09 | Name suggestion chip looked like a type chip | Suggestions are filled Sand chips without an outline | Suggestion and type chips look different |
| I10 | Long titles and links were cut off in single-line fields | Title and link wrap up to 3 lines | Full title visible while editing |
| I11 | In landscape the keyboard covered the whole screen | flagNoExtractUi on all fields | Form stays visible above the keyboard |
| I12 | At 2x font, "Discard" split as "Discar d" and rating buttons had uneven heights | Pop-out buttons share the row; paired buttons match height | Clean wrapping at 2x in both panels and Detail |

## Improvements from the M4 design pass (2026-11-23)

| ID | Evidence | Change | Result |
|---|---|---|---|
| I13 | At 2x font and in landscape the search and filters never collapsed, leaving room for about one row | Toolbar moved out of the collapsing header so the filters can scroll away; title stays pinned | At 2x, scrolling hides the filters and shows five rows |
| I14 | First launch showed an empty search bar and filter chips with nothing to filter | Search, filters and Pick for me hide until there is at least one pick | First run shows only the empty message and + |

## M5 review (2026-11-23)

**Security review** (by hand; the `security-review` skill needs a remote branch and could not start):

| Area | Finding | Action |
|---|---|---|
| SQL injection | Every query uses `?` arguments; search text has `%`, `_` and `\` escaped before LIKE | None needed. Repository test `searchTreatsWildcardsAsText` covers it |
| Exported components | Only the launcher and the Share alias are exported. The alias ignores `rec_id`, so other apps cannot open the edit form for a pick | Fixed in M3 review; checked again |
| Shared text | Text shared from other apps had no size cap | Capped at 501 characters, so a too-long link still shows "Keep the link to 500 characters or less." |
| Links | Links show as selectable text, not tappable, so a shared link cannot launch anything by itself | Kept on purpose |
| Network | The app has no INTERNET permission, so data cannot leave the phone through the app | Kept on purpose |
| Debug build | A debug APK lets anyone with USB access read the database with `run-as` | Ship a release APK (not debuggable) in M6 |
| Backups | Android's default backup is on, so picks move to a new phone through Google backup (encrypted with the screen lock) | Kept on; a personal list is worth keeping |
| Logging | No log calls anywhere | None needed |

**Accessibility audit** (uiautomator dump of List, People, Add with suggestions, Discard panel, picked Detail, Delete panel; checks 48dp and labels on every tappable element):

- Touch targets: all at least 48dp.
- Missing label: search icon. Fixed ("Search").
- Pop-out dim layer has no label on purpose: it is hidden from TalkBack, which uses Cancel or Back.

**Code check:** clean build has no Kotlin warnings; lint has 0 errors and 10 notes (minimum-SDK attributes, Gradle version, merge tag, overdraw on row ripples, one plural false positive).

**Known limits kept on purpose:** names match case-insensitively for A-Z only (SQLite NOCASE), so "Ángel" and "ángel" are two people; database work runs on the main thread, which is fine for a personal list of a few thousand picks.
