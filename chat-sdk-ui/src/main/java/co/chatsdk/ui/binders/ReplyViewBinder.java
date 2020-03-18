package co.chatsdk.ui.binders;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import co.chatsdk.core.utils.Dimen;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;

public class ReplyViewBinder {

    public static void onBind(View replyView, TextView replyTextView, ImageView replyImageView, MessageHolder holder) {
        if (replyView == null) {
            return;
        }
        if (holder.isReply()) {
            replyView.setVisibility(View.VISIBLE);

            if (holder.getQuotedImageUrl() != null && !holder.getQuotedImageUrl().isEmpty()) {

                int maxWidth = Dimen.from(replyView.getContext(), R.dimen.reply_image_width);
                int maxHeight = Dimen.from(replyView.getContext(), R.dimen.reply_image_height);

                replyImageView.setVisibility(View.VISIBLE);
                Glide.with(replyImageView)
                        .load(holder.getQuotedImageUrl())
                        .dontAnimate()
                        .placeholder(R.drawable.icn_200_image_message_placeholder)
                        .error(R.drawable.icn_200_image_message_error)
                        .override(maxWidth, maxHeight)
                        .into(replyImageView);
            } else {
                replyImageView.setVisibility(View.GONE);
            }

            // Build the string for the textView
            replyTextView.setText(Html.fromHtml("<b>" + holder.getUser().getName() + "</b><br/>" + holder.getQuotedText()));

        } else {
            replyView.setVisibility(View.GONE);
        }
    }

}
