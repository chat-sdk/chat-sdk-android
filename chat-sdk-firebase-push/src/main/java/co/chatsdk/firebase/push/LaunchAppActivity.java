package co.chatsdk.firebase.push;

import android.content.Intent;
import android.os.Bundle;

import co.chatsdk.ui.main.BaseActivity;
import timber.log.Timber;

/**
 * Created by ben on 5/10/18.
 */

public class LaunchAppActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.v("Activity");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.v("Activity");

    }


}
