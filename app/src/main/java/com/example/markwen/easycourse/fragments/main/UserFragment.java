package com.example.markwen.easycourse.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.SignupLoginActivity;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.facebook.login.LoginManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class UserFragment extends Fragment {

    Button logoutButton;

    public UserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user, container, false);

        logoutButton = (Button) v.findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });

        return v;
    }

    private void logout(final View v) {

        APIFunctions.logout(getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                // Remove userToken and currentUser in SharedPreferences
                SharedPreferences sharedPref = getActivity().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userToken", null);
                editor.putString("currentUser", null);
                editor.apply();

                Log.i("Token after logout:", sharedPref.getString("userToken", "can't get token"));

                // Go back to SignupLoginActivity
                startActivity(new Intent(getContext(), SignupLoginActivity.class));
                // Logout from Facebook
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // Make a Snackbar to notify user with error
                Snackbar.make(v, "Log out failed because of " + res, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
