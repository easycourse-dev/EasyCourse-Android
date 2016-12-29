package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.NewRoom.NewRoomCoursesAdapter;
import com.example.markwen.easycourse.components.main.NewRoom.NewRoomRoomsEndlessRecyclerViewScrollListener;
import com.example.markwen.easycourse.components.main.NewRoom.NewRoomRoomsRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
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
import io.realm.RealmResults;
import io.socket.client.Ack;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;

/**
 * Created by markw on 12/17/2016.
 */

public class NewRoomActivity extends AppCompatActivity {

    Realm realm;
    SocketIO socketIO;
    NewRoomRoomsEndlessRecyclerViewScrollListener roomsOnScrollListener;
    ArrayList<Course> courses = new ArrayList<>();
    NewRoomCoursesAdapter coursesAdapter;
    ArrayList<Room> rooms = new ArrayList<>();
    NewRoomRoomsRecyclerViewAdapter roomsRecyclerViewAdapter;
    User currentUser;

    @BindView(R.id.newRoomToolbar)
    Toolbar toolbar;
    @BindView(R.id.newRoomNoCourseText)
    TextView noCourseText;
    @BindView(R.id.newRoomCoursesSpinner)
    AppCompatSpinner newRoomCoursesSpinner;
    @BindView(R.id.newRoomRoomsLabel)
    TextView existedRoomsLabel;
    @BindView(R.id.existedRoomsList)
    RecyclerView existedRoomView;
    @BindView(R.id.newRoomNameEditText)
    EditText newRoomName;
    @BindView(R.id.newRoomCreateButton)
    Button newRoomButton;
    @BindView(R.id.newRoomInputCard)
    CardView inputCard;
    @BindView(R.id.newRoomResultsCard)
    CardView resultsCard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Join/Create Room");
        }

        // Initially hidden items
        noCourseText.setVisibility(View.GONE);
        newRoomButton.setVisibility(View.GONE);

        socketIO = EasyCourse.getAppInstance().getSocketIO();
        socketIO.syncUser();
        realm = Realm.getDefaultInstance();
        currentUser = User.getCurrentUser(this, realm);

        // Setup courses
        RealmResults<Course> coursesResults = realm.where(Course.class).findAll();
        for (int i = 0; i < coursesResults.size() + 2; i++) {
            if (i == 0) {
                // Add in hint Course
                // TODO: use work around to handle hint on spinner
                courses.add(new Course(null, "This room belongs to...", null, null, 0, null));
            } else if (i == 1) {
                // Add in private option Course
                courses.add(new Course("", "Private Room", null, null, 0, null));
            } else {
                courses.add(coursesResults.get(i - 2));
            }
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (courses.size() == 0) {
            // If no courses then show hint to add courses
            existedRoomView.setVisibility(View.GONE);
            existedRoomsLabel.setVisibility(View.GONE);
            newRoomButton.setVisibility(View.GONE);
            newRoomCoursesSpinner.setVisibility(View.GONE);
            newRoomName.setVisibility(View.GONE);
            noCourseText.setVisibility(View.VISIBLE);
            inputCard.setVisibility(View.GONE);
            resultsCard.setVisibility(View.GONE);
        } else {
            // Setup courses spinner
            // Set dropdown arrow color
            newRoomCoursesSpinner.getBackground().setColorFilter(Color.parseColor("#939393"), PorterDuff.Mode.SRC_ATOP);
            coursesAdapter = new NewRoomCoursesAdapter(getApplicationContext(), courses);
            newRoomCoursesSpinner.setAdapter(coursesAdapter);
            newRoomCoursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    coursesAdapter.setSelectedCourse(i);
                    roomsRecyclerViewAdapter.setCurrentCourse(courses.get(i));
                    doSearchRoom(newRoomName.getText().toString(), 0, coursesAdapter.getSelectedCourse().getId(), coursesAdapter.getSelectedCourse().getCoursename());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Snackbar.make(adapterView, "Nothing selected", Snackbar.LENGTH_LONG).show();
                }
            });
        }

        // Setup rooms view
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsRecyclerViewAdapter = new NewRoomRoomsRecyclerViewAdapter(this, this, rooms, socketIO);
        existedRoomView.setLayoutManager(roomsLayoutManager);
        existedRoomView.setHasFixedSize(true);
        existedRoomView.addItemDecoration(new RecyclerViewDivider(this));

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
                doSearchRoom(editable.toString(), 0, coursesAdapter.getSelectedCourse().getId(), coursesAdapter.getSelectedCourse().getCoursename());
            }
        });

        // Load more rooms
        roomsOnScrollListener = new NewRoomRoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsRecyclerViewAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                doSearchRoom(newRoomName.getText().toString(), page, coursesAdapter.getSelectedCourse().getId(), coursesAdapter.getSelectedCourse().getCoursename());
            }
        };
        existedRoomView.addOnScrollListener(roomsOnScrollListener);
        existedRoomView.setAdapter(roomsRecyclerViewAdapter);

        // Create new room button
        newRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String selectedCourseName = coursesAdapter.getSelectedCourse().getCoursename();
                if (newRoomName.getText().toString().equals("")){
                    Snackbar.make(view, "Please enter a room name", Snackbar.LENGTH_LONG).show();
                } else if (coursesAdapter.getSelectedCourse() == null) {
                    Snackbar.make(view, "Please select a class that this room belongs to", Snackbar.LENGTH_LONG).show();
                } else {
                    try {
                        socketIO.createRoom(newRoomName.getText().toString(), coursesAdapter.getSelectedCourse().getId(), new Ack() {
                            @Override
                            public void call(Object... args) {
                                JSONObject obj = (JSONObject) args[0];
                                if (!obj.has("error")) {
                                    try {
                                        JSONObject temp = obj.getJSONObject("room");

                                        String id = (String) checkIfJsonExists(temp, "_id", null);
                                        String roomName = (String) checkIfJsonExists(temp, "name", null);
                                        String courseID = (String) checkIfJsonExists(temp, "course", null);
                                        String universityID = (String) checkIfJsonExists(temp, "university", null);
                                        boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                                        int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                                        String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                                        String language = (String) checkIfJsonExists(temp, "language", "0");
                                        boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                                        Room room = new Room(
                                                id,
                                                roomName,
                                                new RealmList<Message>(),
                                                courseID,
                                                selectedCourseName,
                                                universityID,
                                                new RealmList<User>(),
                                                memberCounts,
                                                memberCountsDesc,
                                                currentUser,
                                                language,
                                                isPublic,
                                                isSystem);
                                        updateRoomInSocket(room);

                                        Intent chatActivityIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                                        chatActivityIntent.putExtra("roomId", room.getId());
                                        finish();
                                        startActivity(chatActivityIntent);
                                    } catch (JSONException e) {
                                        Log.e("createRoom", e.toString());
                                    }
                                } else {
                                    Log.e("createRoom", obj.toString());
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        realm.close();
    }


    private void doSearchRoom(final String query, final int skip, String courseId, final String courseName) {
        try {
            socketIO.searchCourseSubrooms(query, 20, skip, courseId, new Ack() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray response = obj.getJSONArray("rooms");
                        JSONObject room;
                        if (skip == 0) { // normal
                            rooms.clear();
                            for (int i = 0; i < response.length(); i++) {
                                room = (JSONObject) response.get(i);
                                Room roomObj = new Room(room.getString("_id"), room.getString("name"), courseName);
                                rooms.add(roomObj);
                            }
                            updateRecyclerView(response, query);
                        } else { // load more
                            int roomsOrigSize = rooms.size();
                            for (int i = 0; i < response.length(); i++) {
                                room = (JSONObject) response.get(i);
                                Room roomObj = new Room(room.getString("_id"), room.getString("name"), courseName);
                                if (!rooms.contains(roomObj))
                                    rooms.add(roomObj);
                            }
                            if (rooms.size() > roomsOrigSize) {
                                roomsRecyclerViewAdapter.notifyItemRangeInserted(roomsOrigSize, 20);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        rooms.clear();
                        updateRecyclerView(new JSONArray(), query);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateRoomInSocket(final Room room){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Room.updateRoomToRealm(room, realm);
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public void updateRecyclerView(final JSONArray response, final String query){
        Thread thread = new Thread(){
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            roomsRecyclerViewAdapter.notifyDataSetChanged();
                            roomsOnScrollListener.resetState();
                            Course selectedCourse = coursesAdapter.getSelectedCourse();
                            if (response.length() == 0
                                    && ((selectedCourse != null && selectedCourse.getId() != null)
                                        || selectedCourse.getCoursename().equals("Private Room"))
                                    && !query.equals("")) {
                                newRoomButton.setVisibility(View.VISIBLE);
                                existedRoomView.setVisibility(View.GONE);
                            } else {
                                newRoomButton.setVisibility(View.GONE);
                                existedRoomView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        };
        thread.start();
    }
}
