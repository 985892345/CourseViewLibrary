package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import android.util.SparseArray
import android.view.MotionEvent
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractTouchHandler
import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * 长按生成事务的事件分发者
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 19:14
 */
internal class CreateAffairPointerDispatcher(
    val course: CourseLayout
) : AbstractPointerDispatcher<CourseLayout, CreateAffairTouchHandler>() {

    private val mHandlerById = SparseArray<CreateAffairTouchHandler>(3)

    private val mTouchAffairViewPool = TouchAffairViewPool()

    override fun isPrepareToIntercept(event: IPointerEvent, view: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                val child = course.findItemUnderByXY(x, y)
                if (child == null) {
                    val handler = mHandlerPool.getHandler()
                    mHandlerById.put(event.pointerId, handler)
                    handler.start(event, mTouchAffairViewPool.getView())
                    return true
                }
            }
            UP, CANCEL -> {
                mHandlerById[event.pointerId]?.cancel()
            }
            else -> {}
        }
        return false
    }

    override fun getInterceptHandler(
        event: IPointerEvent,
        view: CourseLayout
    ): IPointerTouchHandler<CourseLayout>? {
        return mHandlerById[event.pointerId]
    }

    override fun createNewHandler(): CreateAffairTouchHandler {
        return CreateAffairTouchHandler(course, this)
    }

    override fun onDispatchTouchEvent(event: MotionEvent, view: CourseLayout) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            removeLastTouchAffairViewNextDown(x, y)
        }
    }

    /**
     * 如果点击的是其他地方就取消之前显示的 mTouchAffairView
     */
    private fun removeLastTouchAffairViewNextDown(x: Int, y: Int) {
        val child = course.findItemUnderByXY(x, y)
        val type = (child?.layoutParams as CourseLayoutParams?)?.type
        if (type == null || type != CourseType.AFFAIR_TOUCH) {
            for (handler in mTouchAffairViewPool) {
                handler.remove()
            }
        }
    }

    /**
     * 得到 [TouchAffairView] 的池子，主要是用于复用
     */
    private inner class TouchAffairViewPool : Iterable<TouchAffairView> {
        private val mViews = ArrayList<TouchAffairView>(5)

        fun getView(): TouchAffairView {
            mViews.forEach {
                if (!it.isUsed) {
                    return it
                }
            }
            return TouchAffairView(course).apply {
                mViews.add(this)
            }
        }

        override fun iterator(): Iterator<TouchAffairView> {
            return mViews.iterator()
        }
    }

    abstract class AbstractCreateAffairTouchHandler(
        private val dispatcher: CreateAffairPointerDispatcher
    ) : AbstractTouchHandler<CourseLayout>() {
        protected fun isAlreadyShow(): Boolean {
            for (view in dispatcher.mTouchAffairViewPool) {
                if (view.isAttachedToWindow) {
                    return true
                }
            }
            return false
        }
    }
}