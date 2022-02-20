package com.mredrock.cyxbs.lib.courseview.helper.multitouch.createaffair

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.ViewExtend
import kotlin.math.roundToInt

/**
 * 显示带有加号的那个 View
 *
 * 该类作用：
 * 1、封装带加号 View 的一些行为；
 *
 * 注意事项：
 * 1、里面包含了一个 ImageView，原因在于实现一个生长的动画，不然直接滑动显得有些生硬
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 20:32
 */
@SuppressLint("ViewConstructor")
internal class TouchAffairView(
    val course: CourseLayout
) : ViewGroup(course.context), ITouchView, ViewExtend {

    /**
     * 从 startUse() 到 remove() 的生命周期
     */
    private var isUsed = false

    fun isUsed(): Boolean {
        if (parent != null) {
            if ((visibility == GONE || animation != null) && !isUsed) {
                // 存在 isUsed = false，但仍有 parent，比如长按整体移动会把 view 添加进 overlay，结束时再加进 course
                return false
            }
            return true
        }
        if (isAttachedToWindow) {
            return true
        }
        return isUsed
    }

    /**
     * 使用的开始
     */
    fun startUse() {
        isUsed = true
    }

    /**
     * 显示 mTouchAffairView（用于添加事务的 View）
     */
    fun show(topRow: Int, bottomRow: Int, initialColumn: Int) {
        animation?.cancel()
        if (visibility == GONE) {
            /*
            * 如果走到这分支，就说明刚才触发了长按整体移动再回来的动画
            * 因为添加进了 overlay 而没有被成功 removeView 掉
            * */
            visibility = VISIBLE
        }
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

    /**
     * 使用的结束，带有动画的消失
     */
    override fun remove() {
        isUsed = false
        if (parent != null) {
            if (visibility == GONE) {
                /*
                * 如果走到这分支，就说明刚才触发了长按整体移动再回来的动画
                * 因为添加进了 overlay 而没有被成功 removeView 掉
                * */
                course.removeView(this)
                visibility = VISIBLE
                return
            }
            startAnimation(
                AlphaAnimation(1F, 0F).apply {
                    duration = 200
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            // 这里是因为长按整体移动的原因，会把它添加进 overlay，是不会被 removeView 掉的
                            // 为降低耦合度，所以只能采用这种方式，在动画结束时再设置它的 visibility
                            if (parent != course) {
                                visibility = GONE
                            }
                        }
                    })
                }
            )
            if (parent === course) {
                course.removeView(this)
            }
        }
    }

    /**
     * 该方法作用：
     * 1、计算当前位置并刷新布局
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
     * 在执行动画时不给 imageView 布局
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mOnNextLayoutCallback?.invoke(this)
        mOnNextLayoutCallback = null
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
                course.clipChildren = false // 请求父布局不要裁剪
                background = null
                mImageView.background = mBackground
            }
            doOnEnd {
                mExpandValueAnimator = null
                course.clipChildren = true // 及时关闭，减少不必要绘制
                // 设置为 ImageView 的背景后，这样会使整体移动中改变 translationZ 后会有阴影效果
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