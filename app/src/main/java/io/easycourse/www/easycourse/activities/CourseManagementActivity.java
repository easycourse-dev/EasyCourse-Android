package io.easycourse.www.easycourse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
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

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.CourseManagement.CourseManagementCoursesRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.main.CourseManagement.CoursesEndlessRecyclerViewScrollListener;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by markw on 12/26/2016.
 */

public class CourseManagementActivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    String chosenUniversity;
    ArrayList<Course> joinedCourses = new ArrayList<>();
    ArrayList<Course> searchResults = new ArrayList<>();
    CourseManagementCoursesRecyclerViewAdapter coursesAdapter;
    CoursesEndlessRecyclerViewScrollListener coursesOnScrollListener;
    Handler handler;
    Runnable searchDelay;

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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CourseManagementActivity.this.onBackPressed();
            }
        });

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        realm = Realm.getDefaultInstance();
        handler = new Handler();

        // Initially hidden items
        noCourseText.setVisibility(View.GONE);

        // Get UniversityID
        chosenUniversity = EasyCourse.getAppInstance().getUniversityId(this);

        // Get already registered classes
        RealmResults<Course> enrolledCoursesRealmResults = realm.where(Course.class).findAll();
        for (int i = 0; i < enrolledCoursesRealmResults.size(); i++) {
            joinedCourses.add(enrolledCoursesRealmResults.get(i));
            searchResults.add(enrolledCoursesRealmResults.get(i));
        }

        // Clear button
        clearButton.setVisibility(View.GONE);
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
            public void afterTextChanged(final Editable editable) {
                if (editable.toString().equals("")) {
                    // Hide clear button
                    clearButton.setVisibility(View.GONE);
                    // Show already joined courses
                    searchResults.clear();
                    for (int i = 0; i < joinedCourses.size(); i++) {
                        searchResults.add(joinedCourses.get(i));
                    }
                    coursesAdapter.setJoinedCourses(joinedCourses);
                    coursesAdapter.notifyDataSetChanged();
                    coursesOnScrollListener.resetState();
                } else {
                    // Show clear button
                    clearButton.setVisibility(View.VISIBLE);
                    // Do search
                    handler.removeCallbacks(searchDelay);
                    searchDelay = new Runnable() {
                        @Override
                        public void run() {
                            APIFunctions.searchCourse(getApplicationContext(), editable.toString(), 20, 0, chosenUniversity, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                    searchResults.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        try {
                                            JSONObject course = (JSONObject) response.get(i);
                                            searchResults.add(new Course(
                                                    course.getString("_id"),
                                                    course.getString("name"),
                                                    course.getString("title"),
                                                    course.getString("description"),
                                                    course.getInt("creditHours"),
                                                    course.getJSONObject("university").getString("_id")
                                            ));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    coursesAdapter.notifyDataSetChanged();
                                    coursesOnScrollListener.resetState();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    Log.e("searchCourse", responseString, throwable);
                                    Snackbar.make(courseSearch, responseString, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    };
                    handler.postDelayed(searchDelay, 250);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        socketIO.syncUser();
    }

    public void loadMoreCourses(String searchQuery, String chosenUniversity, int skip, final RecyclerView view) {
        APIFunctions.searchCourse(getApplicationContext(), searchQuery, 20, skip, chosenUniversity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int startPosition = searchResults.size();
                try {
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
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("searchCourse", responseString);
                Snackbar.make(view, responseString, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
