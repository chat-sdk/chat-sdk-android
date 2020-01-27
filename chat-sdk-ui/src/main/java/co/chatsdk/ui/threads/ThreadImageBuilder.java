package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static co.chatsdk.core.utils.ImageBuilder.bitmapForURL;

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    public static Disposable load(final ImageView imageView, final Thread thread) {
        int size = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.action_bar_avatar_max_size);
        return load(imageView, thread, size);
    }

    public static Disposable load (final ImageView imageView, final Thread thread, int size) {
        return getImageUriForThread(imageView.getContext(), thread, size).subscribe(uri -> Picasso.get().load(uri).into(imageView), throwable -> imageView.setImageURI(defaultBitmapUri(thread)));
    }

    public static Single<Uri> getImageUriForThread(Context context, final Thread thread) {
        int size = context.getResources().getDimensionPixelSize(R.dimen.action_bar_avatar_max_size);
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

            List<String> urls = new ArrayList<>();
            for(User u : users) {
                if(!StringChecker.isNullOrEmpty(u.getAvatarURL())) {
                    urls.add(u.getAvatarURL());
                }
            }

            // If the URL is empty
            if (urls.size() == 0) {
                return Single.error(new Throwable(context.getString(R.string.thread_users_have_no_valid_avatar_urls)));
            }
            else if (urls.size() == 1) {
                return Single.just(Uri.parse(urls.get(0)));
            }
            else {
                return combineBitmaps(urls, size).map(bitmap -> {
                    File file = ImageUtils.compressImageToFile(context, bitmap, hashCode, ".png", false);
                    return Uri.fromFile(file);
                });
            }
        });
    }

    public static String hashCodeForMixedUserAvatar(List<User> users) {
        Collections.sort(users,(o1, o2) -> o1.getEntityID().compareTo(o2.getEntityID()));

        StringBuilder name = new StringBuilder();
        for (User u: users) {
            name.append(u.getEntityID()).append(u.getAvatarURL());
        }
        return String.valueOf(name.toString().hashCode());
    }

    public static Single<Bitmap> combineBitmaps (final List<String> urls, final int size) {
        return Single.defer(() -> {

            final ArrayList<Bitmap> bitmaps = new ArrayList<>();
            final ArrayList<Single<Bitmap>> singles = new ArrayList<>();

            for(String url : urls) {
                if(singles.size() >= 4) {
                    break;
                }
                singles.add(bitmapForURL(url));
            }

            return Single.merge(singles)
                    .doOnNext(bitmaps::add).onErrorResumeNext(throwable -> null)
                    .ignoreElements()
                    .toSingle(() -> {
                return ImageUtils.getMixImagesBitmap(size, size, bitmaps);
            });
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

