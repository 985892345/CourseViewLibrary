package com.mredrock.cyxbs.lib.courseview.course.utils

import android.os.Bundle

/**
 * 用于在 View 因手机转屏或被摧毁时保存必要信息，并在重建时恢复信息
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/30
 */
interface OnSaveBundleListener {
    fun onSaveInstanceState(): Bundle?
    fun onRestoreInstanceState(bundle: Bundle?)
}