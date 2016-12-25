package com.example.markwen.easycourse.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.markwen.easycourse.utils.APIFunctions;
import com.example.markwen.easycourse.utils.SocketIO;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
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
    NewRoomCoursesRecyclerViewAdapter coursesAdapter;
    ExistedRoomsRecyclerViewAdapter roomsRecyclerViewAdapter;

    @BindView(R.id.newRoomToolbar)
    Toolbar toolbar;
    @BindView(R.id.newRoomNoCourseText)
    TextView noCourseText;
    @BindView(R.id.newRoomRoomsLabel)
    TextView existedRoomsLabel;
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

        // Setup courses view
        courses = realm.where(Course.class).findAll();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coursesAdapter = new NewRoomCoursesRecyclerViewAdapter(this, courses);
        if (courses.size() == 0) {
            // If no courses then show hint to add courses
            existedRoomView.setVisibility(View.GONE);
            existedRoomsLabel.setVisibility(View.GONE);
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

        // Setup rooms view
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsRecyclerViewAdapter = new ExistedRoomsRecyclerViewAdapter(this, rooms, socketIO);
        existedRoomView.setLayoutManager(roomsLayoutManager);
        existedRoomView.setHasFixedSize(true);

        // Search room API function
        newRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (coursesAdapter.getSelectedCourse() != null) {
                    doSearchRoom(editable.toString(), 0, UniversityId, coursesAdapter.getSelectedCourse().getCoursename());
                } else {
                    Snackbar.make(newRoomName, "Please select the course that the room belongs to", Snackbar.LENGTH_LONG).show();
                }

            }
        });

        // Load more rooms
        roomsOnScrollListener = new RoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsRecyclerViewAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                doSearchRoom(newRoomName.getText().toString(), page, UniversityId, coursesAdapter.getSelectedCourse().getCoursename());
            }
        };
        existedRoomView.addOnScrollListener(roomsOnScrollListener);
        existedRoomView.setAdapter(roomsRecyclerViewAdapter);

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

        realm.close();
    }

    private void doSearchRoom(String query, int skip, String universityId, final String courseName) {
        if (courseName != null && !courseName.equals("")) {

            if (skip == 0) { // normal
                rooms.clear();
                try {
                    Future<ArrayList<Room>> searchingRoom = socketIO.searchRooms(query, 20, skip, universityId, rooms);
                    rooms = searchingRoom.get();
                } catch (JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                roomsRecyclerViewAdapter.notifyDataSetChanged();
                roomsOnScrollListener.resetState();

            } else { // load more
                // Commented out the socket implementation for load more room
                // because for some reason it is not getting the results
                // fast enough, feel free to figure this out in the future

//                int roomsOrigSize = rooms.size();
//                Room tempRoom;
//                try {
//                    Future<ArrayList<Room>> searchingRoom = socketIO.searchRooms(query, 20, skip, universityId, rooms);
//                    ArrayList<Room> loadMoreResults = searchingRoom.get();
//                    for (int i = 0; i < loadMoreResults.size(); i++) {
//                        tempRoom = loadMoreResults.get(i);
//                        if (!rooms.contains(tempRoom)) {
//                            rooms.add(tempRoom);
//                        }
//                    }
//                } catch (JSONException | InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//                if (rooms.size() > roomsOrigSize) {
//                    roomsRecyclerViewAdapter.notifyItemRangeInserted(roomsOrigSize, 20);
//                }
                
                APIFunctions.searchRoom(getApplicationContext(), query, 20, skip, universityId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            int roomsOrigSize = rooms.size();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject room = (JSONObject) response.get(i);
                                Room roomObj = new Room(room.getString("_id"), room.getString("name"), courseName);
                                if (!rooms.contains(roomObj))
                                    rooms.add(roomObj);
                            }
                            if (rooms.size() > roomsOrigSize) {
                                roomsRecyclerViewAdapter.notifyItemRangeInserted(roomsOrigSize, 20);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        Log.e("com.example.easycourse", "failure" + t.toString());
                    }
                });
            }
        }
    }
}
