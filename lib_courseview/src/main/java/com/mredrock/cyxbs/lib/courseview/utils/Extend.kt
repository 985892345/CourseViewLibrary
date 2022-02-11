package com.mredrock.cyxbs.lib.courseview.utils

import android.content.Context
import android.content.res.Resources
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/6 15:45
 */


internal val Int.dp2px: Int
    get() = this.dp2pxF.roundToInt()

internal val Int.dp2pxF: Float
    get() = Resources.getSystem().displayMetrics.density * this

internal val Float.dp2px: Int
    get() = this.dp2pxF.roundToInt()

internal val Float.dp2pxF: Float
    get() = Resources.getSystem().displayMetrics.density * this

internal fun Int.color(context: Context): Int = ContextCompat.getColor(context, this)

internal fun <T> lazyUnlock(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)