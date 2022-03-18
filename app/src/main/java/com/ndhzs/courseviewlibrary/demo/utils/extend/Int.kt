package com.ndhzs.courseviewlibrary.demo.utils.extend

import android.content.Context
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/13 18:05
 */

internal fun Int.dp2px(context: Context): Int = this.dp2pxF(context).roundToInt()

internal fun Int.dp2pxF(context: Context): Float = context.resources.displayMetrics.density * this

internal fun Int.color(context: Context): Int = ContextCompat.getColor(context, this)