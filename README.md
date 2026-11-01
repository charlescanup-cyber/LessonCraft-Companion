# LessonCraft Companion (Android)

A native Android app (Kotlin, Jetpack Compose) for planning lessons on the go. It is **air-gapped by design**: the manifest declares no `INTERNET` permission, so the app cannot make a network request. Everything runs on the device.

## What it does

- **Create** a lesson from a dual input: a compact **Topic / standard** field (a code like `ELAGSE1RL2` or `RL.1.2`, or a core topic) and a large **Custom instructions** box (a theme, how you teach, what your students need). Either one, or both, is enough. With both, the standard is the rigorous anchor and the instructions are the creative layer.
- **Three ideas, then New ideas.** Each round is drawn only from approaches, hooks, themes and core texts not yet used this session, and checked for word overlap against everything already shown.
- **Build** a full lesson: objective, a four-step sequence that climbs Bloom's levels, UDL for every step, a student handout, an answer key and three tiers of differentiation.
- **Library** with air-gapped **search**, **favorites** and your own **folders** to organize plans.
- **Print and Export** (Android print / Save as PDF, and share as HTML) are part of the **web app's Bundle subscription**. On the free plan they open a prompt that hands off to the Bundle page in the browser. Everything else is free.

## The local rule engine (`:engine`)

`engine/` is plain Kotlin with no Android and no network code, so it runs and tests on any JVM. No model is called anywhere: the same request and session history always produce the same ideas.

| File | Role |
|---|---|
| `StandardsCatalog.kt` | Built-in Common Core standards (K-5 starter set) with Georgia codes derived from them; resolves codes in CCSS or Georgia form, or the closest match by keywords |
| `IdeaEngine.kt` | Deterministic ideas: seeded from request plus history, excludes used lenses, redrafts when a draft echoes an earlier idea |
| `LessonBuilder.kt` | Objective, Bloom-ordered sequence, UDL, differentiation, and grade-specific handout items |
| `Guardrails.kt` | Reading-level limits, **Kindergarten and Grade 1 guard** (decodable student text, only printable task types, teacher scripts for anything spoken), Bloom's mix by grade band, custom-prompt reader |
| `Library.kt` | `LessonSearch` (weighted, deterministic search), library filters, `Gate` (Print/Export vs plan), `LessonHtml` |
| `LessonCodec.kt` | Dependency-free JSON for saving lessons |

The catalog is a starter: a standard that is not in it is still planned from the topic you type. Add entries to `StandardsCatalog.all` to grow it (science and social studies have no entries yet).

## Build

Requirements: JDK 17, Android Studio (Ladybug or newer) or the Android SDK with platform 35.

```bash
./gradlew :engine:test        # rule engine tests, JVM only
./gradlew :app:assembleDebug  # the app
```

Set the Bundle page the locked prompt opens in `gradle.properties` (`bundleUrl`) or with `-PbundleUrl=...`.

## Plans

New installs are on the free plan. `EntitlementStore.redeem(code)` is the extension point for unlocking the Bundle on an air-gapped device (for example a signed activation code issued by the web app); it returns `false` until that exists. Debug builds can switch plans with `setTierForTesting` to exercise the unlocked screens.

## Layout

```
engine/   Kotlin/JVM rule engine and its tests
app/      Android app: data (Room), ui (Compose screens), share (print/export)
```
