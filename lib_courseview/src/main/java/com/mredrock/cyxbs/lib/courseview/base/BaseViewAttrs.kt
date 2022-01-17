package com.mredrock.cyxbs.lib.courseview.base

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.StyleableRes
import androidx.core.content.res.getIntOrThrow

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/13
 */
interface BaseViewAttrs {

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

    class Typedef(val ty: TypedArray, ) {
        fun Int.int(defValue: Int): Int {
            return ty.getInt(this, defValue)
        }

        /**
         * 由于是属性读取时出错，应用会直接闪退，所以不用担心反射的性能消耗
         */
        inline fun <reified E: RuntimeException> Int.intOrThrow(attrsName: String): Int {
            if (!ty.hasValue(this)) {
                throw E::class.java.getConstructor(String::class.java)
                    .newInstance("属性 $attrsName 没有被定义！")
            }
            return this.int(0)
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

