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
    LINK(1),
    AFFAIR(2),
    TIME(3),
    AFFAIR_TOUCH(4),
    ARROW_NOON(5),
    ARROW_DUSK(6),
    UNKNOWN(114514);

    companion object {
        fun default() = TIME

        fun get(i: Int): CourseType {
            return when (i) {
                MY.i -> MY
                LINK.i -> LINK
                AFFAIR.i -> AFFAIR
                TIME.i -> TIME
                AFFAIR_TOUCH.i -> AFFAIR_TOUCH
                ARROW_NOON.i -> ARROW_NOON
                ARROW_DUSK.i -> ARROW_DUSK
                else -> UNKNOWN
            }
        }
    }
}

internal fun Int.courseType(ty: TypedArray): CourseType {
    return CourseType.get(ty.getInt(this, CourseType.default().i))
}