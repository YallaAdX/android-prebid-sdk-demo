# BidOtter Prebid SDK test app (Android)

Sample app that runs banner and video auctions against the BidOtter Prebid Server
(`https://prebid.bidotter.com/openrtb2/auction`) with Prebid Mobile SDK 3.4.0 and renders the results
without an ad server.

## Partner integration

Everything BidOtter-specific is in one file:
[`app/src/main/java/com/bidotter/prebid/BidOtter.kt`](app/src/main/java/com/bidotter/prebid/BidOtter.kt).
Copy it into an app, add `implementation("org.prebid:prebid-mobile-sdk:3.4.0")` and the `INTERNET` /
`ACCESS_NETWORK_STATE` permissions, then:

```kotlin
// Application.onCreate
BidOtter.init(this)

// Banner
val banner = BidOtter.banner(context, "BANNER_PLACEMENT_ID", 320, 50, listener)
container.addView(banner)
banner.loadAd()

// Fullscreen video: call show() from onAdLoaded
val video = BidOtter.videoInterstitial(activity, "VIDEO_PLACEMENT_ID", listener)
video.loadAd()
```

[`MainActivity.kt`](app/src/main/java/com/bidotter/prebidtest/MainActivity.kt) is a working example of both.

## What the helper sends

- Account ID `bidotter` (also the top-level stored request ID, which exists on the server).
- Per imp: `ext.prebid.bidder.bidotter.placementId`, and `ext.prebid.storedrequest: null` so Prebid Server
  doesn't look the placement up as a stored impression (none are configured).
- Banner: `banner.w` / `banner.h` in addition to the SDK's `banner.format`; the exchange sizes the creative from them.
- Video: `ext.prebid.biddercontrols.bidotter.prefmtype = "video"`, because the SDK adds a `banner` object to
  interstitial imps and the adapter is registered as single-format.

## Test placements

| Format | Placement |
| --- | --- |
| Banner | `5poz7emu` |
| Video | `pf7onnh1` |

## Run

Requirements: JDK 17 or 21, the Android SDK (platform 36), and an emulator or device.

Open the project in Android Studio and run the `app` configuration, or from a terminal:

```bash
./gradlew :app:installDebug
```

If Gradle can't find the Android SDK, set `ANDROID_HOME` or create a `local.properties` file with
`sdk.dir=<path to your Android SDK>`.

The app logs to the screen and to logcat under the `BidOtterTest` tag; the full request and response are logged
by the SDK under `PrebidBaseNetworkTask`.

## License

Apache License 2.0, see [LICENSE](LICENSE).
