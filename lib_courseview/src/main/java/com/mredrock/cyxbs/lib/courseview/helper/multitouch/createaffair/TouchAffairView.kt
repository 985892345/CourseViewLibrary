package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.ViewExtend

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 20:32
 */
@SuppressLint("ViewConstructor")
class TouchAffairView(
    val course: CourseLayout
) : ViewGroup(course.context), ViewExtend {

    /**
     * 显示 mTouchAffairView（用于添加事务的 View）
     */
    fun show(topRow: Int, bottomRow: Int, initialColumn: Int) {
        val lp = layoutParams as CourseLayoutParams
        lp.startRow = topRow
        lp.endRow = bottomRow
        lp.startColumn = initialColumn
        lp.endColumn = initialColumn
        // 添加 mTouchAffairView
        if (parent != null) {
            if (parent is CourseLayout) {
                layoutParams = lp
            } else {
                throw RuntimeException("为什么 mTouchAffairView 的父布局不是 CourseLayout?")
            }
        } else {
            course.addView(this, lp)
        }
        // 添加一个入场动画
        startAnimation(
            AlphaAnimation(0F, 1F).apply {
                duration = 200
            }
        )
    }

    fun remove() {
        if (parent != null) {
            startAnimation(
                AlphaAnimation(1F, 0F).apply {
                    duration = 200
                }
            )
            course.removeView(this)
        }
    }

    fun isAdded(): Boolean {
        return parent != null
    }

    fun isCanRefresh(): Boolean {
        return parent != null
    }

    /**
     * 该方法作用：
     * 1、计算当前 [mTouchAffairView] 的位置并刷新布局
     * 2、启动一个生长的动画
     */
    fun refresh(
        oldTopRow: Int,
        oldBottomRow: Int,
        topRow: Int,
        bottomRow: Int
    ) {
        val lp = layoutParams as CourseLayoutParams
        lp.startRow = topRow
        lp.endRow = bottomRow
        layoutParams = lp // 设置属性，刷新布局
        mOnNextLayoutCallback = {
            // 在已经布局完毕后再调用动画，不然得到的高度值是有问题的
            startExpandValueAnimator(oldTopRow, oldBottomRow, topRow, bottomRow)
        }
    }

    // 扩展动画
    private var mExpandValueAnimator: ValueAnimator? = null
    // 下一次布局的回调
    private var mOnNextLayoutCallback: ((View) -> Unit)? = null

    /**
     * 这里并没有直接给 imageView 布局，而是在前面手动调用 layout
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mOnNextLayoutCallback?.invoke(this)
        mOnNextLayoutCallback = null
        if (mExpandValueAnimator == null) {
            mImageView.layout(0, 0, r - l, b - t)
        }
    }

    private val mImageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        background = GradientDrawable().apply {
            val radius = R.dimen.course_course_item_radius.dimens()
            // 设置圆角
            cornerRadii =
                floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
            // 背景颜色
            setColor(R.color.course_affair_color.color())
        }
        // 设置 ImageView 的前景图片
        setImageResource(R.drawable.course_ic_add_circle_white)
    }

    /**
     * 启动生长动画
     */
    private fun startExpandValueAnimator(
        oldTopRow: Int,
        oldBottomRow: Int,
        topRow: Int,
        bottomRow: Int
    ) {
        mExpandValueAnimator?.cancel() // 取消之前的动画
        mExpandValueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener {
                val now = animatedValue as Float
                val oldTop = course.getRowsHeight(0, oldTopRow - 1)
                val newTop = course.getRowsHeight(0, topRow - 1)
                val nowTop = ((oldTop - newTop) * (1 - now)).toInt()
                val oldBottom = course.getRowsHeight(0, oldBottomRow)
                val newBottom = course.getRowsHeight(0, bottomRow)
                val nowBottom = ((newBottom - oldBottom) * now).toInt() + oldBottom - newTop
                mImageView.layout(0, nowTop, width, nowBottom) // 手动调用布局
            }
            doOnEnd {
                mExpandValueAnimator = null
            }
            interpolator = DecelerateInterpolator()
            duration = 120
            start()
        }
    }

    init {
        addView(mImageView)
        course.clipChildren = false // 请求父布局不要裁剪
        layoutParams = CourseLayoutParams(0,0, 0, CourseType.AFFAIR_TOUCH)
    }
}