package tk.wanderingdevelopment.chatsdk.core.interfaces;

import android.graphics.Bitmap;


import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.events.AbstractEventManager;
import com.braunster.chatsdk.object.ChatError;

import org.jdeferred.Promise;

import java.util.Date;
import java.util.List;

import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Observable;

/**
 * Created by KyleKrueger on 13.04.2017.
 */

public interface CoreInterface {

    BUser currentUserModel();

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

    Promise<BUser, ChatError, Void> pushUser();

    void goOnline();

    void goOffline();

    void setUserOnline();

    void setUserOffline();

    /*** Send a request to the server to get the online status of the user. */
    Promise<Boolean, ChatError, Void> isOnline();

    void updateLastOnline();

    Promise<List<BUser>, ChatError, Void> getFollowers(String entityId);

    Promise<List<BUser>, ChatError, Void>  getFollows(String entityId);

    Promise<Void, ChatError, Void> followUser(BUser userToFollow);

    void unFollowUser(BUser userToUnfollow);

    Promise<List<BUser>, ChatError, Integer> usersForIndex(String index, String value);

    String getServerURL();


    public Observable<ImageUploadResult> uploadImage(final Bitmap image, final Bitmap thumbnail) ;
    public Observable<FileUploadResult> uploadImage(final Bitmap image);


    //Promise<String[], ChatError, SaveImageProgress> uploadImage(final Bitmap image, final Bitmap thumbnail);

    //Promise<String, ChatError, SaveImageProgress> uploadImageWithoutThumbnail(final Bitmap image);


}
