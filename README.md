[![](https://jitpack.io/v/985892345/CourseViewLibrary.svg)](https://jitpack.io/#985892345/CourseViewLibrary)


# CourseViewLibrary

 掌上重邮新课表

- [导入方法](#导入方法)
- [参考格式](#参考格式)
- [XML属性](#XML属性)
- [Public方法](#Public方法)
- [依赖关系](#依赖关系)

## 导入方法

### In Project gradle

```groovy
// In build.gradle
allprojects {
    repositories {
        // ......
        maven { url 'https://jitpack.io' }
    }
}
```

or 

```groovy
// In settings.gradle (由于 gradle 7.0.0 以上更改了位置)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // ......
        maven { url 'https://jitpack.io' }
    }
}
```

### In Module gradle 
[![](https://jitpack.io/v/985892345/CourseViewLibrary.svg)](https://jitpack.io/#985892345/CourseViewLibrary)

```groovy
dependencies {
    implementation 'com.github.985892345:CourseViewLibrary:xxx' // xxx请使用最新版本
}
```

## 参考格式

```xml
<com.mredrock.cyxbs.lib.courseview.CourseView
	android:layout_width="match_parent"
	android:layout_height="match_parent" />
```

## XML属性

> 还在补充中。。。

## Public方法

> 还在补充中。。。

| **return** | **method name**               |
| :--------: | :---------------------------- |
|    Unit    | [addMyLesson()](#addMyLesson) |
|            | 添加一节课                    |

### addMyLesson

```kotlin
fun addMyLesson()
```

添加一节课

## 依赖关系

> 补充中。。。
>
> 下面列出一些重要的类（类有些多，说实话，我看的话我都会被吓到）

- layout 布局
  - [NetLayout](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/NetLayout.md)
  - [CourseLayout](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/CourseLayout.md)
  - [LessonView](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/LessonView.md)
  - [CourseScrollView](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/CourseScrollView.md)
- touch 触摸事件
  - TouchDispatcher
  - OnItemTouchListener
  - AbstractMultiTouchDispatcher
  - MultiTouchDispatcherHelper
  - IPointerDispatcher
  - IPointerTouchHandler

- helper
  - [CourseTimelineHelper](https://github.com/985892345/CourseViewLibrary/blob/main/md/helper/CourseTimelineHelper.md)
  - CourseDownAnimHelper
  - CreateAffairPointerDispatcher
    - CreateAffairTouchHandler

  - EntityMovePointerDispatcher
    - AffairMoveTouchHandler
    - LessonMoveTouchHandler

  - FoldPointerDispatcher
    - FoldTouchHandler

  - ScrollTouchHandler


## 更新日志

- 1.0.0

  完成课表展示的基本功能：长按生成事务、长按移动、点击展开中午和傍晚时间段

- 1.1.0

  升级为多指触摸，之前的操作皆可多指实现，写了一个多指事件分发成单指事件的分发类

## 心得体会

[一些心得体会](https://github.com/985892345/CourseViewLibrary/blob/main/md/Some-Feelings.md)
