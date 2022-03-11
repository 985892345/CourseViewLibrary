package com.mredrock.cyxbs.lib.courseview.helper.multitouch.fold

import android.util.SparseArray
import android.view.View
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_LEFT
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.TIME_LINE_RIGHT
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.CourseRecyclerPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.entitymove.EntityMovePointerDispatcher

/**
 * ## 点击时间轴上箭头的事件分发者
 *
 * ### 该类作用：
 * - 拦截点击时间轴上箭头的事件；
 * - 管理点击时间轴上箭头的事件分发；
 *
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 22:21
 */
internal class FoldPointerDispatcher : CourseRecyclerPointerDispatcher() {

    private val mHandlerById = SparseArray<FoldTouchHandler>(3)

    private val mHandlerPool = HandlerPool { FoldTouchHandler(scroll, course, this) }

    override fun isPrepareToIntercept(event: IPointerEvent, view: View): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                val noonState = course.getNoonRowState()
                val duskState = course.getDuskRowState()
                if (noonState == RowState.FOLD_ANIM
                    || noonState == RowState.UNFOLD_ANIM
                    || duskState == RowState.FOLD_ANIM
                    || duskState == RowState.UNFOLD_ANIM
                ) {
                    return false
                }
                // 左侧时间轴的显示范围
                val timeLineLeft = course.getColumnsWidth(0, TIME_LINE_LEFT - 1)
                val timeLineRight = timeLineLeft + course.getColumnsWidth(
                    TIME_LINE_LEFT,
                    TIME_LINE_RIGHT
                )
                // 这里并没有直接采用得到当前触摸 View 的 layoutParams 的 type 来判断
                // 因为在折叠后，那个 View 的高度直接为 0 了，手指是不可能触摸得到那个 View 的
                // 所以只能计算它的绝对区域
                if (x in timeLineLeft..timeLineRight) { // 如果 x 落在左侧时间轴上
                    // 中午那一行的显示范围
                    val noonTopHeight = course.getRowsHeight(0, NOON_TOP - 1)
                    val noonBottomHeight = noonTopHeight + course.getRowsHeight(
                        NOON_TOP,
                        NOON_BOTTOM
                    )
                    val clickRange = 16 // 点击的范围
                    // 如果 y 落在 Noon 的行数
                    if (y in (noonTopHeight - clickRange)..(noonBottomHeight + clickRange)) {
                        val handler = mHandlerPool.getHandler()
                        handler.start(DownWhich.NOON)
                        mHandlerById.put(event.pointerId, handler)
                        // 判断是否有整体移动的 View 在中午时间段上
                        val hasEntityMoveInNoon = mEntityMovePointerDispatcher?.hasEntityInNoon() ?: false
                        return !hasEntityMoveInNoon
                    } else {
                        // 傍晚那一行的显示范围
                        val duskTopHeight = course.getRowsHeight(0, DUSK_TOP - 1)
                        val duskBottomHeight = duskTopHeight + course.getRowsHeight(
                            DUSK_TOP,
                            DUSK_BOTTOM
                        )
                        // 如果 y 落在 Dusk 的行数
                        if (y in (duskTopHeight - clickRange)..(duskBottomHeight + clickRange)) {
                            val handler = mHandlerPool.getHandler()
                            handler.start(DownWhich.DUSK)
                            mHandlerById.put(event.pointerId, handler)
                            // 判断是否有整体移动的 View 在傍晚时间段上
                            val hasEntityMoveInDusk = mEntityMovePointerDispatcher?.hasEntityInDusk() ?: false
                            return !hasEntityMoveInDusk
                        }
                    }
                }
            }
            else -> {}
        }
        return false
    }

    override fun getInterceptHandler(event: IPointerEvent, view: View): IPointerTouchHandler? {
        return mHandlerById[event.pointerId]
    }

    private var mEntityMovePointerDispatcher: EntityMovePointerDispatcher? = null

    override fun onOtherDispatcherRobEvent(
        event: IPointerEvent,
        dispatcher: IPointerDispatcher
    ) {
        if (mEntityMovePointerDispatcher == null && dispatcher is EntityMovePointerDispatcher) {
            mEntityMovePointerDispatcher = dispatcher
        }
    }

    abstract class AbstractFoldTouchHandler(
        private val dispatcher: FoldPointerDispatcher
    ) : RecyclerTouchHandler() {

        /**
         * 从分发者中移除，应该在 UP 和 CANCEL 时调用
         *
         * **NOTE:** 移除后可能仍有动画的进行
         */
        override fun removeFromDispatcher(pointerId: Int) {
            dispatcher.mHandlerById.remove(pointerId)
        }
    }
}