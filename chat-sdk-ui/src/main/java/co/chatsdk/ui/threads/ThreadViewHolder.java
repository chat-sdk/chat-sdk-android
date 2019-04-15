package co.chatsdk.ui.threads;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.ViewHelper;

/**
 * Created by benjaminsmiley-andrews on 07/06/2017.
 */

public class ThreadViewHolder extends RecyclerView.ViewHolder {

    public TextView nameTextView;
    public TextView dateTextView;
    public TextView lastMessageTextView;
    public TextView unreadMessageCountTextView;
    public SimpleDraweeView imageView;
    public View indicator;

    public ThreadViewHolder(View itemView) {
        super(itemView);

        nameTextView = itemView.findViewById(R.id.chat_sdk_txt);
        lastMessageTextView = itemView.findViewById(R.id.txt_last_message);
        dateTextView = itemView.findViewById(R.id.txt_last_message_date);
        imageView = itemView.findViewById(R.id.img_thread_image);
        unreadMessageCountTextView = itemView.findViewById(R.id.txt_unread_messages);
        indicator = itemView.findViewById(R.id.chat_sdk_indicator);

    }

    public void showUnreadIndicator() {
        ViewHelper.setVisible(indicator, true);
    }

    public void hideUnreadIndicator() {
        ViewHelper.setGone(indicator, true);
    }

}
