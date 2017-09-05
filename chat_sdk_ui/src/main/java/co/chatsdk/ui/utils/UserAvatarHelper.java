package co.chatsdk.ui.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageUploadResult;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.R;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

/**
 * Created by ben on 8/15/17.
 */

public class UserAvatarHelper {

    public static Completable loadAvatar(final User user, final ImageView imageView) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                Bitmap image = BitmapFactory.decodeResource(AppContext.shared().context().getResources(),
                        R.drawable.icn_100_profile);

                imageView.setImageBitmap(image);

                avatar(user).subscribe(new BiConsumer<Bitmap, Throwable>() {
                    @Override
                    public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                        imageView.setImageBitmap(bitmap);
                        e.onComplete();
                    }
                });
            }
        });
    }

    public static Single<Bitmap> avatar(final User user) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final SingleEmitter<Bitmap> e) throws Exception {

                final Bitmap defaultBitmap = BitmapFactory.decodeResource(AppContext.shared().context().getResources(),
                        R.drawable.icn_32_profile_placeholder);

                if(user.getAvatarURL() == null) {
                    e.onSuccess(defaultBitmap);
                }
                else {
                    ImageUtils.bitmapForURL(user.getAvatarURL()).subscribe(new BiConsumer<Bitmap, Throwable>() {
                        @Override
                        public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                            if(bitmap != null) {
                                e.onSuccess(bitmap);
                            }
                            else {
                                e.onSuccess(defaultBitmap);
                            }
                        }
                    });
                }
            }
        });
    }

    public static Completable saveProfilePicToServer(final String path, final Activity activity){
        return Single.create(new SingleOnSubscribe<File>() {
            @Override
            public void subscribe(SingleEmitter<File> e) throws Exception {
                File image = new Compressor(activity)
                        .setMaxHeight(Defines.ImageProperties.MAX_HEIGHT_IN_PX)
                        .setMaxWidth(Defines.ImageProperties.MAX_WIDTH_IN_PX)
                        .compressToFile(new File(path));
                e.onSuccess(image);
            }
        }).flatMapCompletable(new Function<File, CompletableSource>() {
            @Override
            public CompletableSource apply(@NonNull File file) throws Exception {

                // Saving the image to backendless.
                final User currentUser = NM.currentUser();

                // TODO: Are we handling the error here
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if(NM.upload() != null) {
                    return NM.upload().uploadImage(bitmap).flatMapCompletable(new Function<MessageUploadResult, Completable>() {
                        @Override
                        public Completable apply(MessageUploadResult profileImageUploadResult) throws Exception {


                            currentUser.setAvatarURL(profileImageUploadResult.imageURL);
                            currentUser.setThumbnailURL(profileImageUploadResult.thumbnailURL);

                            return Completable.complete();
                        }
                    });
                }
                else {

                    // Move the image to the standard profile URL
                    String path = ImageUtils.saveToInternalStorage(bitmap, currentUser.getEntityID());

                    currentUser.setAvatarURL(path);
                    // Reset the hash code to force the image to be uploaded
                    currentUser.setAvatarHash("");
                    return Completable.complete();
                }
            }
        }).concatWith(NM.core().pushUser());
    }

}
