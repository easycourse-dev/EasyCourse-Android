package io.easycourse.www.easycourse.fragments.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.components.main.UserProfile.LanguageRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Language;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.APIFunctions;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;
import io.realm.RealmList;

public class LanguageSetting extends AppCompatActivity {

    @BindView(R.id.languagetoobar)
    Toolbar toolbar;
    @BindView(R.id.existedlang)
    RecyclerView languageView;
    User user = new User();

    Realm realm;
    SocketIO socket;
    LanguageRecyclerViewAdapter languageAdapter;
    RealmList<Language> userLanguages;
    RealmList<Language> allLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Language Settings");
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               LanguageSetting.this.onBackPressed();
            }
        });

        socket = EasyCourse.getAppInstance().getSocketIO();
        realm = Realm.getDefaultInstance();
        user = User.getCurrentUser(this, realm);
        userLanguages = Language.getCheckedLanguages(realm);
        allLanguages = Language.getCheckedLanguages(realm);
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(this);
        roomsLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        languageAdapter = new LanguageRecyclerViewAdapter(allLanguages);
        languageAdapter.setCheckedLanguageList(userLanguages);
        languageView.setHasFixedSize(true);
        languageView.setLayoutManager(roomsLayoutManager);
        languageView.addItemDecoration(new RecyclerViewDivider(this));
        languageView.setAdapter(languageAdapter);
        APIFunctions.getLanguages(this, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    allLanguages.clear();
                    languageAdapter.setLanguageList(new RealmList<Language>());
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            if(response.getJSONObject(i).getString("name").equals("Arabic"))
                                allLanguages.add(new Language(
                                        response.getJSONObject(i).getString("name"),
                                        response.getJSONObject(i).getString("code"),
                                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"+
                                                response.getJSONObject(i).getString("translation")
                                ));
                            else
                            allLanguages.add(new Language(
                                    response.getJSONObject(i).getString("name"),
                                    response.getJSONObject(i).getString("code"),
                                    response.getJSONObject(i).getString("translation")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    languageAdapter.setLanguageList(allLanguages);
                    languageAdapter.setCheckable(true);
                    languageAdapter.notifyDataSetChanged();
                }
            });
        languageView.setOnClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                call( position );
            }
        });

    }
}
