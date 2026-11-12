# ARVids Android project — GitHub APK build

This repository contains the original native Kotlin/Jetpack Compose Android project exported from Google AI Studio, plus a GitHub Actions workflow that builds a debug APK.

## Build an APK on GitHub

1. Create a GitHub repository (for example, `ARVids`).
2. Upload the **contents** of this folder to the repository root (the `app`, `gradle`, `.github`, and Gradle files must be at the top level).
3. Open the repository's **Actions** tab.
4. Select **Build ARVids APK** and click **Run workflow**, or push a commit to `main`/`master`.
5. When the workflow finishes successfully, open that run and download **ARVids-debug-APK** under Artifacts.
6. Extract the downloaded artifact ZIP; it contains the APK.

## Notes

- The workflow builds a **debug APK** for testing. It is not a Play Store release build.
- The debug build uses Gradle's standard debug signing. Do not distribute this as a production release.
- The project has a `.env.example` placeholder. Never commit a real API key, `.env`, keystore, or passwords. If the app requires Gemini API access, configure secrets safely and wire them into the build only if the app's code actually reads them.
- The project README from AI Studio mentions a Gemini API key and a release upload keystore. Those are not included here.
- The first GitHub build may take several minutes while Gradle downloads dependencies.
