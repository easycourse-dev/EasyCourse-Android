package com.example.markwen.easycourse.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.CourseManagement.CourseManagementCoursesRecyclerViewAdapter;
import com.example.markwen.easycourse.components.main.CourseManagement.CoursesEndlessRecyclerViewScrollListener;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.socket.client.Ack;

/**
 * Created by markw on 12/26/2016.
 */

public class CourseManagementAcitivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    String chosenUniversity;
    ArrayList<Course> joinedCourses = new ArrayList<>();
    ArrayList<Course> searchResults = new ArrayList<>();
    CourseManagementCoursesRecyclerViewAdapter coursesAdapter;
    CoursesEndlessRecyclerViewScrollListener coursesOnScrollListener;

    @BindView(R.id.CourseManageToolbar)
    Toolbar toolbar;
    @BindView(R.id.CourseManageSearchCoursesEditText)
    EditText courseSearch;
    @BindView(R.id.CourseManageRecyclerView)
    RecyclerView coursesView;
    @BindView(R.id.CourseNanagementNoCourseTextView)
    TextView noCourseText;
    @BindView(R.id.CourseManageButtonClearEditText)
    Button clearButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_management);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("My Courses");
        }

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();
        realm = Realm.getDefaultInstance();

        // Initially hidden items
        noCourseText.setVisibility(View.GONE);

        // Get UniversityID
        chosenUniversity = User.getCurrentUser(this, realm).getUniversityID();
        if (chosenUniversity == null) {
            // Temporary placeholder with Purdue University ID
            chosenUniversity = "586085386edf9b0011913a9c";
        }

        // Get already registered classes
        RealmResults<Course> enrolledCoursesRealmResults = realm.where(Course.class).findAll();
        for (int i = 0; i < enrolledCoursesRealmResults.size(); i++) {
            joinedCourses.add(enrolledCoursesRealmResults.get(i));
            searchResults.add(enrolledCoursesRealmResults.get(i));
        }

        // Clear button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseSearch.setText("");
            }
        });

        // Set up recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coursesAdapter = new CourseManagementCoursesRecyclerViewAdapter(this, searchResults);
        coursesAdapter.setJoinedCourses(joinedCourses);
        coursesView.setHasFixedSize(true);
        coursesView.setLayoutManager(layoutManager);
        coursesView.setAdapter(coursesAdapter);
        coursesView.addItemDecoration(new RecyclerViewDivider(this));
        coursesOnScrollListener = new CoursesEndlessRecyclerViewScrollListener(layoutManager, coursesAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreCourses(courseSearch.getText().toString(), chosenUniversity, page, view);
            }
        };
        coursesView.addOnScrollListener(coursesOnScrollListener);

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
                if (editable.toString().equals("")) {
                    searchResults.clear();
                    for (int i = 0; i < joinedCourses.size(); i++) {
                        searchResults.add(joinedCourses.get(i));
                    }
                    coursesAdapter.setJoinedCourses(joinedCourses);
                    coursesAdapter.notifyDataSetChanged();
                    coursesOnScrollListener.resetState();
                } else {
                    try {
                        socketIO = new SocketIO(getApplicationContext());
                        socketIO.searchCourses(editable.toString(), 20, 0, chosenUniversity, new Ack() {

                            @Override
                            public void call(Object... args) {

                                JSONObject obj = (JSONObject) args[0];
                                if (!obj.has("error")) {
                                    try {
                                        JSONArray response = obj.getJSONArray("course");
                                        searchResults.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject course = (JSONObject) response.get(i);
                                            searchResults.add(new Course(course.getString("name"), course.getString("title"), course.getString("_id")));
                                        }
                                        updateRecyclerView();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else{
                                    Log.e("com.example.easycourse", "failure" + obj.toString());
                                }
                            }
                        });
                    } catch (URISyntaxException | JSONException e) {
                        Log.e("emit searchCourse", e.toString());
                    }
                }
            }
        });
    }

    public void loadMoreCourses(String searchQuery, String chosenUniversity, int skip, RecyclerView view) {
        try {
            socketIO.searchCourses(searchQuery, 20, skip, chosenUniversity, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    if (!obj.has("error")) {
                        int startPosition = searchResults.size();
                        try {
                            JSONArray response = obj.getJSONArray("course");
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject courseJSON = (JSONObject) response.get(i);
                                Course courseObj = new Course(courseJSON.getString("name"), courseJSON.getString("title"), courseJSON.getString("_id"));
                                if (!searchResults.contains(courseObj))
                                    searchResults.add(courseObj);
                            }
                            coursesAdapter.notifyItemRangeInserted(startPosition, 20);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else{
                        Log.e("com.example.easycourse", "failure" + obj.toString());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateRecyclerView(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            coursesAdapter.notifyDataSetChanged();
                            coursesOnScrollListener.resetState();
                        }
                    });
                }
            }
        };
        thread.start();
    }
}
