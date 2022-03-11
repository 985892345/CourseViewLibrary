package com.mredrock.cyxbs.lib.courseview.scroll

import android.util.SparseArray

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 15:32
 */
internal class AbsolutePointerImpl private constructor() : IAbsoluteCoordinates.IAbsolutePointer {
    override var initialX = 0 // Down 时的初始 X 值
    override var initialY = 0 // Down 时的初始 Y 值
    override var lastMoveX = 0 // Move 时的移动 X 值
    override var lastMoveY = 0 // Move 时的移动 Y 值
    override var diffMoveX = 0 // 每次 Move 的偏移值
    override var diffMoveY = 0 // 每次 Move 的偏移值

    class PointerManger : IAbsoluteCoordinates {
        private val mPointers = SparseArray<AbsolutePointerImpl>(3)

        /**
         * 初始化一个保存当前手指事件的 [IAbsoluteCoordinates.IAbsolutePointer]
         */
        fun initPointer(pointerId: Int, initialX: Int, initialY: Int): AbsolutePointerImpl {
            val pointer = mPointers[pointerId] ?: AbsolutePointerImpl().apply {
                mPointers.put(pointerId, this)
            }
            return pointer.apply {
                this.initialX = initialX
                this.initialY = initialY
                lastMoveX = initialX
                lastMoveY = initialY
                diffMoveX = 0
                diffMoveY = 0
            }
        }

        override fun getPointer(pointerId: Int): AbsolutePointerImpl {
            return mPointers.get(pointerId)
        }
    }
}