package com.ndhzs.courseviewlibrary.demo.widget.course.handler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mredrock.cyxbs.lib.courseview.CourseView
import com.ndhzs.courseviewlibrary.demo.R
import com.ndhzs.courseviewlibrary.demo.bean.StuWeekBean
import com.ndhzs.courseviewlibrary.demo.widget.course.CourseHelper

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/13 19:08
 */
class CourseVpAdapter : ListAdapter<StuWeekBean, CourseVpAdapter.CourseVH>(
    object : DiffUtil.ItemCallback<StuWeekBean>() {
        override fun areItemsTheSame(oldItem: StuWeekBean, newItem: StuWeekBean): Boolean {
            return oldItem.week == newItem.week
        }

        override fun areContentsTheSame(
            oldItem: StuWeekBean,
            newItem: StuWeekBean
        ): Boolean {
            return oldItem.data == newItem.data
        }

        override fun getChangePayload(oldItem: StuWeekBean, newItem: StuWeekBean): Any {
            return "" // 只要不为 null 就可以在刷新时去掉与缓存的互换，减少性能的消耗
        }
    }
) {
    class CourseVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val course: CourseView = itemView.findViewById(R.id.course)
        val helper = CourseHelper(course)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_course, parent, false)
        return CourseVH(view)
    }

    override fun onBindViewHolder(holder: CourseVH, position: Int) {
        val data = getItem(position)
        /*
        * 检测 week 数是否与 position 对应
        * 正常情况下是肯定对应的，因为我在请求数据时就进行了排序
        * */
        if (data.week != position + 1) {
            holder.helper.refresh(getItem(position))
        } else {
            // 如果出现了这个报错，说明你删除了我在请求数据时排序的代码
            throw RuntimeException("传进来的 List 中的周数没有保证顺序！")
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<StuWeekBean>,
        currentList: MutableList<StuWeekBean>
    ) {
        val currentSize = currentList.size
        val handlerSize = mCompareHandlers.size
        if (currentSize > handlerSize) {
            repeat(currentSize - handlerSize) {
                val newHandler = CourseCompareHandler()
                // 先保存初始数据
                newHandler.submitList(currentList[currentSize + it].data)
                mCompareHandlers.add(newHandler)
            }
        } else {
            // 正常来说，只要周数是定下来了的，就不会走该分支
            repeat(handlerSize - currentSize) {
                mCompareHandlers.removeLast()
            }
        }
    }

    // 比对每周数据的处理者，后面的 22 是默认容器大小，后端正常情况下是返回的 22 周
    private val mCompareHandlers = ArrayList<CourseCompareHandler>(22)

    init {
        registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                    repeat(itemCount) {
                        val index = positionStart + it
                        // 开始比对数据
                        mCompareHandlers[index].submitList(getItem(index).data)
                    }
                }
            }
        )
    }
}