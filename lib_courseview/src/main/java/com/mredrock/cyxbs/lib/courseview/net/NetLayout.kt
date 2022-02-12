package com.mredrock.cyxbs.lib.courseview.net

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.net.callback.OnWeightChangeListener
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams
import com.mredrock.cyxbs.lib.courseview.net.utils.SideType
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.max
import kotlin.math.roundToInt

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
 *    ①、在宽高为 match_parent 或 精确值 时，比重减少，所有包含这列或这行的子 View 宽或高减少
 *    ②、在宽高为 wrap_content 时，注意：比重减少，这时会使该父布局的宽高一起减少
 * 5、设计得这么完善是为了让以后的人好扩展，该类被设计成了可继承的类，可以通过继承重构部分代码
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
     */
    fun addItem(item: View, lp: NetLayoutParams) {
        addView(item, lp) // 注意：addView 方法被重写
    }

    /**
     * 倒序查找子 View
     *
     * **NOTE：** 按照 View 的行列范围来查找
     *
     * 如果要修改子 View 的添加顺序可以重写 [getChildAfterIndex]
     * @see getChildAfterIndex
     * @see findItemUnderByRowColumn
     */
    fun findItemUnderByXY(x: Int, y: Int): View? {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            val lp = child.layoutParams.net()
            if (x >= lp.constraintLeft
                && x <= lp.constraintRight
                && y >= lp.constraintTop
                && y <= lp.constraintBottom
            ) {
                return child
            }
        }
        return null
    }

    /**
     * 根据行和列倒序查找子 View
     *
     * 如果要修改子 View 的添加顺序可以重写 [getChildAfterIndex]
     * @see getChildAfterIndex
     * @see findItemUnderByXY
     */
    fun findItemUnderByRowColumn(row: Int, column: Int): View? {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            val lp = child.layoutParams.net()
            if (lp.contains(row, column)) {
                return child
            }
        }
        return null
    }

    /**
     * 得到总列数
     */
    fun getColumnCount(): Int {
        return mNetAttrs.columnCount
    }

    /**
     * 得到总行数
     */
    fun getRowCount(): Int {
        return mNetAttrs.rowCount
    }

    /**
     * 得到 [x] 对应的列数，超出控件范围会得到边界列，控件没有被测量时得到 -1
     * @return 得到 [x] 对应的列数，超出控件范围会得到边界列，控件没有被测量时得到 -1
     */
    fun getColumn(x: Int): Int {
        if (width == 0) return -1
        if (x <= 0) {
            return 0
        } else if (x >= width) {
            return getColumnCount() - 1
        }
        val a = x / width.toFloat()
        var column = (a * getColumnCount()).roundToInt()
        var x1 = getColumnsWidth(0, column - 1)
        var x2 = x1 + getColumnsWidth(column, column)
        while (x < x1) {
            column--
            x1 -= getColumnsWidth(column, column)
        }
        while (x > x2) {
            column++
            x2 += getColumnsWidth(column, column)
        }
        return column
    }

    /**
     * 得到 [y] 对应的行数，超出控件范围会得到边界行，控件没有被测量时得到 -1
     * @return 得到 [y] 对应的行数，超出控件范围会得到边界行，控件没有被测量时得到 -1
     */
    fun getRow(y: Int): Int {
        if (height == 0) return -1
        if (y <= 0) {
            return 0
        } else if (y >= height) {
            return getRowCount() - 1
        }
        val a = y / height.toFloat()
        var row = (a * getRowCount()).roundToInt()
        var y1 = getRowsHeight(0, row - 1)
        var y2 = y1 + getRowsHeight(row, row)
        while (y < y1) {
            row--
            y1 -= getRowsHeight(row, row)
        }
        while (y > y2) {
            row++
            y2 += getRowsHeight(row, row)
        }
        return row
    }

    /**
     * 得到 [start] 到 [end] 列之间的宽度，当视图没有被测量时返回 -1
     *
     * 该方法也可用于得到某列开始和结束的 X 值
     * ```
     * 形参如下：
     * (0, -1) 为第零列开始的 X     (0, 0)  为第零列结束的 X
     * (0, 0)  为第一列开始的 X     (0, 1)  为第一列结束的 X
     * (0, 1)  为第二列开始的 X     (0, 2)  为第二列结束的 X
     * ......
     * ```
     */
    fun getColumnsWidth(start: Int, end: Int): Int {
        if (start > mNetAttrs.columnCount || start < 0) {
            throw IllegalArgumentException(
                "start = $start 不能大于或等于 ${mNetAttrs.columnCount} 且小于 -1！")
        }
        if (end >= mNetAttrs.columnCount || end < -1) {
            throw IllegalArgumentException(
                "end = $end 不能大于或等于 ${mNetAttrs.columnCount} 且小于 -1！")
        }
        if (width == 0) return -1
        if (start == mNetAttrs.columnCount) return width
        return getColumnsWidthInternal(start, end, width)
    }

    /**
     * 得到 [start] 到 [end] 行之间的宽度，当视图没有被测量时返回 -1
     *
     * 该方法也可用于得到某行开始和结束的 Y 值
     * ```
     * 形参如下：
     * (0, -1) 为第零行开始的 Y     (0, 0)  为第零行结束的 Y
     * (0, 0)  为第一行开始的 Y     (0, 1)  为第一行结束的 Y
     * (0, 1)  为第二行开始的 Y     (0, 2)  为第二行结束的 Y
     * ......
     * ```
     */
    fun getRowsHeight(start: Int, end: Int): Int {
        if (start > mNetAttrs.rowCount || start < 0) {
            throw IllegalArgumentException(
                "start = $start 不能大于 ${mNetAttrs.rowCount} 且小于 -1！")
        }
        if (end >= mNetAttrs.rowCount || end < -1) {
            throw IllegalArgumentException(
                "end = $end 不能大于 ${mNetAttrs.rowCount} 且小于 -1！")
        }
        if (height == 0) return -1
        if (start == mNetAttrs.rowCount) return height
        return getRowsHeightInternal(start, end, height)
    }

    /**
     * 重新分配第 [column] 列的比重
     * @param weight 比重，默认情况下为 1F
     */
    fun setColumnWeight(column: Int, weight: Float) {
        checkColumn(column)
        val old = mColumnChangedWeight[column] ?: 1F
        if (old == weight) return
        mOnWeightChangeListeners.forEach {
            it.onChange(old, weight, column, SideType.COLUMN)
        }
        when {
            weight == 1F -> mColumnChangedWeight.remove(column)
            weight > 0F -> mColumnChangedWeight[column] = weight
            else -> mColumnChangedWeight[column] = 0F
        }
        requestLayout()
    }

    /**
     * 重新分配第 [row] 行的比重
     * @param weight 比重，默认情况下为 1F
     */
    fun setRowWeight(row: Int, weight: Float) {
        checkRow(row)
        val old = mRowChangedWeight[row] ?: 1F
        if (old == weight) return
        mOnWeightChangeListeners.forEach {
            it.onChange(old, weight, row, SideType.ROW)
        }
        when {
            weight == 1F -> mRowChangedWeight.remove(row)
            weight > 0F -> mRowChangedWeight[row] = weight
            else -> mRowChangedWeight[row] = 0F
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
        mInitialSelfColumnWeight = 0F // 重置
        getInitialSelfColumnWeight() // 立马重新设置，防止在下一次布局请求前调用了 setColumnWeight()
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
        mInitialSelfRowWeight = 0F // 重置
        getInitialSelfRowWeight() // 立马重新设置，防止在下一次布局请求前调用了 setRowWeight()
    }

    /**
     * 得到第 [start] - [end] 列的比重
     */
    fun getColumnsWeight(start: Int, end: Int): Float {
        if (start !in 0..end || end >= mNetAttrs.columnCount) {
            throw IllegalArgumentException(
                "start = $start 必须大于 0 且小于 end，end = $end 不能大于或等于 ${mNetAttrs.columnCount}！")
        }
        return getColumnsWeightInternal(start, end)
    }

    /**
     * 得到 [start] - [end] 行的比重
     */
    fun getRowsWeight(start: Int, end: Int): Float {
        if (start !in 0..end || end >= mNetAttrs.rowCount) {
            throw IllegalArgumentException(
                "start = $start 必须大于 0 且小于 end，end = $end 不能大于或等于 ${mNetAttrs.rowCount}！")
        }
        return getRowsWeightInternal(start, end)
    }

    /**
     * 设置行数和列数
     */
    fun setRowColumnCount(row: Int, column: Int) {
        checkColumn(column)
        checkRow(row)
        mNetAttrs.columnCount = column
        mNetAttrs.rowCount = row
        requestLayout()
    }

    /**
     * 设置行或列比重被修改的监听
     */
    fun setOnWeightChangeListener(l: OnWeightChangeListener) {
        mOnWeightChangeListeners.add(l)
    }

    /**
     * 将自身的列比重与 [layout] 同步
     * @return 不符合要求返回 false
     */
    fun syncColumnWeight(layout: NetLayout): Boolean {
        if (getColumnCount() == layout.getColumnCount()) {
            mColumnChangedWeight.clear()
            mColumnChangedWeight.putAll(layout.mColumnChangedWeight)
            requestLayout()
            return true
        }
        return false
    }

    /**
     * 将自身的行比重与 [layout] 同步
     * @return 不符合要求返回 false
     */
    fun syncRowWeight(layout: NetLayout): Boolean {
        if (getRowCount() == layout.getRowCount()) {
            mRowChangedWeight.clear()
            mRowChangedWeight.putAll(layout.mRowChangedWeight)
            requestLayout()
            return true
        }
        return false
    }

    // 属性值
    protected val mNetAttrs: NetLayoutAttrs

    // 参考 FrameLayout，用于在自身 wrap_content 而子 View 为 match_parent 时的测量
    private val mMatchParentChildren = ArrayList<View>()

    // 记录行比重不为 1F 的行数和比重数
    private val mRowChangedWeight = LinkedHashMap<Int, Float>()
    // 记录列比重不为 1F 的列数和比重数
    private val mColumnChangedWeight = LinkedHashMap<Int, Float>()

    // 在 layout_height 为 wrap_content 时，记录第一次测量的自身总行比重，用于后面扩大行比重使父布局变高
    private var mInitialSelfRowWeight = 0F
    // 在 layout_width 为 wrap_content 时，记录第一次测量的自身总列比重，用于后面扩大列比重使父布局变宽
    private var mInitialSelfColumnWeight = 0F

    // 比重改变监听
    private val mOnWeightChangeListeners = ArrayList<OnWeightChangeListener>(2)

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
     * 可在继承 [NetLayout] 的前提下重写 [NetLayoutParams.compareTo] 方法
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

        var maxWidth = 0
        var maxHeight = 0
        var childState = 0

        val parentColumnWeight = getSelfColumnsWeight()
        val parentRowWeight = getSelfRowsWeight()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams.net()
                if (!lp.isComplete()) continue

                val childColumnWeight = getColumnsWeightInternal(lp.startColumn, lp.endColumn)
                val childRowWeight = getRowsWeightInternal(lp.startRow, lp.endRow)

                val childWithParentColumnMultiple = childColumnWeight / parentColumnWeight
                val childWithParentRowMultiple = childRowWeight / parentRowWeight

                val childWidthRatio =
                    if (widthIsWrap) childColumnWeight / getInitialSelfColumnWeight()
                    else childWithParentColumnMultiple

                val childHeightRatio =
                    if (heightIsWrap) childRowWeight / getInitialSelfRowWeight()
                    else childWithParentRowMultiple

                lp.oldChildWidthRatio = childWithParentColumnMultiple
                lp.oldChildHeightRatio = childWithParentRowMultiple

                measureChildWithRatio(
                    child,
                    widthMeasureSpec, heightMeasureSpec,
                    childWidthRatio, childHeightRatio
                )

                maxWidth = max(
                    maxWidth,
                    when {
                        childWithParentColumnMultiple == 0F -> 0
                        child.measuredWidth == 0 -> 0
                        else -> ((child.measuredWidth + lp.leftMargin + lp.rightMargin) /
                                childWithParentColumnMultiple).toInt()
                    }
                )
                maxHeight = max(
                    maxHeight,
                    when {
                        childWithParentRowMultiple == 0F -> 0
                        child.measuredHeight == 0 -> 0
                        else -> ((child.measuredHeight + lp.topMargin + lp.bottomMargin) /
                                childWithParentRowMultiple).toInt()
                    }
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
        // 在与 ScrollView 嵌套中可能你会设置一个 minHeight，
        // 如果此时你高度又设置了 wrap，然后调用 setRowWeight()，本意是扩大控件高度，
        // 但会受到你设置的 minHeight 限制，所以需要在既设置 wrap 又设置了 minHeight 的情况下扩大你设置的 minHeight
        val minWidth = if (widthIsWrap) {
            (suggestedMinimumWidth * (parentColumnWeight / getInitialSelfColumnWeight())).toInt()
        } else suggestedMinimumWidth
        val minHeight = if (heightIsWrap) {
            (suggestedMinimumHeight * (parentRowWeight / getInitialSelfRowWeight())).toInt()
        } else suggestedMinimumHeight
        maxWidth = max(maxWidth, minWidth)
        maxHeight = max(maxHeight, minHeight)

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
            mMatchParentChildren.clear()
        }
    }

    /**
     * 获取开始列到结束列所占的总比例大小
     */
    private fun getColumnsWeightInternal(start: Int, end: Int): Float {
        if (end >= getColumnCount()) throw RuntimeException("end = $end   rowCount = ${getColumnCount()}")
        var childColumnSize = 0F
        for (column in start..end) {
            childColumnSize += mColumnChangedWeight[column] ?: 1F
        }
        return childColumnSize
    }

    /**
     * 获取开始行到结束行所占的总比例大小
     */
    private fun getRowsWeightInternal(start: Int, end: Int): Float {
        if (end >= getRowCount()) throw RuntimeException("end = $end   rowCount = ${getRowCount()}")
        var childRowSize = 0F
        for (row in start..end) {
            childRowSize += mRowChangedWeight[row] ?: 1F
        }
        return childRowSize
    }

    /**
     * 获取自身全部列所占的比例大小
     */
    private fun getSelfColumnsWeight(): Float {
        var parentColumnSize = 0F
        mColumnChangedWeight.forEach {
            parentColumnSize += it.value
        }
        parentColumnSize += mNetAttrs.columnCount - mColumnChangedWeight.size
        return parentColumnSize
    }

    /**
     * 获取自身全部行所占的比例大小
     */
    private fun getSelfRowsWeight(): Float {
        var parentRowSize = 0F
        mRowChangedWeight.forEach {
            parentRowSize += it.value
        }
        parentRowSize += mNetAttrs.rowCount - mRowChangedWeight.size
        return parentRowSize
    }

    /**
     * 获取当自身宽为 warp_content 时第一次测量的全部列所占的比例大小
     */
    private fun getInitialSelfColumnWeight(): Float {
        if (mInitialSelfColumnWeight == 0F) {
            mColumnChangedWeight.forEach {
                mInitialSelfColumnWeight += it.value
            }
            mInitialSelfColumnWeight += mNetAttrs.columnCount - mColumnChangedWeight.size
        }
        return mInitialSelfColumnWeight
    }

    /**
     * 获取当自身高为 warp_content 时第一次测量的全部行所占的比例大小
     */
    private fun getInitialSelfRowWeight(): Float {
        if (mInitialSelfRowWeight == 0F) {
            mRowChangedWeight.forEach {
                mInitialSelfRowWeight += it.value
            }
            mInitialSelfRowWeight += mNetAttrs.rowCount - mRowChangedWeight.size
        }
        return mInitialSelfRowWeight
    }

    /**
     * 在计算宽度和高度时进行了部分修改，使其用一个比例来测量子 View
     * @param childWidthRatio [child] 宽度占父布局的总宽度的比例
     * @param childHeightRatio [child] 宽度占父布局的总高度的比例
     */
    protected open fun measureChildWithRatio(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        childWidthRatio: Float,
        childHeightRatio: Float
    ) {
        val lp = child.layoutParams.net()
        val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
        val wMode = MeasureSpec.getMode(parentWidthMeasureSpec)
        val childWidth = (childWidthRatio * (parentWidth)).toInt()
        val childWidthMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(childWidth, wMode),
            lp.leftMargin + lp.rightMargin, lp.width
        )

        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val hMode = MeasureSpec.getMode(parentHeightMeasureSpec)
        val childHeight = (childHeightRatio * (parentHeight)).toInt()
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(childHeight, hMode),
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
                        getColumnsWidthInternal(0, lp.startColumn - 1, totalColumnWidth)
                val parentRight = parentLeft + (lp.oldChildWidthRatio * totalColumnWidth).toInt()
                val parentTop = paddingTop +
                        getRowsHeightInternal(0, lp.startRow - 1, totalRowHeight)
                val parentBottom = parentTop + (lp.oldChildHeightRatio * totalRowHeight).toInt()

                lp.constraintLeft = parentLeft
                lp.constraintRight = parentRight
                lp.constraintTop = parentTop
                lp.constraintBottom = parentBottom

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

    private fun getColumnsWidthInternal(start: Int, end: Int, totalColumnWidth: Int): Int {
        if (end < start) return 0
        val childColumnWeight = getColumnsWeightInternal(start, end)
        val parentColumnWeight = getSelfColumnsWeight()
        return (childColumnWeight / parentColumnWeight * totalColumnWidth).toInt()
    }

    private fun getRowsHeightInternal(start: Int, end: Int, totalRowHeight: Int): Int {
        if (end < start) return 0
        val childRowWeight = getRowsWeightInternal(start, end)
        val parentRowWeight = getSelfRowsWeight()
        return (childRowWeight / parentRowWeight * totalRowHeight).toInt()
    }

    var DEBUG = false
    private val DEBUG_LINE_PAINT = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2F
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (DEBUG) {
            var childRowWeight = 0F
            val parentRowWeight = getSelfRowsWeight()
            for (row in 0 until getRowCount() - 1) {
                childRowWeight += mRowChangedWeight[row] ?: 1F
                val y = childRowWeight / parentRowWeight * height
                canvas.drawLine(0F, y, width.toFloat(), y, DEBUG_LINE_PAINT)
            }

            var childColumnWeight = 0F
            val parentColumnWeight = getSelfColumnsWeight()
            for (column in 0 until getColumnCount() - 1) {
                childColumnWeight += mColumnChangedWeight[column] ?: 1F
                val x = childColumnWeight / parentColumnWeight * width
                canvas.drawLine(x, 0F, x, height.toFloat(), DEBUG_LINE_PAINT)
            }
        }
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
            NetLayoutParams.UNSET,
            NetLayoutParams.UNSET,
            NetLayoutParams.UNSET,
            NetLayoutParams.UNSET
        )
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is NetLayoutParams
    }

    private fun checkColumn(column: Int) {
        if (column >= mNetAttrs.columnCount || column < 0) {
            throw IllegalArgumentException("column 不能大于或等于 ${mNetAttrs.columnCount} 且小于 0！")
        }
    }

    private fun checkRow(row: Int) {
        if (row >= mNetAttrs.rowCount || row < 0) {
            throw IllegalArgumentException("row 不能大于或等于 ${mNetAttrs.rowCount} 且小于 0！")
        }
    }

    protected fun LayoutParams.net(): NetLayoutParams = this as NetLayoutParams
}