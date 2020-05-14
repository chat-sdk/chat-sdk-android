package sdk.chat.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.xmpp.utils.ServerKeyStorage;
import io.reactivex.annotations.NonNull;
import sdk.chat.realtime.R;
import sdk.chat.realtime.R2;
import sdk.guru.common.RX;

public class LaunchFragment extends BaseFragment {
    @BindView(R2.id.button)
    Button button;

    @Override
    protected int getLayout() {
        return R.layout.fragment_launch;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();

        return view;
    }


    @Override
    protected void initViews() {
        button.setOnClickListener(v -> {
            DemoConfigBuilder.Database database = DemoConfigBuilder.shared().getDatabase();
            if (database == DemoConfigBuilder.Database.Custom) {
                if (!new ServerKeyStorage(getContext()).valid()) {
                    showToast("XMPP Server Address must be set");
                    return;
                }
            }
            DemoConfigBuilder.shared().save(getContext());
            DemoConfigBuilder.shared().setupChatSDK(getContext());

            dm.add(ChatSDK.auth().logout().observeOn(RX.main()).subscribe(() -> ChatSDK.ui().startSplashScreenActivity(getContext())));

        });
    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }
}
