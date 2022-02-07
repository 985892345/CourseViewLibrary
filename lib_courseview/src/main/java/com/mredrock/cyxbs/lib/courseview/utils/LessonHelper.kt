package com.mredrock.cyxbs.lib.courseview.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.mredrock.cyxbs.lib.courseview.R

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/6 20:01
 */
object LessonHelper {

    fun getLessonItem(
        beginLesson: Int,
        period: Int,
        parent: ViewGroup,
        type: LessonType
    ): View {
        val context = parent.context
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.item_lesson, parent, false) as ViewGroup
        val cardView: CardView = view.findViewById(R.id.cd_background)
        val tvTop: TextView = view.findViewById(R.id.tv_top)
        val tvBottom: TextView = view.findViewById(R.id.tv_bottom)
        when (type) {
            LessonType.MY -> {
                when {
                    beginLesson <= 4 -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.common_morning_course_color
                            )
                        )
                        val color =
                            ContextCompat.getColor(context, R.color.common_morning_course_text_color)
                        tvTop.setTextColor(color)
                        tvBottom.setTextColor(color)
                    }
                    beginLesson <= 8 -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.common_afternoon_course_color
                            )
                        )
                        val color =
                            ContextCompat.getColor(context, R.color.common_afternoon_course_text_color)
                        tvTop.setTextColor(color)
                        tvBottom.setTextColor(color)
                    }
                    else -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.common_evening_course_color
                            )
                        )
                        val color =
                            ContextCompat.getColor(context, R.color.common_evening_course_text_color)
                        tvTop.setTextColor(color)
                        tvBottom.setTextColor(color)
                    }
                }
            }
            LessonType.LINK -> {}
            LessonType.AFFAIR -> {}
        }
        return view
    }

    enum class LessonType {
        MY,
        LINK,
        AFFAIR
    }
}