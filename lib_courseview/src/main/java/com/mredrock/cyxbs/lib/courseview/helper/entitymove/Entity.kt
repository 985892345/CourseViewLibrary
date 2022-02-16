package com.mredrock.cyxbs.lib.courseview.helper.entitymove

import android.graphics.Canvas
import android.util.SparseArray
import android.view.View
import android.view.ViewConfiguration
import androidx.core.util.forEach
import androidx.core.util.valueIterator
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.draw.ItemDecoration
import com.mredrock.cyxbs.lib.courseview.utils.VibratorUtil

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/15 13:46
 */
class Entity private constructor(
    val course: CourseLayout,
    val pointerId: Int,
    private val viewInAnimList: MutableList<View>
) : ItemDecoration<CourseLayout> {

    fun startLongPressDelay() {
        mLongPressRunnable.start()
    }

    fun cancelLongPress() {
        mLongPressRunnable.cancel()
    }



    private var mIsInLongPress = false
    private var mDistanceDownToViewTop = 0

    private var mLongPressRunnable = object : Runnable {

        // 识别为长按所需的时间
        private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

        override fun run() {
            mIsInLongPress = true
            VibratorUtil.start(course.context, 36)
        }

        fun start() {
            course.postDelayed(this, mLongPressTimeout)
        }

        fun cancel() {
            course.removeCallbacks(this)
        }
    }

    private fun reset() {

    }

    override fun onDrawBelow(canvas: Canvas, view: CourseLayout) {

    }

    class EntityManger(
        private val course: CourseLayout
    ) : Iterable<Entity> {
        private val mEntities = SparseArray<Entity>(3)
        private val mDeletedEntities = ArrayDeque<Entity>(3)
        private val mViewInAnimList = ArrayList<View>(3)

        fun createEntity(pointerId: Int, distanceDownToViewTop: Int): Entity {
            val entity = mDeletedEntities.removeLastOrNull()
                ?: Entity(course, pointerId, mViewInAnimList)
            return (entity).apply {
                mDistanceDownToViewTop = distanceDownToViewTop
            }
        }

        fun getEntity(pointerId: Int): Entity {
            return mEntities.get(pointerId)
        }

        fun removeEntity(pointerId: Int) {
            mDeletedEntities.addLast(
                mEntities.get(pointerId).apply {
                    mEntities.remove(pointerId)
                }
            )
        }

        fun containEntity(pointerId: Int): Boolean {
            return mEntities.indexOfKey(pointerId) >= 0
        }

        fun isCanHandle(child: View): Boolean {
            if (mViewInAnimList.contains(child)) {
                return false
            }
            return true
        }

        override fun iterator(): Iterator<Entity> {
            return mEntities.valueIterator()
        }
    }
}