package com.mredrock.cyxbs.lib.courseview.utils

import android.content.res.Resources
import kotlin.math.roundToInt

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/6 15:45
 */

internal fun Int.dp2px(): Int = this.dp2pxF().roundToInt()

internal fun Int.dp2pxF(): Float = Resources.getSystem().displayMetrics.density * this

internal fun Float.dp2px(): Int = this.dp2pxF().roundToInt()

internal fun Float.dp2pxF(): Float = Resources.getSystem().displayMetrics.density * this