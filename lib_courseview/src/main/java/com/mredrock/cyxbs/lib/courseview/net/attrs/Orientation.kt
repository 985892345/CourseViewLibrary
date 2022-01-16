package com.mredrock.cyxbs.lib.courseview.net.attrs

import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/14
 */
enum class Orientation(val v: Int) {
    LEFT_RIGHT(0),
    RIGHT_LEFT(1),
    TOP_BOTTOM(2),
    BOTTOM_TOP(3);

    companion object {
        fun get(v: Int): Orientation {
            return when (v) {
                LEFT_RIGHT.v -> LEFT_RIGHT
                RIGHT_LEFT.v -> RIGHT_LEFT
                TOP_BOTTOM.v -> TOP_BOTTOM
                BOTTOM_TOP.v -> BOTTOM_TOP
                else -> default()
            }
        }

        fun default(): Orientation {
            return LEFT_RIGHT
        }
    }
}

fun Int.orientation(typedef: BaseViewAttrs.Typedef): Orientation {
    return Orientation.get(typedef.ty.getInt(this, Orientation.default().v))
}