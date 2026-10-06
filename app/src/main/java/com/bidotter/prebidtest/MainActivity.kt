package com.bidotter.prebidtest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bidotter.prebid.BidOtter
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.RewardedAdUnit
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener
import org.prebid.mobile.rendering.interstitial.rewarded.Reward
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * One screen, one button per ad format. Each button requests an ad from a BidOtter test
 * placement and renders it; the log underneath shows every SDK callback.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bannerContainer: FrameLayout
    private lateinit var logView: TextView
    private lateinit var logScroll: ScrollView

    private var bannerView: BannerView? = null
    private var interstitial: InterstitialAdUnit? = null
    private var rewarded: RewardedAdUnit? = null

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bannerContainer = findViewById(R.id.bannerContainer)
        logView = findViewById(R.id.log)
        logScroll = findViewById(R.id.logScroll)

        findViewById<Button>(R.id.btnBanner320x50).setOnClickListener { loadBanner(320, 50) }
        findViewById<Button>(R.id.btnBanner320x100).setOnClickListener { loadBanner(320, 100) }
        findViewById<Button>(R.id.btnBanner300x250).setOnClickListener { loadBanner(300, 250) }
        findViewById<Button>(R.id.btnInterstitial).setOnClickListener { loadInterstitial() }
        findViewById<Button>(R.id.btnVideo).setOnClickListener { loadVideo() }
        findViewById<Button>(R.id.btnRewarded).setOnClickListener { loadRewarded() }

        log("Server: ${BidOtter.SERVER_URL}")
        log("Account: ${BidOtter.ACCOUNT_ID}")
    }

    private fun loadBanner(width: Int, height: Int) {
        bannerView?.destroy()
        bannerContainer.removeAllViews()

        log("Banner ${width}x$height: loading placement $BANNER_PLACEMENT")
        val banner = BidOtter.banner(this, BANNER_PLACEMENT, width, height, object : BannerViewListener {
            override fun onAdLoaded(bannerView: BannerView) {
                val bid = bannerView.bidResponse?.winningBid
                log("Banner: loaded, price=${bid?.price} size=${bid?.width}x${bid?.height}")
            }

            override fun onAdDisplayed(bannerView: BannerView) = log("Banner: displayed")
            override fun onAdFailed(bannerView: BannerView, exception: AdException) =
                log("Banner: FAILED ${exception.message}")

            override fun onAdClicked(bannerView: BannerView) = log("Banner: clicked")
            override fun onAdClosed(bannerView: BannerView) = log("Banner: closed")
        })
        bannerView = banner
        bannerContainer.addView(banner)
        banner.loadAd()
    }

    private fun loadInterstitial() {
        interstitial?.destroy()

        log("Interstitial: loading placement $INTERSTITIAL_PLACEMENT")
        val adUnit = BidOtter.interstitial(this, INTERSTITIAL_PLACEMENT, fullscreenListener("Interstitial"))
        interstitial = adUnit
        adUnit.loadAd()
    }

    private fun loadVideo() {
        interstitial?.destroy()

        log("Video: loading placement $VIDEO_PLACEMENT")
        val adUnit = BidOtter.videoInterstitial(this, VIDEO_PLACEMENT, fullscreenListener("Video"))
        interstitial = adUnit
        adUnit.loadAd()
    }

    private fun loadRewarded() {
        rewarded?.destroy()

        log("Rewarded: loading placement $REWARDED_PLACEMENT")
        val adUnit = BidOtter.rewardedVideo(this, REWARDED_PLACEMENT, object : RewardedAdUnitListener {
            override fun onAdLoaded(rewardedAdUnit: RewardedAdUnit) {
                log("Rewarded: loaded, price=${rewardedAdUnit.bidResponse?.winningBid?.price}, showing")
                rewardedAdUnit.show()
            }

            override fun onAdDisplayed(rewardedAdUnit: RewardedAdUnit) = log("Rewarded: displayed")
            override fun onAdFailed(rewardedAdUnit: RewardedAdUnit, exception: AdException) =
                log("Rewarded: FAILED ${exception.message}")

            override fun onAdClicked(rewardedAdUnit: RewardedAdUnit) = log("Rewarded: clicked")
            override fun onAdClosed(rewardedAdUnit: RewardedAdUnit) = log("Rewarded: closed")

            // This is where an app grants the reward.
            override fun onUserEarnedReward(rewardedAdUnit: RewardedAdUnit, reward: Reward?) =
                log("Rewarded: user earned reward ${reward?.count ?: ""} ${reward?.type ?: ""}".trimEnd())
        })
        rewarded = adUnit
        adUnit.loadAd()
    }

    /** Shows the ad as soon as it has loaded and logs the rest of its lifecycle. */
    private fun fullscreenListener(label: String) = object : InterstitialAdUnitListener {
        override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit) {
            log("$label: loaded, price=${interstitialAdUnit.bidResponse?.winningBid?.price}, showing")
            interstitialAdUnit.show()
        }

        override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit) = log("$label: displayed")
        override fun onAdFailed(interstitialAdUnit: InterstitialAdUnit, exception: AdException) =
            log("$label: FAILED ${exception.message}")

        override fun onAdClicked(interstitialAdUnit: InterstitialAdUnit) = log("$label: clicked")
        override fun onAdClosed(interstitialAdUnit: InterstitialAdUnit) = log("$label: closed")
    }

    private fun log(message: String) {
        Log.i(App.TAG, message)
        logView.append("${timeFormat.format(Date())}  $message\n")
        logScroll.post { logScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        bannerView?.destroy()
        interstitial?.destroy()
        rewarded?.destroy()
        super.onDestroy()
    }

    /** BidOtter's public test placements. They always fill, with ads marked "Test ad". */
    private companion object {
        const val BANNER_PLACEMENT = "5poz7emu"
        const val INTERSTITIAL_PLACEMENT = "zssnke42"
        const val VIDEO_PLACEMENT = "pf7onnh1"
        const val REWARDED_PLACEMENT = "3czizw1b"
    }
}
