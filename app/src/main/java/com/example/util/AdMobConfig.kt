package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Centralized Google AdMob Configuration & Lifecycle Manager.
 * All AdMob App IDs and Ad Unit IDs are maintained in this single source of truth.
 */
object AdMobConfig {
    private const val TAG = "AdMobConfig"

    // =========================================================================
    // EXACT PRODUCTION ADMOB IDS
    // =========================================================================
    const val ADMOB_APP_ID: String = "ca-app-pub-1895906484640218~5320396350"
    const val BANNER_AD_UNIT_ID: String = "ca-app-pub-1895906484640218/4398925873"
    const val INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-1895906484640218/1694695310"
    const val REWARDED_AD_UNIT_ID: String = "ca-app-pub-1895906484640218/5536018777"

    // Pro User Status (manually toggled or purchased)
    var isProUser by mutableStateOf(false)

    // Temporary Pro pass timestamp unlocked voluntarily via Rewarded Ad
    var rewardedProExpiryTimestamp by mutableStateOf(0L)

    val isRewardedProActive: Boolean
        get() = isProUser || (rewardedProExpiryTimestamp > System.currentTimeMillis())

    // Cooldown management for Interstitial ads (at least 60 seconds between displays)
    private const val INTERSTITIAL_COOLDOWN_MS = 60_000L
    private var lastInterstitialShownTime = 0L

    // Preloaded Ad References
    private var interstitialAd: InterstitialAd? = null
    private val isInterstitialLoading = AtomicBoolean(false)

    private var rewardedAd: RewardedAd? = null
    private val isRewardedLoading = AtomicBoolean(false)

    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Initializes Google Mobile Ads SDK safely at application start.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "AdMob MobileAds initialized successfully: $status")
                // Preload interstitial and rewarded ads for smooth, low-latency playback
                loadInterstitialAd(context.applicationContext)
                loadRewardedAd(context.applicationContext)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize AdMob SDK: ${e.message}", e)
        }
    }

    /**
     * Builds standard ad request.
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Preloads an Interstitial Ad in the background.
     */
    fun loadInterstitialAd(context: Context) {
        if (isRewardedProActive || interstitialAd != null || isInterstitialLoading.get()) return
        isInterstitialLoading.set(true)

        mainHandler.post {
            try {
                InterstitialAd.load(
                    context,
                    INTERSTITIAL_AD_UNIT_ID,
                    buildAdRequest(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            isInterstitialLoading.set(false)
                            Log.d(TAG, "Interstitial ad loaded successfully")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            interstitialAd = null
                            isInterstitialLoading.set(false)
                            Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                        }
                    }
                )
            } catch (e: Throwable) {
                isInterstitialLoading.set(false)
                Log.e(TAG, "Error while requesting Interstitial Ad: ${e.message}", e)
            }
        }
    }

    /**
     * Displays an Interstitial Ad at natural navigation points, respecting cooldown.
     * Always calls onDismissed() so navigation and user flows are never blocked or hung.
     */
    fun showInterstitialIfReady(activity: Activity?, onDismissed: () -> Unit) {
        if (isRewardedProActive || activity == null) {
            onDismissed()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInterstitialShownTime < INTERSTITIAL_COOLDOWN_MS) {
            Log.d(TAG, "Interstitial ad skipped due to cooldown (${(currentTime - lastInterstitialShownTime) / 1000}s elapsed)")
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready, queuing reload")
            loadInterstitialAd(activity.applicationContext)
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastInterstitialShownTime = System.currentTimeMillis()
                loadInterstitialAd(activity.applicationContext)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                loadInterstitialAd(activity.applicationContext)
                onDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                lastInterstitialShownTime = System.currentTimeMillis()
            }
        }

        mainHandler.post {
            try {
                ad.show(activity)
            } catch (e: Throwable) {
                Log.e(TAG, "Exception showing interstitial ad: ${e.message}", e)
                interstitialAd = null
                onDismissed()
            }
        }
    }

    /**
     * Preloads a Rewarded Ad in the background.
     */
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading.get()) return
        isRewardedLoading.set(true)

        mainHandler.post {
            try {
                RewardedAd.load(
                    context,
                    REWARDED_AD_UNIT_ID,
                    buildAdRequest(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardedAd = ad
                            isRewardedLoading.set(false)
                            Log.d(TAG, "Rewarded ad loaded successfully")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            rewardedAd = null
                            isRewardedLoading.set(false)
                            Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                        }
                    }
                )
            } catch (e: Throwable) {
                isRewardedLoading.set(false)
                Log.e(TAG, "Error while requesting Rewarded Ad: ${e.message}", e)
            }
        }
    }

    /**
     * Displays a Rewarded Ad where users can voluntarily watch an ad to receive a reward.
     * Rewards user with 2 hours of Pro Pass (unlocks ad-free experience & VIP insights).
     */
    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: (Long) -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        if (activity == null) {
            onDismissed()
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Rewarded ad was not ready")
            loadRewardedAd(activity.applicationContext)
            onDismissed()
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd(activity.applicationContext)
                if (rewardGranted) {
                    // Grant 2 hours of Pro Pass
                    val durationMs = 2 * 3600 * 1000L
                    rewardedProExpiryTimestamp = System.currentTimeMillis() + durationMs
                    onRewardEarned(rewardedProExpiryTimestamp)
                }
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                loadRewardedAd(activity.applicationContext)
                onDismissed()
            }
        }

        mainHandler.post {
            try {
                ad.show(activity) { rewardItem ->
                    rewardGranted = true
                    Log.d(TAG, "User successfully earned reward: ${rewardItem.type} x${rewardItem.amount}")
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Exception showing rewarded ad: ${e.message}", e)
                rewardedAd = null
                onDismissed()
            }
        }
    }

    val isRewardedAdReady: Boolean
        get() = rewardedAd != null

    /**
     * Safely unwrap Activity from any Context/ContextWrapper.
     */
    fun getActivity(context: Context): Activity? {
        var currentContext: Context? = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}
