package sdk.chat.ui.views.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import sdk.chat.ui.R;

public class OutgoingTimeView extends LinearLayout {

    public TextView messageTime;
    public ImageView readStatus;

    public OutgoingTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public @LayoutRes
    int getLayout() {
        return R.layout.view_outgoing_message_time;
    }

    public void initViews() {
        LayoutInflater.from(getContext()).inflate(getLayout(), this);
        messageTime = findViewById(R.id.messageTime);
        readStatus = findViewById(R.id.readStatus);
    }
}
