package com.bidotter.prebid

import android.app.Activity
import android.content.Context
import org.json.JSONObject
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.data.InitializationStatus
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import java.util.EnumSet

/**
 * BidOtter integration helper for the Prebid Mobile SDK.
 *
 * Copy this single file into your app (adjust the `package` line) and add the SDK:
 *
 * ```
 * implementation("org.prebid:prebid-mobile-sdk:3.4.0")
 * ```
 *
 * The manifest needs `INTERNET` and `ACCESS_NETWORK_STATE`.
 *
 * Usage:
 *
 * ```
 * // Application.onCreate
 * BidOtter.init(this)
 *
 * // Banner
 * val banner = BidOtter.banner(context, "YOUR_BANNER_PLACEMENT_ID", 320, 50, listener)
 * container.addView(banner)
 * banner.loadAd()
 *
 * // Fullscreen video: call show() from onAdLoaded
 * val video = BidOtter.videoInterstitial(activity, "YOUR_VIDEO_PLACEMENT_ID", listener)
 * video.loadAd()
 * ```
 *
 * Call `destroy()` on banners and interstitials when their screen goes away.
 */
object BidOtter {

    const val SERVER_URL = "https://prebid.bidotter.com/openrtb2/auction"
    const val STATUS_URL = "https://prebid.bidotter.com/status"
    const val ACCOUNT_ID = "bidotter"

    private const val BIDDER = "bidotter"

    /** Call once, before creating any ad. [onReady] runs on the main thread. */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        debug: Boolean = false,
        onReady: (success: Boolean, error: String?) -> Unit = { _, _ -> },
    ) {
        PrebidMobile.setPrebidServerAccountId(ACCOUNT_ID)
        PrebidMobile.setCustomStatusEndpoint(STATUS_URL)
        if (debug) {
            PrebidMobile.setLogLevel(PrebidMobile.LogLevel.DEBUG)
            PrebidMobile.setPbsDebug(true)
        }
        PrebidMobile.initializeSdk(context.applicationContext, SERVER_URL) { status ->
            val success = status != InitializationStatus.FAILED
            onReady(success, if (status == InitializationStatus.SUCCEEDED) null else status.description)
        }
    }

    /** Prebid-rendered banner. Add the returned view to a container, then call `loadAd()`. */
    @JvmStatic
    @JvmOverloads
    fun banner(
        context: Context,
        placementId: String,
        width: Int,
        height: Int,
        listener: BannerViewListener? = null,
    ): BannerView {
        val banner = BannerView(context, placementId, AdSize(width, height))
        // The SDK only sends banner.format; the exchange sizes the creative from banner.w/h.
        val config = JSONObject(impConfig(placementId))
            .put("banner", JSONObject().put("w", width).put("h", height))
        banner.setImpOrtbConfig(config.toString())
        listener?.let(banner::setBannerListener)
        return banner
    }

    /** Fullscreen video. Call `loadAd()`, then `show()` once `onAdLoaded` fires. */
    @JvmStatic
    @JvmOverloads
    fun videoInterstitial(
        activity: Activity,
        placementId: String,
        listener: InterstitialAdUnitListener? = null,
    ): InterstitialAdUnit {
        val adUnit = InterstitialAdUnit(activity, placementId, EnumSet.of(AdUnitFormat.VIDEO))
        adUnit.setImpOrtbConfig(impConfig(placementId))
        // The SDK adds a banner object next to video on interstitial imps. The bidder is
        // single-format, so tell Prebid Server which one to keep or it drops the imp.
        adUnit.setGlobalOrtbConfig(preferredMediaType("video"))
        listener?.let(adUnit::setInterstitialAdUnitListener)
        return adUnit
    }

    /**
     * Imp-level OpenRTB config that routes an ad unit to a BidOtter placement. Use it with
     * `setImpOrtbConfig` on ad units this helper doesn't wrap.
     *
     * `storedrequest` is nulled because the SDK otherwise asks Prebid Server to look the ad unit
     * up as a stored impression; the bidder params are supplied here instead.
     */
    @JvmStatic
    fun impConfig(placementId: String): String =
        JSONObject().put("ext", impExt(placementId)).toString()

    private fun preferredMediaType(type: String): String = JSONObject().put(
        "ext",
        JSONObject().put(
            "prebid",
            JSONObject().put("biddercontrols", JSONObject().put(BIDDER, JSONObject().put("prefmtype", type))),
        ),
    ).toString()

    private fun impExt(placementId: String): JSONObject = JSONObject().put(
        "prebid",
        JSONObject()
            .put("storedrequest", JSONObject.NULL)
            .put("bidder", JSONObject().put(BIDDER, JSONObject().put("placementId", placementId))),
    )
}
