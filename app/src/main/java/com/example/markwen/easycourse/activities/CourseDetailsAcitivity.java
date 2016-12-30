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
import com.example.markwen.easycourse.components.main.CourseDetails.CourseDetailsRoomsRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Course;
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
        try {
            // Get course rooms
            socketIO.searchCourseSubrooms("", 20, 0, courseId, new Ack() {
                @Override
                public void call(Object... args) {
                    try {
                        courseRooms.clear();
                        JSONObject room;
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray response = obj.getJSONArray("rooms");
                        for (int i = 0; i < response.length(); i++) {
                            room = (JSONObject) response.get(i);
                            Room roomObj = new Room(room.getString("_id"), room.getString("name"), courseName);
                            courseRooms.add(roomObj);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsAdapter = new CourseDetailsRoomsRecyclerViewAdapter(courseRooms, socketIO);


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
}
