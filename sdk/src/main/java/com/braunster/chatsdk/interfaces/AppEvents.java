/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BUser;

/**
 * Created by braunster on 30/06/14.
 */

public interface AppEvents {

    int USER_DETAILS_CHANGED = 0;
    int MESSAGE_RECEIVED = 1;
    int THREAD_DETAILS_CHANGED = 2;
    int USER_ADDED_TO_THREAD = 3;
    int FOLLOWER_ADDED = 4;
    int USER_TO_FOLLOW_ADDED = 5;
    int FOLLOWER_REMOVED = 6;
    int USER_TO_FOLLOW_REMOVED = 7;
    int BLOCKED_CHANGED = 8;
    int FRIENDS_CHANGED= 9;



    boolean onUserDetailsChange(BUser user);

    boolean onMessageReceived(BMessage message);

    boolean onThreadIsAdded(String threadId);
    boolean onThreadDetailsChanged(String threadId);
    boolean onUserAddedToThread(String threadId, String userId);


    boolean onFollowerAdded(BUser follower);
    boolean onFollowerRemoved();
    boolean onUserToFollowRemoved();

    void onOnlineUsersChanged();

    void onFriendsChanged();

    void onBlockedChanged();
}
