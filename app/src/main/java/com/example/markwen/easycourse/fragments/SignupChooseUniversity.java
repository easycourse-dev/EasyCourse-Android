package com.example.markwen.easycourse.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.R;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupChooseUniversity extends Fragment {
    public SignupChooseUniversity() {}

    public static SignupChooseUniversity newInstance() {
        return new SignupChooseUniversity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.signup_choose_university, container, false);
    }



    // Call this function when going to SignupChooseCourses
    public void gotoSignupChooseCourses() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseCourses.newInstance());
        transaction.commit();
    }
}
