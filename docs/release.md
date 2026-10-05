# Release build

How to produce the build that would be published or handed to someone else. Day-to-day
work uses the debug build (`./gradlew :app:installDebug`).

## What the release build is

- Code and resources are shrunk and optimised with R8: about 6.6 MB instead of 17.9 MB.
- Not debuggable: the in-app browser cannot be inspected from a computer.
- Signed with your own key when `keystore.properties` exists; unsigned otherwise.

## Sign it

1. Create a key once and keep it safe. Losing it means you cannot publish updates.

   ```
   keytool -genkeypair -v -keystore sticker-bridge-release.jks -alias sticker-bridge \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Copy `keystore.properties.example` to `keystore.properties` and fill in the passwords.
   Both files (`*.jks`, `keystore.properties`) are ignored by git; never commit them.

3. Build:

   | Result | Command | Output |
   |---|---|---|
   | APK to install directly | `./gradlew :app:assembleRelease` | `app/build/outputs/apk/release/` |
   | Bundle for Google Play | `./gradlew :app:bundleRelease` | `app/build/outputs/bundle/release/` |

4. Before each new release, raise `versionCode` (and `versionName`) in `app/build.gradle.kts`.

## Check it on a phone

The release build has only been compiled, never run. R8 can break code that is reached
by name, so run the whole of [`manual-checklist.md`](manual-checklist.md) on the signed
release APK, paying attention to converting an animated sticker (native WebP code).

## Publishing on Google Play

Technically the app is ready to be packaged: adaptive icon, light and dark themes, a
[privacy policy](privacy-policy.md), no permissions beyond internet, target API 36.

Whether Google accepts it is a different question, and the honest answer is that it
probably would not:

- It reads TikTok through an interface TikTok does not offer to third parties, against
  TikTok's terms of service. Play's policies do not allow that.
- The stickers are other people's content, redistributed without their permission.
- It uses the names TikTok and WhatsApp, which needs care around trademarks.

It is a personal and portfolio project; installing the APK directly has none of these
obstacles.
