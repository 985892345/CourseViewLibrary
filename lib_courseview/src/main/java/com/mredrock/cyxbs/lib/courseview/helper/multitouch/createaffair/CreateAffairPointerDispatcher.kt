package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll.ScrollTouchHandler
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import kotlin.math.abs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 19:14
 */
class CreateAffairPointerDispatcher(
    val course: CourseLayout
) : AbstractPointerDispatcher<CourseLayout, CreateAffairTouchHandler>() {

    private val mHandlerById = SparseArray<CreateAffairTouchHandler>(3)

    // 认定是在滑动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    override fun isPrepareToIntercept(event: IPointerEvent, view: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                val child = course.findItemUnderByXY(x, y)
                if (child == null) {
                    val handler = mManger.getHandler()
                    mHandlerById.put(event.pointerId, handler)
                    handler.start(event)
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
        val handler = mHandlerById[event.pointerId]
        if (handler.state == PointerState.OVER) {
            return ScrollTouchHandler.get()
        }
        return if (handler.isHandleEvent()) handler else null
    }

    override fun createNewHandler(): CreateAffairTouchHandler {
        return CreateAffairTouchHandler(course)
    }

    override fun onDispatchTouchEvent(event: MotionEvent, view: CourseLayout) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            removeLastTouchAffairViewNextDown(x, y)
        }
    }

    private fun removeLastTouchAffairViewNextDown(x: Int, y: Int) {
        val child = course.findItemUnderByXY(x, y)
        val type = (child?.layoutParams as CourseLayoutParams?)?.type
        if (type == null || type != CourseType.AFFAIR_TOUCH) {
            for (handler in mManger) {
                handler.removeTouchAffairView()
            }
        }
    }
}