package co.chatsdk.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import butterknife.BindView;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends BaseActivity {

    public static int AUTH = 1;

    protected ActivitySplashScreenBinding b;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        b.imageView.setImageResource(ChatSDK.config().logoDrawableResourceID);

        stopProgressBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startNextActivity();
    }

    protected void startNextActivity () {
        if (ChatSDK.auth().isAuthenticatedThisSession()) {
            startMainActivity();
        } else if (ChatSDK.auth().isAuthenticated()) {
            startProgressBar();
            if (ChatSDK.auth().isAuthenticating()) {
                ChatSDK.hook().addHook(Hook.sync(data -> {
                    stopProgressBar();
                    startMainActivity();
                }, true), HookEvent.DidAuthenticate);
            } else {
                dm.add(ChatSDK.auth().authenticate()
                        .doFinally(this::stopProgressBar)
                        .subscribe(this::startMainActivity, throwable -> startLoginActivity()));
            }
        } else {
            startLoginActivity();
        }
    }

    protected void startMainActivity () {
        ChatSDK.ui().startMainActivity(this, extras);
    }

    protected void startLoginActivity () {
        startActivityForResult(ChatSDK.ui().getLoginIntent(this, extras), AUTH);
    }

    protected void startProgressBar () {
        b.progressBar.setVisibility(View.VISIBLE);
        b.progressBar.setIndeterminate(true);
        b.progressBar.animate();
    }

    protected void stopProgressBar () {
//        progressBar.setVisibility(View.GONE);
        b.progressBar.setProgress(0);
        b.progressBar.setIndeterminate(false);
    }


    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_splash_screen;
    }

}
