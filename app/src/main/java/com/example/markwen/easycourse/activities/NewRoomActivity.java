package com.example.markwen.easycourse.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ExistedRoomsRecyclerViewAdapter;
import com.example.markwen.easycourse.components.main.NewRoomCoursesRecyclerViewAdapter;
import com.example.markwen.easycourse.components.main.RoomsEndlessRecyclerViewScrollListener;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    RoomsEndlessRecyclerViewScrollListener roomsOnScrollListener;
    RealmResults<Course> courses;
    ArrayList<Room> rooms = new ArrayList<>();
    String UniversityId;

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

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();
        realm = Realm.getDefaultInstance();

        UniversityId = User.getCurrentUser(this, realm).getUniversityID();
        if (UniversityId == null) {
            // Temporary placeholder with Purdue University ID
            UniversityId = "57e2cb6854ad620011c82db4";
        }

        // Setup rooms view
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        final ExistedRoomsRecyclerViewAdapter roomsRecyclerViewAdapter = new ExistedRoomsRecyclerViewAdapter(this, rooms);
        existedRoomView.setLayoutManager(roomsLayoutManager);
        existedRoomView.setHasFixedSize(true);
        existedRoomView.setAdapter(roomsRecyclerViewAdapter);
        roomsOnScrollListener = new RoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsRecyclerViewAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                try {
                    int roomsOrigSize = rooms.size();
                    socketIO.searchRooms(newRoomName.getText().toString(), 20, page, UniversityId, rooms);
                    if (rooms.size() > roomsOrigSize) {
                        roomsRecyclerViewAdapter.notifyItemRangeInserted(roomsOrigSize, 20);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        // Setup courses view
        courses = realm.where(Course.class).findAll();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        final NewRoomCoursesRecyclerViewAdapter coursesAdapter = new NewRoomCoursesRecyclerViewAdapter(this, courses);
        if (courses.size() == 0) {
            // If no courses then show hint to add courses
            existedRoomView.setVisibility(View.GONE);
            roomsLabel.setVisibility(View.GONE);
            newRoomButton.setVisibility(View.GONE);
            newRoomCourseView.setVisibility(View.GONE);
            newRoomName.setVisibility(View.GONE);
            noCourseText.setVisibility(View.VISIBLE);
        } else {
            // Setup courses recycler view
            newRoomCourseView.setLayoutManager(layoutManager);
            newRoomCourseView.setAdapter(coursesAdapter);
            newRoomCourseView.setHasFixedSize(true);
        }

        newRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newRoomName.getText().toString().equals("")){
                    Snackbar.make(view, "Please enter a room name", Snackbar.LENGTH_LONG).show();
                } else if (coursesAdapter.getSelectedCourse() == null) {
                    Snackbar.make(view, "Please select a class that this room belongs to", Snackbar.LENGTH_LONG).show();
                } else {
                    try {
                        socketIO.createRoom(newRoomName.getText().toString(), coursesAdapter.getSelectedCourse().getId());
                        finish();
                    } catch (JSONException e) {
                        Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        // Logic for EditText input change
        // and other view changes based on it
        newRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                rooms.clear();
                try {
                    Future<ArrayList<Room>> search = socketIO.searchRooms(editable.toString(), 20, 0, UniversityId, rooms);
                    rooms = search.get();
                    roomsRecyclerViewAdapter.notifyDataSetChanged();
                    roomsOnScrollListener.resetState();
                } catch (JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        realm.close();
    }
}
