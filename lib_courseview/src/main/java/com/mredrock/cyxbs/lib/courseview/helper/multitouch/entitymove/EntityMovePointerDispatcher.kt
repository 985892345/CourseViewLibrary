package com.mredrock.cyxbs.lib.courseview.helper.multitouch.entitymove

import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import androidx.core.util.forEach
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.ICourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.CourseRecyclerPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.PointerFlag
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.scroll.ScrollTouchHandler
import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * ## 长按整体移动的事件分发者
 *
 * 该类作用：
 * - 管理长按整体移动的事件分发；
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 14:39
 */
internal class EntityMovePointerDispatcher : CourseRecyclerPointerDispatcher() {

    /**
     * 是否有个体移动到了中午时间段
     */
    fun hasEntityInNoon(): Boolean {
        mAffairHandlerPool.forEach {
            if (it.isEntityInNoon()) return true
        }
        mLessonHandlerPool.forEach {
            if (it.isEntityInNoon()) return true
        }
        return false
    }

    /**
     * 是否有个体移动到了傍晚时间段
     */
    fun hasEntityInDusk(): Boolean {
        mAffairHandlerPool.forEach {
            if (it.isEntityInDusk()) return true
        }
        mLessonHandlerPool.forEach {
            if (it.isEntityInDusk()) return true
        }
        return false
    }

    private val mHandlerById = SparseArray<AbstractEntityMoveTouchHandler>(3)

    // 移动事务的事件处理者
    private val mAffairHandlerPool = HandlerPool { AffairMoveTouchHandler(scroll, course, this) }
    // 移动课程的事件处理者
    private val mLessonHandlerPool = HandlerPool { LessonMoveTouchHandler(scroll, course, this) }

    override fun isPrepareToIntercept(event: IPointerEvent, view: View): Boolean {
        if (event.event.action == MotionEvent.ACTION_DOWN) {
            mIsInNoonEnd = false // 重置
            mIsInDuskEnd = false // 重置
            mIsNoonFoldedLongPressStart = false // 重置
            mIsDuskFoldedLongPressStart = false // 重置
            mIsContainNoonLongPressStart = false // 重置
            mIsContainDuskLongPressStart = false // 重置
            mHandlerById.clear() // 重置
        }
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                val child = course.findItemUnderByXY(x, y) ?: return false
                val handler = getEntityHandler(child)
                if (handler != null) {
                    mHandlerById.put(event.pointerId, handler)
                    handler.start(event, child)
                    return true
                }
            }
            UP, CANCEL -> {
                // 因为可能在长按未激活前就抬手或者被外布局拦截，此时处理者是还没有开始处理事件的
                mHandlerById[event.pointerId]?.cancel()
                mHandlerById.remove(event.pointerId)
            }
            else -> {}
        }
        return false
    }

    override fun getInterceptHandler(event: IPointerEvent, view: View): IPointerTouchHandler? {
        val handler = mHandlerById[event.pointerId]
        if (handler.flag == PointerFlag.OVER) {
            mHandlerById.remove(event.pointerId)
            /*
            * 这里说明 handler 已经不想拦截事件了，就把事件给 ScrollTouchHandler，让 ScrollView 滚动
            * 为什么不想拦截了?
            * 原因：
            * 在长按激活前移动的距离大于了 mTouchSlop
            * */
            return ScrollTouchHandler.get(scroll)
        }
        /*
        * 这里与长按生成事务不同
        * 原因在于：
        * 1、长按移动需要长按激活时才能开始处理事件
        * 2、也是为了不拦截 View 自己设置的点击监听
        *
        * 所以它需要到长按激活时才会给出处理者，这样才会开始处理
        * */
        return if (handler.isStartInterceptEvent()) handler else null
    }

    /**
     * 得到想要拦截的处理者
     *
     * 作用：
     * 1、防止多个手指点击同一个 View
     * 2、判断类型，有些类型是不能整体移动的，比如时间轴上的时间
     */
    private fun getEntityHandler(child: View): AffairMoveTouchHandler? {
        mHandlerById.forEach { _, handler ->
            if (handler.isAlreadyHandle(child)) {
                return null
            }
        }
        val lp = child.layoutParams as CourseLayoutParams
        return when (lp.type) {
            CourseType.AFFAIR, CourseType.AFFAIR_TOUCH -> mAffairHandlerPool.getHandler()
            CourseType.MY, CourseType.LINK -> mLessonHandlerPool.getHandler()
            else -> null
        }
    }

    // 全部手指中是否存在一个手指的结束位置包含中午时间段
    private var mIsInNoonEnd = false
    // 全部手指中是否存在一个手指的结束位置包含傍晚时间段
    private var mIsInDuskEnd = false
    // 全部手指中是否存在一个手指长按开始时中午时间段处于折叠状态
    private var mIsNoonFoldedLongPressStart = false
    // 全部手指中是否存在一个手指长按开始时傍晚时间段处于折叠状态
    private var mIsDuskFoldedLongPressStart = false
    // 全部手指中是否存在一个手指长按开始时自身包含中午时间段
    private var mIsContainNoonLongPressStart = false
    // 全部手指中是否存在一个手指长按开始时自身包含傍晚时间段
    private var mIsContainDuskLongPressStart = false

    abstract class AbstractEntityMoveTouchHandler(
        private val dispatcher: EntityMovePointerDispatcher
    ) : RecyclerTouchHandler() {

        // 长按开始时中午时间段是否处于折叠状态
        protected abstract val isNoonFoldedLongPressStart: Boolean
        // 长按开始时傍晚时间段是否处于折叠状态
        protected abstract val isDuskFoldedLongPressStart: Boolean
        // 长按开始时自身是否包含中午时间段
        protected abstract val isContainNoonLongPressStart: Boolean
        // 长按开始时自身是否包含傍晚时间段
        protected abstract val isContainDuskLongPressStart: Boolean

        /**
         * 判断是否需要折叠或展开时间段
         *
         * 有以下几种情况需要折叠：
         * - 刚开始中午(傍晚)时间段处于折叠，最后松手时不包含中午(傍晚)时间段
         * - 刚开始自身包含中午(傍晚)时间段，且没有其他 View 在中午(傍晚)时间段，最后松手时也不包含中午(傍晚)时间段
         *
         * 有个共同部分：最后松手时都不包含中午(傍晚)时间段
         *
         * @param isFinalUpEvent 是否是最后一个手指抬起
         * @param isInNoonEnd 要去的位置是否包含中午时间段
         * @param isInDuskEnd 要去的位置是否包含傍晚时间段
         */
        protected fun recoverFoldState(
            isFinalUpEvent: Boolean,
            isInNoonEnd: Boolean,
            isInDuskEnd: Boolean,
        ) {
            dispatcher.apply {
                if (isInNoonEnd) mIsInNoonEnd = true
                if (isInDuskEnd) mIsInDuskEnd = true
                if (isNoonFoldedLongPressStart) mIsNoonFoldedLongPressStart = true
                if (isDuskFoldedLongPressStart) mIsDuskFoldedLongPressStart = true
                if (isContainNoonLongPressStart) mIsContainNoonLongPressStart = true
                if (isContainDuskLongPressStart) mIsContainDuskLongPressStart = true
                if (isFinalUpEvent) {
                    val isFoldNoon = !mIsInNoonEnd &&
                        (mIsNoonFoldedLongPressStart
                                || mIsContainNoonLongPressStart && !hasViewInNoon(course))
                    val isFoldDusk = !mIsInDuskEnd &&
                        (mIsDuskFoldedLongPressStart
                                || mIsContainDuskLongPressStart && !hasViewInDusk(course))
                    if (isFoldNoon) course.foldNoonForce()
                    if (isFoldDusk) course.foldDuskForce()
                }
            }
        }

        /**
         * 中午时间段是否存在其他 View
         */
        private fun hasViewInNoon(course: ICourseLayout): Boolean {
            for (i in 0 until course.getChildCount()) {
                val child  = course.getChildAt(i)
                val lp = child.layoutParams as CourseLayoutParams
                // 判断中午时间段是否存在 View
                when (lp.type) {
                    // 目前只知道时间轴上的这几个类型不算
                    CourseType.TIME, CourseType.ARROW_NOON, CourseType.ARROW_DUSK,
                    CourseType.UNKNOWN -> {}
                    else -> {
                        if (CourseLayout.isContainNoon(lp)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        /**
         * 傍晚时间段是否存在其他 View
         */
        private fun hasViewInDusk(course: ICourseLayout): Boolean {
            for (i in (course.getChildCount() - 1) downTo 0) {
                val child  = course.getChildAt(i)
                val lp = child.layoutParams as CourseLayoutParams
                // 判断傍晚时间段是否存在 View
                when (lp.type) {
                    // 目前只知道时间轴上的这几个类型不算
                    CourseType.TIME, CourseType.ARROW_NOON, CourseType.ARROW_DUSK,
                    CourseType.UNKNOWN -> {}
                    else -> {
                        if (CourseLayout.isContainDusk(lp)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        /**
         * 从分发者中移除，应该在 UP 和 CANCEL 时调用，这个得让子类自己决定
         *
         * 移除后是允许存在动画的，这并不能作为能否回收复用处理者的依据
         */
        override fun removeFromDispatcher(pointerId: Int) {
            dispatcher.mHandlerById.remove(pointerId)
        }

        /**
         * 通知处理者开始处理事件
         */
        abstract fun start(event: IPointerEvent, child: View)

        /**
         * 通知处理者在长按未激活前就抬手或者被外布局拦截时取消事件
         */
        abstract fun cancel()

        /**
         * 询问处理者是否打算处理事件，不处理事件，可能是处于长按未激活状态
         */
        abstract fun isStartInterceptEvent(): Boolean

        /**
         * 是否在处理当前 View
         */
        abstract fun isAlreadyHandle(child: View): Boolean

        /**
         * 计算当前长按 View 是否与中午时间段有交集
         */
        abstract fun isEntityInNoon(): Boolean

        /**
         * 计算当前长按 View 是否与傍晚时间段有交集
         */
        abstract fun isEntityInDusk(): Boolean
    }
}