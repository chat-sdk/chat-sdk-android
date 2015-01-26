package com.braunster.chatsdk.network.events;

import com.braunster.chatsdk.dao.BUser;

/**
 * Created by braunster on 12/11/14.
 */
public abstract class AbstractEventManager {

    /**Set listener to thread details change.*/
    public abstract void handleThreadDetails(final String threadId);

    /** Set listener to users that are added to thread.*/
    public abstract void handleUsersAddedToThread(final String threadId);

    /** Handle user details change.*/
    public abstract void handleUsersDetailsChange(String userID);

    public abstract void handleUserFollowDataChange(String userID);

    /** Handle incoming messages for thread.*/
    public abstract void handleMessages(String threadId);

    /** Hnadle the thread by given id, If thread is not handled already a listener
     * to thread details change will be assigned. After details received the messages and added users listeners will be assign.*/
    public abstract void handleThread(final String threadID);

    public abstract void observeUser(final BUser user);

    public abstract boolean isListeningToThread(String entityID);

    public abstract void addEvent(Event appEvents);

    /** Removes an app event by tag.*/
    public abstract boolean removeEventByTag(String tag);

    /** Check if there is a AppEvent listener with the currnt tag, Could be AppEvent or one of his child(MessageEventListener, ThreadEventListener, UserEventListener).
     * @return true if found.*/
    public abstract boolean isEventTagExist(String tag);

}
