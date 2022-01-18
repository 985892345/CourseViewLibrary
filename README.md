# CourseViewLibrary
 掌上重邮新课表

> 该仓库是从21年暑假接到课表任务后第四回新建仓库写课表，之前写了3个半成品，感觉都不符合心意，决定这是最后一个了，
> 再这样下去怕是什么都写不出😂
>
> ### 简单总结一下之前写的
>
> - 第一个：从21年暑假接到任务时开始写起，写了应该有两个星期左右，是写出来的3个中写得最多的一个，基本实现了课表的简单展示功能
>   - 设计思路：把一个课表进行细分成几个ViewGroup的组合，通过一个自定义的Adapter来实现添加课程
>   - 缺点：
>     - 由于水平问题，有些View设计的不够完善，部分地方写得很死
>     - ViewGroup嵌套过多，以后有人接手谁也不想看这么多层的View嵌套（嵌套了4层）
>     - 课程的布局有些写死了，如果以后出现长按移动课程这些扩展就很麻烦
> - 第二个：与第一个相隔了一段时间，主要是想解决一些因水平问题导致的View设计问题，但因时间问题，没有写几天就没写了
> - 第三个：在22年寒假左右开工，在意识到第一个的问题后开始有所改进，主要是想解决第一个的缺点三
>   - 设计思路：为了更好的扩展性，想设计一个更通用的View，开始的想法是设计一个网状的ViewGroup，这样课表就只是它其中一种布局形式，但因为对于以后的扩展想得过多（每行和每列都想设计成可变的宽度、相对布局），而且刚开始没有很好的构思，最终放弃
> - 第四个：就是目前仓库这个
>   - 设计思路：在前三个的基础上，打算重新写一个，还是以第三次的设计为准，写一个网状的Viewgroup，但得提前想好整体的构思，每行和每列都为固定值，且以绝对布局来设计（继续往下看，最后还是抛弃了设计网状的想法）
>
> 时间：2022年1月16日



> 我快被自定义View整疯了😵
>
> ### 方案总结
>
> 一共有三个方案：
>
> - 多层嵌套
>   - 优点：设计简单、每个方块都是一个View，方便扩展新功能
>   - 缺点：嵌套过多
> - 自定义一个网状的ViewGroup
>   - 优点：可减少嵌套
>   - 缺点：设计复杂（不能光为了写一个课表而设计）、扩展相较于View比较麻烦
> - 自定义rv的LayoutManger
>   - 优点：rv的很多功能都能使用，而且只需要写一个LayoutManger即可
>   - 缺点：rv自定义LayoutManger的话坑很多，还有一些不需要的功能（如：Recycler）
>
> 现在动手写就是想着这样设计没那方案好，那样设计没这方案好，各有各的优点，始终决定不了该使用什么方案 
>
> 时间：2022年1月17日



> 经过长时间的思考过后，还是决定使用方案一，在尽可能的情况下减少不必要的嵌套，但为了扩展性，每个 ViewGroup 我会尽量设置成可单独使用的一个 ViewGroup
>
> ### 选用方案一原因：
>
> - 时间问题：方案二和三都需要花很多时间
> - 效率问题：方案二和三需要考虑的情况有些多，做出来的成果可能不行，bug 应该会很多
> - 难度问题：方案一更易于设计
> - 扩展问题：方案一更利于扩展
>
> 截止目前 2022年1月17日晚22:00，已经设计出了一个用于单天显示课程的 ViewGroup（CourseDayLayout，目前已改为 NetLayout），支持单独拿出来使用
>
> 时间：2022年1月17日22:01:49



> 又经过一夜的思考，突然发现这个 CourseDayLayout 只要稍微改改就可以变成之前想设计的网状 ViewGroup，🤣。所以在设计一方案的时候把三方案给写出来了
>
> 之前写不出来应该是自身能力不够，因为在测量时要思考的情况有点多，而昨天（17号）在细读了 FrameLayout 的测量过程后，直接就写出来了只有单方向摆放的 CourseDayLayout，而在思路清晰后发现只需稍稍改下就能实现双方向，这不就是之前我要设计的网状 ViewGroup（NetLayout）吗。。。。。。所以阅读官方源码真的很有必要！！！
>
> **因此，最后又回到方案三，还是以网状 ViewGroup（NetLayout）来设计课表**
>
> 时间：2022年1月18日10:39:54
