package sdk.chat.app.xmpp.wow;

import android.view.View;

import sdk.chat.ui.chat.model.ImageMessageHolder;
import sdk.chat.ui.utils.ImageLoaderPayload;
import sdk.chat.ui.view_holders.OutcomingImageMessageViewHolder;

public class WowOutcomingImageMessageViewHolder extends OutcomingImageMessageViewHolder {
    public WowOutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    protected Object getPayloadForImageLoader(ImageMessageHolder message) {
        ImageLoaderPayload payload = new ImageLoaderPayload(message.placeholder());
        payload.ar = 1.6f;
        return payload;
    }

}
