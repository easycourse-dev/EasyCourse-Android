package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.CourseDetailsFragment;
import io.easycourse.www.easycourse.fragments.main.CourseManagementFragment;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.models.main.University;

/**
 * Created by noahrinehart on 2/11/17.
 */

public class MyCoursesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_courses);


        Intent intent = getIntent();
        if (intent != null) {
            String courseId = intent.getStringExtra("courseId");
            boolean isJoined = intent.getBooleanExtra("isJoined", false);
            CourseDetailsFragment fragment = CourseDetailsFragment.newInstance(courseId, isJoined);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.myCoursesContent, fragment)
                    .commit();
        } else {
            goToMyCourses();
        }

    }

    private void goToMyCourses() {
        CourseManagementFragment fragment = new CourseManagementFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.myCoursesContent, fragment)
                .commit();
    }
}
