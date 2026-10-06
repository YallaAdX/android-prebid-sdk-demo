# BidOtter Prebid SDK test app (Android)

Sample app that runs banner, interstitial, video and rewarded video auctions against the BidOtter Prebid Server
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

// Fullscreen ads: call show() from onAdLoaded
val interstitial = BidOtter.interstitial(activity, "INTERSTITIAL_PLACEMENT_ID", listener)
val video = BidOtter.videoInterstitial(activity, "VIDEO_PLACEMENT_ID", listener)
val rewarded = BidOtter.rewardedVideo(activity, "REWARDED_PLACEMENT_ID", rewardedListener)
interstitial.loadAd()
```

| Format | Helper | Listener |
| --- | --- | --- |
| Banner | `BidOtter.banner` | `BannerViewListener` |
| Interstitial (display) | `BidOtter.interstitial` | `InterstitialAdUnitListener` |
| Interstitial (video) | `BidOtter.videoInterstitial` | `InterstitialAdUnitListener` |
| Rewarded video | `BidOtter.rewardedVideo` | `RewardedAdUnitListener`; grant the reward in `onUserEarnedReward` |

[`MainActivity.kt`](app/src/main/java/com/bidotter/prebidtest/MainActivity.kt) is a working example of each.

## What the helper sends

- Account ID `bidotter` (also the top-level stored request ID, which exists on the server).
- Per imp: `ext.prebid.bidder.bidotter.placementId`, and `ext.prebid.storedrequest: null` so Prebid Server
  doesn't look the placement up as a stored impression (none are configured).
- Banner: `banner.w` / `banner.h` in addition to the SDK's `banner.format`; the exchange sizes the creative from them.
- Video and rewarded video: `ext.prebid.biddercontrols.bidotter.prefmtype = "video"`, because the SDK adds a
  `banner` object to fullscreen imps and the adapter is registered as single-format.

## Test placements

These placements always fill, with BidOtter house ads marked "Test ad".

| Format | Placement | Sizes |
| --- | --- | --- |
| Banner | `5poz7emu` | 320x50, 320x100, 300x250, 728x90 |
| Interstitial (display) | `zssnke42` | 320x480, 480x320, 768x1024 |
| Interstitial (video) | `pf7onnh1` | portrait and landscape, 6 to 30 seconds |
| Rewarded video | `3czizw1b` | portrait and landscape, 15 seconds |

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
