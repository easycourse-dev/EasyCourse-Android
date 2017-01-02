package io.easycourse.www.easycourse.fragments.signup;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.easycourse.www.easycourse.components.signup.EndlessRecyclerViewScrollListener;
import io.easycourse.www.easycourse.components.signup.SignupChooseCoursesAdapter;
import io.easycourse.www.easycourse.models.signup.Course;
import io.easycourse.www.easycourse.activities.SignupLoginActivity;
import io.easycourse.www.easycourse.models.signup.UserSetup;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.SocketIO;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupChooseCourses extends Fragment {

    private static final String TAG = "SignupChooseCourses";

    RecyclerView courseRecyclerView;
    SignupChooseCoursesAdapter coursesAdapter;
    LinearLayoutManager coursesLayoutManager;
    EndlessRecyclerViewScrollListener coursesOnScrollListener;
    ArrayList<Course> courses = new ArrayList<>();
    Handler handler;
    Runnable searchDelay;

    Button nextButton;
    Button prevButton;
    Button clearEditTextButton;

    UserSetup userSetup;

    SocketIO socketIO;

    public SignupChooseCourses() {
    }

    public static SignupChooseCourses newInstance() {
        return new SignupChooseCourses();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSetup = ((SignupLoginActivity) getActivity()).userSetup;
        try {
            socketIO = new SocketIO(getContext());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(io.easycourse.www.easycourse.R.layout.signup_choose_courses, container, false);

        final String chosenUniversity = userSetup.getUniversityID();

        final EditText searchCoursesEditText = (EditText) rootView.findViewById(io.easycourse.www.easycourse.R.id.edit_choose_courses);
        nextButton = (Button) rootView.findViewById(io.easycourse.www.easycourse.R.id.buttonChooseCoursesNext);
        prevButton = (Button) rootView.findViewById(io.easycourse.www.easycourse.R.id.buttonChooseCoursesPrev);
        clearEditTextButton = (Button)rootView.findViewById(io.easycourse.www.easycourse.R.id.buttonClearEditText);

        courses = userSetup.getSelectedCourses();
        coursesAdapter = new SignupChooseCoursesAdapter(courses);

        coursesLayoutManager = new LinearLayoutManager(getContext());
        coursesLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        courseRecyclerView = (RecyclerView) rootView.findViewById(io.easycourse.www.easycourse.R.id.choose_courses_recycler_view);
        courseRecyclerView.setLayoutManager(coursesLayoutManager);
        courseRecyclerView.setHasFixedSize(true);

        coursesOnScrollListener = new EndlessRecyclerViewScrollListener(coursesLayoutManager, coursesAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreCourses(searchCoursesEditText.getText().toString(), chosenUniversity, page, view);
            }
        };

        courseRecyclerView.addOnScrollListener(coursesOnScrollListener);
        courseRecyclerView.setAdapter(coursesAdapter);

        searchCoursesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (editable.toString().equals("")) {
                    courses.clear();
                    ArrayList<Course> checkedCourses = coursesAdapter.getCheckedCourseList();
                    for (int i = 0; i < checkedCourses.size(); i++) {
                        courses.add(checkedCourses.get(i));
                    }
                    coursesAdapter.notifyDataSetChanged();
                    coursesOnScrollListener.resetState();
                } else {
                    handler.removeCallbacks(searchDelay);
                    searchDelay = new Runnable() {
                        @Override
                        public void run() {
                            APIFunctions.searchCourse(getContext(), editable.toString(), 20, 0, chosenUniversity, new JsonHttpResponseHandler(){
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                    courses.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        try {
                                            JSONObject course = (JSONObject) response.get(i);
                                            courses.add(new Course(
                                                    course.getString("name"),
                                                    course.getString("title"),
                                                    course.getString("_id"),
                                                    chosenUniversity));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    coursesAdapter.notifyDataSetChanged();
                                    coursesOnScrollListener.resetState();
//                            updateRecyclerView();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    Log.e("searchCourse", responseString);
                                    Snackbar.make(searchCoursesEditText, responseString, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    };
                    handler.postDelayed(searchDelay, 250);
//                    try {
//                        socketIO.searchCourses(editable.toString(), 20, 0, chosenUniversity, new Ack() {
//
//                            @Override
//                            public void call(Object... args) {
//
//                                JSONObject obj = (JSONObject) args[0];
//                                if (!obj.has("error")) {
//                                    try {
//                                        JSONArray response = obj.getJSONArray("course");
//                                        courses.clear();
//                                        for (int i = 0; i < response.length(); i++) {
//                                            JSONObject course = (JSONObject) response.get(i);
//                                            courses.add(new Course(
//                                                    course.getString("name"),
//                                                    course.getString("title"),
//                                                    course.getString("_id"),
//                                                    chosenUniversity));
//                                        }
//                                        updateRecyclerView();
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                } else{
//                                    Log.e("com.easycourse.www", "failure" + obj.toString());
//                                }
//                            }
//                        });
//                    } catch (JSONException e) {
//                        Log.e("com.easycourse.www", "jsonex" + e.toString());
//                    }
                }
            }
        });

        if (prevButton != null)
            prevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveToUserSetup();
                    goBackSignupChooseUniversity();
                }
            });

        if (nextButton != null)
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveToUserSetup();
                    gotoSignupChooseLanguage(v);
                }
            });

        if (clearEditTextButton != null) {
            clearEditTextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchCoursesEditText.setText("");
                }
            });
        }


        return rootView;
    }

    public void updateRecyclerView(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    getActivity().runOnUiThread(new Runnable() {
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

    public void loadMoreCourses(String searchQuery, final String chosenUniversity, int skip, RecyclerView view) {
        APIFunctions.searchCourse(getContext(), searchQuery, 20, skip, chosenUniversity, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int startPosition = courses.size();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject courseJSON = (JSONObject) response.get(i);
                        Course courseObj = new Course(
                                courseJSON.getString("name"),
                                courseJSON.getString("title"),
                                courseJSON.getString("_id"),
                                chosenUniversity);
                        if (!courses.contains(courseObj))
                            courses.add(courseObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                coursesAdapter.notifyItemRangeInserted(startPosition, 20);
            }
        });
//        try {
//            socketIO.searchCourses(searchQuery, 20, skip, chosenUniversity, new Ack() {
//                @Override
//                public void call(Object... args) {
//                    JSONObject obj = (JSONObject) args[0];
//                    if (!obj.has("error")) {
//                        Log.e("com.easycourse.www", "success" + obj.toString());
//                        int startPosition = courses.size();
//                        try {
//                            JSONArray response = obj.getJSONArray("course");
//                            for (int i = 0; i < response.length(); i++) {
//                                JSONObject courseJSON = (JSONObject) response.get(i);
//                                Course courseObj = new Course(
//                                        courseJSON.getString("name"),
//                                        courseJSON.getString("title"),
//                                        courseJSON.getString("_id"),
//                                        chosenUniversity);
//                                if (!courses.contains(courseObj))
//                                    courses.add(courseObj);
//                            }
//                            coursesAdapter.notifyItemRangeInserted(startPosition, 20);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    } else{
//                        Log.e("com.easycourse.www", "failure" + obj.toString());
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    public void saveToUserSetup() {
        ArrayList<Course> checkedCourses = coursesAdapter.getCheckedCourseList();
        String[] courseStringList = new String[checkedCourses.size()];
        for (int i = 0; i < courseStringList.length; i++) {
            courseStringList[i] = checkedCourses.get(i).getId();
        }
        userSetup.setCourseCodeArray(courseStringList);
        userSetup.setSelectedCourses(checkedCourses);
    }


    // Call this function when going to SignupChooseCourses
    public void gotoSignupChooseLanguage(View v) {

        // Prevent user from continuing without enrolling into a course
        if (courses.size() == 0) {
            Snackbar.make(v, "Please enroll into a course", Snackbar.LENGTH_LONG).show();
            return;
        }

        saveToUserSetup();
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(io.easycourse.www.easycourse.R.anim.enter_from_right, io.easycourse.www.easycourse.R.anim.exit_to_left);
        transaction.replace(io.easycourse.www.easycourse.R.id.activity_signuplogin_container, SignupChooseLanguage.newInstance());
        transaction.addToBackStack("SignupChooseCourses");
        transaction.commit();
    }

    // Call this function when going back to SignupChooseUniversity
    public void goBackSignupChooseUniversity() {
        saveToUserSetup();
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(io.easycourse.www.easycourse.R.anim.enter_from_left, io.easycourse.www.easycourse.R.anim.exit_to_right);
        transaction.replace(io.easycourse.www.easycourse.R.id.activity_signuplogin_container, SignupChooseUniversity.newInstance());
        transaction.commit();
    }
}