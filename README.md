[![](https://jitpack.io/v/985892345/CourseViewLibrary.svg)](https://jitpack.io/#985892345/CourseViewLibrary)


# CourseViewLibrary

 掌上重邮新课表

- [导入方法](#导入方法)
- [参考格式](#参考格式)
- [XML属性](#XML属性)
- [Public方法](#Public方法)
- [依赖关系](#依赖关系)

## 导入方法

### In Project build

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

### In Module build 
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
|    Unit    | [addMyCourse()](#addMyCourse) |
|            | 添加一节课                    |

### addMyCourse

```kotlin
fun addMyCourse()
```

添加一节课

## 依赖关系

> 补充中。。。

- layout
  - [NetLayout](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/NetLayout.md)
  - [CourseLayout](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/CourseLayout.md)
  - [LessonView](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/LessonView.md)
  - [CourseScrollView](https://github.com/985892345/CourseViewLibrary/blob/main/md/layout/CourseScrollView.md)
- helper
  - [CourseTimelineHelper](https://github.com/985892345/CourseViewLibrary/blob/main/md/helper/CourseTimelineHelper.md)
  - [CourseCreateAffairHelper](https://github.com/985892345/CourseViewLibrary/blob/main/md/helper/CourseCreateAffairHelper.md)
  - [CourseFoldHelper](https://github.com/985892345/CourseViewLibrary/blob/main/md/helper/CourseFoldHelper.md)
  - [CourseLongPressAffairHelper](https://github.com/985892345/CourseViewLibrary/blob/main/md/helper/CourseLongPressAffairHelper.md)
  - [CourseLongPressEntityMoveHelper](https://github.com/985892345/CourseViewLibrary/blob/main/md/helper/CourseLongPressEntityMoveHelper.md)



## 心得体会

[一些心得体会](https://github.com/985892345/CourseViewLibrary/blob/main/md/Some-Feelings.md)
