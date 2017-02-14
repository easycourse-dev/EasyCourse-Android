package io.easycourse.www.easycourse.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
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
import io.easycourse.www.easycourse.utils.JSONUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Ack;

/**
 * Created by noahrinehart on 2/1/17.
 */

public class CourseDetailsFragment extends BaseFragment {


    Course course;
    String courseId;
    boolean isJoinIn;
    int creditHrs;
    University university;
    String universityName;
    ArrayList<Room> courseRooms = new ArrayList<>();
    CourseDetailsRoomsRecyclerViewAdapter roomsAdapter;
    CourseDetailsRoomsEndlessRecyclerViewScrollListener scrollListener;

    @BindView(R.id.courseDetailsToolbar)
    Toolbar toolbar;
    @BindView(R.id.courseDetailsCourseName)
    TextView courseNameView;
    @BindView(R.id.courseDetailsTitle)
    TextView titleView;
    @BindView(R.id.courseDetailsCreditHrs)
    TextView creditHrsView;
    @BindView(R.id.courseDetailsJoinCourse)
    Button joinCourseButton;
    @BindView(R.id.courseDetailsUniv)
    TextView univView;
    @BindView(R.id.courseDetailsRoomsView)
    RecyclerView roomsView;


    public static CourseDetailsFragment newInstance(String courseId, boolean isJoinIn) {
        CourseDetailsFragment courseDetailsFragment = new CourseDetailsFragment();
        courseDetailsFragment.courseId = courseId;
        courseDetailsFragment.isJoinIn = isJoinIn;
        return courseDetailsFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_details, container, false);
        ButterKnife.bind(this, v);

        course = realm.where(Course.class).equalTo("id", courseId).findFirst();
        if (course != null) {
            creditHrs = course.getCreditHours();
            university = realm.where(University.class).equalTo("id", course.getUniversityID()).findFirst();
            if (university != null) universityName = university.getName();
            setupTextViews();
        }
        setupJoinedButton();
        joinCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isJoinIn) {
                    showDropCourseDialog();
                } else {
                    joinCourse();
                }
            }
        });

        searchSubRooms(0);

        setupRecyclerView();

        fetchCourseInfo();
        setupTextViews();

        return v;
    }

    private void setupTextViews() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Course Details");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        }

        courseNameView.setText(course.getCoursename());
        titleView.setText(course.getTitle());
        String creditString;
        if (creditHrs == 1) {
            creditString = "1 credit";
        } else {
            creditString = creditHrs + " credits";
        }
        creditHrsView.setText(creditString);
        univView.setText(universityName);
    }

    private void setupJoinedButton() {
        if (course != null) {
            if (isJoinIn) {
                joinCourseButton.setText(getResources().getString(R.string.joined));
                joinCourseButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.course_details_joined_button));
            } else {
                joinCourseButton.setText(getResources().getString(R.string.join));
                joinCourseButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.course_details_join_button));
            }
        }
    }


    private void setupRecyclerView() {
        // Set up rooms RecyclerView
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(getContext());
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsAdapter = new CourseDetailsRoomsRecyclerViewAdapter(courseRooms, socketIO, isJoinIn, realm, (AppCompatActivity) getActivity());
        roomsView.setHasFixedSize(true);
        roomsView.setLayoutManager(roomsLayoutManager);
        roomsView.addItemDecoration(new RecyclerViewDivider(getContext()));
        scrollListener = new CourseDetailsRoomsEndlessRecyclerViewScrollListener(roomsLayoutManager, roomsAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                searchSubRooms(page);
            }
        };
        roomsView.addOnScrollListener(scrollListener);
        roomsView.setAdapter(roomsAdapter);
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void searchSubRooms(final int skip) {
        APIFunctions.searchCourseSubroom(getContext(), courseId, "", 20, skip, new JsonHttpResponseHandler() {
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
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("searchCourseSubroom", responseString, throwable);
                Snackbar.make(roomsView, responseString, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void notifyRecyclerView() {
        if (roomsAdapter == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showDropCourseDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title("Dropping " + course.getCoursename() + "?")
                .content("If you drop this course, then you will automatically quit all the rooms belonging to " + course.getCoursename() + ".")
                .negativeText("Maybe Not")
                .positiveText("Drop It")
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.colorLogout, null))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                                            isJoinIn = false;
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
                })
                .build();

        dialog.show();
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
                        isJoinIn = true;
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

    private void courseUpdateView(final String courseId, final String courseName, final ArrayList<Room> roomsJoined) {
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                synchronized (this) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateButtonView(isJoinIn);
//                            courseRooms.clear();
//                            roomsAdapter.updateCourse(isJoinIn, roomsJoined);
//                            doSearchRoom(0, courseId);
//                        }
//                    });
//                }
//            }
//        };
//        thread.start();
    }

}