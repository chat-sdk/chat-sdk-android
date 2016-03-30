/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;

import org.jdeferred.Deferred;

/**
 * Created by braunster on 12/11/14.
 */
public abstract class AbstractEventManager {
/*
    *//**Set listener to thread details change.*//*
    public abstract void handleThreadDetails(final String threadId);

    *//** Set listener to users that are added to thread.*//*
    public abstract void handleUsersAddedToThread(final String threadId);

    *//** Handle user details change.*//*
    public abstract void handleUsersDetailsChange(String userID);

    public abstract void handleUserFollowDataChange(String userID);

    *//** Handle incoming messages for thread.*//*
    public abstract void handleMessages(String threadId);

    *//** Hnadle the thread by given id, If thread is not handled already a listener
     * to thread details change will be assigned. After details received the messages and added users listeners will be assign.*//*
    public abstract void handleThread(final String threadID);*/

    public abstract void userOn(final BUser user);

    public abstract void userOff(BUser user);

    /**
     * Handle user meta change.
     **/
    public abstract void userMetaOn(String userID, Deferred<Void, Void, Void> promise);

    /**
     * Stop handling user meta change.
     **/
    public abstract void userMetaOff(String userID);

    public abstract void threadUsersAddedOn(String threadId);

    public abstract void threadUsersAddedOff(String threadId);

    public abstract void messagesOn(String threadId, Deferred<BThread, Void , Void> deferred);

    public abstract void messagesOff(String threadId);

    public abstract void threadOn(String threadId, Deferred<BThread, Void, Void> deferred);

    public abstract void threadOff(String threadId);





    public abstract boolean isListeningToThread(String entityID);

    public abstract void addEvent(Event appEvents);

    /** Removes an app event by tag.*/
    public abstract boolean removeEventByTag(String tag);

    /** Check if there is a AppEvent listener with the currnt tag, Could be AppEvent or one of his child(MessageEventListener, ThreadEventListener, UserEventListener).
     * @return true if found.*/
    public abstract boolean isEventTagExist(String tag);


    /**
     * Removes all the events from the event manger.
     **/
    public abstract void removeAll();

}
