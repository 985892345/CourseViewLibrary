package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isGone
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.IPointerTouchHandler
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent
import com.mredrock.cyxbs.lib.courseview.net.touch.multiple.event.IPointerEvent.Action.*
import com.mredrock.cyxbs.lib.courseview.net.save.OnSaveStateListener
import com.mredrock.cyxbs.lib.courseview.helper.multitouch.CourseRecyclerPointerDispatcher
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import kotlin.math.min

/**
 * 长按生成事务的事件分发者
 *
 * 该类作用：
 * 1、管理长按生成事务的事件分发；
 * 2、继承自 [CourseRecyclerPointerDispatcher]，实现了复用处理者的功能；
 *
 * 注意事项：
 * 1、
 *
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 19:14
 */
internal class CreateAffairPointerDispatcher : CourseRecyclerPointerDispatcher(), OnSaveStateListener {

    /**
     * 设置带有加号 View 的点击事件
     */
    fun setOnClickListener(onCLick: (view: ITouchView) -> Unit) {
        mOnCLickListener = onCLick
    }

    private var mOnCLickListener: ((view: ITouchView) -> Unit)? = null
    private val mHandlerById = SparseArray<CreateAffairTouchHandler>(3)
    private val mHandlerPool = HandlerPool { CreateAffairTouchHandler(scroll, course, this) }
    private val mTouchAffairViewPool = TouchAffairViewPool() // TouchAffairView 复用池
    private var mIsCanShowAffairView = true // 下一次点击能否显示 TouchAffairView

    override fun isPrepareToIntercept(event: IPointerEvent, view: View): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            DOWN -> {
                if (mIsCanShowAffairView) {
                    val child = course.findItemUnderByXY(x, y)
                    if (child == null || child.isGone) {
                        val handler = mHandlerPool.getHandler()
                        mHandlerById.put(event.pointerId, handler)
                        val touchAffairView = mTouchAffairViewPool.getView()
                        touchAffairView.startUse()
                        handler.start(event, touchAffairView)
                        return true
                    }
                }
            }
            else -> {}
        }
        return false
    }

    override fun getInterceptHandler(event: IPointerEvent, view: View): IPointerTouchHandler? {
        /*
        * 这里与长按整体移动不同，是直接把事件给出去的
        * 原因如下：
        * 1、在短按时也需要生成加号 View，但只有一格
        *
        * 所以这里并不是长按激活后再给出处理者
        * */
        return mHandlerById[event.pointerId]
    }

    override fun onDispatchTouchEvent(event: MotionEvent, view: View) {
        if (event.action == MotionEvent.ACTION_DOWN
            || event.action == MotionEvent.ACTION_POINTER_DOWN
        ) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            mIsCanShowAffairView = true // 重置
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
            if (mTouchAffairViewPool.hasViewInUsed()) {
                mIsCanShowAffairView = false
                for (handler in mTouchAffairViewPool) {
                    handler.remove()
                }
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
                if (!it.isUsed()) {
                    return it
                }
            }
            return TouchAffairView(course).apply {
                mViews.add(this)
                setOnClickListener { mOnCLickListener?.invoke(this) }
            }
        }

        fun hasViewInUsed(): Boolean {
            mViews.forEach {
                if (it.isUsed()) {
                    Log.d("ggg", "(CreateAffairPointerDispatcher.kt:130)-->> " +
                            "it = $it")
                    return true
                }
            }
            return false
        }

        fun saveBundle(): Parcelable? {
            val array = ArrayList<CourseLayoutParams>(5)
            mTouchAffairViewPool.forEach {
                if (it.isUsed()) {
                    // TouchAffairView 被使用了就保存 layoutParams
                    array.add(it.layoutParams as CourseLayoutParams)
                }
            }
            return if (array.isNotEmpty()) {
                Bundle().apply { putSerializable(mTouchAffairViewPool::class.simpleName, array) }
            } else {
                null
            }
        }

        fun restoreBundle(savedState: Parcelable?) {
            if (savedState !is Bundle) return
            if (savedState.classLoader == null) {
                savedState.classLoader = javaClass.classLoader
            }
            // 恢复之前保存的 layoutParams
            val array = savedState.getSerializable(mTouchAffairViewPool::class.simpleName)
                    as ArrayList<*>
            var minTopRow = course.rowCount
            array.forEach {
                if (it is CourseLayoutParams) {
                    minTopRow = min(minTopRow, it.startRow)
                    val view = getView()
                    course.addItem(view, it)
                }
            }
            course.post {
                scroll.smoothScrollTo(
                    0, course.getRowsHeight(0, minTopRow - 1) - 50, 1200)
            }
        }

        override fun iterator(): Iterator<TouchAffairView> {
            return mViews.iterator()
        }
    }

    override fun onSaveState(): Parcelable? {
        return mTouchAffairViewPool.saveBundle()
    }

    override fun onRestoreState(savedState: Parcelable?) {
        mTouchAffairViewPool.restoreBundle(savedState)
    }

    init {
        // 在 View 被摧毁时保存临时生成的 TouchAffairView
        course.addSaveStateListener(38278467, this)
    }

    abstract class AbstractCreateAffairTouchHandler(
        private val dispatcher: CreateAffairPointerDispatcher
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