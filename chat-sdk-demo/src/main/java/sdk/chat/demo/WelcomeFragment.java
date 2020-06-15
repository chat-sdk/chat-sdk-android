package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import butterknife.BindView;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.fragments.BaseFragment;
import io.reactivex.annotations.NonNull;

public class WelcomeFragment extends BaseFragment {

    @BindView(R2.id.button)
    Button button;

    @Override
    protected int getLayout() {
        return R.layout.fragment_welcome;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();

        return view;
    }

    @Override
    protected void initViews() {

        if (!DemoConfigBuilder.shared().isConfigured()) {
            button.setVisibility(View.INVISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
        }

        button.setOnClickListener(v -> {
            DemoConfigBuilder.shared().save(getContext());
            try {
                DemoConfigBuilder.shared().setupChatSDK(getContext());
                ChatSDK.ui().startSplashScreenActivity(getContext());
            } catch (Exception e) {
                showToast(e.getLocalizedMessage());
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });

    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }
}
