package com.mredrock.cyxbs.lib.courseview.helper.multitouch.fold

import android.util.SparseArray
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_LEFT
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_RIGHT
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.course.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.AbstractPointerDispatcher

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 22:21
 */
class FoldPointerDispatcher(
    val course: CourseLayout
) : AbstractPointerDispatcher<CourseLayout, FoldTouchHandler>() {

    private val mHandlerById = SparseArray<FoldTouchHandler>(3)

    override fun isPrepareToIntercept(event: IPointerEvent, view: CourseLayout): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                val noonState = view.getNoonRowState()
                val duskState = view.getDuskRowState()
                if (noonState == RowState.FOLD_ANIM
                    || noonState == RowState.UNFOLD_ANIM
                    || duskState == RowState.FOLD_ANIM
                    || duskState == RowState.UNFOLD_ANIM
                ) {
                    return false
                }
                // 左侧时间轴的显示范围
                val timeLineLeft = view.getColumnsWidth(0, TIME_LINE_LEFT - 1)
                val timeLineRight = timeLineLeft + view.getColumnsWidth(
                    TIME_LINE_LEFT,
                    TIME_LINE_RIGHT
                )
                // 这里并没有直接采用得到当前触摸 View 的 layoutParams 的 type 来判断
                // 因为在折叠后，那个 View 的高度直接为 0 了，手指是不可能触摸得到那个 View 的
                // 所以只能计算它的绝对区域
                if (x in timeLineLeft..timeLineRight) { // 如果 x 落在左侧时间轴上
                    // 中午那一行的显示范围
                    val noonTopHeight = view.getRowsHeight(0, NOON_TOP - 1)
                    val noonBottomHeight = noonTopHeight + view.getRowsHeight(
                        NOON_TOP,
                        NOON_BOTTOM
                    )
                    val clickRange = 16 // 点击的范围
                    // 如果 y 落在 Noon 的行数
                    if (y in (noonTopHeight - clickRange)..(noonBottomHeight + clickRange)) {
                        val handler = mManger.getHandler()
                        handler.start(DownWhich.NOON)
                        mHandlerById.put(event.pointerId, handler)
                        return true
                    } else {
                        // 傍晚那一行的显示范围
                        val duskTopHeight = view.getRowsHeight(0, DUSK_TOP - 1)
                        val duskBottomHeight = duskTopHeight + view.getRowsHeight(
                            DUSK_TOP,
                            DUSK_BOTTOM
                        )
                        // 如果 y 落在 Dusk 的行数
                        if (y in (duskTopHeight - clickRange)..(duskBottomHeight + clickRange)) {
                            val handler = mManger.getHandler()
                            handler.start(DownWhich.DUSK)
                            mHandlerById.put(event.pointerId, handler)
                            return true
                        }
                    }
                }
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

    override fun createNewHandler(): FoldTouchHandler {
        return FoldTouchHandler(course)
    }
}