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
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bannerContainer: FrameLayout
    private lateinit var logView: TextView
    private lateinit var logScroll: ScrollView

    private var bannerView: BannerView? = null
    private var interstitial: InterstitialAdUnit? = null

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bannerContainer = findViewById(R.id.bannerContainer)
        logView = findViewById(R.id.log)
        logScroll = findViewById(R.id.logScroll)

        findViewById<Button>(R.id.btnBanner320).setOnClickListener { loadBanner(320, 50) }
        findViewById<Button>(R.id.btnBanner300).setOnClickListener { loadBanner(300, 250) }
        findViewById<Button>(R.id.btnVideo).setOnClickListener { loadVideo() }

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

    private fun loadVideo() {
        interstitial?.destroy()

        log("Video: loading placement $VIDEO_PLACEMENT")
        val adUnit = BidOtter.videoInterstitial(this, VIDEO_PLACEMENT, object : InterstitialAdUnitListener {
            override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit) {
                log("Video: loaded, price=${interstitialAdUnit.bidResponse?.winningBid?.price}, showing")
                interstitialAdUnit.show()
            }

            override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit) = log("Video: displayed")
            override fun onAdFailed(interstitialAdUnit: InterstitialAdUnit, exception: AdException) =
                log("Video: FAILED ${exception.message}")

            override fun onAdClicked(interstitialAdUnit: InterstitialAdUnit) = log("Video: clicked")
            override fun onAdClosed(interstitialAdUnit: InterstitialAdUnit) = log("Video: closed")
        })
        interstitial = adUnit
        adUnit.loadAd()
    }

    private fun log(message: String) {
        Log.i(App.TAG, message)
        logView.append("${timeFormat.format(Date())}  $message\n")
        logScroll.post { logScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        bannerView?.destroy()
        interstitial?.destroy()
        super.onDestroy()
    }

    private companion object {
        const val BANNER_PLACEMENT = "5poz7emu"
        const val VIDEO_PLACEMENT = "pf7onnh1"
    }
}
