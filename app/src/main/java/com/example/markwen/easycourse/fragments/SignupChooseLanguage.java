package com.example.markwen.easycourse.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.markwen.easycourse.MainActivity;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.SignupLoginActivity;
import com.example.markwen.easycourse.components.SignupChooseLanguageAdapter;
import com.example.markwen.easycourse.models.Language;
import com.example.markwen.easycourse.models.UserSetup;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupChooseLanguage extends Fragment {

    private static final String TAG = "SignupChooseLanguage";

    RecyclerView languageRecyclerView;
    SignupChooseLanguageAdapter languageAdapter;
    LinearLayoutManager languageLayoutManager;

    Button nextButton;
    Button prevButton;

    ArrayList<Language> languageList;

    UserSetup userSetup;

    public SignupChooseLanguage() {}

    public static SignupChooseLanguage newInstance() {
        return new SignupChooseLanguage();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        languageList = new ArrayList<>();
        languageAdapter = new SignupChooseLanguageAdapter(languageList);

        fetchLanguages();

        userSetup = ((SignupLoginActivity) getActivity()).userSetup;

        fillLanguages();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.signup_choose_language, container, false);

        languageLayoutManager = new LinearLayoutManager(getContext());
        languageLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        languageRecyclerView = (RecyclerView) v.findViewById(R.id.choose_languages_recycler_view);
        languageRecyclerView.setLayoutManager(languageLayoutManager);
        languageRecyclerView.setAdapter(languageAdapter);

        nextButton = (Button) v.findViewById(R.id.buttonChooseLanguageNext);
        prevButton = (Button) v.findViewById(R.id.buttonChooseLanguagePrev);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] languageCodes = getLanguageCodes();
                if (languageCodes != null) {
                    userSetup.setLanguageCodeArray(languageCodes);
                    postSignupData(userSetup);
                } else {
                    Log.d(TAG, "No language selected!");
                    Snackbar.make(v, "No language selected!", Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackSignupChooseCourses();
            }
        });

        return v;
    }

    @Nullable
    private int[] getLanguageCodes() {
        ArrayList<Language> languages = languageAdapter.getLanguageList();
        ArrayList<Integer> checkedLanguages = new ArrayList<>();
        boolean oneChecked = false;
        for (Language language : languages) {
            if (language.isChecked()) {
                checkedLanguages.add(language.getCode());
                oneChecked = true;
            }
        }
        int[] languageCodes = new int[checkedLanguages.size()];

        for (int i = 0; i < checkedLanguages.size(); i++) {
            if (checkedLanguages.get(i) != null) {
                languageCodes[i] = checkedLanguages.get(i);
            }
        }
        if (oneChecked)
            return languageCodes;
        return null;
    }

    public void fetchLanguages() {

        APIFunctions.getLanguages(getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    languageList.clear();
                    Iterator languages = response.keys();
                    while (languages.hasNext()) {
                        String key = (String) languages.next();
                        JSONObject obj = response.getJSONObject(key);
                        String name = obj.getString("name");
                        int code = obj.getInt("code");
                        Language language = new Language(name, code);
                        languageList.add(language);
                    }
                    languageAdapter.notifyDataSetChanged();
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
        });
    }

    public void fillLanguages(){
        //TODO: Fill languages from signupUser
        int[] languages = userSetup.getLanguageCodeArray();
        if(languages == null) return;
        ArrayList<Language> languageArrayList = languageAdapter.getLanguageList();
        for (int i = 0; i < languages.length; i++) {
            for(Language language : languageArrayList) {
                if(language.getCode() == languages[i]){
                    language.setChecked(true);
                }
            }
        }
    }

    // Posts the signupData
    public void postSignupData(UserSetup userSetup) {
        try {
            //Post University
            APIFunctions.updateUser(getContext(), userSetup.getUniversityID(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Successfully posted university id");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    // Make a Snackbar to notify user with error
                    Log.d(TAG, "Failed to post university id");
                    return;
                }
            });

            APIFunctions.setCoursesAndLanguages(getContext(), userSetup.getLanguageCodeArray(), userSetup.getCourseCodeArray(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Successfully posted courses and languages");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    // Make a Snackbar to notify user with error
                    Log.d(TAG, "Failed to post courses and languages");
                    return;
                }
            });

            Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            getActivity().finish();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    // Call this function when going back to SignupChooseUniversity
    public void goBackSignupChooseCourses() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseCourses.newInstance());
        transaction.commit();
    }

}
