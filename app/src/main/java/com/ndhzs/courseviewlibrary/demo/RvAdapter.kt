package com.ndhzs.courseviewlibrary.demo

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mredrock.cyxbs.lib.courseview.course.Course1View
import com.mredrock.cyxbs.lib.courseview.course.Course4View

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/21
 */
class RvAdapter(
    private val courseAdapter: CourseAdapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNoonState = false
    private var mDuskState = false

    init {
        courseAdapter.setRvAdapter(this)
    }

    class Course4VH(val itemView: Course4View) : RecyclerView.ViewHolder(itemView)
    class Course1VH(val itemView: Course1View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            COURSE_4 -> Course4VH(Course4View(parent.context))
            COURSE_1 -> Course1VH(Course1View(parent.context))
            else -> throw RuntimeException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        var count = 3 // 初始化时中午和傍晚的不显示
        if (mNoonState) count++
        if (mDuskState) count++
        return count
    }

    override fun getItemViewType(position: Int): Int {
        if (mNoonState && mDuskState) {
            return when (position) {
                0 -> COURSE_4
                1 -> COURSE_1
                2 -> COURSE_4
                3 -> COURSE_1
                4 -> COURSE_4
                else -> throw RuntimeException()
            }
        } else if (mNoonState && !mDuskState) {
            return when (position) {
                0 -> COURSE_4
                1 -> COURSE_1
                2 -> COURSE_4
                3 -> COURSE_4
                else -> throw RuntimeException()
            }
        } else if (!mNoonState && mDuskState) {
            return when (position) {
                0 -> COURSE_4
                1 -> COURSE_4
                2 -> COURSE_1
                3 -> COURSE_4
                else -> throw RuntimeException()
            }
        } else {
            return when (position) {
                0 -> COURSE_4
                1 -> COURSE_4
                2 -> COURSE_4
                else -> throw RuntimeException()
            }
        }
    }

    companion object {
        const val COURSE_4 = 0
        const val COURSE_1 = 1
    }
}