package sdk.chat.ui.chat.model;

import com.stfalcon.chatkit.commons.models.IUser;

import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.DisposableMap;

public class UserHolder implements IUser {

    protected User user;
    protected DisposableMap dm = new DisposableMap();
    protected boolean isDirty = false;

    protected boolean isOnline = false;
    protected String name = null;

    public UserHolder(User user) {
        this.user = user;

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(
                        EventType.UserPresenceUpdated
                ))
                .filter(NetworkEvent.filterUserEntityID(getId()))
                .subscribe(networkEvent -> {
                    updateIsOnline();
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(
                        EventType.UserMetaUpdated
                ))
                .filter(NetworkEvent.filterUserEntityID(getId()))
                .subscribe(networkEvent -> {
                    updateName();
                }));

        updateIsOnline();
        updateName();
    }

    public void updateIsOnline() {
        boolean isOnline = user.getIsOnline();
        isDirty = isDirty || isOnline != this.isOnline;
        this.isOnline = isOnline;
    }

    public void updateName() {
        String name = user.getName();
        isDirty = isDirty || StringChecker.areEqual(name, this.name);
        this.name = name;
    }

    @Override
    public String getId() {
        return user.getEntityID();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getAvatar() {
        if (!UIModule.config().showAvatarInChatView) {
            return null;
        }
        String url = user.getAvatarURL();
        if (url == null) {
            url =  ImageUtils.uriForResourceId(ChatSDK.ctx(), UIModule.config().defaultProfilePlaceholder).toString();
        }

        return url;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof UserHolder && getId().equals(((UserHolder)object).getId());
    }

    public User getUser() {
        return user;
    }

    public boolean isOnline() {
        return user.getIsOnline();
    }

    public boolean isMe() {
        return user.isMe();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void makeClean() {
        isDirty = false;
    }

}
