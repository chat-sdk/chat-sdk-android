package sdk.chat.ui.chat.model;

import sdk.chat.core.dao.User;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;

public class SystemUserHolder extends UserHolder {
    public SystemUserHolder(User user) {
        super(user);
    }

    @Override
    public String getAvatar() {
        return ImageUtils.uriForResourceId(ChatSDK.ctx(), R.drawable.icn_50_system).toString();
    }

}
