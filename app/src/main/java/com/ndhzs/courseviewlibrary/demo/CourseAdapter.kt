package com.ndhzs.courseviewlibrary.demo

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/21
 */
class CourseAdapter {

    private lateinit var mRvAdapter: RvAdapter

    private var mNoonState = false
    private var mDuskState = true

    fun setRvAdapter(adapter: RvAdapter) {
        mRvAdapter = adapter
    }

    fun getItemCount(): Int {
        var count = 3 // 初始化时中午和傍晚的不显示
        if (mNoonState) count++
        if (mDuskState) count++
        return count
    }

    companion object {

    }
}