package com.mredrock.cyxbs.lib.courseview.net.attrs

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.base.int

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
open class NetLayoutParams : ViewGroup.MarginLayoutParams, BaseViewAttrs, Comparable<NetLayoutParams> {

    open fun isComplete(): Boolean {
        return startRow in 0..endRow
                && startColumn in 0..endColumn
    }

    var gravity: Int

    var startRow: Int
    var endRow: Int
    var startColumn: Int
    var endColumn: Int

    val rowCount: Int
        get() = endRow - startRow + 1
    val columnCount: Int
        get() = endColumn - startColumn + 1

    constructor(
        c: Context,
        attrs: AttributeSet
    ) : super(c, attrs) {
        val ty = c.obtainStyledAttributes(attrs, R.styleable.NetLayout_Layout)
        startRow = R.styleable.NetLayout_Layout_net_layout_startRow.int(ty, UNSET)
        endRow = R.styleable.NetLayout_Layout_net_layout_endRow.int(ty, UNSET)
        startColumn = R.styleable.NetLayout_Layout_net_layout_startColumn.int(ty, UNSET)
        endColumn = R.styleable.NetLayout_Layout_net_layout_endColumn.int(ty, UNSET)
        gravity = R.styleable.NetLayout_Layout_net_layout_gravity.int(ty, Gravity.CENTER)
        ty.recycle()
    }
    constructor(
        width: Int,
        height: Int,
        startRow: Int,
        endRow: Int,
        startColumn: Int,
        endColumn: Int,
        gravity: Int = Gravity.CENTER
    ) : super(width, height) {
        this.startRow = startRow
        this.endRow = endRow
        this.startColumn = startColumn
        this.endColumn = endColumn
        this.gravity = gravity
    }
    constructor(source: NetLayoutParams, ) : super(source) {
        this.startRow = source.startRow
        this.endRow = source.endRow
        this.startColumn = source.startColumn
        this.endColumn = source.endColumn
        this.gravity = source.gravity
    }
    constructor(source: ViewGroup.MarginLayoutParams) : super(source) {
        this.startRow = UNSET
        this.endRow = UNSET
        this.startColumn = UNSET
        this.endColumn = UNSET
        this.gravity = UNSET
    }
    constructor(source: ViewGroup.LayoutParams) : super(source) {
        this.startRow = UNSET
        this.endRow = UNSET
        this.startColumn = UNSET
        this.endColumn = UNSET
        this.gravity = UNSET
    }

    override fun compareTo(other: NetLayoutParams): Int {
        val dArea = other.rowCount * other.columnCount - rowCount * columnCount
        if (dArea == 0) {
            val dRow = startRow - other.startRow
            if (dRow == 0) {
                val dColumn = startColumn - other.startColumn // 开始列小的在下面
                if (dColumn == 0) {
                    // 间距大的（面积就小）在上面
                    return leftMargin + rightMargin + topMargin + bottomMargin -
                            (other.leftMargin + other.rightMargin + other.topMargin + other.bottomMargin)
                }
            }
            return dRow // 开始行小的在下面
        }
        return dArea // 面积大的在下面
    }

    companion object {
        const val UNSET = -1
    }
}