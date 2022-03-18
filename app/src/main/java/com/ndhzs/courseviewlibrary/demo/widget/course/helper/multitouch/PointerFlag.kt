package com.ndhzs.courseviewlibrary.demo.widget.course.helper.multitouch

/**
 * ## 处理每个手指触摸的类的当前处理进度标记
 *
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/18 17:03
 */
enum class PointerFlag {
    START, // 表示开始处理
    OVER; // 表示处理结束，可以回收
}