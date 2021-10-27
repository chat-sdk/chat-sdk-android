package sdk.chat.app.xmpp.wow;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import butterknife.BindView;
import sdk.chat.demo.xmpp.R;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.options.MediaChatOption;
import sdk.chat.ui.chat.options.MediaType;

public class WowChatActivity extends ChatActivity {

    MediaChatOption option;

    @BindView(R.id.overlayView) View overlayView;

    protected @LayoutRes
    int getLayout() {
        return R.layout.wow_activity_chat;
    }

    public int overlayHeight = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.chatView.setBackgroundColor(getResources().getColor(R.color.wowBackground));

        option = new MediaChatOption(getResources().getString(sdk.chat.ui.R.string.image_or_photo), MediaType.choosePhoto());

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

    public void showOptions() {
        // We don't want to remove the user if we load another activity
        // Like the sticker activity
        removeUserFromChatOnExit = false;

        option.execute(this, this.thread).subscribe();

//        if (overlayView.getLayoutParams().height > 0) {
//            hideOverlay();
//        } else {
//            showOverlay();
//        }
    }


}
