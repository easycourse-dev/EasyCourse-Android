package io.easycourse.www.easycourse.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.components.main.CourseDetails.CourseDetailsRoomsEndlessRecyclerViewScrollListener;
import io.easycourse.www.easycourse.components.main.CourseDetails.CourseDetailsRoomsRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
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

    Realm realm;
    SocketIO socketIO;

    Course course;
    String courseId, courseName, title, courseDesc, universityId, universityName;
    int creditHrs = 0;
    boolean isJoined;
    ArrayList<Room> courseRooms = new ArrayList<>();
    CourseDetailsRoomsRecyclerViewAdapter roomsAdapter;
    CourseDetailsRoomsEndlessRecyclerViewScrollListener scrollListener;

    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsToolbar)
    Toolbar toolbar;
    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsCourseName)
    TextView courseNameView;
    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsTitle)
    TextView titleView;
    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsCreditHrs)
    TextView creditHrsView;
    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsJoinCourse)
    Button joinCourseButton;
    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsUniv)
    TextView univView;
    @BindView(io.easycourse.www.easycourse.R.id.CourseDetailsRoomsView)
    RecyclerView roomsView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.easycourse.www.easycourse.R.layout.activity_course_details);
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

        // Set up local variables
        Intent courseManageIntent = getIntent();
        courseId = courseManageIntent.getStringExtra("courseId");
        isJoined = courseManageIntent.getBooleanExtra("isJoined", false);
        try {
            socketIO.getCourseInfo(courseId, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    try {
                        JSONObject res = obj.getJSONObject("course");
                        courseName = res.getString("name");
                        title = res.getString("title");
                        courseDesc = res.getString("description");
                        creditHrs = res.getInt("creditHours");
                        universityId = res.getJSONObject("university").getString("_id");
                        universityName = res.getJSONObject("university").getString("name");
                        course = new Course(courseId, courseName, title, courseDesc, creditHrs, universityId);

                        // Set up TextViews
                        setupTextViews();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
            joinCourseButton.setText("Joined");
            joinCourseButton.setBackground(ResourcesCompat.getDrawable(getResources(), io.easycourse.www.easycourse.R.drawable.course_details_joined_button, null));
        } else {
            joinCourseButton.setText("Join");
            joinCourseButton.setBackground(ResourcesCompat.getDrawable(getResources(), io.easycourse.www.easycourse.R.drawable.course_details_join_button, null));
        }
    }

    private void setupTextViews() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            courseNameView.setText(courseName);
                            titleView.setText(title);
                            String creditHrsText;
                            if (creditHrs == 1) {
                                creditHrsText = "1 credit";
                            } else {
                                creditHrsText = creditHrs + " credits";
                            }
                            creditHrsView.setText(creditHrsText);
                            univView.setText(universityName);
                            doSearchRoom(0, courseId);
                        }
                    });
                }
            }
        };
        thread.start();
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
                                        courseName,
                                        universityId,
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
                                        courseName,
                                        universityId,
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
                                        courseName,
                                        universityId,
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
                                        courseName,
                                        universityId,
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
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("searchCourseSubroom", responseString);
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
//                        e.printStackTrace();
//                        courseRooms.clear();
//                        updateRecyclerView();
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    private void updateRecyclerView(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            roomsAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    private void showDropCourseDialog() {
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(this);

        customBuilder.setTitle("Dropping " + course.getCoursename() + "?")
                .setMessage("If you drop this course, then you will automatically quit all the rooms belonging to " + course.getCoursename() + ".")
                .setNegativeButton("Maybe Not", null)
                .setPositiveButton("Drop It", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Drop the course
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
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog dialog = customBuilder.create();
        dialog.show();

        Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(b != null) {
            b.setTextColor(ResourcesCompat.getColor(getResources(), io.easycourse.www.easycourse.R.color.colorLogout, null));
        }
    }

    private void courseUpdateView(final String courseId, final String courseName, final ArrayList<Room> roomsJoined){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
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
            }
        };
        thread.start();
    }

    private void joinCourse() {
        try {
            socketIO.joinCourse(courseId, new Ack() {
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
                            Course.updateCourseToRealm(course, tempRealm);
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
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
