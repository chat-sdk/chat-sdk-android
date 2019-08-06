package co.chatsdk.ui.chat.viewholder;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ImageMessageOnClickHandler;
import io.reactivex.subjects.PublishSubject;

public class ImageMessageViewHolder extends BaseMessageViewHolder {
    public ImageMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView, activity, actionPublishSubject);
    }

    @Override
    public void setMessage(Message message) {
        super.setMessage(message);

        setImageHidden(false);

        int viewWidth = maxWidth();
        int viewHeight = maxHeight();

        String url = getImageURL();

        if (url != null && url.length() > 0) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setResizeOptions(new ResizeOptions(viewWidth, viewHeight))
                    .build();

            messageImageView.setController(
                    Fresco.newDraweeControllerBuilder()
                            .setOldController(messageImageView.getController())
                            .setImageRequest(request)
                            .build());
        } else {
            // Loads the placeholder
            messageImageView.setActualImageResource(R.drawable.icn_200_image_message_loading);
        }
    }

    @Override
    public void onClick (View v) {
        super.onClick(v);
        if (message != null) {
            ImageMessageOnClickHandler.onClick(activity.get(), v, getImageURL());
        }
    }

    public String getImageURL () {
        return message.stringForKey(Keys.MessageImageURL);
    }
}
