package com.mredrock.cyxbs.lib.courseview.helper.multitouch.fold

import android.view.ViewConfiguration
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractTouchHandler
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerState
import kotlin.math.abs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 22:21
 */
class FoldTouchHandler(
    val course: CourseLayout
) : AbstractTouchHandler<CourseLayout>() {

    fun start(downWhich: DownWhich) {
        state = PointerState.START
        mDownWhich = downWhich
    }

    override var state: PointerState = PointerState.OVER

    private var mDownWhich = DownWhich.OTHER

    // 认定是在滑动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private val mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    override fun onPointerTouchEvent(event: IPointerEvent, view: CourseLayout) {
        when (event.action) {
            UP -> {
                val pointer = course.getAbsolutePointer(event.pointerId)
                if (abs(pointer.diffMoveX) <= mTouchSlop
                    || abs(pointer.diffMoveY) <= mTouchSlop
                ) {
                    when (mDownWhich) {
                        DownWhich.NOON -> {
                            clickNoon()
                        }
                        DownWhich.DUSK -> {
                            clickDusk()
                        }
                        DownWhich.OTHER -> {} // 不做任何处理
                    }
                }
                state = PointerState.OVER
            }
            CANCEL -> {
                state = PointerState.OVER
            }
            else -> {}
        }
    }

    /**
     * 点击中午
     */
    private fun clickNoon() {
        when (course.getNoonRowState()) {
            RowState.FOLD -> course.unfoldNoonForce()
            RowState.UNFOLD -> course.foldNoonForce()
            else -> return
        }
    }

    /**
     * 点击傍晚
     */
    private fun clickDusk() {
        when (course.getDuskRowState()) {
            RowState.FOLD -> course.unfoldDuskForce()
            RowState.UNFOLD -> course.foldDuskForce()
            else -> return
        }
    }
}