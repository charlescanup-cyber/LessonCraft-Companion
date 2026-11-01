package com.lessoncraft.companion.data

import android.content.Context
import com.lessoncraft.companion.BuildConfig
import com.lessoncraft.engine.Tier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Which plan this device is on. New installs are on the free plan: planning, search, favorites and folders work, and Print and Export show the web
 * app's Bundle prompt. The app has no network access, so a real unlock has to arrive some other way (for example, a signed activation code from the
 * web app); that belongs in [redeem] once the web app issues one. Until then only debug builds can switch plans, to test the unlocked screens.
 */
class EntitlementStore(context: Context) {
    private val prefs = context.getSharedPreferences("entitlement", Context.MODE_PRIVATE)
    private val _tier = MutableStateFlow(read())
    val tier: StateFlow<Tier> = _tier.asStateFlow()

    private fun read(): Tier = runCatching { Tier.valueOf(prefs.getString("tier", Tier.FREE.name) ?: Tier.FREE.name) }.getOrDefault(Tier.FREE)

    /** Debug builds only: flip between plans to test both states. A release build ignores this. */
    fun setTierForTesting(tier: Tier) {
        if (!BuildConfig.DEBUG) return
        prefs.edit().putString("tier", tier.name).apply()
        _tier.value = tier
    }

    /** Extension point for a signed activation code from the web app. Returns false until that exists, so nothing can unlock the Bundle by accident. */
    @Suppress("UNUSED_PARAMETER")
    fun redeem(code: String): Boolean = false
}
