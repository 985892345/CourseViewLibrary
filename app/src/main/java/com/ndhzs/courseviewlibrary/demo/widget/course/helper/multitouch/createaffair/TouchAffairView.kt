package com.ndhzs.courseviewlibrary.demo.widget.course.helper.multitouch.createaffair

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnNextLayout
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.ndhzs.courseviewlibrary.demo.utils.ViewExtend
import kotlin.math.roundToInt

/**
 * 显示带有加号的那个 View
 *
 * 该类作用：
 * - 封装带加号 View 的一些行为；
 *
 * 注意事项：
 * - 里面包含了一个 ImageView，原因在于实现一个生长的动画，不然直接滑动显得有些生硬
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 20:32
 */
@SuppressLint("ViewConstructor")
internal class TouchAffairView(
    val course: AbstractCourseLayout
) : ViewGroup(course.context), ITouchView, ViewExtend {

    /**
     * 从 startUse() 到 remove() 的生命周期
     */
    private var isUsed = false

    fun isUsed(): Boolean {
        if (parent != null) {
            if ((visibility == GONE || mHideAnimator != null) && !isUsed) {
                // 存在 isUsed = false，但仍有 parent，比如长按整体移动会把 view 添加进 overlay，结束时再加进 course
                return false
            }
            return true
        } else {
            // parent 为 null 一定没有被使用了
            isUsed = false // 重置
            return false
        }
    }

    /**
     * 使用的开始，应该在一个手指开始处理事件时就调用，对于取消使用会在判断是否使用时智能判断
     */
    fun startUse() {
        isUsed = true
    }

    /**
     * 显示 mTouchAffairView（用于添加事务的 View）
     */
    fun show(topRow: Int, bottomRow: Int, initialColumn: Int) {
        // 先取消可能存在的动画
        mHideAnimator?.cancel() 
        // 添加一个入场动画
        startAnimation(mShowAnimation)
        if (visibility == GONE) {
            visibility = VISIBLE
        }
        val lp = layoutParams as CourseLayoutParams
        lp.startRow = topRow
        lp.endRow = bottomRow
        lp.startColumn = initialColumn
        lp.endColumn = initialColumn
        // 添加 mTouchAffairView
        if (parent != null) {
            if (parent is AbstractCourseLayout) {
                layoutParams = lp
            } else {
                /*
                * 这里只要你没有改动其他代码，是不会出现这个 bug 的
                * 即使在长按移动中会添加进 overlay，但本身 show() 方法需要一个长按的延时，
                * 这个时候长按整体移动的动画肯定结束了，所以不会触发这个 bug
                * */
                throw RuntimeException("为什么 mTouchAffairView 的父布局不是 CourseLayout?")
            }
        } else {
            course.addCourse(this, lp)
        }
    }

    /**
     * 使用的结束，带有动画的消失
     */
    override fun remove() {
        if (!isUsed) return
        isUsed = false // 重置
        if (parent != null) {
            /*
            * 记录一下：
            * 因为是直接修改 alpha，最开始我直接使用的补间动画，但后来出现了阴影重叠的问题，所以改为属性动画
            * 猜测原因：
            * 补间动画会直接将绘制拦截，然后单独处理，所以可能在这个阶段时，设置 translationZ 会失效，
            * 导致背景图片的阴影效果无法取消（仅是猜测）
            * */
            if (mHideAnimator == null) {
                mHideAnimator = ValueAnimator.ofFloat(1F, 0F).apply {
                    addUpdateListener {
                        alpha = animatedValue as Float
                    }
                    doOnEnd {
                        mHideAnimator = null
                        alpha = 1F
                        /*
                        * 这里直接设置 visibility 有以下原因：
                        * 1、方便处理，不需要直接 course.removeView()
                        * 2、长按整体移动时会把 View 添加进 overlay，这时调用 course.removeView() 是没有效果的
                        * */
                        visibility = GONE
                    }
                    doOnCancel {
                        mHideAnimator = null
                        alpha = 1F
                    }
                    duration = 200
                    start()
                }
            }
        }
    }

    /**
     * 该方法作用：
     * - 计算当前位置并刷新布局
     * - 启动一个生长的动画
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
        doOnNextLayout {
            // 在已经布局完毕后再调用动画，不然 TouchAffairView 高度值还是之前的大小，导致 ImageView 要闪
            startExpandValueAnimator(oldTopRow, oldBottomRow, topRow, bottomRow)
        }
    }

    // 扩展动画
    private var mExpandValueAnimator: ValueAnimator? = null
    // 开始显示时的淡入动画动画
    private var mShowAnimation = AlphaAnimation(0F, 1F).apply { duration = 200 }
    // 点击其他区域的淡出动画
    private var mHideAnimator: ValueAnimator? = null

    /**
     * 在执行动画时不给 imageView 布局
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mExpandValueAnimator == null) {
            mImageView.layout(0, 0, r - l, b - t)
        }
    }

    /**
     * 灰色圆角背景
     */
    private val mBackground = GradientDrawable().apply {
        val radius = R.dimen.course_course_item_radius.dimens()
        // 设置圆角
        cornerRadii =
            floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        // 背景颜色
        setColor(R.color.course_affair_color.color())
    }

    private val mImageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        background = mBackground
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
        mExpandValueAnimator?.end() // 取消之前的动画
        mExpandValueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener {
                val now = animatedValue as Float
                val oldTop = course.getRowsHeight(0, oldTopRow - 1)
                val newTop = course.getRowsHeight(0, topRow - 1)
                val nowTop = ((oldTop - newTop) * (1 - now)).roundToInt()
                val oldBottom = course.getRowsHeight(0, oldBottomRow)
                val newBottom = course.getRowsHeight(0, bottomRow)
                val nowBottom =
                    ((newBottom - oldBottom) * now).roundToInt() + oldBottom - newTop - 2 * mMargin
                // 手动调用布局
                mImageView.layout(0, nowTop, width, nowBottom)
            }
            doOnStart {
                course.setClipChildren(false) // 请求父布局不要裁剪
                background = null
                // 把背景交给 ImageView
                mImageView.background = mBackground
            }
            doOnEnd {
                mExpandValueAnimator = null
                course.setClipChildren(true) // 及时关闭，减少不必要绘制
                // 设置为 ImageView 的背景后，这样会使整体移动中改变 translationZ 带有阴影效果
                background = mImageView.background
                mImageView.background = null
            }
            interpolator = DecelerateInterpolator()
            duration = 120
            start()
        }
    }

    override fun cloneLp(): CourseLayoutParams {
        return (layoutParams as CourseLayoutParams).clone()
    }

    private val mMargin = 1.dp2pxF().roundToInt()

    init {
        addView(mImageView)
        val lp = CourseLayoutParams(0,0, 0, CourseType.AFFAIR_TOUCH)
        lp.leftMargin = mMargin
        lp.rightMargin = mMargin
        lp.topMargin = mMargin
        lp.bottomMargin = mMargin
        layoutParams = lp
    }
}