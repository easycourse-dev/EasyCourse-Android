package com.example.markwen.easycourse.fragments;

import android.os.Bundle;
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
import android.widget.EditText;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.Course;
import com.example.markwen.easycourse.components.EndlessRecyclerViewScrollListener;
import com.example.markwen.easycourse.components.SignupChooseCoursesAdapter;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupChooseCourses extends Fragment {
    private RecyclerView mRecyclerView;
    private SignupChooseCoursesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private EndlessRecyclerViewScrollListener mOnScrollListener;
    private ArrayList<Course> courses = new ArrayList<>();

    public SignupChooseCourses() {}

    public static SignupChooseCourses newInstance() {
        return new SignupChooseCourses();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAdapter = new SignupChooseCoursesAdapter(courses);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.signup_choose_courses, container, false);

        final EditText searchCoursesEditText = (EditText)rootView.findViewById(R.id.edit_choose_courses);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.choose_courses_recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mOnScrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreCourses(searchCoursesEditText.getText().toString(), page, view);
            }
        };

        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setAdapter(mAdapter);

        searchCoursesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int pageOffset = 0;
                Log.e("com.example.easycourse",editable.toString());
                APIFunctions.searchCourse(rootView.getContext(),editable.toString(), 20, 0, "57e2cb6854ad620011c82db4", new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.e("com.example.easycourse","success "+response.toString());
                        try {
                            courses.clear();
                            for(int i = 0; i < response.length(); i++) {
                                JSONObject course = (JSONObject) response.get(i);
                                courses.add(new Course(course.getString("name"), course.getString("title")));
                            }
                            mAdapter.notifyDataSetChanged();
                            mOnScrollListener.resetState();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        Log.e("com.example.easycourse","failure"+t.toString());
                    }
                });
            }
        });

        return rootView;
    }

    public void loadMoreCourses(String searchQuery, int skip, RecyclerView view){
        APIFunctions.searchCourse(view.getContext(), searchQuery, 20, skip, "57e2cb6854ad620011c82db4", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.e("com.example.easycourse","success "+response.toString());
                int startPosition = courses.size();
                try {
                    //TODO: Animate when loading more courses
                    for(int i = 0; i < response.length(); i++) {
                        JSONObject courseJSON = (JSONObject) response.get(i);
                        Course courseObj = new Course(courseJSON.getString("name"), courseJSON.getString("title"));
                        if(!courses.contains(courseObj))
                            courses.add(courseObj);
                    }
                    mAdapter.notifyItemRangeInserted(startPosition, 20);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Log.e("com.example.easycourse","failure"+t.toString());
            }
        });
    }

    // Call this function when going to SignupChooseCourses
    public void gotoSignupChooseLanguage() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseLanguage.newInstance());
        transaction.addToBackStack("SignupChooseCourses");
        transaction.commit();
    }

    // Call this function when going back to SignupChooseUniversity
    public void goBackSignupChooseUniversity() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseUniversity.newInstance());
        transaction.commit();
    }
}
