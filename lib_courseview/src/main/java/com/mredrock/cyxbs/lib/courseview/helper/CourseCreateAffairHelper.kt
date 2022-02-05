package com.mredrock.cyxbs.lib.courseview.helper

import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.mredrock.cyxbs.lib.courseview.R
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.DUSK_TOP
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_BOTTOM
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout.Companion.NOON_TOP
import com.mredrock.cyxbs.lib.courseview.course.attrs.CourseLayoutParams
import com.mredrock.cyxbs.lib.courseview.course.utils.OnCourseTouchListener
import com.mredrock.cyxbs.lib.courseview.course.utils.OnSaveBundleListener
import com.mredrock.cyxbs.lib.courseview.course.utils.RowState
import com.mredrock.cyxbs.lib.courseview.scroll.CourseScrollView
import com.mredrock.cyxbs.lib.courseview.utils.CourseType
import com.mredrock.cyxbs.lib.courseview.utils.Vibrator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * ```
 * 该类作用：
 * 1、封装长按生成自定义行程的功能
 *
 * 该类设计：
 * 1、对 CourseLayout 增加事件监听来实现
 * 2、事件监听参考了 RV 的 ItemTouchHelper 的设计
 * 3、手机转屏后仍能恢复之前长按显示的事务（虽然目前掌邮不会用到这个功能）
 *
 * 注意事项：
 * 1、该类只管理创建事务，请不要添加一些不属于该类的功能，想添加功能应该再写一个 OnCourseTouchListener
 * ```
 * @author 985892345 (Guo Xiangrui)
 * @email 2767465918@qq.com
 * @date 2022/1/27
 */
class CourseCreateAffairHelper private constructor(
    private val course: CourseLayout
) : OnCourseTouchListener, OnSaveBundleListener {

    /**
     * 设置触摸空白区域生成的用于添加事务的 View 的点击监听
     */
    fun setTouchAffairViewClickListener(l: View.OnClickListener) {
        mTouchAffairView.setOnClickListener(l)
    }

    /**
     * 替代当前触摸生成的用于添加事务的 View，位置大小都会不变
     */
    fun replaceTouchAffairView(view: View) {
        if (mTouchAffairView.parent != null) {
            val lp = (mTouchAffairView.layoutParams as CourseLayoutParams).clone()
            course.addCourse(view, lp)
            course.removeView(mTouchAffairView)
        }
    }

    /**
     * 设置触摸空白区域生成的用于添加事务的 View 的事件监听
     */
    fun setTouchAffairListener(l: OnTouchAffairListener) {
        mOnTouchAffairListener = l
    }

    private var mInitialX = 0 // Down 时的初始 X 值
    private var mInitialY = 0 // Down 时的初始 Y 值
    private var mLastMoveX = 0 // Move 时的移动 X 值
    private var mLastMoveY = 0 // Move 时的移动 Y 值

    // 认定是滚动的最小移动值，其中 ScrollView 拦截事件就与该值有关，不建议修改该值
    private var mTouchSlop = ViewConfiguration.get(course.context).scaledTouchSlop

    // 识别为长按所需的时间
    private var mLongPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    // 是否处于长按状态
    private var mIsInLongPress = false

    // 长按时执行的 Runnable
    private val mLongPressRunnable = Runnable {
        mIsInLongPress = true
        showTouchAffairView()
        Vibrator.start(course.context, 36) // 长按被触发来个震动提醒
    }

    private var mTopRow = 0 // Move 事件中选择区域的开始行数
    private var mBottomRow = 0 // Move 事件中选择区域的结束行数
    private var mInitialRow = 0 // Down 事件中触摸的初始行数
    private var mInitialColumn = 0 // Down 事件中触摸的初始列数

    private var mTouchRow = 0 // 当前触摸的行数
    private var mUpperRow = 0 // 选择区域的上限
    private var mLowerRow = course.getRowCount() - 1 // 选择区域的下限

    private var mOnTouchAffairListener: OnTouchAffairListener?= null

    init {
        // 给 CourseLayout 设置触摸监听
        course.addCourseTouchListener(this)
        // 设置 course 被摧毁重建的监听，主要是保存一些必要信息
        course.addSaveBundleListener(this)
    }

    /**
     * 用于添加事务的 View，注意：并不是最终显示事务的那个 View，该 View 只是在长按后生成的那个灰色的 View
     * ```
     * 这里直接使用 ImageView 的原因：
     * 1、显示更方便，主要设置图片、背景这些，比直接在 canvas 上画图更简便
     * 2、计算更方便，直接修改该 View 的 layoutParams 即可
     * 3、可以更好的设置点击监听
     * 缺点：
     * 1、稍稍耗性能，每次修改 layoutParams 都会重新布局，比不上在 onDraw() 里面绘图（但绘图需要计算，比较麻烦）
     * ```
     */
    private val mTouchAffairView by lazy(LazyThreadSafetyMode.NONE) {
        val radius = course.context.resources.getDimension(R.dimen.course_course_item_radius)
        ImageView(course.context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = GradientDrawable().apply {
                // 设置圆角
                cornerRadii =
                    floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
                // 背景颜色
                setColor(ContextCompat.getColor(course.context, R.color.course_add_affair_color))
            }
            // 设置 ImageView 的前景图片
            setImageResource(R.drawable.course_ic_add_circle_white)
            layoutParams = CourseLayoutParams(0,0, 0, CourseType.AFFAIR_TOUCH)
        }
    }

    /**
     * 显示 mTouchAffairView（用于添加事务的 View）
     * @author 985892345
     * @date 2022/1/31 18:07
     */
    private fun showTouchAffairView() {
        val lp = mTouchAffairView.layoutParams as CourseLayoutParams
        // 回调监听
        mOnTouchAffairListener?.onCreate(mTouchAffairView, lp)
        // 添加 mTouchAffairView
        course.addCourse(
            mTouchAffairView,
            lp.apply {
                startRow = mTopRow
                endRow = mBottomRow
                startColumn = mInitialColumn
                endColumn = mInitialColumn
            }
        )
    }

    /**
     * 你可能会比较疑惑为什么在事件分发中对事件拦截进行提前判断? 或者为什么我要重写这个方法?
     * ```
     * 原因如下：
     * 1、onDispatchTouchEvent() 早于 isIntercept() 调用，所以提前判断是否拦截不会出现问题
     * 2、主要原因在于何时取消 mTouchAffairView 的显示，需要知道当前点击的是否是这个 mTouchAffairView，
     *       如果不是就 remove 掉
     *    而却会出现事件被 CourseLayout 的子 View 拦截的情况，出现这种情况时，isIntercept() 是不会被调用的，
     *    所以就没有办法去 remove 掉 mTouchAffairView
     * 3、接 2，为了将 mTouchAffairView 的 remove 封装起来，就只能将拦截提前判断
     * 缺点：
     * 1、在一些不必要的情况下（比如子 View 会拦截时）也进行了判断的处理，但为了更好的封装性，这点消耗还是能接受的
     * ```
     */
    override fun onDispatchTouchEvent(event: MotionEvent, course: CourseLayout) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mIsInIntercepting = false
            val x = event.x.toInt()
            val y = event.y.toInt()
            val touchView = course.findItemUnderByXY(x, y)
            /*
            * 这里 touchView = null 时一定会将事件给 CourseLayout#onTouchEvent() 处理，
            * 之后事件就会分发到每个 OnCourseTouchListener 中，
            * 如果事件能够传递到该 listener，则会直接交给自身的 onTouchEvent() 处理
            *
            * 但存在顺序在前面的 OnCourseTouchListener 提前拦截事件，
            * 所以需要重写 onCancelDownEvent() 方法取消 mLongPressRunnable
            * */
            if (touchView == null) {
                mIsInIntercepting = true

                mInitialX = x
                mInitialY = y
                mLastMoveX = x
                mLastMoveY = y
                mInitialRow = course.getRow(y)
                mInitialColumn = course.getColumn(x)
                mTopRow = mInitialRow
                mBottomRow = mInitialRow

                mUpperRow = 0 // 重置
                mLowerRow = course.getRowCount() - 1 // 重置

                calculateUpperLowerRow() // 计算上下限

                mIsInLongPress = false // 重置
                course.postDelayed(mLongPressRunnable, mLongPressTimeout)
                // 禁止外面的 ScrollView 拦截事件
                course.parent.requestDisallowInterceptTouchEvent(true)
            }
            if (touchView !== mTouchAffairView) {
                // 此时说明点击的是其他地方，不是 mTouchAffairView，则 remove 掉 mTouchAffairView
                if (mTouchAffairView.parent != null) {
                    course.removeView(mTouchAffairView)
                }
            }
        }
    }

    /**
     * 计算 [mUpperRow] 和 [mLowerRow]
     */
    private fun calculateUpperLowerRow() {
        for (i in 0 until course.childCount) {
            val child = course.getChildAt(i)
            val lp = child.layoutParams as CourseLayoutParams
            if (mInitialColumn in lp.startColumn..lp.endColumn) {
                if (lp.endRow < mInitialRow) {
                    mUpperRow = max(mUpperRow, lp.endRow + 1)
                } else if (lp.startRow > mInitialRow) {
                    mLowerRow = min(mLowerRow, lp.startRow - 1)
                }
            }
        }
    }

    private var mIsInIntercepting = false // 是否处于拦截中

    override fun isIntercept(event: MotionEvent, course: CourseLayout): Boolean {
        return mIsInIntercepting
    }

    override fun onTouchEvent(event: MotionEvent, course: CourseLayout) {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (mIsInLongPress) { // 处于长按状态
                    mLastMoveX = x
                    mLastMoveY = y

                    if (!mScrollRunnable.isInScrolling) {
                        /*
                        * 如果 isInScrolling = true，则 changeTouchAffairView()
                        * 该方法应交由 mScrollRunnable 调用，而不是让 onTouchEvent() 调用
                        * 原因如下：
                        * 1、避免在同一帧时与 mScrollRunnable 重复调用
                        * 2、存在手指正触摸让 ScrollView 的滚动区，但却因为手指没有移动而不回调 onTouchEvent()
                        *    这种情况下就得让 mScrollRunnable 加上 ScrollView 将偏移的值来
                        *    调用 changeTouchAffairView()，不然就会出现滚轴滚动，
                        *    但 AffairView 没有一起移动（横屏时尤其明显）
                        * */
                        changeTouchAffairView(y)
                    }
                    scrollIsNecessary(y)
                } else {
                    if (abs(x - mLastMoveX) <= mTouchSlop
                        && abs(y - mLastMoveY) <= mTouchSlop
                    ) {
                        /*
                        * 走到该分支说明：
                        * 1、不处于长按状态，即 mIsInLongPress = false
                        * 2、移动距离小于认定是滑动的 mTouchSlop
                        *
                        * 下一次移动有下面三条路可以走
                        * 1、可能仍然回到这里
                        * 2、如果移动距离大于 mTouchSlop，则会走下面那个 else 分支
                        * 3、如果 mIsInLongPress 被赋值为 true（说明达到长按时间）
                        * */
                    } else {
                        // 走到该分支说明在判定为长按的时间内，移动的距离大于了 mTouchSlop
                        // 接下来的一系列事件就不该自身处理，应该被 CourseScrollView 当成滚动而拦截
                        course.parent.requestDisallowInterceptTouchEvent(false)
                        course.removeCallbacks(mLongPressRunnable)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mScrollRunnable.cancel()
                course.removeCallbacks(mLongPressRunnable)
                if (abs(x - mLastMoveX) <= mTouchSlop
                    && abs(y - mLastMoveY) <= mTouchSlop
                ) {
                    // 这里说明移动的距离小于 mTouchSlop，但还是得把点击的事务给绘制上，但是只有一格
                    if (mTouchAffairView.parent == null) { // 防止之前已经被添加
                        showTouchAffairView()
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mScrollRunnable.cancel()
                course.removeCallbacks(mLongPressRunnable)
            }
        }
    }

    override fun onCancelDownEvent(course: CourseLayout) {
        // 因为是在 onDispatchTouchEvent 开启的 Runnable，
        // 存在被顺序在前面的 OnCourseTouchListener 提前拦截事件，所以需要在这里 remove 掉
        course.removeCallbacks(mLongPressRunnable)
    }

    /**
     * 该方法作用：
     *
     * 1、根据 [y] 值来计算当前 mTouchAffairView 的位置并刷新布局
     */
    private fun changeTouchAffairView(y: Int) {
        val touchView = course.findItemUnderByXY(mInitialX, y)
        if (touchView == null || touchView === mTouchAffairView) {
            mTouchRow = course.getRow(y) // 当前触摸的行数
            var topRow: Int
            var bottomRow: Int
            // 根据当前触摸的行数与初始行数比较，得到 topRow、bottomRow
            if (mTouchRow > mInitialRow) {
                topRow = mInitialRow
                bottomRow = mTouchRow
            } else {
                topRow = mTouchRow
                bottomRow = mInitialRow
            }
            if (topRow < mUpperRow) topRow = mUpperRow // 根据上限再次修正 topRow
            if (bottomRow > mLowerRow) bottomRow = mLowerRow // 根据下限再次修正 bottomRow
            if (topRow != mTopRow || bottomRow != mBottomRow) { // 避免不必要的刷新
                mTopRow = topRow
                mBottomRow = bottomRow
                val lp = mTouchAffairView.layoutParams as CourseLayoutParams
                lp.startRow = topRow
                lp.endRow = bottomRow
                mTouchAffairView.layoutParams = lp // 设置属性，刷新布局
            }
            // 判断是否展开中午或者傍晚时间段（在滑过中午或者傍晚时需要将他们自动展开）
            unfoldNoonOrDuskIfNecessary()
        }
    }

    /**
     * 如果你手指滑到屏幕显示边缘区域时，则可能需要调用 [CourseScrollView] 滚动
     * ```
     * 该方法作用：
     * 1、计算当前触摸位置与 ScrollView 的距离来判断是否需要让 ScrollView 滚动
     * 2、计算让 ScrollView 滚动的速度
     * ```
     */
    private fun scrollIsNecessary(y: Int) {
        mScrollRunnable.startIfCan(y)
    }

    // 滑到显示区域顶部或者底部时，使 mCourseScrollView 滚动的 Runnable
    private val mScrollRunnable = object : Runnable {

        var isInScrolling = false // 是否处于滚动状态
            private set

        private var velocity = 0 // 滚动的速度

        private var touchY = 0 // 当前手指触摸的 Y 值

        override fun run() {
            if (isAllowScrollAndCalculateVelocity()) {
                val startScrollY = course.mCourseScrollView.scrollY
                course.mCourseScrollView.scrollBy(0, velocity)
                val endScrollY = course.mCourseScrollView.scrollY
                // 调用 scrollBy 后不一定会滑动你设置的值
                // 所有需要得到滚动前和滚动后的 scrollY 来算偏移量
                touchY += endScrollY - startScrollY
                changeTouchAffairView(touchY) // 防止手指不移动而出现选择区域不变的情况
                ViewCompat.postOnAnimation(course, this)
            } else {
                isInScrolling = false
            }
        }

        /**
         * 如果能开启滚动的话就开启滚动，注意：滚动是会自己取消的
         */
        fun startIfCan(y: Int) {
            touchY = y
            if (!isInScrolling) { // 防止重复允许 Runnable
                isInScrolling = true
                run()
            }
        }

        /**
         * 取消滚动
         */
        fun cancel() {
            if (isInScrolling) {
                isInScrolling = false
                course.removeCallbacks(this)
            }
        }

        /**
         * 是否允许滚动，如果允许，则计算滚动速度给 [velocity] 变量
         */
        private fun isAllowScrollAndCalculateVelocity(): Boolean {
            val scroll = course.mCourseScrollView
            val nowHeight = touchY + course.getDiffHeightWithScrollView()
            val moveBoundary = 100 // 移动的边界值
            // 向上滚动，即手指移到底部，需要显示下面的内容
            val isNeedScrollUp =
                nowHeight > scroll.height - moveBoundary
                        && mTouchRow < mLowerRow // 当前触摸的行数在下限以上
                        && scroll.height + scroll.scrollY != scroll.getChildAt(0).height // 是否滑到底

            // 向下滚动，即手指移到顶部，需要显示上面的内容
            val isNeedScrollDown =
                nowHeight < moveBoundary
                        && mTouchRow > mUpperRow // 当前触摸的行数在上限以下
                        && scroll.scrollY != 0 // 是否滑到顶
            val isAllowScroll = isNeedScrollUp || isNeedScrollDown
            if (isAllowScroll) {
                velocity = if (isNeedScrollUp) {
                    // 速度最小为 6，最大为 20，与边界的差值成线性关系
                    min((nowHeight - (scroll.height - moveBoundary)) / 10 + 6, 20)
                } else {
                    // 速度最小为 6，最大为 20，与边界的差值成线性关系
                    -min(((moveBoundary - nowHeight) / 10 + 6), 20)
                }
            }
            return isAllowScroll
        }
    }

    /**
     * 判断当前滑动中是否需要自动展开中午或者傍晚时间段
     */
    private fun unfoldNoonOrDuskIfNecessary() {
        if (mTopRow <= NOON_TOP && mBottomRow >= NOON_BOTTOM) {
            if (course.getNoonRowState() == RowState.FOLD) {
                course.unfoldNoon()
            }
        }
        if (mTopRow <= DUSK_TOP && mBottomRow >= DUSK_BOTTOM) {
            if (course.getDuskRowState() == RowState.FOLD) {
                course.unfoldDusk()
            }
        }
    }

    override fun onSaveInstanceState(): Bundle? {
        // mTouchAffairView 没有被添加时不用保存信息
        if (mTouchAffairView.parent == null) return null
        // mTouchAffairView 被添加时保存 layoutParams
        return Bundle().also {
            it.putSerializable(
                this::mTouchAffairView.name,
                mTouchAffairView.layoutParams as CourseLayoutParams
            )
        }
    }

    override fun onRestoreInstanceState(bundle: Bundle?) {
        if (bundle == null) return
        // 恢复之前保存的 layoutParams
        val lp = bundle.getSerializable(this::mTouchAffairView.name) as CourseLayoutParams
        course.addCourse(mTouchAffairView, lp)
        // 从竖屏到横屏时，显示的 mTouchAffairView 可能会在屏幕显示区域外，所以需要调整 ScrollView 的 scrollY，
        // 让 mTouchAffairView 刚好显示在屏幕内
        // 但回调 onRestoreInstanceState() 时还没有开始布局，得不到 top 值，所以需要用 post 在布局后设置
        course.post {
            course.mCourseScrollView.scrollY =
                mTouchAffairView.top + course.getDiffHeightWithScrollView() - 60
        }
    }

    companion object {
        /**
         * 换成一个静态方法来 attach 到 CourseLayout，
         * 感觉似乎没有必要，但这样写更能让以后维护的人能看懂这个类是用来干嘛的
         *
         * attach 有连接、依附的意思，比直接给构造器传入形参相比，更能看出该类对于 [CourseLayout] 的侵入性
         */
        fun attach(course: CourseLayout): CourseCreateAffairHelper {
            return CourseCreateAffairHelper(course)
        }
    }

    interface OnTouchAffairListener {
        /**
         * 创建 TouchAffairView 的监听
         *
         * 在 addView 之前回调，可以直接设置一些属性
         */
        fun onCreate(view: ImageView, lp: CourseLayoutParams)
    }
}