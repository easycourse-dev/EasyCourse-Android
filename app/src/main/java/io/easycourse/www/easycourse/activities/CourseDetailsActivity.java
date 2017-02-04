package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.CourseDetails.CourseDetailsRoomsEndlessRecyclerViewScrollListener;
import io.easycourse.www.easycourse.components.main.CourseDetails.CourseDetailsRoomsRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.models.main.Language;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.University;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.SocketIO;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Ack;

/**
 * Created by markw on 12/27/2016.
 */

public class CourseDetailsActivity extends AppCompatActivity {

    private static final String TAG = "CourseDetailsActivity";

    Realm realm;
    SocketIO socketIO;
    User currentUser;

    Course course;
    String courseId, universityName;
    int creditHrs = 0;
    boolean isJoined;
    ArrayList<Room> courseRooms = new ArrayList<>();
    CourseDetailsRoomsRecyclerViewAdapter roomsAdapter;
    CourseDetailsRoomsEndlessRecyclerViewScrollListener scrollListener;

    @BindView(R.id.CourseDetailsToolbar)
    Toolbar toolbar;
    @BindView(R.id.courseDetailsProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.CourseDetailsCourseName)
    TextView courseNameView;
    @BindView(R.id.CourseDetailsTitle)
    TextView titleView;
    @BindView(R.id.CourseDetailsCreditHrs)
    TextView creditHrsView;
    @BindView(R.id.CourseDetailsJoinCourse)
    Button joinCourseButton;
    @BindView(R.id.CourseDetailsUniv)
    TextView univView;
    @BindView(R.id.CourseDetailsRoomsView)
    RecyclerView roomsView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Course Details");
            getSupportActionBar().setElevation(0);
        }

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        realm = Realm.getDefaultInstance();

        currentUser = User.getCurrentUser(this, realm);

        progressBar.setVisibility(View.VISIBLE);

        // Set up local variables
        Intent courseManageIntent = getIntent();
        courseId = courseManageIntent.getStringExtra("courseId");
        isJoined = courseManageIntent.getBooleanExtra("isJoined", false);
        course = realm.where(Course.class).equalTo("id", courseId).findFirst();
        if (course != null) {
            University university = realm.where(University.class).equalTo("id", course.getUniversityID()).findFirst();
            if (university != null) universityName = university.getName();
            setupTextViews();
        }
        fetchCourseInfo();

        setupJoinButton();

        setupRecyclerView();

    }

    private void fetchCourseInfo() {
        try {
            socketIO.getCourseInfo(courseId, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    try {
                        JSONObject res = obj.getJSONObject("course");
                        String courseName = res.getString("name");
                        String title = res.getString("title");
                        String courseDesc = res.getString("description");
                        creditHrs = res.getInt("creditHours");
                        String universityId = res.getJSONObject("university").getString("_id");
                        universityName = res.getJSONObject("university").getString("name");
                        course = new Course(courseId, courseName, title, courseDesc, creditHrs, universityId);
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(course);
                        realm.commitTransaction();
                        realm.close();
                        // Set up TextViews
                        setupTextViews();
                    } catch (JSONException e) {
                        Log.e(TAG, "call: ", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "call: ", e);
        }
    }

    private void setupJoinButton() {
        // Set up join button
        updateButtonView(isJoined);
        joinCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isJoined) {
                    // Show dialog and drop course if confirmed
                    showDropCourseDialog();
                } else {
                    // Click to joinCourse
                    joinCourse();
                }
            }
        });
    }

    private void setupRecyclerView() {
        // Set up rooms RecyclerView
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsAdapter = new CourseDetailsRoomsRecyclerViewAdapter(courseRooms, socketIO, isJoined, realm, this);
        roomsView.setHasFixedSize(true);
        roomsView.setLayoutManager(roomsLayoutManager);
        roomsView.addItemDecoration(new RecyclerViewDivider(this));
        scrollListener = new CourseDetailsRoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                doSearchRoom(page, courseId);
            }
        };
        roomsView.addOnScrollListener(scrollListener);
        roomsView.setAdapter(roomsAdapter);
    }

    private void updateButtonView(Boolean joined) {
        if (joined) {
            joinCourseButton.setText(getResources().getString(R.string.joined));
            joinCourseButton.setBackground(ContextCompat.getDrawable(this, R.drawable.course_details_joined_button));
        } else {
            joinCourseButton.setText(getResources().getString(R.string.join));
            joinCourseButton.setBackground(ContextCompat.getDrawable(this, R.drawable.course_details_join_button));
        }
    }

    private void setupTextViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                courseNameView.setText(course.getCoursename());
                titleView.setText(course.getTitle());
                String creditHrsText;
                if (creditHrs == 1) {
                    creditHrsText = "1 credit";
                } else {
                    creditHrsText = creditHrs + " credits";
                }
                creditHrsView.setText(creditHrsText);
                univView.setText(universityName);
                progressBar.setVisibility(View.GONE);
                doSearchRoom(0, courseId);
            }
        });
    }

    private void doSearchRoom(final int skip, final String courseId) {
        APIFunctions.searchCourseSubroom(this, courseId, "", 20, skip, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                JSONObject room;
                JSONObject founderJSON;
                Room roomObj;
                try {
                    if (skip == 0) { // normal
                        courseRooms.clear();
                        for (int i = 0; i < response.length(); i++) {
                            room = (JSONObject) response.get(i);
                            if (!room.isNull("founder")) {
                                founderJSON = (JSONObject) room.get("founder");
                                roomObj = new Room(
                                        room.getString("_id"),
                                        room.getString("name"),
                                        new RealmList<Message>(),
                                        courseId,
                                        course.getCoursename(),
                                        course.getUniversityID(),
                                        new RealmList<User>(),
                                        room.getInt("memberCounts"),
                                        room.getString("memberCountsDescription"),
                                        new User(
                                                (String) JSONUtils.checkIfJsonExists(founderJSON, "_id", null),
                                                (String) JSONUtils.checkIfJsonExists(founderJSON, "displayName", null),
                                                null,
                                                (String) JSONUtils.checkIfJsonExists(founderJSON, "avatarUrl", null),
                                                null, null),
                                        null,
                                        true,
                                        false
                                );
                            } else {
                                roomObj = new Room(
                                        room.getString("_id"),
                                        room.getString("name"),
                                        new RealmList<Message>(),
                                        courseId,
                                        course.getCoursename(),
                                        course.getUniversityID(),
                                        new RealmList<User>(),
                                        room.getInt("memberCounts"),
                                        room.getString("memberCountsDescription"),
                                        new User(),
                                        room.getString("language"),
                                        true,
                                        true
                                );
                            }

                            courseRooms.add(roomObj);
                        }
                        roomsAdapter.notifyDataSetChanged();
//                        updateRecyclerView();
                    } else { // load more
                        int roomsOrigSize = courseRooms.size();
                        for (int i = 0; i < response.length(); i++) {
                            room = (JSONObject) response.get(i);
                            if (!room.isNull("founder")) {
                                founderJSON = (JSONObject) room.get("founder");
                                roomObj = new Room(
                                        room.getString("_id"),
                                        room.getString("name"),
                                        new RealmList<Message>(),
                                        courseId,
                                        course.getCoursename(),
                                        course.getUniversityID(),
                                        new RealmList<User>(),
                                        room.getInt("memberCounts"),
                                        room.getString("memberCountsDescription"),
                                        new User(
                                                founderJSON.getString("_id"),
                                                founderJSON.getString("displayName"),
                                                null,
                                                founderJSON.getString("avatarUrl"),
                                                null, null),
                                        null,
                                        true,
                                        false
                                );
                            } else {
                                roomObj = new Room(
                                        room.getString("_id"),
                                        room.getString("name"),
                                        new RealmList<Message>(),
                                        courseId,
                                        course.getCoursename(),
                                        course.getUniversityID(),
                                        new RealmList<User>(),
                                        room.getInt("memberCounts"),
                                        room.getString("memberCountsDescription"),
                                        new User(),
                                        room.getString("language"),
                                        true,
                                        true
                                );
                            }
                            if (!courseRooms.contains(roomObj))
                                courseRooms.add(roomObj);
                        }
                        if (courseRooms.size() > roomsOrigSize) {
                            roomsAdapter.notifyItemRangeInserted(roomsOrigSize, 20);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "call: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("searchCourseSubroom", responseString, throwable);
                Snackbar.make(roomsView, responseString, Snackbar.LENGTH_LONG).show();
            }
        });
//        final String query = "";
//        try {
//            socketIO.searchCourseSubrooms(query, 20, skip, courseId, new Ack() {
//                @Override
//                public void call(Object... args) {
//                    try {
//                        JSONObject obj = (JSONObject) args[0];
//                        JSONArray response = obj.getJSONArray("rooms");
//                        JSONObject room;
//                        JSONObject founderJSON;
//                        Room roomObj;
//                        if (skip == 0) { // normal
//                            courseRooms.clear();
//                            for (int i = 0; i < response.length(); i++) {
//                                room = (JSONObject) response.get(i);
//                                if (!room.isNull("founder")) {
//                                    founderJSON = (JSONObject) room.get("founder");
//                                    roomObj = new Room(
//                                            room.getString("_id"),
//                                            room.getString("name"),
//                                            new RealmList<Message>(),
//                                            room.getString("course"),
//                                            courseName,
//                                            universityId,
//                                            new RealmList<User>(),
//                                            room.getInt("memberCounts"),
//                                            room.getString("memberCountsDescription"),
//                                            new User(
//                                                    founderJSON.getString("_id"),
//                                                    founderJSON.getString("displayName"),
//                                                    null,
//                                                    founderJSON.getString("avatarUrl"),
//                                                    null, null),
//                                            null,
//                                            true,
//                                            false
//                                    );
//                                } else {
//                                    roomObj = new Room(
//                                            room.getString("_id"),
//                                            room.getString("name"),
//                                            new RealmList<Message>(),
//                                            room.getString("course"),
//                                            courseName,
//                                            universityId,
//                                            new RealmList<User>(),
//                                            room.getInt("memberCounts"),
//                                            room.getString("memberCountsDescription"),
//                                            new User(),
//                                            room.getString("language"),
//                                            true,
//                                            true
//                                    );
//                                }
//
//                                courseRooms.add(roomObj);
//                            }
//                            updateRecyclerView();
//                        } else { // load more
//                            int roomsOrigSize = courseRooms.size();
//                            for (int i = 0; i < response.length(); i++) {
//                                room = (JSONObject) response.get(i);
//                                if (!room.isNull("founder")) {
//                                    founderJSON = (JSONObject) room.get("founder");
//                                    roomObj = new Room(
//                                            room.getString("_id"),
//                                            room.getString("name"),
//                                            new RealmList<Message>(),
//                                            room.getString("course"),
//                                            courseName,
//                                            universityId,
//                                            new RealmList<User>(),
//                                            room.getInt("memberCounts"),
//                                            room.getString("memberCountsDescription"),
//                                            new User(
//                                                    founderJSON.getString("_id"),
//                                                    founderJSON.getString("displayName"),
//                                                    null,
//                                                    founderJSON.getString("avatarUrl"),
//                                                    null, null),
//                                            null,
//                                            true,
//                                            false
//                                    );
//                                } else {
//                                    roomObj = new Room(
//                                            room.getString("_id"),
//                                            room.getString("name"),
//                                            new RealmList<Message>(),
//                                            room.getString("course"),
//                                            courseName,
//                                            universityId,
//                                            new RealmList<User>(),
//                                            room.getInt("memberCounts"),
//                                            room.getString("memberCountsDescription"),
//                                            new User(),
//                                            room.getString("language"),
//                                            true,
//                                            true
//                                    );
//                                }
//                                if (!courseRooms.contains(roomObj))
//                                    courseRooms.add(roomObj);
//                            }
//                            if (courseRooms.size() > roomsOrigSize) {
//                                roomsAdapter.notifyItemRangeInserted(roomsOrigSize, 20);
//                            }
//                        }
//                    } catch (JSONException e) {
//                        Log.e(TAG, "call: ", e)();
//                        courseRooms.clear();
//                        updateRecyclerView();
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            Log.e(TAG, "call: ", e)();
//        }
    }

    private void showDropCourseDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Dropping " + course.getCoursename() + "?")
                .content("If you drop this course, then you will automatically quit all the rooms belonging to " + course.getCoursename() + ".")
                .negativeText("Maybe Not")
                .positiveText("Drop It")
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.colorLogout, null))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Drop the course
                        dropCourse();
                    }
                })
                .build();

        dialog.show();
    }

    private void courseUpdateView(final String courseId, final String courseName, final ArrayList<Room> roomsJoined) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateButtonView(isJoined);
                courseRooms.clear();
                roomsAdapter.updateCourse(isJoined, roomsJoined);
                doSearchRoom(0, courseId);
            }
        });
    }

    private void joinCourse() {
        try {
            socketIO.joinCourse(courseId, Language.getCheckedLanguageCodeArrayList(realm), new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject res = (JSONObject) args[0];
                    try {
                        JSONArray courseArrayJSON = res.getJSONArray("joinedCourse");
                        JSONArray roomArrayJSON = res.getJSONArray("joinedRoom");
                        JSONObject temp;
                        Realm tempRealm = Realm.getDefaultInstance();

                        // Courses handling
                        for (int i = 0; i < courseArrayJSON.length(); i++) {
                            temp = courseArrayJSON.getJSONObject(i);
                            String id = (String) JSONUtils.checkIfJsonExists(temp, "_id", null);
                            String courseName = (String) JSONUtils.checkIfJsonExists(temp, "name", null);
                            String title = (String) JSONUtils.checkIfJsonExists(temp, "title", null);
                            String courseDescription = (String) JSONUtils.checkIfJsonExists(temp, "description", null);
                            int creditHours = Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "creditHours", "0"));
                            String universityID = (String) JSONUtils.checkIfJsonExists(temp, "university", null);

                            Course course = new Course(id, courseName, title, courseDescription, creditHours, universityID);
                            User tempCurrentUser = User.getCurrentUser(CourseDetailsActivity.this, tempRealm);
                            tempRealm.beginTransaction();
                            if (tempCurrentUser != null) {
                                tempCurrentUser.getJoinedCourses().add(course);
                                tempRealm.copyToRealmOrUpdate(tempCurrentUser);
                            }
                            tempRealm.commitTransaction();
                        }

                        // Rooms handling
                        ArrayList<Room> newRooms = new ArrayList<>();
                        Room tempRoom;
                        for (int i = 0; i < roomArrayJSON.length(); i++) {
                            temp = roomArrayJSON.getJSONObject(i);
                            final String id = (String) JSONUtils.checkIfJsonExists(temp, "_id", null);
                            final String roomName = (String) JSONUtils.checkIfJsonExists(temp, "name", null);
                            final String courseID = (String) JSONUtils.checkIfJsonExists(temp, "course", null);
                            final String courseName = Course.getCourseById(courseID, tempRealm).getCoursename();
                            final String universityID = (String) JSONUtils.checkIfJsonExists(temp, "university", null);
                            final boolean isPublic = (boolean) JSONUtils.checkIfJsonExists(temp, "isPublic", true);
                            final int memberCounts = Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "memberCounts", "1"));
                            final String memberCountsDesc = (String) JSONUtils.checkIfJsonExists(temp, "memberCountsDescription", null);
                            final String language = (String) JSONUtils.checkIfJsonExists(temp, "language", "0");
                            final boolean isSystem = (boolean) JSONUtils.checkIfJsonExists(temp, "isSystem", true);

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
                                    isSystem
                            );
                            newRooms.add(tempRoom);
                            Room.updateRoomToRealm(tempRoom, tempRealm);
                        }
                        tempRealm.close();
                        // Update view
                        isJoined = true;
                        courseUpdateView(course.getId(), course.getCoursename(), newRooms);
                    } catch (JSONException e) {
                        Log.e(TAG, "call: ", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "call: ", e);
        }
        currentUser = User.getCurrentUser(this, realm);
    }

    private void dropCourse() {
        try {
            socketIO.dropCourse(course.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    try {
                        Realm tempRealm = Realm.getDefaultInstance();
                        JSONObject obj = (JSONObject) args[0];
                        boolean status = obj.getBoolean("success");
                        ArrayList<Room> deletedRooms = new ArrayList<>();
                        if (status) {
                            JSONArray quitedRoomsJSON = obj.getJSONArray("quitRooms");
                            String quitedRoomId;
                            Room quitedRoom;
                            for (int i = 0; i < quitedRoomsJSON.length(); i++) {
                                // Quit rooms in Realm
                                quitedRoomId = quitedRoomsJSON.getString(i);
                                quitedRoom = Room.getRoomById(tempRealm, quitedRoomId);
                                deletedRooms.add(quitedRoom);
                                Room.deleteRoomFromRealm(quitedRoom, tempRealm);
                            }
                            Course.deleteCourseFromRealm(course, tempRealm);
                            tempRealm.close();
                            // Update view
                            isJoined = false;
                            courseUpdateView(course.getId(), course.getCoursename(), deletedRooms);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "call: ", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "dropCourse: ", e);
        }
    }
}
