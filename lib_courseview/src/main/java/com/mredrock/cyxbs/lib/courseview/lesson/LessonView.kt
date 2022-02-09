package com.mredrock.cyxbs.lib.courseview.lesson

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import kotlin.math.abs
import kotlin.math.pow

/**
 * ...
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/2/7 14:44
 */
class LessonView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs) {

    private var mInitialX = 0 // Down 时的初始 X 值
    private var mInitialY = 0 // Down 时的初始 Y 值
    private var mLastMoveX = 0 // Move 时的移动 X 值
    private var mLastMoveY = 0 // Move 时的移动 Y 值

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val mRadius = context.resources.getDimension(R.dimen.course_course_item_radius)
    private val mPath = Path()

    init {
        isClickable = true // 默认让 onTouchEvent 拦截事件
    }

    override fun draw(canvas: Canvas) {
        /*
        * 裁剪成圆角
        * 你可能会问为什么不直接用 CardView?
        * 原因如下：
        * 1、添加 Affair 时时添加的一个 Drawable，会使 CardView 的圆角失效
        * 2、CardView 的阴影效果使用不到（如果你想拥有阴影效果，可以直接设置 translationZ）
        * */
        mPath.reset()
        mPath.addRoundRect(
            0F,
            0F,
            width.toFloat(),
            height.toFloat(),
            mRadius,
            mRadius,
            Path.Direction.CCW
        )
        canvas.save()
        canvas.clipPath(mPath)
        super.draw(canvas)
        canvas.restore()

        changeAlphaInFoldAnim()
    }

    /**
     * 如果自身只在中午或者傍晚时间段，则在折叠和展开时实时改变透明度
     */
    private fun changeAlphaInFoldAnim() {
        val parent = parent
        if (parent is CourseLayout) {
            val lp = layoutParams as CourseLayoutParams
            if (lp.startRow == CourseLayout.NOON_TOP
                && lp.endRow == CourseLayout.NOON_BOTTOM
                || lp.startRow == CourseLayout.DUSK_TOP
                && lp.endRow == CourseLayout.DUSK_BOTTOM
            ) {
                val totalHeight = lp.rowCount * parent.getRowsHeight(0, 0)
                val x = height / totalHeight.toFloat()
                alpha = x * x
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = x
                mInitialY = y
                mLastMoveX = x
                mLastMoveY = y
                startAnim()
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(x - mLastMoveX) > mTouchSlop
                    || abs(y - mLastMoveY) > mTouchSlop
                ) {
                    recoverAnim()
                }
            }
            MotionEvent.ACTION_UP -> {
                recoverAnim()
            }
            MotionEvent.ACTION_CANCEL -> {
                recoverAnim()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 实现按下后的 Q 弹动画
     */
    private fun startAnim() {
        animate()
            .scaleX(0.85F)
            .scaleY(0.85F)
            .setInterpolator { 1 - 1F / (1F + it).pow(6) }
            .start()
    }

    private fun recoverAnim() {
        animate().cancel()
        animate()
            .scaleX(1F)
            .scaleY(1F)
            .setInterpolator { 1 - 1F / (1F + it).pow(6) }
            .start()
    }
}