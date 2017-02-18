package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
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

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.NewRoom.NewRoomCoursesAdapter;
import io.easycourse.www.easycourse.components.main.NewRoom.NewRoomRoomsEndlessRecyclerViewScrollListener;
import io.easycourse.www.easycourse.components.main.NewRoom.NewRoomRoomsRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.socket.client.Ack;

public class NewRoomActivity extends BaseActivity {


    NewRoomRoomsEndlessRecyclerViewScrollListener roomsOnScrollListener;
    ArrayList<Course> courses = new ArrayList<>();
    NewRoomCoursesAdapter coursesAdapter;
    ArrayList<Room> rooms = new ArrayList<>();
    NewRoomRoomsRecyclerViewAdapter roomsRecyclerViewAdapter;
    Handler handler;
    Runnable searchDelay;

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
    @BindView(R.id.newRoomButtonClearEditText)
    Button clearText;

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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewRoomActivity.this.onBackPressed();
            }
        });

        // Initially hidden items
        noCourseText.setVisibility(View.GONE);
        newRoomButton.setVisibility(View.GONE);


        handler = new Handler();

        // Setup courses
        RealmResults<Course> coursesResults = realm.where(Course.class).findAll();
        for (int i = 0; i < coursesResults.size() + 2; i++) {
            if (i == 0) {
                // Add in hint Course
                // TODO: use work around to handle hint on spinner
                courses.add(new Course(null, "This room belongs to...", null, null, 0, null));
            } else if (i == 1) {
                // Add in private option Course
                courses.add(new Course("", "Private Room", "Private Room", null, 0, null));
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
                    if (i == 0) {
                        // On hint selected
                        newRoomButton.setVisibility(View.GONE);
                        resultsCard.setVisibility(View.GONE);
                    } else if (i == 1) {
                        // On private room selected
                        coursesAdapter.setSelectedCourse(1);
                        newRoomButton.setVisibility(View.VISIBLE);
                        resultsCard.setVisibility(View.GONE);
                    } else {
                        resultsCard.setVisibility(View.VISIBLE);
                        coursesAdapter.setSelectedCourse(i);
                        roomsRecyclerViewAdapter.setCurrentCourse(courses.get(i));
                        doSearchRoom(newRoomName.getText().toString(), 0, coursesAdapter.getSelectedCourse().getId(), coursesAdapter.getSelectedCourse().getCoursename(), view);
                    }
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

        // Clear text button
        clearText.setVisibility(View.GONE);
        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newRoomName.setText("");
            }
        });

        // Search room API function
        newRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                final Course selectedCourse = coursesAdapter.getSelectedCourse();
                if (selectedCourse != null && !selectedCourse.getId().equals("")) {
                    // Do search
                    handler.removeCallbacks(searchDelay);
                    searchDelay = new Runnable() {
                        @Override
                        public void run() {
                            doSearchRoom(editable.toString(), 0, selectedCourse.getId(), coursesAdapter.getSelectedCourse().getCoursename(), existedRoomView);
                        }
                    };
                    handler.postDelayed(searchDelay, 250);
                }
                if (editable.toString().equals("")) {
                    // Show clear button
                    clearText.setVisibility(View.GONE);
                    // Create room button
                    newRoomButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.login_button_disable, null));
                } else {
                    // Show clear button
                    clearText.setVisibility(View.VISIBLE);
                    // Create room button
                    newRoomButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.login_button, null));
                }
            }
        });

        // Load more rooms
        roomsOnScrollListener = new NewRoomRoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsRecyclerViewAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                doSearchRoom(newRoomName.getText().toString(), page, coursesAdapter.getSelectedCourse().getId(), coursesAdapter.getSelectedCourse().getCoursename(), view);
            }
        };
        existedRoomView.addOnScrollListener(roomsOnScrollListener);
        existedRoomView.setAdapter(roomsRecyclerViewAdapter);

        // Create new room button
        newRoomButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.login_button_disable, null));
        newRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String selectedCourseName = coursesAdapter.getSelectedCourse().getCoursename();
                if (newRoomName.getText().toString().equals("")) {
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

                                        String id = (String) JSONUtils.checkIfJsonExists(temp, "_id", null);
                                        String roomName = (String) JSONUtils.checkIfJsonExists(temp, "name", null);
                                        String courseID = (String) JSONUtils.checkIfJsonExists(temp, "course", null);
                                        String universityID = (String) JSONUtils.checkIfJsonExists(temp, "university", null);
                                        boolean isPublic = (boolean) JSONUtils.checkIfJsonExists(temp, "isPublic", true);
                                        int memberCounts = Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "memberCounts", "1"));
                                        String memberCountsDesc = (String) JSONUtils.checkIfJsonExists(temp, "memberCountsDescription", null);
                                        String language = (String) JSONUtils.checkIfJsonExists(temp, "language", "0");
                                        boolean isSystem = (boolean) JSONUtils.checkIfJsonExists(temp, "isSystem", true);

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
                                        room.setJoinIn(true);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void doSearchRoom(final String query, final int skip, String courseId, final String courseName, final View v) {
        APIFunctions.searchCourseSubroom(this, courseId, query, 20, skip, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                JSONObject room;
                try {
                    if (skip == 0) { // normal
                        rooms.clear();
                        for (int i = 0; i < response.length(); i++) {
                            room = (JSONObject) response.get(i);
                            Room roomObj = new Room(room.getString("_id"), room.getString("name"), courseName);
                            rooms.add(roomObj);
                        }
                        roomsRecyclerViewAdapter.notifyDataSetChanged();
                        roomsOnScrollListener.resetState();
                        Course selectedCourse = coursesAdapter.getSelectedCourse();
                        if (response.length() == 0
                                && (selectedCourse != null && selectedCourse.getId() != null)
                                && !query.equals("")) {
                            newRoomButton.setVisibility(View.VISIBLE);
                            existedRoomView.setVisibility(View.GONE);
                        } else {
                            newRoomButton.setVisibility(View.GONE);
                            existedRoomView.setVisibility(View.VISIBLE);
                        }
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
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("searchCourseSubroom", responseString);
                Snackbar.make(v, responseString, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void updateRoomInSocket(final Room room) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Room.updateRoomToRealm(room, realm);
            }
        });
    }
}
