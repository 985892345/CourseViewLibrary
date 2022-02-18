package com.mredrock.cyxbs.lib.courseview.helper.multitouch.entitymove

import android.util.SparseArray
import android.view.View
import androidx.core.util.forEach
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll.ScrollTouchHandler
import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 14:39
 */
class EntityMovePointerDispatcher(
    val course: CourseLayout
) : AbstractPointerDispatcher<CourseLayout, EntityMoveTouchHandler>() {

    private val mHandlerById = SparseArray<EntityMoveTouchHandler>(3)

    override fun isPrepareToIntercept(event: IPointerEvent, view: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                val child = course.findItemUnderByXY(x, y) ?: return false
                if (isCanEntityMove(child)) {
                    val handler = mManger.getHandler()
                    mHandlerById.put(event.pointerId, handler)
                    handler.start(event, child)
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

    override fun createNewHandler(): EntityMoveTouchHandler {
        return EntityMoveTouchHandler(course)
    }

    private fun isCanEntityMove(child: View): Boolean {
        mHandlerById.forEach { _, handler ->
            if (handler.isAlreadyHandle(child)) {
                return false
            }
        }
        val lp = child.layoutParams as CourseLayoutParams
        return when (lp.type) {
            CourseType.AFFAIR, CourseType.AFFAIR_TOUCH -> true
            else -> false
        }
    }
}