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
internal interface BaseViewAttrs {

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

    class Typedef(val ty: TypedArray) {
        fun Int.int(defValue: Int): Int = this.int(ty, defValue)
        fun Int.color(defValue: Int): Int = this.color(ty, defValue)
        fun Int.dimens(defValue: Int): Int = this.dimens(ty, defValue)
        fun Int.dimens(defValue: Float): Float = this.dimens(ty, defValue)
        fun Int.string(defValue: String? = null): String = this.string(ty, defValue)
        fun Int.boolean(defValue: Boolean): Boolean = this.boolean(ty, defValue)
        inline fun <reified E: RuntimeException> Int.intOrThrow(
            attrsName: String): Int = this.intOrThrow<E>(ty, attrsName)
        inline fun <reified E: RuntimeException> Int.stringOrThrow(
            attrsName: String): String = this.stringOrThrow<E>(ty, attrsName)
    }
}

internal inline fun <T> Context.getAttrs(
    attrs: AttributeSet,
    @StyleableRes
    styleableId: IntArray,
    func: BaseViewAttrs.Typedef.() -> T
) = BaseViewAttrs.newAttrs(this, attrs, styleableId, func)

internal fun Int.int(ty: TypedArray, defValue: Int): Int {
    return ty.getInt(this, defValue)
}

internal fun Int.color(ty: TypedArray, defValue: Int): Int {
    return ty.getColor(this, defValue)
}

internal fun Int.dimens(ty: TypedArray, defValue: Int): Int {
    return ty.getDimensionPixelSize(this, defValue)
}

internal fun Int.dimens(ty: TypedArray, defValue: Float): Float {
    return ty.getDimension(this, defValue)
}

internal fun Int.string(ty: TypedArray, defValue: String? = null): String {
    return ty.getString(this) ?: defValue ?: ""
}

internal fun Int.boolean(ty: TypedArray, defValue: Boolean): Boolean {
    return ty.getBoolean(this, defValue)
}

/**
 * 由于是属性读取时出错，应用一般是直接闪退，所以不用担心反射的性能消耗
 */
internal inline fun <reified E: RuntimeException> Int.intOrThrow(ty: TypedArray, attrsName: String): Int {
    if (!ty.hasValue(this)) {
        throw E::class.java.getConstructor(String::class.java)
            .newInstance("属性 $attrsName 没有被定义！")
    }
    return this.int(ty, 0)
}

internal inline fun <reified E: java.lang.RuntimeException> Int.stringOrThrow(
    ty: TypedArray, attrsName: String
): String {
    if (!ty.hasValue(this)) {
        throw E::class.java.getConstructor(String::class.java)
            .newInstance("属性 $attrsName 没有被定义！")
    }
    return this.string(ty)
}