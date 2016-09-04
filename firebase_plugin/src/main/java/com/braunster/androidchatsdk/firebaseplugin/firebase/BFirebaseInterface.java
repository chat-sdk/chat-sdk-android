/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.FollowerLink;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BFirebaseDefines;
import com.braunster.chatsdk.network.BPath;
import com.google.firebase.database.DataSnapshot;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

public class BFirebaseInterface {

    private static final String TAG = BFirebaseInterface.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseInterface;

    public static Object objectFromSnapshot(DataSnapshot dataSnapshot){
        if (dataSnapshot == null)
        {
            if (DEBUG) Log.e(TAG, "objectFromSnapshot, Snapshot is null.");
            return null;
        }
        else if (dataSnapshot.getValue() == null)
        {
            if (DEBUG) Log.e(TAG, "objectFromSnapshot, Values is null.");
            return null;
        }

        if (DEBUG)Log.v(TAG, "objectFromSnapshot, Path: " + dataSnapshot.getRef().toString());
        BPath path = BPath.pathWithPath(dataSnapshot.getRef().toString());

         if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.FollowerLinks))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUsersPath and FollowerLinks");
            String followerFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(followerFirebaseID))
            {
                return getFollower(dataSnapshot, userFirebaseID, followerFirebaseID, FollowerLink.Type.FOLLOWER);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }
        // ---------------
        // Follower Class Type.
        else if (path.isEqualToComponent(BFirebaseDefines.Path.BUsersPath, BFirebaseDefines.Path.BFollows))
        {
            if (DEBUG) Log.i(TAG, "objectFromSnapshot, BUsersPath and BFollows");
            String followerFirebaseID = path.idForIndex(1);
            String userFirebaseID = path.idForIndex(0);

            if (StringUtils.isNotEmpty(followerFirebaseID))
            {
                return getFollower(dataSnapshot, userFirebaseID, followerFirebaseID, FollowerLink.Type.FOLLOWS);
            }
            else return childrenFromSnapshot(dataSnapshot);
        }

        return null;
    }

    public static Object[] childrenFromSnapshot(DataSnapshot dataSnapshot){
        if (DEBUG) Log.v(TAG, "childrenFromSnapshot");
        Object children[] = new Object[(int) dataSnapshot.getChildrenCount()];

        int count = 0;
        for(DataSnapshot o :dataSnapshot.getChildren())
        {
            children[count] = objectFromSnapshot(o);
            count++;
        }

        return children;
    }

    private static class GetFollowerCall implements Callable<FollowerLink>{
        private String userEntityId, followerEntityId;
        private int type = -1;

        private GetFollowerCall(String userEntityId, String followerEntityId, int type) {
            this.userEntityId = userEntityId;
            this.followerEntityId = followerEntityId;
            this.type = type;
        }

        @Override
        public FollowerLink call() throws Exception {
            BUser user = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, userEntityId);
            BUser followerUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class, followerEntityId);
            return user.fetchOrCreateFollower(followerUser, type);
        }
    }

    private static FollowerLink getFollower(DataSnapshot snapshot, String userFirebaseId, String followerFirebaseId, int followerType){
        if (DEBUG) Log.v(TAG, "getFollower");
        try {
            return DaoCore.daoSession.callInTx(new GetFollowerCall(userFirebaseId, followerFirebaseId, followerType));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "get follower call exception, follower: " + e.getMessage());
            return null;
        }
    }
}








