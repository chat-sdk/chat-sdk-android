package tk.wanderingdevelopment.chatsdk.core.abstracthandlers;

import android.content.Context;
import android.graphics.Bitmap;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.BPushHandler;
import com.braunster.chatsdk.interfaces.BUploadHandler;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.AbstractEventManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.SaveImageProgress;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.Date;
import java.util.List;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.interfaces.CoreInterface;

/**
 * Created by KyleKrueger on 11.04.2017.
 */

public abstract class CoreManager implements CoreInterface {

    public CoreManager authInterface;
    protected boolean DEBUG = Debug.CoreManager;
    private Context context;
    private AbstractEventManager eventManager;
    public BPushHandler pushHandler;
    public BUploadHandler uploadHandler;

    public CoreManager(Context context){
        this.context = context;
    }


    public void setUploadHandler(BUploadHandler uploadHandler) {
        this.uploadHandler = uploadHandler;
    }

    @Override
    public BUploadHandler getUploadHandler() {
        return uploadHandler;
    }

    public void setPushHandler(BPushHandler pushHandler) {
        this.pushHandler = pushHandler;
    }

    @Override
    public BPushHandler getPushHandler() {
        return pushHandler;
    }

    public BUser currentUserModel(){
        String authID = BNetworkManager.getAuthInterface().getCurrentUserAuthenticationId();
        if (StringUtils.isNotEmpty(authID))
        {
            BUser currentUser = DaoCore.fetchEntityWithEntityID(BUser.class, authID);

            if(DEBUG) {
                if (currentUser == null) Timber.e("Current user is null");
                else if (StringUtils.isEmpty(currentUser.getEntityID()))
                    Timber.e("Current user entity id is null");
            }

            return currentUser;
        }
        if (DEBUG) Timber.e("getCurrentUserAuthenticationIdr is null");
        return null;
    }

    public boolean backendlessEnabled(){
        return StringUtils.isNotEmpty(context.getString(R.string.backendless_app_id)) && StringUtils.isNotEmpty(context.getString(R.string.backendless_secret_key));
    }

    public boolean facebookEnabled(){
        return StringUtils.isNotEmpty(context.getString(R.string.facebook_id));
    }

    public boolean googleEnabled(){
        return false;
    }

    public boolean twitterEnabled(){
        return (StringUtils.isNotEmpty(context.getString(R.string.twitter_consumer_key))
                && StringUtils.isNotEmpty(context.getString(R.string.twitter_consumer_secret)))

                ||

                (StringUtils.isNotEmpty(context.getString(R.string.twitter_access_token))
                        && StringUtils.isNotEmpty(context.getString(R.string.twitter_access_token_secret)));
    }

    public void setEventManager(AbstractEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public AbstractEventManager getEventManager() {
        return eventManager;
    }


    public abstract void setLastOnline(Date date);



    /**
     * @return the current user contacts list.
     **/
    public List<BUser> getContacs() {
        return currentUserModel().getContacts();
    }

    public abstract Promise<BUser, BError, Void> pushUser();


    public abstract void goOnline();

    public abstract void goOffline();

    public abstract void setUserOnline();

    public abstract void setUserOffline();


    /*** Send a request to the server to get the online status of the user. */
    public abstract Promise<Boolean, BError, Void> isOnline();




    public abstract Promise<List<BUser>, BError, Void> getFollowers(String entityId);

    public abstract Promise<List<BUser>, BError, Void>  getFollows(String entityId);

    public abstract Promise<Void, BError, Void> followUser(BUser userToFollow);

    public abstract void unFollowUser(BUser userToUnfollow);

    public abstract Promise<List<BUser>, BError, Integer> usersForIndex(String index, String value);

    public abstract String getServerURL();


    public Promise<String[], BError, SaveImageProgress> uploadImage(final Bitmap image, final Bitmap thumbnail) {

        if(image == null || thumbnail == null) return rejectMultiple();

        final Deferred<String[], BError, SaveImageProgress> deferred = new DeferredObject<String[], BError, SaveImageProgress>();

        final String[] urls = new String[2];

        BNetworkManager.getCoreInterface().getUploadHandler().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg")
                .done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String url) {
                        urls[0] = url;

                        BNetworkManager.getCoreInterface().getUploadHandler().uploadFile(ImageUtils.getImageByteArray(thumbnail), "thumbnail.jpg", "image/jpeg")
                                .done(new DoneCallback<String>() {
                                    @Override
                                    public void onDone(String url) {
                                        urls[1] = url;

                                        deferred.resolve(urls);
                                    }
                                })
                                .fail(new FailCallback<BError>() {
                                    @Override
                                    public void onFail(BError error) {
                                        deferred.reject(error);
                                    }
                                });
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        deferred.reject(error);
                    }
                });

        return deferred.promise();
    }

    public Promise<String, BError, SaveImageProgress> uploadImageWithoutThumbnail(final Bitmap image) {

        if(image == null) return reject();

        final Deferred<String, BError, SaveImageProgress> deferred = new DeferredObject<String, BError, SaveImageProgress>();

        BNetworkManager.getCoreInterface().getUploadHandler().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg")
                .done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String url) {
                        deferred.resolve(url);
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        deferred.reject(error);
                    }
                });

        return deferred.promise();
    }

    private static Promise<String, BError, SaveImageProgress> reject(){
        return new DeferredObject<String, BError, SaveImageProgress>().reject(new BError(BError.Code.NULL, "Image Is Null"));
    }

    private static Promise<String[], BError, SaveImageProgress> rejectMultiple(){
        return new DeferredObject<String[], BError, SaveImageProgress>().reject(new BError(BError.Code.NULL, "Image Is Null"));
    }
}
