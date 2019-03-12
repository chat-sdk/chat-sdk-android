package co.chatsdk.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;

public class SplashScreenActivity extends BaseActivity {

    ConstraintLayout mainView;

    protected ImageView imageView;
    protected ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activityLayout());

        mainView = findViewById(R.id.chat_sdk_root_view);

        imageView = (ImageView) mainView.getViewById(R.id.imageView);
        progressBar = (ProgressBar) mainView.getViewById(R.id.progressBar);

        if (ChatSDK.config().logoDrawableResourceID > 0) {
            imageView.setImageResource(ChatSDK.config().logoDrawableResourceID);
        }

        stopProgressBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (ChatSDK.auth().isAuthenticatedThisSession()) {
            startMainActivity();
        } else if (ChatSDK.auth().isAuthenticated()) {
            startProgressBar();
            ChatSDK.auth().authenticate().doFinally(() -> stopProgressBar()).subscribe(() -> {
                // Launch the Chat SDK
                startMainActivity();
            }, throwable -> {
                // Show the login screen
                startLoginActivity();
            });
        } else {
            startLoginActivity();
        }

    }

    protected void startMainActivity () {
        ChatSDK.ui().startMainActivity(this, extras);
    }

    protected void startLoginActivity () {
        ChatSDK.ui().startLoginActivity(this, extras);
    }

    protected void startProgressBar () {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate();
    }

    protected void stopProgressBar () {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected @LayoutRes
    int activityLayout() {
        return R.layout.chat_sdk_activity_splash_screen;
    }

}
