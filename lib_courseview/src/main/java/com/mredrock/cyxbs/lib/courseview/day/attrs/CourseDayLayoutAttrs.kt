package com.mredrock.cyxbs.lib.courseview.day.attrs

import android.content.Context
import android.util.AttributeSet
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs
import com.mredrock.cyxbs.lib.courseview.base.BaseViewAttrs.Companion.newAttrs

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/17
 */
data class CourseDayLayoutAttrs(
    internal val lessonCount: Int
) : BaseViewAttrs {
    companion object {
        fun newInstance(
            context: Context,
            attrs: AttributeSet
        ): CourseDayLayoutAttrs {
            return newAttrs(context, attrs, R.styleable.CourseDayLayout) {
                CourseDayLayoutAttrs(
                    R.styleable.CourseDayLayout_day_lessonCount.int(LESSON_COUNT)
                )
            }
        }

        const val LESSON_COUNT = 4
    }
}