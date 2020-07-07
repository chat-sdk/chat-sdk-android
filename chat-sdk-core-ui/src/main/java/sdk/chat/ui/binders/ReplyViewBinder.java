package sdk.chat.ui.binders;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.commons.ImageLoader;

import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.utils.ImageLoaderPayload;

public class ReplyViewBinder {

    public void onBind(View replyView, TextView replyTextView, ImageView replyImageView, MessageHolder holder, ImageLoader imageLoader) {
        if (replyView == null) {
            return;
        }
        if (holder.isReply()) {
            replyView.setVisibility(View.VISIBLE);

            if (holder.getQuotedImageUrl() != null && !holder.getQuotedImageUrl().isEmpty()) {

                int maxWidth = Dimen.from(replyView.getContext(), R.dimen.reply_image_width);
                int maxHeight = Dimen.from(replyView.getContext(), R.dimen.reply_image_height);

                replyImageView.setVisibility(View.VISIBLE);

                imageLoader.loadImage(replyImageView, holder.getQuotedImageUrl(), new ImageLoaderPayload(maxWidth, maxHeight, R.drawable.icn_200_image_message_placeholder, R.drawable.icn_200_image_message_loading));

//                Glide.with(replyImageView)
//                        .load(holder.getQuotedImageUrl())
//                        .dontAnimate()
//                        .placeholder(R.drawable.icn_200_image_message_placeholder)
//                        .error(R.drawable.icn_200_image_message_error)
//                        .override(maxWidth, maxHeight)
//                        .into(replyImageView);
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
