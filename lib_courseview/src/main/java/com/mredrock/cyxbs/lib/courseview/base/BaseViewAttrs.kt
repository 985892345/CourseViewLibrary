package com.mredrock.cyxbs.lib.courseview.base

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import androidx.core.content.res.getIntOrThrow

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/13
 */
interface BaseViewAttrs{

    companion object {
        inline fun <T> newAttrs(
            context: Context,
            attrs: AttributeSet,
            @StyleableRes
            styleableId: IntArray,
            func: Typedef.() -> T
        ): T {
            val ty = context.obtainStyledAttributes(attrs, styleableId)
            try {
                return Typedef(ty).func()
            } finally {
                ty.recycle()
            }
        }
    }

    class Typedef(internal val ty: TypedArray) {
        fun Int.int(defValue: Int): Int {
            return ty.getInt(this, defValue)
        }

        fun Int.intOrThrow(): Int {
            return ty.getIntOrThrow(this)
        }

        fun Int.color(defValue: Int): Int {
            return ty.getColor(this, defValue)
        }

        fun Int.dimens(defValue: Int): Int {
            return ty.getDimensionPixelSize(this, defValue)
        }

        fun Int.dimens(defValue: Float): Float {
            return ty.getDimension(this, defValue)
        }

        fun Int.string(defValue: String? = null): String {
            return ty.getString(this) ?: defValue ?: ""
        }
    }
}

