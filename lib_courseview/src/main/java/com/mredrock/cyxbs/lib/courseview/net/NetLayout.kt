package com.mredrock.cyxbs.lib.courseview.net

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.util.forEach
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams
import kotlin.collections.ArrayList
import kotlin.math.max

/**
 * 一个网状的 ViewGroup
 * ```
 * 1、为了更好的设计，该 ViewGroup 可以单独拿出来使用
 * 2、onMeasure、onLayout 设计思路参考了 FrameLayout，
 *    如果你对 FrameLayout 的 onMeasure、onLayout 有研究，基本可以看懂设计思路
 * 3、支持 padding 属性
 *    支持子 View 的 layout_margin 属性
 *    支持 wrap_content 和子 View 为 match_parent 时的使用（几乎支持所有类型的测量）
 * 4、支持单独设置某几行或某几列的比重
 *    ①、在宽高为 match_parent 或 精确值 时，比重减少，所有包含这列或这行的子 View 全部减少
 *    ②、在宽高为 wrap_content 时，注意：比重减少，会使该父布局的宽高一起减少
 * 4、设计得这么完善是为了让以后的人好扩展，该类被设计成了可继承的类，可以通过继承重构部分代码
 *
 * 使用思路：
 * <com...NetLayout
 *     app:net_rowCount="6" // 控制总行数和总列数
 *     app:net_columnCount="6">
 *
 *     // 填写 net_startRow、net_endRow、net_startColumn、net_endColumn 后
 *     // 1、这个 View 将会被限制在坐标 (0, 0) - (1, 2) 以内进行测量和摆放
 *     // 2、该 View 的 layout_margin 属性就是相对于限制范围内的边距控制
 *     // 3、layout_width、layout_height、net_layoutGravity 属性也是在这个限制范围内进行控制
 *     <View
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent"
 *         app:net_layoutGravity="center"
 *         app:net_startRow="0"
 *         app:net_endRow="1"
 *         app:net_startColumn="0"
 *         app:net_endColumn="2"/>
 *
 * </com...NetLayout>
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayout : ViewGroup {

    /**
     * 添加一个子 View
     * ```
     * 1、该方法保证 View 的排序有序
     * ```
     * @return 是否添加成功
     */
    fun addItem(item: View, lp: NetLayoutParams): Boolean {
        if (!lp.isComplete()) return false
        addView(item, lp) // 注意：addView 方法被重写
        return true
    }

    /**
     * 改变 View 的测量范围
     */
    fun setItemAttrs(item: View, lp: NetLayoutParams): Boolean {
        return if (lp.isComplete()) {
            removeViewInLayout(item)
            addItem(item, lp)
        } else false
    }

    /**
     * 倒叙查找子 View
     *
     * 如果要修改子 View 的添加顺序可以重写 [getChildAfterIndex]
     * @see getChildAfterIndex
     */
    fun findItemUnder(x: Float, y: Float): View? {
        val count = childCount
        for (i in count - 1 downTo 0) {
            val child = getChildAt(i)
            val translationX = child.translationX
            val translationY = child.translationY
            if (x >= child.left + translationX
                && x <= child.right + translationX
                && y >= child.top + translationY
                && y <= child.bottom + translationY
            ) {
                return child
            }
        }
        return null
    }

    /**
     * 重新分配第 [column] 列的比重
     * @param weight 比重，默认情况下为 1F
     */
    fun setColumnWeight(column: Int, weight: Float) {
        if (column >= mNetAttrs.columnCount || column < 0) {
            throw IllegalArgumentException("column 不能大于或等于 ${mNetAttrs.columnCount} 且小于 0！")
        }
        if (weight == 1F) {
            mColumnChangedSize.remove(column)
        } else {
            mColumnChangedSize.put(column, weight)
        }
        requestLayout()
    }

    /**
     * 重新分配第 [row] 行的比重
     * @param weight 比重，默认情况下为 1F
     */
    fun setRowWeight(row: Int, weight: Float) {
        if (row >= mNetAttrs.rowCount || row < 0) {
            throw IllegalArgumentException("row 不能大于或等于 ${mNetAttrs.rowCount} 且小于 0！")
        }
        if (weight == 1F) {
            mRowChangedSize.remove(row)
        } else {
            mRowChangedSize.put(row, weight)
        }
        requestLayout()
    }

    /**
     * 设置第 [column] 列（以 0 开始）的***初始比重***，如果是布局后需要改变比重，请使用 [setColumnWeight] 方法
     *
     * **NOTE：** 与 [setColumnWeight] 的区别在于：会立即使初始状态的列比重分配失效
     *
     * **NOTE：** 初始分配值只有在 `layout_width` 为 `wrap_content` 时才有用，
     * 所以该方法应该在 `layout_width` 为 `wrap_content` 时使用
     *
     * @param weight 比重，默认情况下为 1F
     * @see setColumnWeight
     */
    fun setColumnInitialWeight(column: Int, weight: Float) {
        setColumnWeight(column, weight)
        mInitialSelfColumnSize = 0F // 重置
    }

    /**
     * 设置第 [row] 行（以 0 开始）的***初始比重***，如果是布局后需要改变比重，请使用 [setRowWeight] 方法
     *
     * **NOTE：** 与 [setRowWeight] 的区别在于：会立即使初始状态的行比重分配失效
     *
     * **NOTE：** 初始分配值只有在 `layout_height` 为 `wrap_content` 时才有用，
     * 所以该方法应该在 `layout_height` 为 `wrap_content` 时使用
     *
     * @param weight 比重，默认情况下为 1F
     * @see setRowWeight
     */
    fun setRowInitialWeight(row: Int, weight: Float) {
        setRowWeight(row, weight)
        mInitialSelfRowSize = 0F // 重置
    }

    // 属性值
    protected val mNetAttrs: NetLayoutAttrs

    // 参考 FrameLayout，用于在自身 wrap_content 而子 View 为 match_parent 时的测量
    private val mMatchParentChildren = ArrayList<View>()

    private val mRowChangedSize = LinkedHashMap<Int, Float>()
    private val mColumnChangedSize = LinkedHashMap<Int, Float>()

    private var mInitialSelfRowSize = 0F
    private var mInitialSelfColumnSize = 0F

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mNetAttrs = NetLayoutAttrs.newInstance(context, attrs)
    }

    constructor(context: Context, attrs: NetLayoutAttrs) : super(context) {
        mNetAttrs = attrs
    }

    /**
     * 控制 View 被添加进来时的顺序，如果有重复的相同值，则是最后一位相同值的索引加 1
     * ```
     * 如：
     *             2  4  6  6  8
     * 添加 6 进来: 2  4  6  6  6  8
     *                         ↑
     * ```
     * **NOTE：** 排序基于 [NetLayoutParams.compareTo] 方法，
     * 可在继承该 ViewGroup 的前提下重写该 compareTo 方法
     */
    protected open fun getChildAfterIndex(child: View, params: NetLayoutParams): Int {
        var start = 0
        var end = childCount
        while (start < end) {
            val half = (start + end) / 2
            val view = getChildAt(half)
            val lp = view.layoutParams.net()
            when { // 折半插入，自己画图就能看懂
                params < lp -> end = half - 1
                else -> start = half + 1
            }
        }
        return start
    }

    /**
     * 设计思路参考了 FrameLayout
     *
     * 为了更好的扩展性，该 onMeasure 支持全类型的测量，
     * 意思就是 [NetLayout] 可以单独拿出来当一个 ViewGroup 使用
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount

        val widthIsExactly = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
        val heightIsExactly = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY

        val widthIsWrap = layoutParams.width == LayoutParams.WRAP_CONTENT
        val heightIsWrap = layoutParams.height == LayoutParams.WRAP_CONTENT

        mMatchParentChildren.clear()

        var maxWidth = 0
        var maxHeight = 0
        var childState = 0

        val parentColumnSize = getSelfColumnsSize()
        val parentRowSize = getSelfRowsSize()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams.net()
                if (!lp.isComplete()) continue

                val childColumnSize = getColumnsSize(lp.startColumn, lp.endColumn)
                val childRowSize = getRowsSize(lp.startRow, lp.endRow)

                val childWithParentColumnMultiple = childColumnSize / parentColumnSize
                val childWithParentRowMultiple = childRowSize / parentRowSize

                val childWidthRatio =
                    if (widthIsWrap) childColumnSize / getInitialSelfColumnSize()
                    else childWithParentColumnMultiple

                val childHeightRatio =
                    if (heightIsWrap) childRowSize / getInitialSelfRowSize()
                    else childWithParentRowMultiple

                lp.oldChildWidthRatio = childWidthRatio
                lp.oldChildHeightRatio = childHeightRatio

                measureChildWithRatio(
                    child,
                    widthMeasureSpec, heightMeasureSpec,
                    childWidthRatio, childHeightRatio
                )

                maxWidth = max(
                    maxWidth,
                    if (childWithParentColumnMultiple == 0F) 0
                    else ((child.measuredWidth + lp.leftMargin + lp.rightMargin) /
                            childWithParentColumnMultiple).toInt()
                )
                maxHeight = max(
                    maxHeight,
                    if (childWithParentRowMultiple == 0F) 0
                    else ((child.measuredHeight + lp.topMargin + lp.bottomMargin) /
                            childWithParentRowMultiple).toInt()
                )
                childState = combineMeasuredStates(childState, child.measuredState)
                if (!widthIsExactly && lp.width == LayoutParams.MATCH_PARENT ||
                    !heightIsExactly && lp.height == LayoutParams.MATCH_PARENT
                ) {
                    mMatchParentChildren.add(child)
                }
            }
        }

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
        * 在 FrameLayout 宽和高都为 warp_content，其中只有一个子 View 为 match_parent 时
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
                val child = mMatchParentChildren[i]
                val lp = child.layoutParams.net()
                // 这里与 FrameLayout 源码差异有点大，我这里直接将整个子 View 的测量交给了 measureChildWithRatio 方法
                // 提供的宽和高都是自身的，所以与 FrameLayout 的写法无本质区别
                measureChildWithRatio(
                    child,
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY),
                    lp.oldChildWidthRatio, lp.oldChildHeightRatio
                )
            }
        }
    }

    /**
     * 获取开始列到结束列所占的总比例大小
     */
    private fun getColumnsSize(startColumn: Int, endColumn: Int): Float {
        var childColumnSize = 0F
        for (column in startColumn..endColumn) {
            childColumnSize += mColumnChangedSize[column] ?: 1F
        }
        return childColumnSize
    }

    /**
     * 获取开始行到结束行所占的总比例大小
     */
    private fun getRowsSize(startRow: Int, endRow: Int): Float {
        var childRowSize = 0F
        for (row in startRow..endRow) {
            childRowSize += mRowChangedSize[row] ?: 1F
        }
        return childRowSize
    }

    /**
     * 获取自身全部列所占的比例大小
     */
    private fun getSelfColumnsSize(): Float {
        var parentColumnSize = 0F
        mColumnChangedSize.forEach {
            parentColumnSize += it.value
        }
        parentColumnSize += mNetAttrs.columnCount - mColumnChangedSize.size
        return parentColumnSize
    }

    /**
     * 获取自身全部行所占的比例大小
     */
    private fun getSelfRowsSize(): Float {
        var parentRowSize = 0F
        mRowChangedSize.forEach {
            parentRowSize += it.value
        }
        parentRowSize += mNetAttrs.rowCount - mRowChangedSize.size
        return parentRowSize
    }

    /**
     * 获取当自身宽为 warp_content 时第一次测量的全部列所占的比例大小
     */
    private fun getInitialSelfColumnSize(): Float {
        if (layoutParams.width != LayoutParams.WRAP_CONTENT) {
            throw RuntimeException("该方法只允许在宽度为 wrap_content 时调用！")
        }
        if (mInitialSelfColumnSize == 0F) {
            mColumnChangedSize.forEach {
                mInitialSelfColumnSize += it.value
            }
            mInitialSelfColumnSize += mNetAttrs.columnCount - mColumnChangedSize.size
        }
        return mInitialSelfColumnSize
    }

    /**
     * 获取当自身高为 warp_content 时第一次测量的全部行所占的比例大小
     */
    private fun getInitialSelfRowSize(): Float {
        if (layoutParams.height != LayoutParams.WRAP_CONTENT) {
            throw RuntimeException("该方法只允许在高度为 wrap_content 时调用！")
        }
        if (mInitialSelfRowSize == 0F) {
            mRowChangedSize.forEach {
                mInitialSelfRowSize += it.value
            }
            mInitialSelfRowSize += mNetAttrs.rowCount - mRowChangedSize.size
        }
        return mInitialSelfRowSize
    }

    /**
     * 在计算宽度和高度时进行了部分修改，使其用一个比例来测量子 View
     * @param childWidthRatio [child] 宽度占父布局的总宽度的比例
     * @param childHeightRatio [child] 宽度占父布局的总高度的比例
     */
    private fun measureChildWithRatio(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        childWidthRatio: Float,
        childHeightRatio: Float
    ) {
        val lp = child.layoutParams.net()
        val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
        val wMode = MeasureSpec.getMode(parentWidthMeasureSpec)
        val childWidth = (childWidthRatio * parentWidth).toInt()
        val childWidthMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childWidth,
                /*
                * 如果直接给 UNSPECIFIED 值，会使子 View 的宽直接变成 0，直接为 0 了就没了意义。下同
                * 原因可看：View#onMeasure() -> View#getDefaultSize()
                * */
                if (wMode == MeasureSpec.UNSPECIFIED) MeasureSpec.AT_MOST else wMode
            ),
            lp.leftMargin + lp.rightMargin, lp.width
        )

        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val hMode = MeasureSpec.getMode(parentHeightMeasureSpec)
        val childHeight = (childHeightRatio * parentHeight).toInt()
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childHeight,
                if (hMode == MeasureSpec.UNSPECIFIED) MeasureSpec.AT_MOST else hMode
            ),
            lp.topMargin + lp.bottomMargin, lp.height
        )

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    /**
     * 基于 FrameLayout 的部分代码修改，主要修改了对 parentLeft、parentRight、parentTop、parentBottom 的计算
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount

        val totalRowHeight = b - t - paddingTop - paddingBottom
        val totalColumnWidth = r - l - paddingLeft - paddingRight

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams.net()
                if (!lp.isComplete()) continue

                val parentLeft = paddingLeft +
                        getColumnsWidth(0, lp.startColumn - 1, totalColumnWidth)
                val parentRight = parentLeft +
                        getColumnsWidth(lp.startColumn, lp.endColumn, totalColumnWidth)
                val parentTop = paddingTop +
                        getRowsHeight(0, lp.startRow - 1, totalRowHeight)
                val parentBottom = parentTop +
                        getRowsHeight(lp.startRow, lp.endRow, totalRowHeight)

                val width = child.measuredWidth
                val height = child.measuredHeight
                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = Gravity.TOP or Gravity.START
                }

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

    private fun getColumnsWidth(start: Int, end: Int, totalColumnWidth: Int): Int {
        if (end < start) return 0
        val childColumnSize = getColumnsSize(start, end)
        val parentColumnSize = getSelfColumnsSize()
        return (childColumnSize / parentColumnSize * totalColumnWidth).toInt()
    }

    private fun getRowsHeight(start: Int, end: Int, totalRowHeight: Int): Int {
        if (end < start) return 0
        val childRowSize = getRowsSize(start, end)
        val parentRowSize = getSelfRowsSize()
        return (childRowSize / parentRowSize * totalRowHeight).toInt()
    }

    /**
     * 重写是为了保证 child 的添加有序
     */
    override fun addView(child: View, index: Int, params: LayoutParams) {
        val lp = if (params is NetLayoutParams) {
            params
        } else {
            generateLayoutParams(params) as NetLayoutParams
        }
        super.addView(child, getChildAfterIndex(child, lp), params)
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
        return NetLayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            NetLayoutParams.UNSET,
            NetLayoutParams.UNSET,
            NetLayoutParams.UNSET,
            NetLayoutParams.UNSET
        )
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is NetLayoutParams
    }

    override fun setLayoutParams(params: LayoutParams) {
        if (params.width == LayoutParams.WRAP_CONTENT) {
            mInitialSelfColumnSize = 0F // 重置
        }
        if (params.height == LayoutParams.WRAP_CONTENT) {
            mInitialSelfRowSize = 0F // 重置
        }
        super.setLayoutParams(params)
    }

    private fun LayoutParams.net(): NetLayoutParams = this as NetLayoutParams
}