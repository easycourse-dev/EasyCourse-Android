package io.easycourse.www.easycourse.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.realm.Realm;

/**
 * Created by noahrinehart on 2/11/17.
 */

public abstract class BaseFragment extends Fragment {


    // Make sure to create custom onDestroy to close realm
    protected Realm realm;
    protected SocketIO socketIO;
    protected User currentUser;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
        currentUser = User.getCurrentUser(getContext(), realm);
    }

}


