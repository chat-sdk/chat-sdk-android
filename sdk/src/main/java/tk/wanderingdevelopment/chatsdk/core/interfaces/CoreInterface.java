package tk.wanderingdevelopment.chatsdk.core.interfaces;

import android.graphics.Bitmap;

import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * Created by KyleKrueger on 13.04.2017.
 */

@Deprecated
public interface CoreInterface {

    boolean facebookEnabled();

    boolean googleEnabled();

    boolean twitterEnabled();

//    void setEventManager(AbstractEventManager eventManager);
//
//    AbstractEventManager getEventManager();

    void setLastOnline(Date date);

    /**
     * @return the current user contacts list.
     **/
    List<BUser> getContacts();

    Completable pushUser();

    void goOnline();

    void goOffline();

    void setUserOnline();

    void setUserOffline();

    /*** Send a request to the server to get the online status of the user. */
    Single<Boolean> isOnline();

    void updateLastOnline();

    Observable<BUser> getFollowers(String entityId);

    Observable<BUser>  getFollows(String entityId);

    Completable followUser(BUser userToFollow);

    void unFollowUser(BUser userToUnfollow);

    Observable<BUser> usersForIndex(String index, String value);

    String getServerURL();


    public Observable<ImageUploadResult> uploadImage(final Bitmap image, final Bitmap thumbnail) ;
    public Observable<FileUploadResult> uploadImage(final Bitmap image);


    //Promise<String[], ChatError, SaveImageProgress> uploadImage(final Bitmap image, final Bitmap thumbnail);

    //Promise<String, ChatError, SaveImageProgress> uploadImageWithoutThumbnail(final Bitmap image);


}
