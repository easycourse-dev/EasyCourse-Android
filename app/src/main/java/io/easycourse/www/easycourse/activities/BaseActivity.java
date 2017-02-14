package io.easycourse.www.easycourse.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;

/**
 * Created by noahrinehart on 2/11/17.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected Realm realm;
    protected SocketIO socketIO;
    protected User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        currentUser = User.getCurrentUser(this, realm);
        EasyCourse.bus.register(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
