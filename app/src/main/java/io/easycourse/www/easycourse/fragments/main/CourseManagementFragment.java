package io.easycourse.www.easycourse.fragments.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;


public class CourseManagementFragment extends BaseFragment {

    String chosenUniversity;
    ArrayList<Course> joinedCourses = new ArrayList<>();
    ArrayList<Course> searchResults = new ArrayList<>();
    CourseManagementCoursesRecyclerViewAdapter coursesAdapter;
    CoursesEndlessRecyclerViewScrollListener coursesOnScrollListener;
    Handler handler;
    Runnable searchDelay;


    @BindView(R.id.courseManagerToolbar)
    Toolbar toolbar;
    @BindView(R.id.courseManagerSearchCoursesEditText)
    EditText courseSearch;
    @BindView(R.id.courseManagerButtonClearEditText)
    Button clearButton;
    @BindView(R.id.courseManagerNoCourseTextView)
    TextView noCourseText;
    @BindView(R.id.courseManagerRecyclerView)
    RecyclerView recyclerView;

    public CourseManagementFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_management, container, false);
        ButterKnife.bind(this, v);
        setupToolbar();


        handler = new Handler();

        // Initially hidden items
        noCourseText.setVisibility(View.GONE);

        // Clear button
        clearButton.setVisibility(View.GONE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseSearch.setText("");
            }
        });


        // Get UniversityID
        chosenUniversity = EasyCourse.getAppInstance().getUniversityId(getContext());

        fetchCourses();

        setupCourseRecyclerView();
        setupTextListener();

        return v;
    }


    private void setupToolbar() {
        toolbar.setTitle("My Courses");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupCourseRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coursesAdapter = new CourseManagementCoursesRecyclerViewAdapter(getContext(), searchResults);
        coursesAdapter.setJoinedCourses(joinedCourses);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(coursesAdapter);
        recyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));
        coursesOnScrollListener = new CoursesEndlessRecyclerViewScrollListener(layoutManager, coursesAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreCourses(courseSearch.getText().toString(), chosenUniversity, page, view);
            }
        };
        recyclerView.addOnScrollListener(coursesOnScrollListener);
    }

    private void setupTextListener() {
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
                            APIFunctions.searchCourse(getContext(), editable.toString(), 20, 0, chosenUniversity, new JsonHttpResponseHandler() {
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

    private void fetchCourses() {
        RealmResults<Course> enrolledCoursesRealm = realm.where(Course.class).findAll();
        joinedCourses.clear();
        searchResults.clear();
        for (Course course : enrolledCoursesRealm) {
            joinedCourses.add(course);
            searchResults.add(course);
        }
        if (coursesAdapter != null)
            coursesAdapter.notifyDataSetChanged();
    }

    public void loadMoreCourses(String searchQuery, String chosenUniversity, int skip, final RecyclerView view) {
        APIFunctions.searchCourse(getContext(), searchQuery, 20, skip, chosenUniversity, new JsonHttpResponseHandler() {
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


    @Override
    public void onResume() {
        super.onResume();
        fetchCourses();
        if (coursesAdapter != null)
            coursesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null)
            realm.close();
    }
}