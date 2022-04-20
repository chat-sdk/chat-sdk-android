package sdk.chat.app.xmpp.wow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import butterknife.BindView;
import sdk.chat.core.dao.Thread;
import sdk.chat.demo.xmpp.R;
import sdk.chat.ui.chat.options.MediaChatOption;
import sdk.chat.ui.chat.options.MediaType;
import sdk.chat.ui.fragments.ChatFragment;

public class WowChatFragment extends ChatFragment {

    public WowChatFragment(Thread thread, Delegate delegate) {
        super(thread, delegate);
    }

    MediaChatOption option;

    @BindView(R.id.overlayView)
    View overlayView;

    protected @LayoutRes
    int getLayout() {
        return R.layout.wow_activity_chat;
    }

    public int overlayHeight = 500;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.chatView.setBackgroundColor(getResources().getColor(R.color.wowBackground));
        option = new MediaChatOption(R.string.image_or_photo, R.drawable.icn_100_gallery, MediaType.choosePhoto());
        return view;
    }

    public void showOverlay() {
        overlayView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, overlayHeight));

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin + overlayHeight);
        chatView.setLayoutParams(params);
    }

    public void hideOverlay() {
        overlayView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) chatView.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin - overlayHeight);
        chatView.setLayoutParams(params);

    }

    public void toggleOptions() {
        // We don't want to remove the user if we load another activity
        // Like the sticker activity
        removeUserFromChatOnExit = false;

        if (getActivity() != null) {
            option.execute(getActivity(), this.thread).subscribe();
        }

//        if (overlayView.getLayoutParams().height > 0) {
//            hideOverlay();
//        } else {
//            showOverlay();
//        }
    }
}
