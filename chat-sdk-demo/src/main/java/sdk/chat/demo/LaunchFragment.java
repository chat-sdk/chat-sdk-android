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
import sdk.guru.common.RX;

public class LaunchFragment extends BaseFragment {

    Button button;

    @Override
    protected int getLayout() {
        return R.layout.fragment_launch;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        button = view.findViewById(R.id.button);

        initViews();

        return view;
    }


    @Override
    protected void initViews() {
        button.setOnClickListener(v -> {

            DemoConfigBuilder.Database database = DemoConfigBuilder.shared().getDatabase();
            if (database == DemoConfigBuilder.Database.Custom) {
//                if (!new ServerKeyStorage(getContext()).valid()) {
//                    showToast("XMPP Server Address must be set");
//                    return;
//                }
            }
            DemoConfigBuilder.shared().save(getContext());

            try {
                DemoConfigBuilder.shared().setupChatSDK(getContext());

                if (ChatSDK.a() != null) {
                    button.setEnabled(false);
                    ChatSDK.ui().startSplashScreenActivity(getContext());
                } else {
                    showToast("Something went wrong! Please contact team@sdk.chat");
                    dm.add(ChatSDK.auth().logout().observeOn(RX.main()).subscribe(() -> ChatSDK.ui().startSplashScreenActivity(getContext())));
                }
            } catch (Exception e) {
                showToast(e.getLocalizedMessage());
//                FirebaseCrashlytics.getInstance().recordException(e);
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

    @Override
    public void onResume() {
        super.onResume();
        button.setEnabled(true);
    }
}
