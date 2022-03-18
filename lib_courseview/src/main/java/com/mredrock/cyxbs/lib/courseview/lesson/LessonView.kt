package com.mredrock.cyxbs.lib.courseview.lesson

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.AbstractCourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.utils.ViewExtend

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/7 14:44
 */
open class LessonView(
    context: Context,
    attrs: AttributeSet?
) : CardView(context, attrs), ViewExtend {

    init {
        isClickable = true // 默认让 onTouchEvent 拦截事件
        cardElevation = 0F
        radius = R.dimen.course_course_item_radius.dimens()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        changeAlphaInFoldAnim()
    }

    /**
     * 如果自身只在中午或者傍晚时间段，则在折叠和展开时实时改变透明度
     */
    private fun changeAlphaInFoldAnim() {
        val parent = parent
        if (parent is CourseLayout) {
            val lp = layoutParams as CourseLayoutParams
            if (lp.startRow == NOON_TOP
                && lp.endRow == NOON_BOTTOM
                || lp.startRow == DUSK_TOP
                && lp.endRow == DUSK_BOTTOM
            ) {
                val totalHeight = lp.rowCount * parent.getRowsHeight(0, 0)
                val x = height / totalHeight.toFloat()
                alpha = x * x
            }
        }
    }

    override fun setBackgroundColor(color: Int) {
        setCardBackgroundColor(color)
    }
}