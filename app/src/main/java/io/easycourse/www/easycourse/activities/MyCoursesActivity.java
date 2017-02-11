package io.easycourse.www.easycourse.activities;

import android.os.Bundle;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.CourseManagementFragment;

/**
 * Created by noahrinehart on 2/11/17.
 */

public class MyCoursesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_courses);

        goToMyCourses();
    }

    private void goToMyCourses() {
        CourseManagementFragment fragment = new CourseManagementFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.myCoursesContent, fragment)
                .addToBackStack("myCourses")
                .commit();
    }
}
