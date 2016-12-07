package com.example.markwen.easycourse.fragments.signup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.SignupLoginActivity;
import com.example.markwen.easycourse.components.signup.SignupChooseUniversityAdapter;
import com.example.markwen.easycourse.models.signup.University;
import com.example.markwen.easycourse.models.signup.UserSetup;
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

public class SignupChooseUniversity extends Fragment {

    private static final String TAG = "SignupChooseUniversity";

    RecyclerView uniRecyclerView;
    SignupChooseUniversityAdapter uniAdapter;
    LinearLayoutManager uniLayoutManager;

    Button nextButton;

    ArrayList<University> uniList;

    UserSetup userSetup;

    public SignupChooseUniversity() {
    }

    public static SignupChooseUniversity newInstance() {
        return new SignupChooseUniversity();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uniList = new ArrayList<>();
        uniAdapter = new SignupChooseUniversityAdapter(uniList);

        fetchUniversities();

        userSetup = ((SignupLoginActivity) getActivity()).userSetup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.signup_choose_university, container, false);

        uniLayoutManager = new LinearLayoutManager(getContext());
        uniLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        uniRecyclerView = (RecyclerView) v.findViewById(R.id.choose_university_recycler_view);
        uniRecyclerView.setLayoutManager(uniLayoutManager);
        uniRecyclerView.setAdapter(uniAdapter);

        nextButton = (Button) v.findViewById(R.id.buttonChooseUniversityNext);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String universityId = getUniversityId();
                if (universityId != null && userSetup != null) {
                    userSetup.setUniversityID(universityId);
                    gotoSignupChooseCourses();
                } else {
                    Log.d(TAG, "No language selected!");
                    Snackbar.make(v, "No university selected!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // Disable back button
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        return v;
    }

    @Nullable
    private String getUniversityId() {
        ArrayList<University> universities = uniAdapter.getUniversityList();
        String universityId = null;
        for (University university : universities) {
            if (university.isSelected()) {
                universityId = university.getId();
            }
        }
        return universityId;
    }

    private void fetchUniversities() {
        APIFunctions.getUniversities(getContext(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            uniList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonobject = response.getJSONObject(i);
                                String id = jsonobject.getString("_id");
                                String name = jsonobject.getString("name");
                                University university = new University(id, name);
                                uniList.add(university);
                            }
                            uniAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        Log.e(TAG, "status failure " + statusCode);
                        Log.e(TAG, res);
                    }
                }

        );
    }


    // Call this function when going to SignupChooseCourses
    public void gotoSignupChooseCourses() {
        String unId = userSetup.getUniversityID();
        if (unId != null)
            Log.d(TAG, "Passing: " + unId);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseCourses.newInstance());
        transaction.addToBackStack("SignupChooseUniversity");
        transaction.commit();
    }

}
