# Kids Number Quest

Production-oriented Jetpack Compose educational game for ages 4+, covering:
- Before Numbers
- After Numbers
- Greater Than / Less Than
- In-Between Numbers
- Shapes Identification

Each module has Easy and Hard modes. Number levels contain 30 generated questions per difficulty, with adjacent target answers guaranteed not to repeat. Correct answers show 3 seconds of confetti and enable an explicit Next Question button. Incorrect attempts use a gentle wiggle and never use red error UI.

## Build
1. Install Android Studio Ladybug or newer with Android SDK 35.
2. Open this folder in Android Studio.
3. Allow Gradle to sync and install missing SDK components if prompted.
4. Run on an Android 8.0+ device/emulator.
5. Build > Generate App Bundles or APKs > Generate APKs.
6. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
7. Release APK: configure signing in Android Studio, then Generate Signed App Bundle/APK.

## Architecture
- `core`: handwriting recognition, question generation, audio and feedback.
- `game`: domain models and game engine.
- `ui`: Compose screens and visual components.

## Handwriting
The recognizer is fully on-device and dependency-free. It normalizes strokes, segments multi-digit input using horizontal gaps, compares normalized stroke geometry against digit/symbol templates, and returns a confidence score. Low confidence is treated as a retry opportunity, not an immediate wrong answer.
