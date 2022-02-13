package com.mredrock.cyxbs.lib.courseview.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/12 15:51
 */
interface ViewExtend {
    val context: Context

    fun Int.dp2px(): Int = this.dp2pxF().roundToInt()

    fun Int.dp2pxF(): Float = context.resources.displayMetrics.density * this

    fun Float.dp2px(): Int = this.dp2pxF().roundToInt()

    fun Float.dp2pxF(): Float = context.resources.displayMetrics.density * this

    fun Int.color(): Int = ContextCompat.getColor(context, this)

    fun Int.dimens(): Float = context.resources.getDimension(this)

    fun Int.string(): String = context.resources.getString(this)

    fun Int.drawable(): Drawable? = AppCompatResources.getDrawable(context, this)
}