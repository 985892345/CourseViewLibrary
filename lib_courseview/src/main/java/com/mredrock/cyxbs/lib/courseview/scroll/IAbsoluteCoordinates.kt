package com.mredrock.cyxbs.lib.courseview.scroll

/**
 * 绝对坐标系
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 14:21
 */
interface IAbsoluteCoordinates {
    /**
     * 获取保存了当前手指绝对坐标信息的数据类
     */
    fun getPointer(pointerId: Int): IAbsolutePointer

    /**
     * 绝对坐标系下 pointerId 对应的绝对坐标
     */
    interface IAbsolutePointer {
        val initialX: Int // Down 时的初始 X 值
        val initialY: Int // Down 时的初始 Y 值
        val lastMoveX: Int // Move 时的移动 X 值
        val lastMoveY: Int // Move 时的移动 Y 值
        val diffMoveX: Int // 每次 Move 的偏移值
        val diffMoveY: Int // 每次 Move 的偏移值
    }
}