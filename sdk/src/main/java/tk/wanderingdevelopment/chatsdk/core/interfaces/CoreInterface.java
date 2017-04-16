package tk.wanderingdevelopment.chatsdk.core.interfaces;

import android.graphics.Bitmap;


import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.BPushHandler;
import com.braunster.chatsdk.interfaces.BUploadHandler;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.AbstractEventManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.SaveImageProgress;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Promise;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by KyleKrueger on 13.04.2017.
 */

public interface CoreInterface {


    void setUploadHandler(BUploadHandler uploadHandler);

    void setPushHandler(BPushHandler pushHandler);

    BUploadHandler getUploadHandler();

    BPushHandler getPushHandler();

    BUser currentUserModel();

    boolean backendlessEnabled();

    boolean facebookEnabled();

    boolean googleEnabled();

    boolean twitterEnabled();

    void setEventManager(AbstractEventManager eventManager);

    AbstractEventManager getEventManager();


    void setLastOnline(Date date);

    /**
     * @return the current user contacts list.
     **/
    List<BUser> getContacs();

    Promise<BUser, BError, Void> pushUser();

    void goOnline();

    void goOffline();

    void setUserOnline();

    void setUserOffline();

    /*** Send a request to the server to get the online status of the user. */
    Promise<Boolean, BError, Void> isOnline();

    void updateLastOnline();

    Promise<List<BUser>, BError, Void> getFollowers(String entityId);

    Promise<List<BUser>, BError, Void>  getFollows(String entityId);

    Promise<Void, BError, Void> followUser(BUser userToFollow);

    void unFollowUser(BUser userToUnfollow);

    Promise<List<BUser>, BError, Integer> usersForIndex(String index, String value);

    String getServerURL();


    Promise<String[], BError, SaveImageProgress> uploadImage(final Bitmap image, final Bitmap thumbnail);

    Promise<String, BError, SaveImageProgress> uploadImageWithoutThumbnail(final Bitmap image);


}
