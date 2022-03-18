package com.mredrock.cyxbs.lib.courseview.course.fold

/**
 * 表示行状态
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/29
 */
enum class RowState {
    FOLD, // 完全展开
    UNFOLD, // 完全折叠
    FOLD_ANIM, // 处于折叠动画中
    UNFOLD_ANIM // 处于展开动画中
}