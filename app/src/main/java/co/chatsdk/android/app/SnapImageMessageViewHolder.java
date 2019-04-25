package co.chatsdk.android.app;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import co.chatsdk.ui.chat.viewholder.ImageMessageViewHolder;

public class SnapImageMessageViewHolder extends ImageMessageViewHolder implements Serializable {

    public SnapImageMessageViewHolder(View itemView, Activity activity) {
        super(itemView, activity);
    }

    @Override
    public void onClick(View v) {

        if (message != null) {
            Intent i = new Intent(activity, SnapImageViewActivity.class);
            Object messageLifetimeObject = message.valueForKey("message-lifetime");
            if (messageLifetimeObject instanceof Integer) {
                Integer lifetime = (Integer) messageLifetimeObject;
                i.putExtra("lifetime", lifetime);
            } else {
                Toast.makeText(activity,"ERROR: Message Lifetime Object is not an Object", Toast.LENGTH_LONG).show();
            }
            i.putExtra("imageURL", getImageURL());
            String messageEntityID = message.getEntityID();
            i.putExtra("messageEntityID", messageEntityID);
            activity.startActivity(i);
        }
    }
}