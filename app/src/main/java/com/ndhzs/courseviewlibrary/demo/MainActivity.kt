package com.ndhzs.courseviewlibrary.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mredrock.cyxbs.lib.courseview.course.CourseLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_course)

        val courseLayout: CourseLayout = findViewById(R.id.course)

        courseLayout.setRowInitialWeight(4, 0F)
        courseLayout.setRowInitialWeight(9, 0F)

        val view: View = findViewById(R.id.view1)

        var i = 0
        view.setOnClickListener {
            if (i % 2 == 0) {
                courseLayout.setRowWeight(4, 1F)
                courseLayout.setRowWeight(9, 1F)
            } else {
                courseLayout.setRowWeight(4, 0F)
                courseLayout.setRowWeight(9, 0F)
            }
            i++
        }
    }
}