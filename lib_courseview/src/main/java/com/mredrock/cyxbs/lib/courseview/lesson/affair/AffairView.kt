package com.mredrock.cyxbs.lib.courseview.lesson.affair

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.utils.ViewExtend
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 描述:课表中事务的背景View，
 * 别问为什么不用图片，问就是图片太麻烦，而且效果还不好
 *
 * @author Jovines
 * @create 2020-01-26 2:36 PM
 *
 * @author 985892345
 * @data 2022/2/7 16:40
 * @describe 因需求变更，我开始重构课表，简单优化了一下之前学长写的逻辑
 */
class AffairView(context: Context) : View(context), ViewExtend {

    private val mBgColor = R.color.white.color()
    private val mPaint = Paint().apply {
        color = R.color.common_transaction_background_stripe_color.color()
    }
    private val mRectF = RectF()
    private val mClipBounds = Rect()

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(mBgColor)
        canvas.getClipBounds(mClipBounds)
        val width = mClipBounds.width()
        val height = mClipBounds.height()
        val drawEdge = max(width, height) * sqrt(2F)
        val space = 8.dp2px()
        val num = (drawEdge / (space * 2)).toInt()
        canvas.save()
        canvas.translate(width / 2F, height / 2F)
        canvas.rotate(45F)
        mRectF.set(
            -drawEdge / 2,
            drawEdge / 2,
            -drawEdge / 2 + space,
            -drawEdge / 2
        )
        for (i in 0 until num) {
            canvas.drawRect(mRectF, mPaint)
            mRectF.set(
                mRectF.left + (space * 2),
                mRectF.top,
                mRectF.right + (space * 2),
                mRectF.bottom
            )
        }
        canvas.restore()
    }
}