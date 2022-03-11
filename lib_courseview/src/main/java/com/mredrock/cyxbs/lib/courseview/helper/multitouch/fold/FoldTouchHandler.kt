package com.mredrock.cyxbs.lib.courseview.helper.multitouch.fold

import android.view.View
import android.view.ViewConfiguration
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.ICourseLayout
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerFlag
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView
import kotlin.math.abs

/**
 * ## 点击时间轴上箭头的事件处理者
 *
 * ### 该类作用：
 * - 绑定一根手指的事件；
 * - 封装点击中午和傍晚时间段功能
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 22:21
 */
internal class FoldTouchHandler(
    val scroll: ICourseScrollView,
    val course: ICourseLayout,
    dispatcher: FoldPointerDispatcher
) : FoldPointerDispatcher.AbstractFoldTouchHandler(dispatcher) {

    fun start(downWhich: DownWhich) {
        flag = PointerFlag.START
        mDownWhich = downWhich
    }

    override var flag: PointerFlag = PointerFlag.OVER

    private var mDownWhich = DownWhich.OTHER

    // 认定是在滑动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private val mTouchSlop = ViewConfiguration.get(course.getContext()).scaledTouchSlop

    override fun onPointerTouchEvent(event: IPointerEvent, view: View) {
        when (event.action) {
            UP -> {
                val pointer = scroll.getPointer(event.pointerId)
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
                flag = PointerFlag.OVER
            }
            CANCEL -> {
                flag = PointerFlag.OVER
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