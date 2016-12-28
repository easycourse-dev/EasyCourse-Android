package com.example.markwen.easycourse.fragments.signup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.MainActivity;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.SocketIO;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmList;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;

/**
 * Created by noahrinehart on 10/29/16.
 */

public class SignupLogin extends Fragment {

    private static final String TAG = "SignupLogin";


    @BindView(R.id.textViewTitle)
    TextView titleTextView;
    @BindView(R.id.editTextEmail)
    EditText emailEditText;
    @BindView(R.id.editTextPassword)
    EditText passwordEditText;
    @BindView(R.id.editTextVerifyPassword)
    EditText verifyPasswordEditText;
    @BindView(R.id.editTextUsername)
    EditText usernameEditText;

    @BindView(R.id.inputLayoutEmail)
    TextInputLayout emailInputLayout;
    @BindView(R.id.inputLayoutPassword)
    TextInputLayout passwordInputLayout;
    @BindView(R.id.inputLayoutVerifyPassword)
    TextInputLayout verifyPasswordInputLayout;
    @BindView(R.id.inputLayoutUsername)
    TextInputLayout usernameInputLayout;


    @BindView(R.id.buttonSignup)
    Button signupButton;
    @BindView(R.id.buttonLogin)
    Button loginButton;
    @BindView(R.id.fbThemeButton)
    Button facebookThemeButton;
    @BindView(R.id.buttonFacebookLogin)
    LoginButton facebookButton;

    CallbackManager callbackManager;
    LinearLayout signupLinearLayout;
    SharedPreferences sharedPref;


    Animation titleAnimEnter;
    Animation emailAnimEnter;
    Animation passwordAnimEnter;
    Animation verifyPasswordAnimEnter;
    Animation usernameAnimEnter;
    Animation loginAnimEnter;
    Animation signupAnimEnter;
    Animation facebookAnimEnter;

    Realm realm;

    User currentUser;

    public SignupLogin() {
    }


    //http://stackoverflow.com/questions/9245408/best-practice-for-instantiating-a-new-android-fragment
    public static SignupLogin newInstance() {
        return new SignupLogin();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppEventsLogger.activateApp(getActivity().getApplication());
        realm = Realm.getDefaultInstance();
        currentUser = new User();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.signup_login, container, false);

        ButterKnife.bind(this, v);


        facebookButton.setFragment(this);
        facebookButton.setReadPermissions("email");
        facebookButton.setReadPermissions(Arrays.asList("user_status"));
        callbackManager = CallbackManager.Factory.create();
        signupLinearLayout = (LinearLayout) v.findViewById(R.id.linearLayoutSignup);


        // Set username and verify passwords initially gone
        verifyPasswordInputLayout.setVisibility(View.GONE);
        usernameInputLayout.setVisibility(View.GONE);

        // Changing EditText background colors
        emailEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        passwordEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        verifyPasswordEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        usernameEditText.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        // Change EditText text color
        emailEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));
        passwordEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));
        verifyPasswordEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));
        usernameEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteText));

        // Add textWatchers to EditTexts's
        emailEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(emailEditText));
        passwordEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(passwordEditText));
        verifyPasswordEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(verifyPasswordEditText));
        usernameEditText.addTextChangedListener(new SignupLogin.SignupLoginTextWatcher(usernameEditText));


        // Animations for views when activity starts/resumes
        titleAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        emailAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        emailAnimEnter.setStartOffset(250);
        passwordAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        passwordAnimEnter.setStartOffset(250);
        verifyPasswordAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        verifyPasswordAnimEnter.setStartOffset(250);
        usernameAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        usernameAnimEnter.setStartOffset(250);
        loginAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        loginAnimEnter.setStartOffset(250 * 2);
        signupAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        signupAnimEnter.setStartOffset(250 * 2);
        facebookAnimEnter = AnimationUtils.loadAnimation(getContext(), R.anim.fade_move_in);
        facebookAnimEnter.setStartOffset(250 * 2);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(signupButton.getWindowToken(), 0);
                signup(v);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(loginButton.getWindowToken(), 0);
                emailLogin(v);
            }
        });

        // Connect facebookThemeButton with facebookButton
        facebookThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                facebookButton.performClick();
                facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    final Snackbar fbLoginErrorSnackbar = Snackbar
                            .make(v, "Facebook log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);
                    final Snackbar fbLoginCancelSnackbar = Snackbar
                            .make(v, "Facebook log in cancelled.", Snackbar.LENGTH_LONG);

                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        APIFunctions.facebookLogin(getContext(), loginResult.getAccessToken().getToken(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                parseLoginResponse(statusCode, headers, response);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // Make a Snackbar to notify user with error
                                fbLoginErrorSnackbar.show();
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        fbLoginCancelSnackbar.show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        // Make a Snackbar to notify user with error
                        fbLoginErrorSnackbar.show();
                    }
                });
            }
        });


        startAnimations();

        return v;
    }


    public void signup(View v) {

        // Brings in verify password and username edittexts if not visible
        if (verifyPasswordInputLayout.getVisibility() == View.GONE) {
            verifyPasswordInputLayout.setVisibility(View.VISIBLE);
            usernameInputLayout.setVisibility(View.VISIBLE);
            signupButton.setBackgroundResource(R.drawable.login_button);
            loginButton.setBackgroundResource(R.drawable.signup_button);

        } else { // Edittexts are shown, do logic
            Log.d(TAG, "Signup clicked-doing signup");
            // Get inputs and check if fields are empty
            // only execute login API when fields are all filled

            try {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String verifyPassword = verifyPasswordEditText.getText().toString();
                String username = usernameEditText.getText().toString();

                if (!validateEmail())
                    return;
                if (!validatePassword())
                    return;
                if (!validateVerifyPassword())
                    return;
                if (!validateUsername())
                    return;


                // make API call only if the 2 passwords are the same
                // make a Toast and don't do anything if they don't match
                if (!password.equals(verifyPassword)) {
                    Snackbar passwordMismatchSnackbar = Snackbar
                            .make(v, "Passwords don't match, please try again.", Snackbar.LENGTH_LONG);
                    passwordMismatchSnackbar.show();
                    passwordEditText.setText("");
                    verifyPasswordEditText.setText("");
                } else {
                    final Snackbar signupErrorSnackbar = Snackbar
                            .make(v, "UserFragment could not be created, check if the email is already registered.", Snackbar.LENGTH_LONG);
                    APIFunctions.signUp(getContext(), email, password, username, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            String userToken = "";

                            //for each header in array Headers scan for Auth header
                            for (Header header : headers) {
                                if (header.toString().contains("Auth"))
                                    userToken = header.toString().substring(header.toString().indexOf(":") + 2);
                            }

                            sharedPref = getActivity().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userToken", userToken);
                            editor.putString("currentUser", response.toString());
                            editor.apply();

                            try {
                                currentUser.setId(response.getString("_id"));
                                currentUser.setEmail(response.getString("email"));
                                currentUser.setUsername(response.getString("displayName"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            realm.beginTransaction();
                            realm.copyToRealmOrUpdate(currentUser);
                            realm.commitTransaction();

                            gotoSignupChooseCourses();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.e("com.example.easycourse", "status failure " + statusCode);
                            Log.e("com.example.easycourse", res);
                            signupErrorSnackbar.show();
                        }
                    });
                }
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("com.example.easycourse", e.toString());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void emailLogin(final View v) {

        // Removes verify password and username edittexts if visible
        if (verifyPasswordInputLayout.getVisibility() == View.VISIBLE) {
            verifyPasswordInputLayout.setVisibility(View.GONE);
            usernameInputLayout.setVisibility(View.GONE);
            signupButton.setBackgroundResource(R.drawable.signup_button);
            loginButton.setBackgroundResource(R.drawable.login_button);

        } else { // Edittexts are hidden, do logic
            Log.d(TAG, "Login clicked-doing login");
            // Get inputs and check if fields are empty
            // only execute login API when fields are all filled
            try {
                String email = emailEditText.getText().toString();
                String pwd = passwordEditText.getText().toString();

                if (!validateEmail())
                    return;
                if (!validatePassword())
                    return;

                final Snackbar loginErrorSnackbar = Snackbar
                        .make(v, "Log in failed, please check your credentials and network connection.", Snackbar.LENGTH_LONG);

                APIFunctions.login(getContext(), email, pwd, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        parseLoginResponse(statusCode, headers, response);

                        try {
                            SocketIO socketIO = new SocketIO(v.getContext());
                            socketIO.syncUser();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // Make a Snackbar to notify user with error
                        loginErrorSnackbar.show();
                    }
                });
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("com.example.easycourse", e.toString());
            }
        }
    }


    //Parses response from login to realm and sharedprefs
    public void parseLoginResponse(int statusCode, Header[] headers, JSONObject response) {
        String userToken = "";

        //for each header in array Headers scan for Auth header
        for (Header header : headers) {
            if (header.toString().contains("Auth"))
                userToken = header.toString().substring(header.toString().indexOf(":") + 2);
        }
        // Store user at SharedPreferences
        sharedPref = getActivity().getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userToken", userToken);
        editor.putString("currentUser", response.toString());
        try {
            editor.putString("userId", response.getString("_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.apply();


        RealmList<Room> joinedRoomList = new RealmList<>();
        RealmList<Course> joinedCourseList = new RealmList<>();
        RealmList<Room> silentRoomList = new RealmList<>();
        Room room;
        Course course;
        try {
            currentUser.setId(response.getString("_id"));
            currentUser.setEmail(response.getString("email"));
            currentUser.setUsername(response.getString("displayName"));

            // Adding courses, rooms, and silent rooms
            JSONArray joinedCourses = response.getJSONArray("joinedCourse");
            for (int i = 0; i < joinedCourses.length(); i++) {
                JSONObject temp = joinedCourses.getJSONObject(i);
                String id = (String) checkIfJsonExists(temp, "_id", null);
                String courseName = (String) checkIfJsonExists(temp, "name", null);
                String title = (String) checkIfJsonExists(temp, "title", null);
                String courseDescription = (String) checkIfJsonExists(temp, "description", null);
                int creditHours = Integer.parseInt((String) checkIfJsonExists(temp, "creditHours", "0"));
                String universityID = (String) checkIfJsonExists(temp, "university", null);

                course = new Course(id, courseName, title, courseDescription, creditHours, universityID);
                Course.updateCourseToRealm(course, realm);
                joinedCourseList.add(course);
            }

            JSONArray joinedRooms = response.getJSONArray("joinedRoom");
            for (int i = 0; i < joinedRooms.length(); i++) {
                JSONObject temp = joinedRooms.getJSONObject(i);
                String id = (String) checkIfJsonExists(temp, "_id", null);
                String roomName = (String) checkIfJsonExists(temp, "name", null);
                String courseID = (String) checkIfJsonExists(temp, "course", null);
                String courseName = Course.getCourseById(courseID, realm).getCoursename();
                String universityID = (String) checkIfJsonExists(temp, "university", null);
                boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                String language = (String) checkIfJsonExists(temp, "language", "0");
                boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                room = new Room(id, roomName, new RealmList<Message>(), courseID, courseName, universityID, new RealmList<User>(), memberCounts, memberCountsDesc, null, language, isPublic, isSystem);
                Room.updateRoomToRealm(room, realm);
                joinedRoomList.add(room);
            }

            JSONArray silentRooms = response.getJSONArray("silentRoom");
            for (int i = 0; i < silentRooms.length(); i++) {
                JSONObject object = silentRooms.getJSONObject(i);
                room = new Room();
                room.setId(object.getString("_id"));
                room.setRoomName(object.getString("name"));
                room.setCourseID(object.getString("course"));
                room.setCourseName(object.getString("courseName"));
                room.setPublic(object.getBoolean("isPublic"));
                room.setSystem(object.getBoolean("isSystem"));
                room.setLanguage(object.getString("language"));
                room.setMemberCounts(object.getInt("memberCounts"));
                room.setMemberList(new RealmList<User>());
                room.getMemberList().add(currentUser);
                silentRoomList.add(room);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        currentUser.setSilentRooms(silentRoomList);
        currentUser.setJoinedCourses(joinedCourseList);
        currentUser.setJoinedRooms(joinedRoomList);


        realm.beginTransaction();
        realm.copyToRealmOrUpdate(currentUser);
        realm.commitTransaction();

        // Make an Intent to move on to the next activity
        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
        startActivity(mainActivityIntent);
        getActivity().finish();
    }


    // Call this function when going to SignupChooseUniversity
    public void gotoSignupChooseCourses() {
        // Switch fragment to SignupChooseUniversity
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseUniversity.newInstance());
        transaction.commit();
    }


    private void startAnimations() {
        titleTextView.startAnimation(titleAnimEnter);
        emailInputLayout.startAnimation(emailAnimEnter);
        passwordInputLayout.startAnimation(passwordAnimEnter);
        // If signup info visible, show animation
        if (verifyPasswordInputLayout.getVisibility() == View.VISIBLE) {
            verifyPasswordInputLayout.startAnimation(verifyPasswordAnimEnter);
            usernameInputLayout.startAnimation(usernameAnimEnter);
        }
        loginButton.startAnimation(loginAnimEnter);
        signupButton.startAnimation(signupAnimEnter);
        facebookThemeButton.startAnimation(facebookAnimEnter);
    }

    // Validates the email for inconsistencies
    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailInputLayout.setError("Missing email");
            emailEditText.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email is not correct");
            emailEditText.requestFocus();
            return false;
        }

        emailInputLayout.setErrorEnabled(false);
        return true;
    }

    // Validates the password for inconsistencies
    private boolean validatePassword() {
        String password = passwordEditText.getText().toString().trim();

        if (password.isEmpty()) {
            passwordInputLayout.setError("Missing password");
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6 || password.length() > 20) {
            passwordInputLayout.setError("Password length not between 6 and 20");
            passwordEditText.requestFocus();
            return false;
        }


        passwordInputLayout.setErrorEnabled(false);
        return true;
    }

    // Validates the verify password for inconsistencies
    private boolean validateVerifyPassword() {
        String password = passwordEditText.getText().toString().trim();
        String verifyPassword = verifyPasswordEditText.getText().toString().trim();

        if (!password.equals(verifyPassword)) {
            passwordInputLayout.setError("Passwords don't match");
            verifyPasswordInputLayout.setError("Passwords don't match");
            verifyPasswordEditText.requestFocus();
            return false;
        }

        verifyPasswordInputLayout.setErrorEnabled(false);
        passwordInputLayout.setErrorEnabled(false);
        return true;
    }


    // Validates the username for inconsistencies
    private boolean validateUsername() {
        String username = usernameEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameInputLayout.setError("Missing username");
            usernameEditText.requestFocus();
            return false;
        }

        if (username.length() < 6 || username.length() > 20) {
            usernameInputLayout.setError("Username length not between 6 and 20");
            usernameEditText.requestFocus();
            return false;
        }

        usernameInputLayout.setErrorEnabled(false);
        return true;
    }


    // TextWatcher to show error when typing out email/password ..etc
    public class SignupLoginTextWatcher implements TextWatcher {

        private View view;

        private SignupLoginTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.editTextEmail:
                    validateEmail();
                    break;
                case R.id.editTextPassword:
                    validatePassword();
                    break;
                case R.id.editTextVerifyPassword:
                    validateVerifyPassword();
                    break;
                case R.id.editTextUsername:
                    validateUsername();
                    break;
            }
        }
    }

}
