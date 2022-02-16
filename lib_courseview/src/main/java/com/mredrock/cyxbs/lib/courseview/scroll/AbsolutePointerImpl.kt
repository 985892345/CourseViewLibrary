package com.mredrock.cyxbs.lib.courseview.scroll

import android.util.SparseArray
import androidx.core.util.forEach
import com.mredrock.cyxbs.lib.courseview.helper.entitymove.IAbsoluteCoordinates

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 15:32
 */
class AbsolutePointerImpl private constructor() : IAbsoluteCoordinates.IAbsolutePointer {
    override var initialX = 0 // Down 时的初始 X 值
    override var initialY = 0 // Down 时的初始 Y 值
    override var lastMoveX = 0 // Move 时的移动 X 值
    override var lastMoveY = 0 // Move 时的移动 Y 值
    override var diffMoveX = 0 // 每次 Move 的偏移值
    override var diffMoveY = 0 // 每次 Move 的偏移值

    class PointerManger {
        private val mPointers = SparseArray<AbsolutePointerImpl>(3)
        private val mDeletedPointers = ArrayDeque<AbsolutePointerImpl>(3)

        fun createPointer(pointerId: Int, initialX: Int, initialY: Int): AbsolutePointerImpl {
            val pointer = mDeletedPointers.removeLastOrNull() ?: AbsolutePointerImpl()
            mPointers.put(pointerId, pointer)
            return pointer.apply {
                this.initialX = initialX
                this.initialY = initialY
                lastMoveX = initialX
                lastMoveY = initialY
                diffMoveX = 0
                diffMoveY = 0
            }
        }

        fun getPointer(pointerId: Int): AbsolutePointerImpl {
            return mPointers.get(pointerId)
        }

        fun removePointer(pointerId: Int) {
            mDeletedPointers.addLast(
                mPointers.get(pointerId).apply {
                    mPointers.remove(pointerId)
                }
            )
        }

        fun removeAll() {
            mPointers.forEach { id, pointer ->
                mDeletedPointers.addLast(pointer)
                mPointers.remove(id)
            }
        }
    }
}