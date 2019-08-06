package co.chatsdk.ui.chat.viewholder;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.ui.chat.LocationMessageOnClickHandler;
import io.reactivex.subjects.PublishSubject;

public class LocationMessageViewHolder extends BaseMessageViewHolder {
    public LocationMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView, activity, actionPublishSubject);
    }

    @Override
    public void setMessage(Message message) {
        super.setMessage(message);

        setImageHidden(false);

        int viewWidth = maxWidth();
        int viewHeight = maxHeight();

        LatLng latLng = getLatLng();
        messageImageView.setImageURI(GoogleUtils.getMapImageURL(latLng, viewWidth, viewHeight));

    }

    @Override
    public void onClick (View v) {
        super.onClick(v);
        if (message != null) {
            LocationMessageOnClickHandler.onClick(activity.get(), getLatLng());
        }
    }

    public LatLng getLatLng() {
        double longitude = message.doubleForKey(Keys.MessageLongitude);
        double latitude = message.doubleForKey(Keys.MessageLatitude);
        return new LatLng(latitude, longitude);
    }

}
