package com.mredrock.cyxbs.lib.courseview.utils

import android.content.res.TypedArray

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
enum class CourseType {
    OTHER,
    AM,
    NOON,
    PM,
    DUSK,
    NIGHT;

    companion object {
        fun get(i: Int): CourseType {
            return when (i) {
                0 -> AM
                1 -> NOON
                2 -> PM
                3 -> DUSK
                4 -> NIGHT
                else -> OTHER
            }
        }

        fun default(): CourseType = OTHER
    }
}

fun Int.courseType(ty: TypedArray): CourseType {
    return CourseType.get(ty.getInt(this, -1))
}