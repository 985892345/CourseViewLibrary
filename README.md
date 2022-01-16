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
> - 第二个：与第一个相隔了一段时间，主要是相解决一些因水平问题导致的View设计问题，但因时间问题，没有写几天就没写了
> - 第三个：在22年寒假左右开工，在意识到第一个的问题后开始有所改进，主要是想解决第一个的缺点三
>   - 设计思路：为了更好的扩展性，想设计一个更通用的View，开始的想法是设计一个网状的ViewGroup，这样课表就只是它其中一种布局形式，但因为对于以后的扩展想得过多（每行和每列都想设计成可变的宽度、相对布局），而且刚开始没有很好的构思，最终放弃
> - 第四个：就是目前仓库这个
>   - 设计思路：在前三个的基础上，打算重新写一个，还是以第三次的设计为准，写一个网状的Viewgroup，但得提前想好整体的构思，每行和每列都为固定值，且以绝对布局来设计
>
> 时间：2022年1月16日
