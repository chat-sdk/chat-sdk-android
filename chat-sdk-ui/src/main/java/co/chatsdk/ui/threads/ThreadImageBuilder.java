package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
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

    public static class ImageCache {

        public static final ImageCache instance = new ImageCache();

        HashMap<String, String> threadImageURLMap = new HashMap<>();

        public ImageCache() {
            Disposable d = ChatSDK.events().source().filter(NetworkEvent.filterType(EventType.ThreadDetailsUpdated, EventType.ThreadUsersChanged)).subscribe(networkEvent -> {
                if (networkEvent.thread != null) {
                    clear(networkEvent.thread.getEntityID());
                }
            });
        }

        public static ImageCache shared() {
            return instance;
        }

        public void put(String threadEntityID, String url) {
            threadImageURLMap.put(threadEntityID, url);
        }

        public String get(String threadEntityID) {
            return threadImageURLMap.get(threadEntityID);
        }

        public void clear(String threadEntityID) {
            threadImageURLMap.remove(threadEntityID);
        }

    }

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

            String uriString = ImageCache.shared().get(thread.getEntityID());
            if (uriString != null) {
                return Single.just(Uri.parse(uriString));
            }

            List<User> users = thread.getUsers();

            List<String> urls = new ArrayList<>();
            for(User u : users) {
                if(!StringChecker.isNullOrEmpty(u.getAvatarURL()) && !u.isMe()) {
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
                    File file = ImageUtils.compressImageToFile(context, bitmap, "avatar", ".png");
                    Uri uri = Uri.fromFile(file);

                    ImageCache.shared().put(thread.getEntityID(), uri.toString());

                    return Uri.fromFile(file);
                });
            }
        });
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

