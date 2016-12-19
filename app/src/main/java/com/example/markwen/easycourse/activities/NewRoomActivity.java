package com.example.markwen.easycourse.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.NewRoomRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by markw on 12/17/2016.
 */

public class NewRoomActivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    RealmResults<Course> courses;

    @BindView(R.id.newRoomToolbar)
    Toolbar toolbar;
    @BindView(R.id.newRoomNoCourseText)
    TextView noCourseText;
    @BindView(R.id.newRoomRoomsLabel)
    TextView roomsLabel;
    @BindView(R.id.newRoomCoursesLabel)
    TextView coursesLabel;
    @BindView(R.id.newRoomCourseList)
    RecyclerView newRoomCourseView;
    @BindView(R.id.existedRoomsList)
    RecyclerView existedRoomView;
    @BindView(R.id.newRoomNameEditText)
    EditText newRoomName;
    @BindView(R.id.newRoomCreateButton)
    Button newRoomButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);

        // Binds all the views
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle("Join/Create Room");
        // Initially hidden items
        noCourseText.setVisibility(View.GONE);
        coursesLabel.setVisibility(View.GONE);
        newRoomCourseView.setVisibility(View.GONE);

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();

        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        existedRoomView.setLayoutManager(roomsLayoutManager);
//        existedRoomView.setAdapter(new ExistedRoomsRecyclerViewAdapter(this, ));

        // Get all courses user registered
        realm = Realm.getDefaultInstance();
        courses = realm.where(Course.class).findAll();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        final NewRoomRecyclerViewAdapter adapter = new NewRoomRecyclerViewAdapter(this, courses);
        if (courses.size() == 0) {
            // If no courses then show hint to add courses
            newRoomButton.setVisibility(View.GONE);
            newRoomCourseView.setVisibility(View.GONE);
            newRoomName.setVisibility(View.GONE);
            noCourseText.setVisibility(View.VISIBLE);
        } else {
            // Setup recycler view
            newRoomCourseView.setLayoutManager(layoutManager);
            newRoomCourseView.setAdapter(adapter);
            newRoomCourseView.setHasFixedSize(true);
        }
        realm.close();

        newRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newRoomName.getText().toString().equals("")){
                    Snackbar.make(view, "Please enter a room name", Snackbar.LENGTH_LONG).show();
                } else if (adapter.getSelectedCourse() == null) {
                    Snackbar.make(view, "Please select a class that this room belongs to", Snackbar.LENGTH_LONG).show();
                } else {
                    try {
                        socketIO.createRoom(newRoomName.getText().toString(), adapter.getSelectedCourse().getId());
                        finish();
                    } catch (JSONException e) {
                        Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
