package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.CourseDetails.CourseDetailsRoomsEndlessRecyclerViewScrollListener;
import com.example.markwen.easycourse.components.main.CourseDetails.CourseDetailsRoomsRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.University;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Ack;

/**
 * Created by markw on 12/27/2016.
 */

public class CourseDetailsAcitivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    Course course;
    String courseId, courseName, title, courseDesc, universityId, universityName;
    int creditHrs = 0;
    boolean isJoined;
    ArrayList<Room> courseRooms = new ArrayList<>();
    CourseDetailsRoomsRecyclerViewAdapter roomsAdapter;
    CourseDetailsRoomsEndlessRecyclerViewScrollListener scrollListener;

    @BindView(R.id.CourseDetailsToolbar)
    Toolbar toolbar;
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

        // Set up local variables
        Intent courseManageIntent = getIntent();
        courseId = courseManageIntent.getStringExtra("courseId");
        courseName = courseManageIntent.getStringExtra("courseName");
        title = courseManageIntent.getStringExtra("title");
        courseDesc = courseManageIntent.getStringExtra("courseDesc");
        universityId = courseManageIntent.getStringExtra("univId");
        creditHrs = courseManageIntent.getIntExtra("courseCred", 3);
        isJoined = courseManageIntent.getBooleanExtra("isJoined", false);
        course = new Course(courseId, courseName, title, courseDesc, creditHrs, universityId);
        if (universityId == null) {
            universityId = User.getCurrentUser(this, realm).getUniversityID();
        }
        universityName = University.getUniversityById(universityId, realm).getName();

        // Set up TextViews
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

        // Set up join button
        updateButtonView(isJoined);

        // Set up rooms RecyclerView
        doSearchRoom(0, courseId, courseName);
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsAdapter = new CourseDetailsRoomsRecyclerViewAdapter(courseRooms, socketIO, isJoined, realm);
        roomsView.setHasFixedSize(true);
        roomsView.setLayoutManager(roomsLayoutManager);
        roomsView.addItemDecoration(new RecyclerViewDivider(this));
        scrollListener = new CourseDetailsRoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                doSearchRoom(page, courseId, courseName);
            }
        };
        roomsView.addOnScrollListener(scrollListener);
        roomsView.setAdapter(roomsAdapter);
    }

    private void updateButtonView(Boolean joined) {
        if (joined) {
            joinCourseButton.setText("Joined");
            joinCourseButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.course_details_joined_button, null));
        } else {
            joinCourseButton.setText("Join");
            joinCourseButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.course_details_join_button, null));
        }
    }

    private void doSearchRoom(final int skip, String courseId, final String courseName) {
        final String query = "";
        try {
            socketIO.searchCourseSubrooms(query, 20, skip, courseId, new Ack() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray response = obj.getJSONArray("rooms");
                        JSONObject room;
                        JSONObject founderJSON;
                        Room roomObj;
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
                                            room.getString("course"),
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
                                            room.getString("course"),
                                            courseName,
                                            universityId,
                                            new RealmList<User>(),
                                            room.getInt("memberCounts"),
                                            room.getString("memberCountsDescription"),
                                            null,
                                            room.getString("language"),
                                            true,
                                            true
                                    );
                                }

                                courseRooms.add(roomObj);
                            }
                            updateRecyclerView();
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
                                            room.getString("course"),
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
                                            room.getString("course"),
                                            courseName,
                                            universityId,
                                            new RealmList<User>(),
                                            room.getInt("memberCounts"),
                                            room.getString("memberCountsDescription"),
                                            null,
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
                        courseRooms.clear();
                        updateRecyclerView();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
