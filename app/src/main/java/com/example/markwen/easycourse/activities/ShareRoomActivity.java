package com.example.markwen.easycourse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.RoomListViewAdapter;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.utils.SocketIO;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by nisarg on 5/1/17.
 */

public class ShareRoomActivity extends AppCompatActivity {

    private static final String TAG = "ShareRoomActivity";

    @BindView(R.id.rooms)
    ListView roomsList;
    @BindView(R.id.toolbarShareRoom)
    Toolbar toolbar;

    private Realm realm;
    private SocketIO socketIO;

    RealmResults<Room> rooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_room);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent i = getIntent();
        final String roomShareId = i.getStringExtra("roomID");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Share Room");

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();

        rooms = realm.where(Room.class).findAll();

        RoomListViewAdapter adapter = new RoomListViewAdapter(getApplicationContext(), rooms);
        roomsList.setAdapter(adapter);
        roomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = rooms.get(position);

//                try {
//                    socketIO.sendMessage(null, room.getId(), null, roomShareId, null, 0, 0);
//                } catch (JSONException e) {
//                    Log.e(TAG, e.toString());
//                }

                finish();
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
