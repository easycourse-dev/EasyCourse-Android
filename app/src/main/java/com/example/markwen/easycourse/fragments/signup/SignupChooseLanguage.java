package com.example.markwen.easycourse.fragments.signup;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.MainActivity;
import com.example.markwen.easycourse.activities.SignupLoginActivity;
import com.example.markwen.easycourse.components.signup.SignupChooseLanguageAdapter;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.models.signup.Language;
import com.example.markwen.easycourse.models.signup.UserSetup;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.SocketIO;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Ack;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;
import static com.example.markwen.easycourse.utils.ListsUtils.stringArrayToArrayList;

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

    public SignupChooseLanguage() {
    }

    public static SignupChooseLanguage newInstance() {
        return new SignupChooseLanguage();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets screen to portrait
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        languageList = new ArrayList<>();
        languageAdapter = new SignupChooseLanguageAdapter(languageList);

        fetchLanguages();

        userSetup = ((SignupLoginActivity) getActivity()).userSetup;
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
        languageRecyclerView.setHasFixedSize(true);

        nextButton = (Button) v.findViewById(R.id.buttonChooseLanguageNext);
        prevButton = (Button) v.findViewById(R.id.buttonChooseLanguagePrev);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] languageCodes = getLanguageCodes();
                if (languageCodes == null) {
                    userSetup.setLanguageCodeArray(new String[0]);
                } else {
                    userSetup.setLanguageCodeArray(languageCodes);
                }
                postSignupData(userSetup);
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
    private String[] getLanguageCodes() {
        ArrayList<Language> checkedLanguages = userSetup.getSelectedLanguages();

        if (checkedLanguages.size() == 0)
            return null;

        String[] languageCodes = new String[checkedLanguages.size()];
        for (int i = 0; i < checkedLanguages.size(); i++) {
            if (checkedLanguages.get(i) != null) {
                languageCodes[i] = checkedLanguages.get(i).getCode();
            }
        }
        return languageCodes;
    }

    private void fetchLanguages() {
        final Realm tempRealm = Realm.getDefaultInstance();

        APIFunctions.getLanguages(getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    languageList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = (JSONObject) response.get(i);
                        String name = obj.getString("name");
                        String code = obj.getString("code");
                        String translation = obj.getString("translation");
                        Language language = new Language(name, code, translation);
                        languageList.add(language);
                        com.example.markwen.easycourse.models.main.Language.updateLanguageToRealm(
                                new com.example.markwen.easycourse.models.main.Language(
                                        name, code, translation
                                ), tempRealm
                        );
                    }
                    tempRealm.close();
                    ArrayList<Language> selectedLanguages = userSetup.getSelectedLanguages();
                    languageAdapter.setCheckedLanguageList(selectedLanguages);
                    for (int i = 0; i < languageList.size(); i++) {
                        for (int j = 0; j < selectedLanguages.size(); j++) {
                            if (selectedLanguages.get(j).getCode().equals(languageList.get(i).getCode())) {
                                languageList.get(i).setChecked(true);
                            }
                        }
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

    // Posts the signupData
    public void postSignupData(UserSetup userSetup) {
        try {
            // Saving languages
            ArrayList<Language> chosenLanguages = languageAdapter.getCheckedLanguageList();
            Realm tempRealm = Realm.getDefaultInstance();
            tempRealm.beginTransaction();
            for (int i = 0; i < chosenLanguages.size(); i++) {
                com.example.markwen.easycourse.models.main.Language.getLanguageByCode(chosenLanguages.get(i).getCode(), tempRealm).setChecked(true);
            }
            tempRealm.commitTransaction();
            tempRealm.close();
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
                }
            });

            final SocketIO socketIO = EasyCourse.getAppInstance().getSocketIO();

            // Language handling
            ArrayList<String> languageCodes = stringArrayToArrayList(userSetup.getLanguageCodeArray());
            for (int i = 0; i < languageCodes.size(); i++) {
                com.example.markwen.easycourse.models.main.Language.getLanguageByCode(languageCodes.get(i), tempRealm).setChecked(true, tempRealm);
            }

            socketIO.joinCourse(
                    stringArrayToArrayList(userSetup.getCourseCodeArray()),
                    languageCodes,
                    new Ack() {
                        @Override
                        public void call(Object... args) {
                            JSONObject res = (JSONObject) args[0];
                            try {
                                JSONArray courseArrayJSON = res.getJSONArray("joinedCourse");
                                JSONArray roomArrayJSON = res.getJSONArray("joinedRoom");
                                JSONObject temp;
                                Realm realm = Realm.getDefaultInstance();

                                // Courses handling
                                for (int i = 0; i < courseArrayJSON.length(); i++) {
                                    temp = courseArrayJSON.getJSONObject(i);
                                    String id = (String) checkIfJsonExists(temp, "_id", null);
                                    String courseName = (String) checkIfJsonExists(temp, "name", null);
                                    String title = (String) checkIfJsonExists(temp, "title", null);
                                    String courseDescription = (String) checkIfJsonExists(temp, "description", null);
                                    int creditHours = Integer.parseInt((String) checkIfJsonExists(temp, "creditHours", "0"));
                                    String universityID = (String) checkIfJsonExists(temp, "university", null);

                                    Course course = new Course(id, courseName, title, courseDescription, creditHours, universityID);
                                    Course.updateCourseToRealm(course, realm);
                                }

                                // Rooms handling
                                for (int i = 0; i < roomArrayJSON.length(); i++) {
                                    temp = roomArrayJSON.getJSONObject(i);
                                    final String id = (String) checkIfJsonExists(temp, "_id", null);
                                    final String roomName = (String) checkIfJsonExists(temp, "name", null);
                                    final String courseID = (String) checkIfJsonExists(temp, "course", null);
                                    final String courseName = Course.getCourseById(courseID, realm).getCoursename();
                                    final String universityID = (String) checkIfJsonExists(temp, "university", null);
                                    final boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                                    final int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                                    final String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                                    final String language = (String) checkIfJsonExists(temp, "language", "0");
                                    final boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                                    // Save user to Realm
                                    Room.updateRoomToRealm(
                                            new Room(
                                                    id,
                                                    roomName,
                                                    new RealmList<Message>(),
                                                    courseID,
                                                    courseName,
                                                    universityID,
                                                    new RealmList<User>(),
                                                    memberCounts,
                                                    memberCountsDesc,
                                                    new User(),
                                                    language,
                                                    isPublic,
                                                    isSystem), realm
                                    );
                                }
                                goToMainActivity();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void saveToUserSetup() {
        ArrayList<Language> checkedLanguages = languageAdapter.getCheckedLanguageList();
        String[] languageStringList = new String[checkedLanguages.size()];
        for (int i = 0; i < languageStringList.length; i++) {
            languageStringList[i] = checkedLanguages.get(i).getCode();
        }
        userSetup.setLanguageCodeArray(languageStringList);
        userSetup.setSelectedLanguages(checkedLanguages);
    }

    // Function to go to MainActivity
    private void goToMainActivity() {
        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
        mainActivityIntent.putExtra("UserSetup", userSetup);
        startActivity(mainActivityIntent);
        getActivity().finish();
    }


    // Call this function when going back to SignupChooseUniversity
    public void goBackSignupChooseCourses() {
        saveToUserSetup();
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseCourses.newInstance());
        transaction.commit();
    }
}
