package com.mredrock.cyxbs.lib.courseview.helper.entitymove

import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import androidx.core.util.forEach
import androidx.core.util.valueIterator
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/16 14:12
 */
class PointerTouchHandlerImpl private constructor(
    val manager: Manger
): IPointerTouchHandler {
    override fun isIntercept(event: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onTouchEvent(event: MotionEvent) {
        TODO("Not yet implemented")
    }

    override var pointerId: Int = -1
    private var mTouchView: View? = null
    private var mDownToViewTopDy: Int = 0
    private var mAbsolutePointer: IAbsoluteCoordinates.IAbsolutePointer? = null
    private var mScrollView: ICourseScrollView? = null

    private fun reset() {
        pointerId = -1
        mTouchView = null
        mDownToViewTopDy = 0
        mAbsolutePointer = null
        mScrollView = null
    }

    class Manger : IPointerTouchHandler.IManger {
        private val mHandlers = SparseArray<PointerTouchHandlerImpl>(3)
        private val mDeletedHandlers = ArrayDeque<PointerTouchHandlerImpl>(3)

        override fun createHandlerIfCan(
            view: View,
            y: Int,
            pointerId: Int,
            absolutePointer: IAbsoluteCoordinates.IAbsolutePointer,
            scrollView: ICourseScrollView
        ): IPointerTouchHandler? {
            // 说明有一个手指正在触摸当前 View
            mHandlers.forEach { _, handler ->
                if (view === handler.mTouchView) {
                    return null
                }
            }
            // 说明该 View 正在执行一个手指抬起时的动画
            mDeletedHandlers.forEach {
                if (view === it.mTouchView) {
                    return null
                }
            }
            val handler = mDeletedHandlers.removeLastOrNull() ?: PointerTouchHandlerImpl(this)
            mHandlers.put(pointerId, handler)
            return handler.apply {
                this.pointerId = pointerId
                mTouchView = view
                mDownToViewTopDy = y - view.top
                mAbsolutePointer = absolutePointer
                mScrollView = scrollView
            }
        }

        override fun getHandler(pointerId: Int): IPointerTouchHandler {
            return mHandlers.get(pointerId)
        }

        fun _removeHandler(pointerId: Int) {
            mDeletedHandlers.addLast(
                mHandlers.get(pointerId).apply {
                    reset()
                    mHandlers.remove(pointerId)
                }
            )
        }

        override fun containHandler(pointerId: Int): Boolean {
            return mHandlers.indexOfKey(pointerId) >= 0
        }

        override fun iterator(): Iterator<IPointerTouchHandler> {
            return mHandlers.valueIterator()
        }
    }
}