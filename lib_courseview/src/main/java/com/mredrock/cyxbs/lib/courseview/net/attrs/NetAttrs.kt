package com.mredrock.cyxbs.lib.courseview.net.attrs

import android.content.Context
import android.util.AttributeSet
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.net.utils.NetLayoutAttrsException

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
data class NetAttrs(
    internal var _startRow: Int,
    internal var _endRow: Int,
    internal var _startColumn: Int,
    internal var _endColumn: Int
) : BaseViewAttrs, Comparable<NetAttrs> {
    val startRow: Int
        get() = _startRow
    val endRow: Int
        get() = _endRow
    val startColumn: Int
        get() = _startColumn
    val endColumn: Int
        get() = _endColumn

    val rowCount: Int
        get() = endRow - startRow + 1
    val columnCount: Int
        get() = endColumn - startColumn + 1

    override fun compareTo(other: NetAttrs): Int {
        val dArea = other.rowCount * other.columnCount - rowCount * columnCount
        if (dArea == 0) {
            val dRow = startRow - other.startRow
            if (dRow == 0) {
                return startColumn - other.startColumn // 开始列小的在下面
            }
            return dRow // 开始行小的在下面
        }
        return dArea // 面积大的在下面
    }

    companion object {
        fun newInstance(
            context: Context,
            attrs: AttributeSet
        ): NetAttrs {
            return BaseViewAttrs.newAttrs(context, attrs, R.styleable.NetLayout_Layout) {
                NetAttrs(
                    R.styleable.NetLayout_Layout_net_startRow
                        .intOrThrow<NetLayoutAttrsException>("net_startRow"),
                    R.styleable.NetLayout_Layout_net_endRow
                        .intOrThrow<NetLayoutAttrsException>("net_endRow"),
                    R.styleable.NetLayout_Layout_net_startColumn
                        .intOrThrow<NetLayoutAttrsException>("net_startColumn"),
                    R.styleable.NetLayout_Layout_net_endColumn
                        .intOrThrow<NetLayoutAttrsException>("net_endColumn")
                )
            }
        }

        const val UNSET = -1
    }
}