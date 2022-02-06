package com.mredrock.cyxbs.lib.courseview.course

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.animation.addListener
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutAttrs
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.*
import com.mredrock.cyxbs.lib.courseview.net.NetLayout
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView

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

    /**
     * 添加课程
     */
    fun addCourse(view: View, lp: CourseLayoutParams) {
        addItem(view, lp)
    }

    /**
     * 仿照 RV 的 ItemDecoration 设计。用于自定义绘制一些东西
     */
    fun addCourseDecoration(decor: CourseDecoration, index: Int = mCourseDecoration.size) {
        mCourseDecoration.add(index, decor)
    }

    /**
     * 仿照 RV 的 OnItemTouchListener 设计。用于处理滑动事件，
     * 这样以后要扩展不是直接在这个 [CourseLayout] 里面添加代码，而是添加一个 [OnCourseTouchListener] 来增加新的功能
     */
    fun addCourseTouchListener(l: OnCourseTouchListener, index: Int = mCourseTouchListener.size) {
        mCourseTouchListener.add(index, l)
    }

    /**
     * 用于在 CourseDecoration 和 OnCourseTouchListener 中，[CourseLayout] 即将被摧毁时保存一些必要的信息
     */
    fun addSaveBundleListener(l: OnSaveBundleListener) {
        mSaveBundleListeners.add(l)
    }

    /**
     * 得到当前中午那一行的状态
     */
    fun getNoonRowState(): RowState {
        if (mNoonAnimation is FoldAnimation) return RowState.ANIM_FOLD
        if (mNoonAnimation is UnfoldAnimation) return RowState.ANIM_UNFOLD
        return when (getRowsWeight(NOON_TOP, NOON_BOTTOM) / (NOON_BOTTOM - NOON_TOP + 1)) {
            1F -> RowState.UNFOLD
            0F -> RowState.FOLD
            else -> throw RuntimeException("出现这个 error 你把中午时间段的比重私自修改了")
        }
    }

    /**
     * 得到当前中午那一行的状态
     */
    fun getDuskRowState(): RowState {
        if (mDuskAnimation is FoldAnimation) return RowState.ANIM_FOLD
        if (mDuskAnimation is UnfoldAnimation) return RowState.ANIM_UNFOLD
        return when (getRowsWeight(DUSK_TOP, DUSK_BOTTOM) / (DUSK_BOTTOM - DUSK_TOP + 1)) {
            1F -> RowState.UNFOLD
            0F -> RowState.FOLD
            else -> throw RuntimeException("出现这个 error 你把傍晚时间段的比重私自修改了")
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
            mNoonImageView.visibility = VISIBLE
        } else if (mNoonAnimation is UnfoldAnimation) {
            mNoonAnimation?.cancel()
            mNoonAnimation = null
            changeNoonWeight(0F)
            mNoonImageView.visibility = VISIBLE
        }
    }

    /**
     * 不带动画的立即展开中午时间段。如果此时正处于折叠动画，则立马取消；如果正处于展开动画，则不做取消操作
     */
    fun unfoldNoonWithoutAnim() {
        if (mNoonAnimation == null) {
            changeNoonWeight(1F)
            mNoonImageView.visibility = INVISIBLE
        } else if (mNoonAnimation is FoldAnimation) {
            mNoonAnimation?.cancel()
            mNoonAnimation = null
            changeNoonWeight(1F)
            mNoonImageView.visibility = INVISIBLE
        }
    }

    /**
     * 带有动画的折叠中午时间段，折叠动画和展开动画不共存，如果整叠正在运行，则调用展开动画将会失败
     */
    fun foldNoon(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mNoonAnimation == null) {
            mNoonAnimation = FoldAnimation {
                changeNoonWeight(it)
                onChanged?.invoke(it)
            }.addEndListener {
                mNoonAnimation = null
                mNoonImageView.visibility = VISIBLE
                onEnd?.invoke()
            }.start()
        }
    }

    /**
     * 带有动画的展开中午时间段，折叠动画和展开动画不共存，如果展开正在运行，则调用折叠动画将会失败
     */
    fun unfoldNoon(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mNoonAnimation == null) {
            mNoonImageView.visibility = INVISIBLE
            mNoonAnimation = UnfoldAnimation {
                changeNoonWeight(it)
                onChanged?.invoke(it)
            }.addEndListener {
                mNoonAnimation = null
                onEnd?.invoke()
            }.start()
        }
    }

    /**
     * 不带动画的立即折叠傍晚时间段。如果此时正处于展开动画，则立马取消；如果正处于折叠动画，则不做取消操作
     */
    fun foldDuskWithoutAnim() {
        if (mDuskAnimation == null) {
            changeDuskWeight(0F)
            mDuskImageView.visibility = VISIBLE
        } else if (mDuskAnimation is UnfoldAnimation) {
            mDuskAnimation?.cancel()
            mDuskAnimation = null
            changeDuskWeight(0F)
            mDuskImageView.visibility = VISIBLE
        }
    }

    /**
     * 不带动画的立即展开傍晚时间段。如果此时正处于折叠动画，则立马取消；如果正处于展开动画，则不做取消操作
     */
    fun unfoldDuskWithoutAnim() {
        if (mDuskAnimation == null) {
            changeDuskWeight(1F)
            mDuskImageView.visibility = INVISIBLE
        } else if (mDuskAnimation is FoldAnimation) {
            mDuskAnimation?.cancel()
            mDuskAnimation = null
            changeDuskWeight(1F)
            mDuskImageView.visibility = INVISIBLE
        }
    }

    /**
     * 带有动画的折叠傍晚时间段，折叠动画和展开动画不共存，如果整叠正在运行，则调用展开动画将会失败
     */
    fun foldDusk(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mDuskAnimation == null) {
            mDuskAnimation = FoldAnimation {
                changeDuskWeight(it)
                onChanged?.invoke(it)
            }.addEndListener {
                mDuskAnimation = null
                mDuskImageView.visibility = VISIBLE
                onEnd?.invoke()
            }.start()
        }
    }

    /**
     * 带有动画的展开傍晚时间段，折叠动画和展开动画不共存，如果展开正在运行，则调用折叠动画将会失败
     */
    fun unfoldDusk(onEnd: (() -> Unit)? = null, onChanged: ((Float) -> Unit)? = null) {
        if (mDuskAnimation == null) {
            mDuskImageView.visibility = INVISIBLE
            mDuskAnimation = UnfoldAnimation {
                changeDuskWeight(it)
                onChanged?.invoke(it)
            }.addEndListener {
                mDuskAnimation = null
                onEnd?.invoke()
            }.start()
        }
    }

    fun addNoonAnimationEndListener(onEnd: () -> Unit): Boolean {
        mNoonAnimation?.addEndListener(onEnd)
        return mNoonAnimation != null
    }

    fun addDuskAnimationEndListener(onEnd: () -> Unit): Boolean {
        mDuskAnimation?.addEndListener(onEnd)
        return mDuskAnimation != null
    }

    /**
     * 得到 [CourseLayout] 外层包裹的 [CourseScrollView]
     *
     * 因为在长按选择事务时，滑到屏幕显示边缘区域时需要调用 [CourseScrollView] 进行滚动，
     * 所以只能采用这种强耦合的方式
     */
    val mCourseScrollView: CourseScrollView by lazy(LazyThreadSafetyMode.NONE) {
        var scrollView: CourseScrollView? = null
        var parent = parent
        while (parent is ViewGroup) {
            if (parent is CourseScrollView) {
                scrollView = parent
                break
            }
            parent = parent.parent
        }
        if (scrollView == null) throw RuntimeException(
            "CourseLayout 必须拥有 CourseScrollView 父布局，因为在一些情况下要调用它滚动"
        )
        scrollView
    }

    /**
     * 得到自身与 [mCourseScrollView] 之间相差的高度，是眼睛能看见的高度差
     * ```
     * 如：
     *                 |---------- CourseScrollView ----------|
     *                               |------------- CourseLayout -------------|
     *                 |-- 得到的值 --| (值为正)
     * or
     *                 |---------- CourseScrollView ----------|
     *   |------------- CourseLayout -------------|
     *   |-- 得到的值 --| (注意：此时值为负)
     * ```
     */
    fun getDiffHeightWithScrollView(): Int {
        var dHeight = top // 与 mCourseScrollView 去掉 scrollY 后的高度差，即屏幕上显示的高度差
        var parent = parent
        while (parent is ViewGroup) { // 这个循环用于计算 dHeight
            dHeight -= parent.scrollY
            if (parent === mCourseScrollView) { // 找到 mCourseScrollView 就结束
                break
            }
            dHeight += parent.top
            parent = parent.parent
        }
        return dHeight
    }

    private val mCourseAttrs: CourseLayoutAttrs

    // 自定义绘图的监听
    private val mCourseDecoration = ArrayList<CourseDecoration>(5)
    // 自定义事件处理的监听
    private val mCourseTouchListener = ArrayList<OnCourseTouchListener>(5)
    // 自定义事件处理中拦截的监听者
    private var mInterceptingOnTouchListener: OnCourseTouchListener? = null
    // 在 View 被摧毁时需要保存必要信息的监听
    private val mSaveBundleListeners = ArrayList<OnSaveBundleListener>(3)

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

    init {
        /*
        * 以下两个 for 循环有如下作用：
        * 1、设置初始时中午和傍晚时间段的比重为 0，为了让板块初始时刚好撑满整个能够显示的高度，
        *    使中午和傍晚在折叠的状态下，外面的 ScrollView 不用滚动就刚好能显示其余板块
        * */
        for (row in NOON_TOP..NOON_BOTTOM) {
            setRowInitialWeight(row, 0F)
        }
        for (row in DUSK_TOP..DUSK_BOTTOM) {
            setRowInitialWeight(row, 0F)
        }
        // 下面这个 for 用于设置时间轴的初始化宽度
        for (column in TIME_LINE_LEFT..TIME_LINE_RIGHT) {
            setColumnInitialWeight(column, 0.8F)
        }
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
                *    wrap_content，再调用 setRowInitialWeight() 来隐藏中午和傍晚时间段
                * 5、课表如果要显示中午和傍晚时间段，则外布局需要包裹一个 NestedScrollView，这时，父布局得到的
                *    测量模式为 UNSPECIFIED，该模式会使课表初始状态不再填充父布局，所以需要改为 EXACTLY 模式
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
            // 分配自定义事件处理的监听
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

    // 折叠动画
    private class FoldAnimation(
        onChanged: (Float) -> Unit
    ) : ChangeWeightAnimation(0.99999F, 0F, 200, onChanged)

    // 展开动画
    private class UnfoldAnimation(
        onChanged: (Float) -> Unit
    ) : ChangeWeightAnimation(0.00001F, 1F, 200, onChanged)

    // 比重改变的动画封装类
    private abstract class ChangeWeightAnimation(
        startWeight: Float,
        endWeight: Float,
        val time: Long,
        private val onChanged: (Float) -> Unit
    ) {
        private var animator: ValueAnimator = ValueAnimator.ofFloat(startWeight, endWeight)
        fun start(): ChangeWeightAnimation {
            animator.run {
                addUpdateListener { onChanged.invoke(animatedValue as Float) }
                duration = time
                this.start()
            }
            return this
        }
        fun addEndListener(onEnd: () -> Unit): ChangeWeightAnimation {
            animator.addListener(onEnd = { onEnd.invoke() })
            return this
        }

        fun cancel() {
            animator.cancel()
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        // 先恢复 mSaveBundleListeners 的状态
        for (i in state.saveBundleListeners.indices) {
            mSaveBundleListeners[i].onRestoreInstanceState(state.saveBundleListeners[i])
        }
        // 再恢复被摧毁时的折叠状态
        if (state.isFoldNoon) foldNoonWithoutAnim() else unfoldNoonWithoutAnim()
        if (state.isFoldDusk) foldDuskWithoutAnim() else unfoldDuskWithoutAnim()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        // 保存 mSaveBundleListeners 的状态
        ss.saveBundleListeners = Array(mSaveBundleListeners.size) {
            mSaveBundleListeners[it].onSaveInstanceState()
        }

        // 即将被摧毁，保存折叠状态
        when (getNoonRowState()) {
            RowState.FOLD, RowState.ANIM_FOLD -> ss.isFoldNoon = true
            RowState.UNFOLD, RowState.ANIM_UNFOLD -> ss.isFoldNoon = false
        }
        when (getDuskRowState()) {
            RowState.FOLD, RowState.ANIM_FOLD -> ss.isFoldDusk = true
            RowState.UNFOLD, RowState.ANIM_UNFOLD -> ss.isFoldDusk = false
        }
        return ss
    }

    /**
     * 用于在 [CourseLayout] 被摧毁时保存必要的信息
     */
    private class SavedState : BaseSavedState {
        lateinit var saveBundleListeners: Array<Bundle?> // 保存的 mSaveBundleListeners 的信息
        var isFoldNoon = true // 是否折叠了中午时间段
        var isFoldDusk = true // 是否折叠了傍晚时间段

        constructor(superState: Parcelable?) : super(superState)

        @SuppressLint("ParcelClassLoader")
        constructor(source: Parcel) : super(source) {
            val size = source.readInt() // 先读取之前设置的 mSaveBundleListeners 的数量
            saveBundleListeners = Array(size) {
                source.readBundle()
            }
            isFoldNoon = source.readInt() == 1
            isFoldDusk = source.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(saveBundleListeners.size) // 先写入设置的 mSaveBundleListeners 的数量
            saveBundleListeners.forEach {
                out.writeBundle(it)
            }
            out.writeInt(if (isFoldNoon) 1 else 0)
            out.writeInt(if (isFoldDusk) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        /*
        * 为什么要这样区分开始行和结束行？
        * 原因如下：
        * 1、方便以后好维护
        * 2、虽然目前中午和傍晚只有一行，但也不能保证以后不改为两行，所以中午和傍晚也得分为开始行和结束行
        * 3、课的话也是如此，但因为有了中午和傍晚，所以课与行数不对应
        * 尽量不要自己写数字来固定
        * */
        const val AM_TOP = 0 // 上午板块开始行
        const val AM_BOTTOM = 3 // 上午板块结束行
        const val NOON_TOP = 4 // 中午板块开始行
        const val NOON_BOTTOM = 4 // 中午板块结束行
        const val PM_TOP = 5 // 下午板块开始行
        const val PM_BOTTOM = 8 // 下午板块结束行
        const val DUSK_TOP = 9 // 傍晚板块开始行
        const val DUSK_BOTTOM = 9 // 傍晚板块结束行
        const val NIGHT_TOP = 10 // 晚上板块开始行
        const val NIGHT_BOTTOM = 13 // 晚上板块结束行

        const val TIME_LINE_LEFT = 0 // 时间轴开始列
        const val TIME_LINE_RIGHT = 0 // 时间轴结束列

        // 第一节课
        const val LESSON_1_TOP = 0
        const val LESSON_1_BOTTOM = 0
        // 第二节课
        const val LESSON_2_TOP = 1
        const val LESSON_2_BOTTOM = 1
        // 第三节课
        const val LESSON_3_TOP = 2
        const val LESSON_3_BOTTOM = 2
        // 第四节课
        const val LESSON_4_TOP = 3
        const val LESSON_4_BOTTOM = 3
        // 第五节课
        const val LESSON_5_TOP = 5
        const val LESSON_5_BOTTOM = 5
        // 第六节课
        const val LESSON_6_TOP = 6
        const val LESSON_6_BOTTOM = 6
        // 第七节课
        const val LESSON_7_TOP = 7
        const val LESSON_7_BOTTOM = 7
        // 第八节课
        const val LESSON_8_TOP = 8
        const val LESSON_8_BOTTOM = 8
        // 第九节课
        const val LESSON_9_TOP = 10
        const val LESSON_9_BOTTOM = 10
        // 第十节课
        const val LESSON_10_TOP = 11
        const val LESSON_10_BOTTOM = 11
        // 第十一节课
        const val LESSON_11_TOP = 12
        const val LESSON_11_BOTTOM = 12
        // 第十二节课
        const val LESSON_12_TOP = 13
        const val LESSON_12_BOTTOM = 13
    }
}