package co.chatsdk.ui.chat.model;

import co.chatsdk.ui.R;
import co.chatsdk.ui.module.DefaultUIModule;
import sdk.chat.core.dao.User;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;

public class SystemUserHolder extends UserHolder {
    public SystemUserHolder(User user) {
        super(user);
    }

    @Override
    public String getAvatar() {
        return ImageUtils.uriForResourceId(ChatSDK.ctx(), R.drawable.icn_50_system).toString();
    }

}
