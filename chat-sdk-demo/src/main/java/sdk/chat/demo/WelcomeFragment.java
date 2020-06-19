package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import io.reactivex.annotations.NonNull;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.fragments.BaseFragment;

public class WelcomeFragment extends BaseFragment {

    @BindView(R2.id.button)
    public Button button;

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
                e.printStackTrace();
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
