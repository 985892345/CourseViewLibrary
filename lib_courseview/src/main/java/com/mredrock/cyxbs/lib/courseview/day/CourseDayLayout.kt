package com.mredrock.cyxbs.lib.courseview.day

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mredrock.cyxbs.lib.courseview.day.adater.CourseDayAdapter
import com.mredrock.cyxbs.lib.courseview.day.attrs.CourseDayLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.day.attrs.CourseDayLayoutParams
import java.util.ArrayList
import kotlin.math.max

/**
 * 显示一天课程的一个 ViewGroup
 * ```
 * 1、为了更好的设计，该 ViewGroup 可以单独拿出来使用
 * 2、onMeasure、onLayout 设计思路参考了 FrameLayout，
 *    如果你对 FrameLayout 的 onMeasure、onLayout 有研究，基本可以看懂设计思路
 * 3、支持 padding 属性
 *    支持子 View 的 layout_margin 属性
 *    支持 wrap_content 和子 View 为 match_parent 时的使用（除此以外支持所有类型测量）
 * 4、设计得这么完善是为了让以后的人好扩展，该类被设计成了可继承的类，可以通过继承重构部分代码
 * ```
 * **NOTE：** 如果没有设置 [CourseDayAdapter]，且添加的 View 缺失课程属性，则 **View 将会添加失败**
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class CourseDayLayout : ViewGroup {

    /**
     * 设置 [CourseDayAdapter]
     *
     * **NOTE：** 如果没有设置 [CourseDayAdapter]，且添加的 View 缺失课程属性，则 **View 将会添加失败**
     */
    open fun setAdapter(adapter: CourseDayAdapter) {
        mAdapter = adapter
    }

    /**
     * 添加一节课
     *
     * **NOTE：** 如果没有设置 [CourseDayAdapter]，且添加的 View 缺失课程属性，则 **View 将会添加失败**
     */
    open fun addLesson(view: View, lp: CourseDayLayoutParams) {
        addView(view, lp) // addView 方法在后面被重写
    }

    // 属性值
    protected val mAttrs: CourseDayLayoutAttrs

    // 与外部连接的 Adapter
    protected lateinit var mAdapter: CourseDayAdapter

    // 参考 FrameLayout，用于在自身 wrap_content 而子 View 为 match_parent 时的测量
    protected val mMatchParentChildren = ArrayList<View>(1)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mAttrs = CourseDayLayoutAttrs.newInstance(context, attrs)
    }
    constructor(context: Context, attrs: CourseDayLayoutAttrs) : super(context) {
        mAttrs = attrs.copy()
    }

    /**
     * 设计思路参考了 FrameLayout
     *
     * 为了更好的扩展性，该 onMeasure 支持全类型的测量，
     * 意思就是 [CourseDayLayout] 可以单独拿出来当一个 ViewGroup 使用
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount

        val measureMatchParentChildren =
            MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                    MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        mMatchParentChildren.clear()

        var maxWidth = 0
        var maxLessonHeight = 0 // 计算每节课的最大高度，与 FrameLayout 的不同点之一
        var childState = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                val lp = child.layoutParams as CourseDayLayoutParams
                maxWidth = max(
                    maxWidth,
                    child.measuredWidth + lp.leftMargin + lp.rightMargin
                )
                maxLessonHeight = max(
                    maxLessonHeight,
                    (child.measuredHeight + lp.topMargin + lp.bottomMargin) / lp.lessonAttrs.length
                )
                childState = combineMeasuredStates(childState, child.measuredState)
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                        lp.height == LayoutParams.MATCH_PARENT
                    ) {
                        mMatchParentChildren.add(child)
                    }
                }
            }
        }

        var maxHeight = maxLessonHeight * mAttrs.lessonCount

        // Account for padding too
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom
        // Check against our minimum height and width
        maxWidth = max(maxWidth, suggestedMinimumWidth)
        maxHeight = max(maxHeight, suggestedMinimumHeight)

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(
                maxHeight, heightMeasureSpec,
                childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )

        /*
        * 在 FrameLayout 源码中，count = mMatchParentChildren.size 这句是在 if 判断之前，
        * 但我感觉似乎是源码写错了，本来是想指 childCount > 1，不然就有点说不通。
        *
        * 在 FrameLayout 宽和高都为 warp_content，其中只有一个子 View 的为 match_parent 时
        * （除它以外其他 View 都宽或高都不为 match_parent）
        * 这时按 FrameLayout 的源代码是不会对这个 View 再次测量的，那这个 View 只会得到 AT_MOST 的值
        *
        * 这就很奇怪了，我子 View 使用的 match_parent，却没有充满父布局的大小，
        * 感觉这是一个 bug（还是一个 feature？）
        *
        * 在 FrameLayout 里面有两个及以上的子 View 为 match_parent 不会有这个问题
        * */
        if (childCount > 1) {
            val count = mMatchParentChildren.size
            for (i in 0 until count) {
                val child: View = mMatchParentChildren[i]
                val lp = child.layoutParams as CourseDayLayoutParams
                val childWidthMeasureSpec = if (lp.width == LayoutParams.MATCH_PARENT) {
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
                } else widthMeasureSpec

                measureChild(
                    child, childWidthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                )
            }
        }
    }

    /**
     * 在计算高度时进行了部分修改
     */
    override fun measureChild(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
    ) {
        val lp = child.layoutParams as CourseDayLayoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin, lp.width
        )
        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val hMode = MeasureSpec.getMode(parentHeightMeasureSpec)
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                parentHeight / mAttrs.lessonCount * lp.lessonAttrs.length, hMode),
            lp.topMargin + lp.bottomMargin, lp.height
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    /**
     * 基于 FrameLayout 的部分代码修改，主要修改了高度的计算
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentLeft: Int = paddingLeft
        val parentRight: Int = r - l - paddingRight
        val lessonHeight = (b - t - paddingTop - paddingBottom) / mAttrs.lessonCount

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as CourseDayLayoutParams

                val startIndex = lp.lessonAttrs.startIndex
                val length = lp.lessonAttrs.length
                val parentTop: Int = paddingTop + lessonHeight * startIndex
                val parentBottom: Int = parentTop + lessonHeight * length

                val width = child.measuredWidth
                val height = child.measuredHeight
                var childLeft: Int
                var childTop: Int
                var gravity = lp.gravity
                if (gravity == -1) { gravity = Gravity.TOP or Gravity.START }

                val layoutDirection = layoutDirection
                val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
                childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> {
                        parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin
                    }
                    Gravity.RIGHT -> {
                        parentLeft + lp.leftMargin
                    }
                    Gravity.LEFT -> parentLeft + lp.leftMargin
                    else -> parentLeft + lp.leftMargin
                }
                childTop = when (verticalGravity) {
                    Gravity.CENTER_VERTICAL -> {
                        parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin
                    }
                    Gravity.TOP -> parentTop + lp.topMargin
                    Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                    else -> parentTop + lp.topMargin
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height)
            }
        }
    }

    /**
     * 1、保证 View 的排序有序
     * 2、回调 [CourseDayAdapter] 设置课的属性
     */
    override fun addView(child: View, index: Int, params: LayoutParams) {
        val newParams: CourseDayLayoutParams =
            if (checkLayoutParams(params)) params as CourseDayLayoutParams
            else generateLayoutParams(params) as CourseDayLayoutParams
        if (!newParams.isComplete()) {
            // 此时 child 的课程属性缺少，但又没有设置 Adapter，所以添加 View 失败
            if (!this::mAdapter.isInitialized) return
            mAdapter.setLessonAttrs(newParams.lessonAttrs)
            // 属性不设置完整就不允许添加 View 进来
            if (!newParams.isComplete()) { // tnnd，你就是不设置是吧？(一个梗)
                return // 那我就不演了！
            }
        }
        var newIndex = -1 // -1 默认插在末尾
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val lp = view.layoutParams as CourseDayLayoutParams
            if (newParams.lessonAttrs < lp.lessonAttrs) {
                newIndex = i
                break
            }
        }
        super.addView(child, newIndex, newParams)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return CourseDayLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
        return when (lp) {
            is CourseDayLayoutParams -> CourseDayLayoutParams(lp)
            is MarginLayoutParams -> CourseDayLayoutParams(lp)
            else -> CourseDayLayoutParams(lp)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return CourseDayLayoutParams()
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is CourseDayLayoutParams
    }
}