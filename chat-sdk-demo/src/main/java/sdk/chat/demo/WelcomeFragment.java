package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.fragments.BaseFragment;
import io.reactivex.annotations.NonNull;
import sdk.chat.micro.R;
import sdk.chat.micro.R2;

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
            DemoConfigBuilder.shared().setupChatSDK(getContext());
            ChatSDK.ui().startSplashScreenActivity(getContext());
        });

    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }
}
