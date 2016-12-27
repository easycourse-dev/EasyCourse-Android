package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Course;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by markw on 12/27/2016.
 */

public class CourseDetailsAcitivity extends AppCompatActivity {

    Course course;

    @BindView(R.id.CourseDetailsToolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);


        Intent courseManageIntent = getIntent();
        course = (Course) courseManageIntent.getSerializableExtra("CourseObj");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(course.getCoursename());
        }
    }
}
