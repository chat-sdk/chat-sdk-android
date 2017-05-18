package tk.wanderingdevelopment.chatsdk.core.abstracthandlers;

import android.content.Context;
import android.graphics.Bitmap;

import com.braunster.chatsdk.R;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.defines.Debug;
import com.braunster.chatsdk.utils.ImageUtils;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.events.AbstractEventManager;
import com.braunster.chatsdk.object.ChatError;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Promise;

import java.util.Date;
import java.util.List;

import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
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

    public CoreManager(Context context){
        this.context = context;
    }

    public BUser currentUserModel(){
        String entityID = NetworkManager.shared().a.auth.getCurrentUserEntityID();

        if (StringUtils.isNotEmpty(entityID))
        {
            BUser currentUser = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

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

    public abstract Promise<BUser, ChatError, Void> pushUser();


    public abstract void goOnline();

    public abstract void goOffline();

    public abstract void setUserOnline();

    public abstract void setUserOffline();


    /*** Send a request to the server to get the online status of the user. */
    public abstract Promise<Boolean, ChatError, Void> isOnline();




    public abstract Promise<List<BUser>, ChatError, Void> getFollowers(String entityId);

    public abstract Promise<List<BUser>, ChatError, Void>  getFollows(String entityId);

    public abstract Promise<Void, ChatError, Void> followUser(BUser userToFollow);

    public abstract void unFollowUser(BUser userToUnfollow);

    public abstract Promise<List<BUser>, ChatError, Integer> usersForIndex(String index, String value);

    public abstract String getServerURL();


    public Observable<ImageUploadResult> uploadImage(final Bitmap image, final Bitmap thumbnail) {

        if(image == null || thumbnail == null) {
            return Observable.error(new Throwable("The image and thumbnail can't be null"));
        }

        // Upload the two images in parallel
        Observable<FileUploadResult> o1 = NetworkManager.shared().a.upload.uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
        Observable<FileUploadResult> o2 = NetworkManager.shared().a.upload.uploadFile(ImageUtils.getImageByteArray(thumbnail), "thumbnail.jpg", "image/jpeg");

        return Observable.zip(o1, o2, new BiFunction<FileUploadResult, FileUploadResult, ImageUploadResult>() {
            @Override
            public ImageUploadResult apply(FileUploadResult s1, FileUploadResult s2) throws Exception {
                String imageURL = null, thumbnailURL = null;

                if (s1.name.equals("image.jpg")) {
                    imageURL = s1.url;
                }
                if (s2.name.equals("image.jpg")) {
                    imageURL = s2.url;
                }
                if (s1.name.equals("thumbnail.jpg")) {
                    thumbnailURL = s1.url;
                }
                if (s2.name.equals("thumbnail.jpg")) {
                    thumbnailURL = s2.url;
                }


                ImageUploadResult p = new ImageUploadResult(imageURL, thumbnailURL);
                p.progress = s1.progress.add(s2.progress);

                return p;
            }
        });
    }

    public Observable<FileUploadResult> uploadImage(final Bitmap image) {

        if(image == null) return Observable.error(new Throwable("Image can not be null"));

        return NetworkManager.shared().a.upload.uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
    }

}
