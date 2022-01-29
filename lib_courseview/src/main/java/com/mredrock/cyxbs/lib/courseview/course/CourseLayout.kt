package com.mredrock.cyxbs.lib.courseview.course

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.NestedScrollingChildHelper
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.CourseDecoration
import com.mredrock.cyxbs.lib.courseview.course.utils.CourseLayoutAttrsException
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.net.NetLayout
import com.mredrock.cyxbs.lib.courseview.utils.CourseType

/**
 * ```
 * 该 View 作用：
 * 1、继承于 NetLayout，自定义对课程的布局
 *
 * ● ======== app:net_columnCount = 8 ======== ●
 * ↓                                           ↓
 * --------------------------------------------- ←- ●
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |       |                                   |    ║
 * |   L   |                  S                |    ║
 * |   E   |                  H                |    ║
 * |   F   |                  O                |    ║ ←-←-←-←-←- Noon 时间段，占 1 x 7
 * |   T   |                  W                |    ║
 * |       |                                   | app:net_rowCount = 14
 * |   T   |                  L                |    ║
 * |   I   |                  E                |    ║
 * |   M   |                  S                |    ║ ←-←-←-←-←- Dusk 时间段，占 1 x 7
 * |   E   |                  S                |    ║
 * |       |                  O                |    ║
 * |       |                  N                |    ║
 * |       |                  S                |    ║
 * |       |                                   |    ║
 * --------------------------------------------- ←- ●
 * ↑       ↑                                   ↑
 * ↑       ↑                                   ↑
 * ↑       ● =========== Column = 7 ========== ●
 * ↑       ↑                  ↑
 * ● ===== ●              这里显示课程
 *     ↑
 *  这一列显示左侧的时间
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/20
 */
class CourseLayout : NetLayout {

    fun addCourse(view: View, lp: CourseLayoutParams) {
        addItem(view, lp)
    }

    fun addCourseDecoration(decor: CourseDecoration, index: Int = mCourseDecoration.size) {
        mCourseDecoration.add(index, decor)
    }

    fun addCourseTouchListener(l: OnCourseTouchListener, index: Int = mCourseTouchListener.size) {
        mCourseTouchListener.add(index, l)
    }

    /**
     * 得到当前中午那一行的状态
     */
    fun getNoonRowState(): RowState {
        if (mNoonAnimation != null) return RowState.ANIMATION
        return when (getRowsWeight(NOON_TOP, NOON_BOTTOM) / (NOON_BOTTOM - NOON_TOP + 1)) {
            1F -> RowState.UNFOLD
            0F -> RowState.FOLD
            else -> RowState.ANIMATION
        }
    }

    /**
     * 得到当前中午那一行的状态
     */
    fun getDuskRowState(): RowState {
        if (mDuskAnimation != null) return RowState.ANIMATION
        return when (getRowsWeight(DUSK_TOP, DUSK_BOTTOM) / (DUSK_BOTTOM - DUSK_TOP + 1)) {
            1F -> RowState.UNFOLD
            0F -> RowState.FOLD
            else -> RowState.ANIMATION
        }
    }

    /**
     * 改变中午时间段所在行数的比重
     */
    fun changeNoonWeight(weight: Float) {
        for (row in NOON_TOP..NOON_BOTTOM) {
            setRowWeight(row, weight)
        }
    }

    /**
     * 改变傍晚时间段所在行数的比重
     */
    fun changeDuskWeight(weight: Float) {
        for (row in DUSK_TOP..DUSK_BOTTOM) {
            setRowWeight(row, weight)
        }
    }

    /**
     * 不带动画的立即折叠中午时间段。如果此时正处于展开动画，则立马取消；如果正处于折叠动画，则不做取消操作
     */
    fun foldNoonWithoutAnim() {
        if (mNoonAnimation == null) {
            changeNoonWeight(0F)
        } else if (mNoonAnimation is UnfoldAnimation) {
            mNoonAnimation?.cancel()
            mNoonAnimation = null
            changeNoonWeight(0F)
        }
    }

    /**
     * 不带动画的立即展开中午时间段。如果此时正处于折叠动画，则立马取消；如果正处于展开动画，则不做取消操作
     */
    fun unfoldNoonWithoutAnim() {
        if (mNoonAnimation == null) {
            changeNoonWeight(1F)
        } else if (mNoonAnimation is FoldAnimation) {
            mNoonAnimation?.cancel()
            mNoonAnimation = null
            changeNoonWeight(1F)
        }
    }

    /**
     * 带有动画的折叠中午时间段，折叠动画和展开动画不共存，如果整叠正在运行，则调用展开动画将会失败
     */
    fun foldNoon(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mNoonAnimation == null) {
            mNoonAnimation = FoldAnimation(
                onEnd = {
                    mNoonAnimation = null
                    mNoonImageView.visibility = View.VISIBLE
                    onEnd?.invoke()
                },
                onChanged = {
                    val nowWeight = it.animatedValue as Float
                    changeNoonWeight(nowWeight)
                    onChanged?.invoke(nowWeight)
                }
            ).apply { start() }
        }
    }

    /**
     * 带有动画的展开中午时间段，折叠动画和展开动画不共存，如果展开正在运行，则调用折叠动画将会失败
     */
    fun unfoldNoon(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mNoonAnimation == null) {
            mNoonImageView.visibility = View.INVISIBLE
            mNoonAnimation = UnfoldAnimation(
                onEnd = {
                    mNoonAnimation = null
                    onEnd?.invoke()
                },
                onChanged = {
                    val nowWeight = it.animatedValue as Float
                    changeNoonWeight(nowWeight)
                    onChanged?.invoke(nowWeight)
                }
            ).apply { start() }
        }
    }

    /**
     * 不带动画的立即折叠傍晚时间段。如果此时正处于展开动画，则立马取消；如果正处于折叠动画，则不做取消操作
     */
    fun foldDuskWithoutAnim() {
        if (mDuskAnimation == null) {
            changeDuskWeight(0F)
        } else if (mDuskAnimation is UnfoldAnimation) {
            mDuskAnimation?.cancel()
            mDuskAnimation = null
            changeDuskWeight(0F)
        }
    }

    /**
     * 不带动画的立即展开傍晚时间段。如果此时正处于折叠动画，则立马取消；如果正处于展开动画，则不做取消操作
     */
    fun unfoldDuskWithoutAnim() {
        if (mDuskAnimation == null) {
            changeDuskWeight(1F)
        } else if (mDuskAnimation is UnfoldAnimation) {
            mDuskAnimation?.cancel()
            mDuskAnimation = null
            changeDuskWeight(1F)
        }
    }

    /**
     * 带有动画的折叠傍晚时间段，折叠动画和展开动画不共存，如果整叠正在运行，则调用展开动画将会失败
     */
    fun foldDusk(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mDuskAnimation == null) {
            mDuskAnimation = FoldAnimation(
                onEnd = {
                    mDuskAnimation = null
                    mDuskImageView.visibility = View.VISIBLE
                    onEnd?.invoke()
                },
                onChanged = {
                    val nowWeight = it.animatedValue as Float
                    changeDuskWeight(nowWeight)
                    onChanged?.invoke(nowWeight)
                }
            ).apply { start() }
        }
    }

    /**
     * 带有动画的展开傍晚时间段，折叠动画和展开动画不共存，如果展开正在运行，则调用折叠动画将会失败
     */
    fun unfoldDusk(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mDuskAnimation == null) {
            mDuskImageView.visibility = View.INVISIBLE
            mDuskAnimation = UnfoldAnimation(
                onEnd = {
                    mDuskAnimation = null
                    onEnd?.invoke()
                },
                onChanged = {
                    val nowWeight = it.animatedValue as Float
                    changeDuskWeight(nowWeight)
                    onChanged?.invoke(nowWeight)
                }
            ).apply { start() }
        }
    }

    private val mCourseAttrs: CourseLayoutAttrs

    private val mCourseDecoration = ArrayList<CourseDecoration>(5)
    private val mCourseTouchListener = ArrayList<OnCourseTouchListener>(5)

    private var mInterceptingOnTouchListener: OnCourseTouchListener? = null

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs) {
        mCourseAttrs = CourseLayoutAttrs(mNetAttrs)
    }

    constructor(
        context: Context,
        attrs: CourseLayoutAttrs
    ) : super(context, attrs) {
        mCourseAttrs = attrs
    }

    private var mNoonAnimation: ChangeWeightAnimation? = null // 中午折叠或者展开的动画
    private var mDuskAnimation: ChangeWeightAnimation? = null // 傍晚折叠或者展开的动画

    // 显示展开中午时间段的箭头，一个 ImageView
    private val mNoonImageView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.img_noon_arrow)
    }

    // 显示展开傍晚时间段的箭头，一个 ImageView
    private val mDuskImageView: ImageView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById(R.id.img_dusk_arrow)
    }

    override fun measureChildWithRatio(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        childWidthRatio: Float,
        childHeightRatio: Float
    ) {
        val lp = child.layoutParams.net()
        val parentWidth = MeasureSpec.getSize(parentWidthMeasureSpec) - paddingLeft - paddingRight
        val childWidth = (childWidthRatio * parentWidth).toInt()
        val childWidthMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childWidth,
                /*
                * 这里为什么直接给 EXACTLY ?
                * 1、课表初始状态需要填满整个父布局
                * 2、如果给 AT_MOST，则会使子 View 变成自己想要的大小,
                * 3、如果给 UNSPECIFIED 值，则会使没被重写的子 View 的宽直接变成 0，直接为 0 了就没了意义。
                *    原因可看：View#onMeasure() -> View#getDefaultSize()
                * */
                MeasureSpec.EXACTLY
            ),
            lp.leftMargin + lp.rightMargin, lp.width
        )

        val parentHeight = MeasureSpec.getSize(parentHeightMeasureSpec) - paddingTop - paddingBottom
        val childHeight = (childHeightRatio * parentHeight).toInt()
        val childHeightMeasureSpec = getChildMeasureSpec(
            MeasureSpec.makeMeasureSpec(
                childHeight,
                /*
                * 这里原因与上面相同，但还需要添加一些
                * 4、目前需求（22年）课表在开始时不显示中午和傍晚时间段，我设计的 NetLayout 可以把高度设置成
                *    wrap_content，再调用 setRowInitialWeight（若不存在，请查看 git）来隐藏中午和傍晚时间段
                * 5、课表如果要显示中午和傍晚时间段，则外布局需要包裹一个 NestedScrollView，这时，父布局得到的
                *    测量模式为 UNSPECIFIED，该模式会使课表初始状态不再填充父布局
                * */
                MeasureSpec.EXACTLY
            ),
            lp.topMargin + lp.bottomMargin, lp.height
        )

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mCourseTouchListener.forEach {
            it.onDispatchTouchEvent(ev, this)
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mInterceptingOnTouchListener = null
            mCourseTouchListener.forEach {
                if (mInterceptingOnTouchListener == null) {
                    if (it.isIntercept(event, this)) {
                        mInterceptingOnTouchListener = it
                    }
                } else {
                    it.onCancelDownEvent(this)
                }
            }
        }
        mInterceptingOnTouchListener?.onTouchEvent(event, this)
        return mInterceptingOnTouchListener != null
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        mCourseDecoration.forEach {
            it.onDrawOver(canvas, this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCourseDecoration.forEach {
            it.onDraw(canvas, this)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return CourseLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: LayoutParams): LayoutParams {
        return when (lp) {
            is CourseLayoutParams -> CourseLayoutParams(lp)
            else -> throw CourseLayoutAttrsException("LayoutParams 必须是 CourseLayoutParams！")
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        throw CourseLayoutAttrsException("强制要求 LayoutParams 必须是 CourseLayoutParams！")
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is CourseLayoutParams
    }

    override fun setLayoutParams(params: LayoutParams) {
        super.setLayoutParams(params)
        /*
        * 放这里的原因：
        * 1、setRowInitialWeight() 只能在设置了 LayoutParams 后调用
        *
        * 以下两个 for 循环有如下作用：
        * 1、设置初始时中午和傍晚时间段的比重为 0，为了让板块刚好撑满整个能够显示的高度，
        *    使在折叠中午和傍晚时外面的 ScrollView 不用滚动就能显示全部
        * */
        for (row in NOON_TOP..NOON_BOTTOM) {
            setRowInitialWeight(row, 0F)
        }
        for (row in DUSK_TOP..DUSK_BOTTOM) {
            setRowInitialWeight(row, 0F)
        }
    }

    private class FoldAnimation(
        onEnd: (() -> Unit),
        onChanged: ValueAnimator.AnimatorUpdateListener
    ) : ChangeWeightAnimation(0.99999F, 0F, 200, onEnd, onChanged)

    private class UnfoldAnimation(
        onEnd: (() -> Unit),
        onChanged: ValueAnimator.AnimatorUpdateListener
    ) : ChangeWeightAnimation(0.00001F, 1F, 200, onEnd, onChanged)

    // 比重改变的动画封装类
    private abstract class ChangeWeightAnimation(
        startWeight: Float,
        endWeight: Float,
        val time: Long,
        private val onEnd: (() -> Unit),
        private val onChanged: ValueAnimator.AnimatorUpdateListener
    ) {
        private var animator: ValueAnimator = ValueAnimator.ofFloat(startWeight, endWeight)
        fun start() {
            animator.run {
                addUpdateListener(onChanged)
                addListener(onEnd = { onEnd.invoke() },)
                duration = time
                this.start()
            }
        }

        fun addEndListener(onEnd: () -> Unit) {
            animator.addListener(onEnd = { onEnd.invoke() })
        }

        fun cancel() {
            animator.cancel()
        }
    }

    companion object {
        const val AM_TOP = 0
        const val AM_BOTTOM = 3
        const val NOON_TOP = 4
        const val NOON_BOTTOM = 4
        const val PM_TOP = 5
        const val PM_BOTTOM = 8
        const val DUSK_TOP = 9
        const val DUSK_BOTTOM = 9
        const val NIGHT_TOP = 10
        const val NIGHT_BOTTOM = 13

        const val TIME_LINE_LEFT = 0
        const val TIME_LINE_RIGHT = 0
    }
}