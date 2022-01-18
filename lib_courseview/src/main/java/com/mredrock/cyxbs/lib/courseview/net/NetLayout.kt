package com.mredrock.cyxbs.lib.courseview.net

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.net.adater.NetAdapter
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams
import java.util.ArrayList
import kotlin.math.max

/**
 * 一个网状的 ViewGroup
 * ```
 * 1、为了更好的设计，该 ViewGroup 可以单独拿出来使用
 * 2、onMeasure、onLayout 设计思路参考了 FrameLayout，
 *    如果你对 FrameLayout 的 onMeasure、onLayout 有研究，基本可以看懂设计思路
 * 3、支持 padding 属性
 *    支持子 View 的 layout_margin 属性
 *    支持 wrap_content 和子 View 为 match_parent 时的使用（除此以外支持所有类型测量）
 * 4、设计得这么完善是为了让以后的人好扩展，该类被设计成了可继承的类，可以通过继承重构部分代码
 * ```
 * **NOTE：** 如果没有设置 [NetAdapter]，且添加的 View 缺失约束属性，则 **View 将会添加失败**
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayout : ViewGroup {

    /**
     * 设置 [NetAdapter]
     *
     * **NOTE：** 如果没有设置 [NetAdapter]，且添加的 View 缺失约束属性，则 **View 将会添加失败**
     */
    open fun setAdapter(adapter: NetAdapter) {
        mAdapter = adapter
    }

    /**
     * 添加一节课
     *
     * **NOTE：** 如果没有设置 [NetAdapter]，且添加的 View 缺失约束属性，则 **View 将会添加失败**
     */
    open fun addLesson(view: View, lp: NetLayoutParams) {
        addView(view, lp) // addView 方法在后面被重写用于添加课程
    }

    // 属性值
    protected val mAttrs: NetLayoutAttrs

    // 与外部连接的 Adapter
    protected lateinit var mAdapter: NetAdapter

    // 参考 FrameLayout，用于在自身 wrap_content 而子 View 为 match_parent 时的测量
    protected val mMatchParentChildren = ArrayList<View>(1)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mAttrs = NetLayoutAttrs.newInstance(context, attrs)
    }
    constructor(context: Context, attrs: NetLayoutAttrs) : super(context) {
        mAttrs = attrs.copy()
    }

    /**
     * 设计思路参考了 FrameLayout
     *
     * 为了更好的扩展性，该 onMeasure 支持全类型的测量，
     * 意思就是 [NetLayout] 可以单独拿出来当一个 ViewGroup 使用
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount

        val measureMatchParentChildren =
            MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                    MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        mMatchParentChildren.clear()

        var maxRowHeight = 0 // 记录行中最高值
        var maxColumnWidth = 0 // 记录列中最宽值
        var childState = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                val lp = child.layoutParams as NetLayoutParams
                maxRowHeight = max(
                    maxRowHeight,
                    (child.measuredHeight + lp.topMargin + lp.bottomMargin) / lp.netAttrs.rowCount
                )
                maxColumnWidth = max(
                    maxColumnWidth,
                    (child.measuredWidth + lp.leftMargin + lp.rightMargin) / lp.netAttrs.columnCount
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

        var maxWidth = maxColumnWidth * mAttrs.columnCount
        var maxHeight = maxRowHeight * mAttrs.rowCount

        // Account for padding too
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom
        // Check against our minimum height and width
        maxWidth = max(maxWidth, suggestedMinimumWidth)
        maxHeight = max(maxHeight, suggestedMinimumHeight)

        // Check against our foreground's minimum height and width

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
        if (childCount > 1) { // 等于 1 时说明只有一个子 View，此时如果自身 wrap_content，就没必要重新测量
            // 在自身 wrap_content 的前提下，对 match_parent 的子 View 重新进行测量
            val count = mMatchParentChildren.size
            for (i in 0 until count) {
                // 这里与 FrameLayout 源码差异有点大，我这里直接将整个子 View 的测量交给了 measureChild 方法
                // 提供的宽和高都是自身已经计算好的，所以与 FrameLayout 的写法无本质区别
                measureChild(
                    mMatchParentChildren[i],
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                )
            }
        }
    }

    /**
     * 在计算宽度和高度时进行了部分修改
     */
    override fun measureChild(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
    ) {
        val lp = child.layoutParams as NetLayoutParams
        val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
        val wMode = MeasureSpec.getMode(parentWidthMeasureSpec)
        val childWidthMeasureSpec = getChildMeasureSpec(
             MeasureSpec.makeMeasureSpec(
                 parentWidth / mAttrs.columnCount * lp.netAttrs.columnCount,
                 wMode
             ),
            lp.leftMargin + lp.rightMargin, lp.width
        )
        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val hMode = MeasureSpec.getMode(parentHeightMeasureSpec)
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                parentHeight / mAttrs.rowCount * lp.netAttrs.rowCount, // 计算课程的高度
                hMode),
            lp.topMargin + lp.bottomMargin, lp.height
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    /**
     * 基于 FrameLayout 的部分代码修改，主要修改了对 parentLeft、parentRight、parentTop、parentBottom 的计算
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        val rowHeight = (b - t - paddingTop - paddingBottom) / mAttrs.rowCount
        val columnWidth = (r - l - paddingLeft - paddingRight) / mAttrs.columnCount

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as NetLayoutParams

                val parentLeft: Int = paddingLeft + columnWidth * lp.netAttrs.startColumn
                val parentRight: Int = parentLeft + columnWidth * lp.netAttrs.columnCount
                val parentTop: Int = paddingTop + rowHeight * lp.netAttrs.startRow
                val parentBottom: Int = parentTop + rowHeight * lp.netAttrs.rowCount

                val width = child.measuredWidth
                val height = child.measuredHeight
                var gravity = lp.gravity
                if (gravity == -1) { gravity = Gravity.TOP or Gravity.START }

                val layoutDirection = layoutDirection
                val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
                val childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
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
                val childTop = when (verticalGravity) {
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
     * 2、回调 [NetAdapter] 设置课的属性
     */
    override fun addView(child: View, index: Int, params: LayoutParams) {
        val newParams: NetLayoutParams =
            if (checkLayoutParams(params)) params as NetLayoutParams
            else generateLayoutParams(params) as NetLayoutParams
        if (!newParams.isComplete()) {
            // 此时 child 缺少课程属性，但又没有设置 Adapter，所以直接 return，添加 View 失败
            if (!this::mAdapter.isInitialized) return
            // 设置了 Adapter，就回调设置课程属性
            mAdapter.setNetAttrs(child, newParams.netAttrs)
            // 属性不设置完整就不允许添加 View 进来
            if (!newParams.isComplete()) {
                return
            }
        }
        var newIndex = -1 // -1 默认插在末尾
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val lp = view.layoutParams as NetLayoutParams
            if (newParams.netAttrs < lp.netAttrs) {
                newIndex = i
                break
            }
        }
        super.addView(child, newIndex, newParams)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return NetLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
        return when (lp) {
            is NetLayoutParams -> NetLayoutParams(lp)
            is MarginLayoutParams -> NetLayoutParams(lp)
            else -> NetLayoutParams(lp)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return NetLayoutParams()
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is NetLayoutParams
    }
}