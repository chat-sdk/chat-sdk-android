package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.R;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    public static Single<Bitmap> getBitmapForThread (final Context context, final BThread thread) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final SingleEmitter<Bitmap> e) throws Exception {
                final List<String> urls = userThumbnailURLs(thread);

                // If the URL is empty
                if (urls.size() == 0) {
                    e.onSuccess(defaultBitmap(context, thread));
                }
                else {

                    final ArrayList<Bitmap> bitmaps = new ArrayList<>();
                    ArrayList<Single<Bitmap>> singles = new ArrayList<>();

                    for(String url : urls) {
                        // Only allow a maximum of 4 images
                        if(singles.size() >= 4) {
                            break;
                        }
                        singles.add(getBitmapForURL(context, url));
                    }

                    Single.merge(singles).doOnNext(new Consumer<Bitmap>() {
                        @Override
                        public void accept(Bitmap bitmap) throws Exception {
                            bitmaps.add(bitmap);
                        }
                    }).doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            if(bitmaps.size() == 1) {
                                e.onSuccess(bitmaps.get(0));
                            }
                            else {
                                int size = context.getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                                Bitmap merged = ImageUtils.getMixImagesBitmap(size, size, bitmaps);
                                e.onSuccess(merged);
                            }
                        }
                    }).doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            e.onSuccess(defaultBitmap(context, thread));
                        }
                    }).subscribe();
                }
            }
        });
    }

    public static List<String> userThumbnailURLs (BThread thread) {
        ArrayList<String> urls = new ArrayList<>();

        if (StringUtils.isNotBlank(thread.getImageURL())) {
            urls.add(thread.getImageURL());
        }
        else {
            if(thread.typeIs(ThreadType.Private)) {

                List<BUser> users = thread.getUsers();

                for (BUser user : users){
                    if (!user.isMe() && !StringUtils.isBlank(user.getThumbnailPictureURL())) {
                        urls.add(user.getThumbnailPictureURL());
                    }
                }
            }
        }
        return urls;
    }


    public static Single<Bitmap> getBitmapForURL (final Context context, final String url) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final SingleEmitter<Bitmap> e) {
                Ion.with(context).load(url).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception exception, Bitmap result) {
                        if(exception == null) {
                            e.onSuccess(result);
                        }
                        else {
                            e.onError(exception);
                        }
                    }
                });
            }
        });
    }

    public static Bitmap defaultBitmap (Context context, BThread thread) {
        if (thread.typeIs(ThreadType.Public)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_users);
        } else if (thread.getUsers().size() < 3 || thread.typeIs(ThreadType.Private1to1)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icn_32_profile_placeholder);
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_users);
        }
    }


}

