package com.mredrock.cyxbs.lib.courseview.course.attrs

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.mredrock.cyxbs.lib.courseview.course.CourseBeanInternal
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.int
import com.mredrock.cyxbs.lib.courseview.net.attrs.NetLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.courseType
import java.io.Serializable

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
open class CourseLayoutParams :
    NetLayoutParams,
    CourseBeanInternal,
    Serializable {

    /**
     * 克隆一个一样的
     */
    fun clone(): CourseLayoutParams {
        return CourseLayoutParams(startRow, endRow, startColumn, endColumn, type).also {
            it.copy(this)
        }
    }

    fun <T : CourseLayoutParams> copy(other: T): CourseLayoutParams {
        super.copy(other)
        type = other.type
        return this
    }

    override var day: Int
        get() = startColumn
        set(value) {
            endColumn = value + columnCount - 1
            startColumn = value
        }
    override var startPos: Int
        get() = startRow
        set(value) {
            endRow = value + rowCount - 1
            startRow = value
        }
    override var length: Int
        get() = endRow - startRow + 1
        set(value) {
            endRow = startRow + value - 1
        }
    final override var type: CourseType

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        val ty = context.obtainStyledAttributes(attrs, R.styleable.CourseLayout_Layout)
        val day = R.styleable.CourseLayout_Layout_course_layout_day.int(ty, UNSET)
        val startPos = R.styleable.CourseLayout_Layout_course_layout_startPos.int(ty, UNSET)
        val length = R.styleable.CourseLayout_Layout_course_layout_length.int(ty, UNSET)
        type = R.styleable.CourseLayout_Layout_course_layout_type.courseType(ty)
        ty.recycle()
        if (startPos != UNSET) {
            startRow = startPos
        }
        if (length != UNSET) {
            endRow = startRow + length - 1
        }
        if (day != UNSET) {
            startColumn = day
            endColumn = day
        }
    }

    constructor(
        day: Int,
        startPos : Int,
        length: Int,
        type: CourseType
    ) : this(
        startPos,
        startPos + length - 1,
        day,
        day,
        type
    )

    constructor(
        startRow: Int,
        endRow: Int,
        startColumn: Int,
        endColumn: Int,
        type: CourseType
    ) : super(startRow, endRow, startColumn, endColumn) {
        this.type = type
    }

    constructor(lp: CourseLayoutParams) : super(lp) {
        type = lp.type
    }

    constructor(bean: CourseBeanInternal) : this(bean.day, bean.startPos, bean.length, bean.type)

    override fun toString(): String {
        return "${this::class.simpleName}(" +
                "day = $day, " +
                "startPos = $startPos, " +
                "length = $length, " +
                "type = ${type.name}, " +
                "startRow = $startRow, " +
                "endRow = $endRow, " +
                "startColumn = $startColumn, " +
                "endColumn = $endColumn, " +
                "rowCount = $rowCount, " +
                "columnCount = $columnCount)"
    }

    override fun compareTo(other: NetLayoutParams): Int {
        if (type == CourseType.SUBSTITUTE || type == CourseType.UNKNOWN) {
            // 替身 View 只能在最底下，这样才不会干扰其他重叠的 View 的点击
            return -1
        }
        if (other is CourseLayoutParams) {
            val dColumn = startColumn - other.startColumn
            if (dColumn == 0) {
                val dRow = startRow - other.startRow
                if (dRow == 0) {
                    val dLength = length - other.length
                    if (dLength == 0) {
                        return -type.compareTo(other.type) // 这里根据枚举类的定义位置要取个负
                    }
                    return dLength // 越长的越在后面，即显示在上面
                }
                return dRow // 行数越大的越在后面，即显示在上面
            }
            return dColumn // 列数越大的越在后面，即显示在上面
        }
        return super.compareTo(other)
    }
}