package com.mredrock.cyxbs.lib.courseview.helper.entitymove

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.draw.ItemDecoration
import com.mredrock.cyxbs.lib.courseview.course.touch.OnItemTouchListener
import com.mredrock.cyxbs.lib.courseview.scroll.ICourseScrollView
import kotlin.math.abs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 13:44
 */
class CourseLongPressHelper(
    private val course: CourseLayout
) : OnItemTouchListener<CourseLayout>, ItemDecoration<CourseLayout> {

    private val mScrollView: ICourseScrollView = course.scrollView
    private val mPointerHandlerManger: IPointerTouchHandler.IManger = PointerTouchHandlerImpl.Manger()
    private val mEntityMoveCallback: EntityMoveCallback? = null

    private var mIsInLongPress = false
    private val mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop
    private val mLongPressRunnable = object : Runnable {
        private val mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
        override fun run() {
            mIsInLongPress = true
        }

        fun start() = course.postDelayed(this, mLongPressTimeout)
        fun cancel() = course.removeCallbacks(this)
    }

    override fun isAdvanceIntercept(event: MotionEvent, view: CourseLayout): Boolean {
        val callback = mEntityMoveCallback ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mLongPressRunnable.start()
                course.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val x = event.getX(index).toInt()
                val y = event.getY(index).toInt()
                val child = view.findItemUnderByXY(x, y) ?: return false
                if (callback.isPrepareToEntityMove(child, index, id)) {
                    val handler = mPointerHandlerManger.createHandlerIfCan(
                        view, y, id, course.getAbsolutePointer(id), mScrollView
                    )
                    if (handler != null) {
                        handler.isIntercept(event)
                    } else {
                        callback.onPlanCancelled(child, index, id)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                if (mIsInLongPress) {
                    mPointerHandlerManger.getHandler(id).onTouchEvent(event)
                } else {
                    val pointer = course.getAbsolutePointer(id)
                    if (abs(pointer.diffMoveX) >= mTouchSlop
                        || abs(pointer.diffMoveY) >= mTouchSlop
                    ) {

                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP-> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                mPointerHandlerManger.getHandler(id).isIntercept(event)
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent, view: CourseLayout) {
        TODO("Not yet implemented")
    }

    override fun onDrawAbove(canvas: Canvas, view: CourseLayout) {
        for (entity in mPointerHandlerManger) {
            entity.onDrawAbove(canvas)
        }
    }

    override fun onDrawBelow(canvas: Canvas, view: CourseLayout) {
        for (entity in mPointerHandlerManger) {
            entity.onDrawBelow(canvas)
        }
    }
}