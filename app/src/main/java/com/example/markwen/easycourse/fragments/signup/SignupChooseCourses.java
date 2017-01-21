package com.example.markwen.easycourse.fragments.signup;


import android.content.Intent;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.MainActivity;
import com.example.markwen.easycourse.activities.SignupLoginActivity;
import com.example.markwen.easycourse.components.signup.EndlessRecyclerViewScrollListener;
import com.example.markwen.easycourse.components.signup.SignupChooseCoursesAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.models.signup.Course;
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

public class SignupChooseCourses extends Fragment {

    private static final String TAG = "SignupChooseCourses";

    RecyclerView courseRecyclerView;
    SignupChooseCoursesAdapter coursesAdapter;
    LinearLayoutManager coursesLayoutManager;
    EndlessRecyclerViewScrollListener coursesOnScrollListener;
    ArrayList<Course> courses = new ArrayList<>();
    Handler handler;
    Runnable searchDelay;
    MaterialDialog progress;

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
        socketIO = EasyCourse.getAppInstance().getSocketIO();

        // Signup progress dialog
        progress = new MaterialDialog.Builder(getContext())
                .title("Sign up")
                .content("Signing up...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .build();

        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.signup_choose_courses, container, false);

        final String chosenUniversity = userSetup.getUniversityID();

        final EditText searchCoursesEditText = (EditText) rootView.findViewById(R.id.edit_choose_courses);
        nextButton = (Button) rootView.findViewById(R.id.buttonChooseCoursesNext);
        prevButton = (Button) rootView.findViewById(R.id.buttonChooseCoursesPrev);
        clearEditTextButton = (Button) rootView.findViewById(R.id.buttonClearEditText);

        courses = userSetup.getSelectedCourses();
        coursesAdapter = new SignupChooseCoursesAdapter(courses);

        coursesLayoutManager = new LinearLayoutManager(getContext());
        coursesLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        courseRecyclerView = (RecyclerView) rootView.findViewById(R.id.choose_courses_recycler_view);
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
                            APIFunctions.searchCourse(getContext(), editable.toString(), 20, 0, chosenUniversity, new JsonHttpResponseHandler() {
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
//                    gotoSignupChooseLanguage(v);
                    progress.show();
                    userSetup.setLanguageCodeArray(new String[0]);
                    postSignupData(userSetup);
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

    public void updateRecyclerView() {
        Thread thread = new Thread() {
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
        APIFunctions.searchCourse(getContext(), searchQuery, 20, skip, chosenUniversity, new JsonHttpResponseHandler() {
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


    public void postSignupData(UserSetup userSetup) {
        try {
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

            progress.setContent("Sign up success");
            SocketIO socketIO = EasyCourse.getAppInstance().getSocketIO();

            socketIO.joinCourse(
                    stringArrayToArrayList(userSetup.getCourseCodeArray()),
                    new ArrayList<String>(),
                    new Ack() {
                        @Override
                        public void call(Object... args) {
                            JSONObject res = (JSONObject) args[0];
                            try {
                                JSONArray courseArrayJSON = res.getJSONArray("joinedCourse");
                                JSONArray roomArrayJSON = res.getJSONArray("joinedRoom");
                                JSONObject temp;
                                Room tempRoom;
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

                                    com.example.markwen.easycourse.models.main.Course course = new com.example.markwen.easycourse.models.main.Course(id, courseName, title, courseDescription, creditHours, universityID);
                                    com.example.markwen.easycourse.models.main.Course.updateCourseToRealm(course, realm);
                                }

                                // Rooms handling
                                for (int i = 0; i < roomArrayJSON.length(); i++) {
                                    temp = roomArrayJSON.getJSONObject(i);
                                    final String id = (String) checkIfJsonExists(temp, "_id", null);
                                    final String roomName = (String) checkIfJsonExists(temp, "name", null);
                                    final String courseID = (String) checkIfJsonExists(temp, "course", null);
                                    final String courseName = com.example.markwen.easycourse.models.main.Course.getCourseById(courseID, realm).getCoursename();
                                    final String universityID = (String) checkIfJsonExists(temp, "university", null);
                                    final boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                                    final int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                                    final String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                                    final String language = (String) checkIfJsonExists(temp, "language", "0");
                                    final boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                                    // Save user to Realm
                                    tempRoom = new Room(
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
                                            isSystem);
                                    tempRoom.setJoinIn(true);
                                    Room.updateRoomToRealm(tempRoom, realm);
                                }
                                progress.dismiss();
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

    // Function to go to MainActivity
    private void goToMainActivity() {
        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
        mainActivityIntent.putExtra("UserSetup", userSetup);
        startActivity(mainActivityIntent);
        getActivity().finish();
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
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseLanguage.newInstance());
        transaction.addToBackStack("SignupChooseCourses");
        transaction.commit();
    }

    // Call this function when going back to SignupChooseUniversity
    public void goBackSignupChooseUniversity() {
        saveToUserSetup();
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseUniversity.newInstance());
        transaction.commit();
    }
}