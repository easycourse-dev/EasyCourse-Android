package io.easycourse.www.easycourse.activities;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.easycourse.www.easycourse.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_NOTIFICATIONS = "prefNotifications";
    public static final String KEY_PREF_VIBRATE = "prefVibrate";
    public static final String KEY_PREF_SOUND = "prefSound";

    @BindView(R.id.toolbarSettings)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .replace(R.id.contentSettings, new SettingsFragment())
                .commit();
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {


        Preference prefNotification;
        Preference prefVibrate;
        Preference prefSound;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            prefNotification = getPreferenceManager().findPreference(KEY_PREF_NOTIFICATIONS);
            prefVibrate = getPreferenceManager().findPreference(KEY_PREF_VIBRATE);
            prefSound = getPreferenceScreen().findPreference(KEY_PREF_SOUND);


        }


    }

}
