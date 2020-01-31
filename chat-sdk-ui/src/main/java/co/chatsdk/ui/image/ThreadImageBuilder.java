package co.chatsdk.ui.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;

import static co.chatsdk.core.image.ImageBuilder.bitmapForURL;

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    public static Disposable load(final ImageView imageView, final Thread thread) {
        int size = Dimen.from(imageView.getContext(), R.dimen.action_bar_avatar_size);
        return load(imageView, thread, size);
    }

    public static Disposable load (final ImageView imageView, final Thread thread, int size) {
        return getImageUriForThread(imageView.getContext(), thread, size).subscribe(uri -> Picasso.get().load(uri).into(imageView), throwable -> imageView.setImageURI(defaultBitmapUri(thread)));
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

            // We make a hash code for the user list and their image URLs
            // that means that if the users haven't changed, we can reaload
            // the same image split image we created before
            final String hashCode = hashCodeForMixedUserAvatar(users);
            File cachedImage = ImageUtils.getFileInCacheDirectory(context, hashCode, ".png");
            if(cachedImage.exists()) {
                return Single.just(Uri.fromFile(cachedImage));
            }

            // If the URL is empty
            if (users.size() == 0) {
                return Single.error(new Throwable(context.getString(R.string.thread_users_have_no_valid_avatar_urls)));
            }
            else if (users.size() == 1) {
                return users.get(0).getAvatarBitmap(size, size).map(bitmap -> {
                    File file = ImageUtils.compressImageToFile(context, bitmap, hashCode, ".png", false);
                    return Uri.fromFile(file);
                });
            }
            else {
                return combineBitmapsForUsers(users, size).map(bitmap -> {
                    File file = ImageUtils.compressImageToFile(context, bitmap, hashCode, ".png", false);
                    return Uri.fromFile(file);
                });
            }
        });
    }

    public static String hashCodeForMixedUserAvatar(List<User> users) {
        Collections.sort(users,(o1, o2) -> o1.getEntityID().compareTo( o2.getEntityID()));

        StringBuilder name = new StringBuilder();
        for (User u: users) {
            name.append(u.getEntityID()).append(u.getAvatarURL());
        }
        System.out.println(name.toString().hashCode());
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
        });
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
        });
    }

    public static Single<Bitmap> combineBitmaps(final List<String> urls, final int size) {
        return Single.defer(() -> {
            final ArrayList<Single<Bitmap>> singles = new ArrayList<>();

            for(String url : urls) {
                if(singles.size() >= 4) {
                    break;
                }
                singles.add(bitmapForURL(url, size, size));
            }
            return combineBitmapSingles(singles, size);
        });
    }

    public static int defaultBitmapResId (Thread thread) {
        if (thread == null) {
            return defaultBitmapResId();
        }
        else if (thread.typeIs(ThreadType.Private1to1)) {
            return R.drawable.icn_100_private_thread;
        }
        else {
            return R.drawable.icn_100_public_thread;
        }
    }

    public static Uri defaultBitmapUri (Thread thread) {
        return new Uri.Builder()
                .scheme("res")
                .path(String.valueOf(defaultBitmapResId(thread)))
                .build();
    }

    public static Bitmap defaultBitmap (Context context, Thread thread) {
        return BitmapFactory.decodeResource(context.getResources(), defaultBitmapResId(thread));
    }

    public static int defaultBitmapResId() {
        return R.drawable.icn_100_private_thread;
    }



}

