package co.chatsdk.android.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.Serializable;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.ui.chat.viewholder.ImageMessageViewHolder;
import co.chatsdk.ui.chat.viewholder.TextMessageViewHolder;
import io.reactivex.subjects.PublishSubject;

public class SnapImageMessageViewHolder extends TextMessageViewHolder implements Serializable {

    public SnapImageMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView, activity, actionPublishSubject);
    }

    public Boolean hasMessageBeenSeen(Message message) {
        return message.valueForKey("image-seen") != null;
    }

    @Override
    public void setMessage(Message message) {
        super.setMessage(message);
        setImageHidden(true);
        setIconHidden(false);

        if (!hasMessageBeenSeen(message)) {
            messageIconView.setImageResource(R.drawable.ic_crop_original_green_a700_24dp);
        } else {
            messageIconView.setImageResource(R.drawable.ic_crop_original_red_800_24dp);
        }
    }

    @Override
    public void onClick(View v) {

        if (message != null && !hasMessageBeenSeen(message)) {
            Intent i = new Intent(activity.get(), SnapImageViewActivity.class);
            Object messageLifetimeObject = message.valueForKey("message-lifetime");
            if (messageLifetimeObject instanceof Integer) {
                Integer lifetime = (Integer) messageLifetimeObject;
                i.putExtra("lifetime", lifetime);
            } else {
                Toast.makeText(activity.get(),"ERROR: Message Lifetime Object is not an Object", Toast.LENGTH_LONG).show();
            }
            i.putExtra("imageURL", message.stringForKey(Keys.ImageUrl));
            String messageEntityID = message.getEntityID();
            i.putExtra("messageEntityID", messageEntityID);
            activity.get().startActivity(i);
        }
        else if (hasMessageBeenSeen(message)) {
            Toast.makeText(activity.get(),"You have already seen this image", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(activity.get(),"ERROR: Message is null", Toast.LENGTH_LONG).show();
        }
    }
}