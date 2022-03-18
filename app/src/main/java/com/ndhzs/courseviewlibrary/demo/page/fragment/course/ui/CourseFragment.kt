package com.ndhzs.courseviewlibrary.demo.page.fragment.course.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.ndhzs.courseviewlibrary.demo.widget.course.handler.CourseVpAdapter
import com.ndhzs.courseviewlibrary.demo.R
import com.ndhzs.courseviewlibrary.demo.page.fragment.course.viewmodel.CourseViewModel

/**
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/3/14 11:34
 */
class CourseFragment : Fragment() {

    private val viewModel by viewModels<CourseViewModel>()

    private lateinit var mViewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return LayoutInflater.from(context).inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewPager2 = view.findViewById(R.id.vp_course)
        mViewPager2.adapter = CourseVpAdapter()
    }
}