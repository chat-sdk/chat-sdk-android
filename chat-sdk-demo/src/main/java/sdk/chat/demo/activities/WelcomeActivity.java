package sdk.chat.demo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import butterknife.BindView;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.DemoConfigBuilder;
import sdk.chat.demo.R;
import sdk.chat.demo.R2;
import sdk.chat.demo.WelcomeFragment;

public class WelcomeActivity extends AbstractDemoActivity {

    @BindView(R2.id.launch)
    protected FloatingActionButton launch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatSDK.shared().stop();

        DemoConfigBuilder.shared().load(this);

        WelcomeFragment fragment = (WelcomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null) {
            fragment.button.setVisibility(View.INVISIBLE);
        }

        launch.setImageResource(R.drawable.icons8_launched_rocket_filled);
        if (!DemoConfigBuilder.shared().isConfigured()) {
            launch.setVisibility(View.INVISIBLE);
        } else {
            launch.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.icons8_wrench);
        }

        launch.setOnClickListener(v -> {
            launch.setEnabled(false);
            try {
                DemoConfigBuilder.shared().setupChatSDK(this);

                if (ChatSDK.shared().isValid()) {
                    ChatSDK.ui().startSplashScreenActivity(this);
                } else {
                    showToast("Something went wrong! Please contact team@sdk.chat");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Something went wrong! Chat SDK init failed"));
                }
            } catch (Exception e) {
                showToast(e.getLocalizedMessage());
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
                launch.setEnabled(true);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        launch.setEnabled(true);
    }


    @Override
    protected int getLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void next() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }
}
