package com.ndhzs.courseviewlibrary.demo.widget.course.handler

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.ndhzs.courseviewlibrary.demo.bean.StuWeekBean

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/17 18:40
 */
class CourseCompareHandler : DiffUtil.ItemCallback<StuWeekBean.Lesson>(), ListUpdateCallback {

    private val mDiffer = AsyncListDiffer(this, AsyncDifferConfig.Builder(this).build())
    private lateinit var mOldList: List<StuWeekBean.Lesson>
    private lateinit var mNewList: List<StuWeekBean.Lesson>
    private var mCallback: CourseUpdateCallback? = null
    private var mIsFirstSubmit = true

    fun submitList(newList: List<StuWeekBean.Lesson>, callback: CourseUpdateCallback? = null) {
        mOldList = mDiffer.currentList
        mNewList = newList
        mCallback = callback
        mDiffer.submitList(newList) {
            mCallback = null
        }
    }

    override fun areItemsTheSame(
        oldItem: StuWeekBean.Lesson,
        newItem: StuWeekBean.Lesson
    ): Boolean {
        return oldItem.courseNum == newItem.courseNum
    }

    override fun areContentsTheSame(
        oldItem: StuWeekBean.Lesson,
        newItem: StuWeekBean.Lesson
    ): Boolean {
        return oldItem == newItem
    }

    override fun onInserted(position: Int, count: Int) {
        if (mIsFirstSubmit) {
            // 第一次提交不回调
            mIsFirstSubmit = false
        } else {
            repeat(count) {
                mCallback?.onInserted(mNewList[position + it])
            }
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        repeat(count) {
            mCallback?.onRemoved(mOldList[position + it])
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        mCallback?.onMoved(mOldList[fromPosition], mNewList[toPosition])
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        repeat(count) {
            mCallback?.onChanged(mOldList[position + it], mNewList[position + it])
        }
    }

    interface CourseUpdateCallback {
        fun onInserted(newLesson: StuWeekBean.Lesson)
        fun onRemoved(oldLesson: StuWeekBean.Lesson)
        fun onMoved(oldLesson: StuWeekBean.Lesson, newLesson: StuWeekBean.Lesson)
        fun onChanged(oldLesson: StuWeekBean.Lesson, newLesson: StuWeekBean.Lesson)
    }
}