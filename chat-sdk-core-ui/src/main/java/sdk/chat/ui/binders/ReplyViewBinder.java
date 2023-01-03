package sdk.chat.ui.binders;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;

public class ReplyViewBinder {

    public void onBind(View replyView, TextView replyTextView, ImageView replyImageView, MessageHolder holder) {
        if (replyView == null) {
            return;
        }
        if (holder.isReply()) {
            replyView.setVisibility(View.VISIBLE);

            if (holder.getQuotedImageUrl() != null && !holder.getQuotedImageUrl().isEmpty()) {
                replyImageView.setVisibility(View.VISIBLE);
                ChatSDKUI.provider().imageLoader().loadReply(replyImageView, holder.getQuotedImageUrl(), holder.getQuotedPlaceholder());
            } else {
                replyImageView.setVisibility(View.GONE);
            }

            User fromUser = null;

            // Get the user the message is from
            String replyUserId = holder.getMessage().stringForKey("from");
            if (replyUserId != null && !replyUserId.isEmpty()) {
                fromUser = ChatSDK.core().getUserNowForEntityID(replyUserId);
            }
            if (fromUser == null) {
                String replyMessageId = holder.getMessage().stringForKey("id");

                if (replyMessageId != null && !replyMessageId.isEmpty()) {
                    // Get the message
                    Message message = ChatSDK.db().fetchMessageWithEntityID(replyMessageId);
                    if (message != null) {
                        fromUser = message.getSender();
                    }
                }
            }

            if (fromUser != null) {
                replyTextView.setText(Html.fromHtml("<b>" + fromUser.getName() + "</b><br/>" + holder.getQuotedText()));
            } else {
                replyTextView.setText(Html.fromHtml(holder.getQuotedText()));
            }

            // Build the string for the textView

        } else {
            replyView.setVisibility(View.GONE);
        }

    }

}
