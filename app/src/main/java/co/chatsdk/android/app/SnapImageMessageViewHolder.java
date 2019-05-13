package co.chatsdk.android.app;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.ui.chat.viewholder.ImageMessageViewHolder;
import io.reactivex.subjects.PublishSubject;

public class SnapImageMessageViewHolder extends ImageMessageViewHolder implements Serializable {

    public SnapImageMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView, activity, actionPublishSubject);
    }

    @Override
    public void onClick(View v) {

        if (message != null) {
            Intent i = new Intent(activity.get(), SnapImageViewActivity.class);
            Object messageLifetimeObject = message.valueForKey("message-lifetime");
            if (messageLifetimeObject instanceof Integer) {
                Integer lifetime = (Integer) messageLifetimeObject;
                i.putExtra("lifetime", lifetime);
            } else {
                Toast.makeText(activity.get(),"ERROR: Message Lifetime Object is not an Object", Toast.LENGTH_LONG).show();
            }
            i.putExtra("imageURL", getImageURL());
            String messageEntityID = message.getEntityID();
            i.putExtra("messageEntityID", messageEntityID);
            activity.get().startActivity(i);
        }
    }
}