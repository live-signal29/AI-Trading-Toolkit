package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.AdMobConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Production Google AdMob Banner Ad View.
 * Displays standard 320x50 banner on free user screens without covering buttons, charts, or navigation.
 * Automatically hidden when Pro status or voluntary Rewarded Pass is active.
 */
@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    if (AdMobConfig.isRewardedProActive) {
        return
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobConfig.BANNER_AD_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                            hasError = false
                            Log.d("AdBannerView", "AdMob Banner loaded successfully: ${AdMobConfig.BANNER_AD_UNIT_ID}")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            isAdLoaded = false
                            hasError = true
                            Log.w("AdBannerView", "Banner ad failed to load: ${loadAdError.message} (code ${loadAdError.code})")
                        }
                    }
                    try {
                        loadAd(AdMobConfig.buildAdRequest())
                    } catch (e: Throwable) {
                        Log.e("AdBannerView", "Exception initiating banner ad load: ${e.message}", e)
                        hasError = true
                    }
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Throwable) {
                    Log.e("AdBannerView", "Error releasing adView: ${e.message}")
                }
            }
        )
    }
}
