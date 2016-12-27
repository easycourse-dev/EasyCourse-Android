package com.example.markwen.easycourse.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.CourseManagementCoursesRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.utils.SocketIO;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by markw on 12/26/2016.
 */

public class CourseManagementAcitivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    ArrayList<Course> enrolledCourses;
    ArrayList<Course> searchResults = new ArrayList<>();
    CourseManagementCoursesRecyclerViewAdapter coursesAdapter;

    @BindView(R.id.CourseManageToolbar)
    Toolbar toolbar;
    @BindView(R.id.CourseManageSearchCoursesEditText)
    EditText courseSearch;
    @BindView(R.id.CourseManageRecyclerView)
    RecyclerView coursesView;
    @BindView(R.id.CourseNanagementNoCourseTextView)
    TextView noCourseText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_management);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setTitle("My Courses");

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();
        realm = Realm.getDefaultInstance();

        // Initially hidden items
        noCourseText.setVisibility(View.GONE);

        // Get already registered classes
        RealmResults<Course> enrolledCoursesRealmResults = realm.where(Course.class).findAll();
        for (int i = 0; i < enrolledCoursesRealmResults.size(); i++) {
            enrolledCourses.add(enrolledCoursesRealmResults.get(i));
        }

        // Set up recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coursesAdapter = new CourseManagementCoursesRecyclerViewAdapter(searchResults);
        coursesAdapter.setJoinedCourses(enrolledCourses);
        coursesView.setHasFixedSize(true);
        coursesView.setLayoutManager(layoutManager);
        coursesView.setAdapter(coursesAdapter);

        // On search
        courseSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
