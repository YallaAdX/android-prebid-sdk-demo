package com.bidotter.prebidtest

import android.app.Application
import android.util.Log
import com.bidotter.prebid.BidOtter

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        BidOtter.init(this, debug = true) { success, error ->
            Log.i(TAG, "Prebid SDK init: success=$success error=$error")
        }
    }

    companion object {
        const val TAG = "BidOtterTest"
    }
}
