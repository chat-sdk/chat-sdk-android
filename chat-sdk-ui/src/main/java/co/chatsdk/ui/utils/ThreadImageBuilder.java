package co.chatsdk.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


import org.pmw.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.core.image.ImageUtils;
import co.chatsdk.core.storage.FileManager;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.icons.Icons;
import id.zelory.compressor.Compressor;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    public static Disposable load(final ImageView imageView, final Thread thread) {
        int size = Dimen.from(imageView.getContext(), R.dimen.action_bar_avatar_size);
        return load(imageView, thread, size);
    }

    public static Disposable load (final ImageView imageView, final Thread thread, int size) {
        return getImageUriForThread(imageView.getContext(), thread, size).subscribe(uri -> Glide.with(imageView).load(uri).dontAnimate().into(imageView), throwable -> imageView.setImageDrawable(defaultDrawable(thread)));
    }

    public static Single<Uri> getImageUriForThread(Context context, final Thread thread) {
        int size = Dimen.from(context, R.dimen.action_bar_avatar_size);
        return getImageUriForThread(context, thread, size);
    }

    public static Single<Uri> getImageUriForThread(Context context, final Thread thread, int size) {
        return Single.defer((Callable<SingleSource<Uri>>) () -> {

            if(!StringChecker.isNullOrEmpty(thread.getImageUrl())) {
                return Single.just(Uri.parse(thread.getImageUrl()));
            }

            List<User> users = thread.getUsers();
            users.remove(ChatSDK.currentUser());

            FileManager fm = ChatSDK.shared().fileManager();

            // We make a hash code for the user list and their image URLs
            // that means that if the users haven't changed, we can reaload
            // the same image split image we created before
            final String hashCode = hashCodeForMixedUserAvatar(users);
            File cachedImage = new File(fm.imageCache(), hashCode);
            if(cachedImage.exists()) {
                return Single.just(Uri.fromFile(cachedImage));
            }

            // If the URL is empty
            if (users.size() == 0) {
                return Single.error(new Throwable(context.getString(R.string.thread_users_have_no_valid_avatar_urls)));
            }
            else if (users.size() == 1) {
                return users.get(0).getAvatarBitmap(size, size).map(bitmap -> {
                    File imageFile = ImageUtils.saveBitmapToFile(bitmap);
                    File compressed = new Compressor(ChatSDK.shared().context())
                            .setMaxHeight(ChatSDK.config().imageMaxThumbnailDimension)
                            .setMaxWidth(ChatSDK.config().imageMaxThumbnailDimension)
                            .setDestinationDirectoryPath(fm.imageCache().getPath())
                            .compressToFile(imageFile, hashCodeForMixedUserAvatar(users));
                    return Uri.fromFile(compressed);
                });
            }
            else {
                return combineBitmapsForUsers(users, size).map(bitmap -> {
                    File imageFile = ImageUtils.saveBitmapToFile(bitmap);
                    File compressed = new Compressor(ChatSDK.shared().context())
                            .setMaxHeight(ChatSDK.config().imageMaxThumbnailDimension)
                            .setMaxWidth(ChatSDK.config().imageMaxThumbnailDimension)
                            .setDestinationDirectoryPath(fm.imageCache().getPath())
                            .compressToFile(imageFile, hashCodeForMixedUserAvatar(users));
                    return Uri.fromFile(compressed);
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static String hashCodeForMixedUserAvatar(List<User> users) {
        Collections.sort(users,(o1, o2) -> o1.getEntityID().compareTo( o2.getEntityID()));

        StringBuilder name = new StringBuilder();
        for (User u: users) {
            name.append(u.getEntityID()).append(u.getAvatarURL());
        }
        Logger.debug("Thread hash code: " + name.toString().hashCode());
        return String.valueOf(name.toString().hashCode());
    }

    public static Single<Bitmap> combineBitmapsForUsers(final List<User> users, final int size) {
        return Single.defer(() -> {
            final ArrayList<Single<Bitmap>> singles = new ArrayList<>();
            for(User user: users) {
                if(singles.size() >= 4) {
                    break;
                }
                singles.add(user.getAvatarBitmap(size, size));
            }
            return combineBitmapSingles(singles, size);
        }).subscribeOn(Schedulers.io());
    }

    public static Single<Bitmap> combineBitmapSingles(final List<Single<Bitmap>> singles, final int size) {
        return Single.defer(() -> {
            final ArrayList<Bitmap> bitmaps = new ArrayList<>();
            return Single.merge(singles)
                    .doOnNext(bitmaps::add)
                    .ignoreElements()
                    .toSingle(() -> {
                        return ImageUtils.getMixImagesBitmap(size, size, bitmaps);
                    });
        }).subscribeOn(Schedulers.io());
    }

    public static Single<Bitmap> combineBitmaps(final List<String> urls, final int size) {
        return Single.defer(() -> {
            final ArrayList<Single<Bitmap>> singles = new ArrayList<>();

            for(String url : urls) {
                if(singles.size() >= 4) {
                    break;
                }
                singles.add(ImageUtils.bitmapForURL(url, size, size));
            }
            return combineBitmapSingles(singles, size);
        }).subscribeOn(Schedulers.io());
    }

    public static Drawable defaultDrawable(Thread thread) {
        if (thread == null || thread.typeIs(ThreadType.Private1to1)) {
            return Icons.get(Icons.choose().users, R.color.thread_default_icon_color);
        }
        else {
            return Icons.get(Icons.choose().publicChat, R.color.thread_default_icon_color);
        }
    }

//    public static int defaultBitmapResId() {
//        return R.drawable.icn_100_private_thread;
//    }



}

