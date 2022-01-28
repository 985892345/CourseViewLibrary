package com.mredrock.cyxbs.lib.courseview.utils

import android.content.res.TypedArray

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
enum class CourseType(val i: Int) {
    MY(0),
    OTHER(1),
    AFFAIR(2),
    TIME(3),
    AFFAIR_TOUCH(4),
    UNKNOWN(114514);

    companion object {
        fun default() = TIME

        fun get(i: Int): CourseType {
            return when (i) {
                MY.i -> MY
                OTHER.i -> OTHER
                AFFAIR.i -> AFFAIR
                TIME.i -> TIME
                else -> UNKNOWN
            }
        }
    }
}

internal fun Int.courseType(ty: TypedArray): CourseType {
    return CourseType.get(ty.getInt(this, CourseType.default().i))
}