package com.mredrock.cyxbs.lib.courseview.course.utils

import android.os.Bundle

/**
 * 用于在 View 因手机转屏或被摧毁时保存必要信息，并在重建时恢复信息（估计现掌邮用不到，因为强制了竖屏）
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/30
 */
interface OnSaveBundleListener {
    fun onSaveInstanceState(): Bundle?
    fun onRestoreInstanceState(bundle: Bundle?)
}