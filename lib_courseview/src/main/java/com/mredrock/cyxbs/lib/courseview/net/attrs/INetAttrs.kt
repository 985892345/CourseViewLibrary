package com.mredrock.cyxbs.lib.courseview.net.attrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
interface INetAttrs : Comparable<INetAttrs> {
    var startRow: Int
    var endRow: Int
    var startColumn: Int
    var endColumn: Int

    val rowCount: Int
        get() = endRow - startRow + 1
    val columnCount: Int
        get() = endColumn - startColumn + 1

    override fun compareTo(other: INetAttrs): Int {
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
}