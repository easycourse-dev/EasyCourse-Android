package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.University;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by markw on 12/27/2016.
 */

public class CourseDetailsAcitivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    Course course;
    String courseId, courseName, title, courseDesc, universityId, universityName;
    int creditHrs = 0;
    boolean isJoined;

    @BindView(R.id.CourseDetailsToolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        realm = Realm.getDefaultInstance();

        Intent courseManageIntent = getIntent();
        courseId = courseManageIntent.getStringExtra("courseId");
        courseName = courseManageIntent.getStringExtra("courseName");
        title = courseManageIntent.getStringExtra("title");
        courseDesc = courseManageIntent.getStringExtra("courseDesc");
        universityId = courseManageIntent.getStringExtra("univId");
        creditHrs = courseManageIntent.getIntExtra("courseCred", 3);
        isJoined = courseManageIntent.getBooleanExtra("isJoined", false);

        course = new Course(courseId, courseName, title, courseDesc, creditHrs, universityId);
        if (universityId == null) {
            universityId = User.getCurrentUser(this, realm).getUniversityID();
        }
        universityName = University.getUniversityById(universityId, realm).getName();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(course.getCoursename());
        }
    }
}
