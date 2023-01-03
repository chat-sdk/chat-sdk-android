package sdk.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.LifecycleService;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.R;
import sdk.guru.common.RX;

public class SplashScreenActivity extends BaseActivity {

    public static int AUTH = 1;

    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected ConstraintLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_splash_screen;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(getBaseContext(), LifecycleService.class));

        // TODO: This better me imageView and not image_view.
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        root = findViewById(R.id.root);


        imageView.setImageResource(ChatSDK.config().logoDrawableResourceID);

        stopProgressBar();

        if (getActionBar() != null) {
            getActionBar().hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ChatSDK.shared().isActive()) {
            startNextActivity();
        } else {
            ChatSDK.shared().addOnActivateListener(() -> {
                startNextActivity();
            });
        }
    }

    protected void startNextActivity() {

        stopProgressBar();
        if (ChatSDK.auth() != null) {
            if (ChatSDK.auth().isAuthenticatedThisSession()) {
                startMainActivity();
                return;
            } else if (ChatSDK.auth().cachedCredentialsAvailable()) {
                startProgressBar();
                dm.add(ChatSDK.auth().authenticate()
                        .observeOn(RX.main())
                        .doFinally(this::stopProgressBar)
                        .subscribe(this::startMainActivity, throwable -> startLoginActivity()));
                return;
            }
        }
        startLoginActivity();
    }

    protected void startMainActivity() {
        if (StringChecker.isNullOrEmpty(ChatSDK.currentUser().getName())) {
            ChatSDK.ui().startPostRegistrationActivity(this, null);
        } else {
            ChatSDK.ui().startMainActivity(this, null);
        }
    }

    protected void startLoginActivity() {
        startActivityForResult(ChatSDK.ui().getLoginIntent(this, null), AUTH);
    }

    protected void startProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.animate();
    }

    protected void stopProgressBar() {
        progressBar.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
    }

}
