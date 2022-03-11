# TouchDispatcher

> 自定义事件分发者，参考 RV 的 `ItemTouchListener` 设计

## 该类作用

> 将 ViewGroup 的事件分发到其他帮助类中，便于扩展功能

## 使用方式

```kotlin
class MyViewGroup(
    context: Context, 
    attrs: AttributeSet?
) : ViewGroup(context, attrs) {
    
    // 自定义事件分发帮助类
    private val mTouchDispatchHelper = TouchDispatcher<MyViewGroup>()
    
    /**
     * @param l 想要添加的监听
     * @param index 想要添加到的位置，前面的监听可以拦截后面的监听
     */
    fun addItemTouchListener(l: OnItemTouchListener<MyViewGroup>, index: Int = mTouchDispatchHelper.size) {
        mTouchDispatchHelper.addCourseTouchListener(l, index)
    }
    
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mTouchDispatchHelper.dispatchTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mTouchDispatchHelper.onInterceptTouchEvent(ev, this)
    }

    // 报黄是因为点击事件被覆盖了，但一般不需要给它设置官方的点击事件吧
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mTouchDispatchHelper.onTouchEvent(event, this)
    }
}
```

## 重要变量

```kotlin
// 自定义事件处理中拦截的监听者
private var mInterceptingOnTouchListener: OnItemTouchListener<T>? = null

// 自定义事件处理中提前拦截的监听者
private var mAdvanceInterceptingOnTouchListener: OnItemTouchListener<T>? = null
```

> **mInterceptingOnTouchListener**
>
> > 自定义事件处理中拦截的监听者
> >
> > 作用：保存 `DOWN` 时 `OnItemTouchListener#isIntercept(MotionEvent, Boolean)` 返回 `true` 时的 `OnItemTouchListener` 监听
> >
> > 条件：
> >
> > - 只有 `DOWN` 时就有机会
> > - 无提前拦截的 `OnItemTouchListener`，即 `mAdvanceInterceptingOnTouchListener` 为空
> > - 子 `View` 不拦截事件，即子 `View` 不拦截并把事件向上传递
>
> **mAdvanceInterceptingOnTouchListener**
>
> > 自定义事件处理中提前拦截的监听者
> >
> > 作用：保存任意事件中 `OnItemTouchListener#isAdvanceIntercept(MotionEvent, Boolean)` 返回 `true` 时的 `OnItemTouchListener` 监听
> >
> > 条件：
> >
> > - 一般为 `DOWN`、`POINTER_DOWN`、`MOVE` 中拦截事件
> > - 可以提前拦截子 `View` 事件和顺序在后面的 `OnItemTouchListener`
> > - 即使 `mInterceptingOnTouchListener` 不为 `null`，也能拦截它的事件

## 重要方法

