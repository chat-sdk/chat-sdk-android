package co.chatsdk.firebase.ui;

import android.content.Context;

import co.chatsdk.ui.manager.BaseInterfaceAdapter;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIInterfaceAdapter extends BaseInterfaceAdapter {

    public FirebaseUIInterfaceAdapter(Context context) {
        super(context);
    }

    @Override
    public Class getLoginActivity() {
        return FirebaseUIModule.shared().getSplashScreenActivity();
    }

}
