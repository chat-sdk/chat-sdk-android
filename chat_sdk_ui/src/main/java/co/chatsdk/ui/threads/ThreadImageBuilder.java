package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.chatsdk.core.NM;
import co.chatsdk.core.base.BaseConfigurationHandler;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static android.os.Environment.isExternalStorageRemovable;
import static co.chatsdk.ui.utils.ImageBuilder.bitmapForURL;

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    // TODO: Localize
    public static void load (final SimpleDraweeView imageView, final Thread thread) {
        getImageUriForThread(imageView.getContext(), thread).subscribe(new Consumer<Uri>() {
            @Override
            public void accept(Uri uri) throws Exception {
                imageView.setImageURI(uri);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                imageView.setImageURI(defaultBitmapUri(imageView.getContext(), thread));
            }
        });
    }

    public static Single<Uri> getImageUriForThread(final Context context, final Thread thread) {
        return Single.create(new SingleOnSubscribe<Uri>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Uri> e) throws Exception {

                if(!StringChecker.isNullOrEmpty(thread.getImageUrl())) {
                    e.onSuccess(Uri.parse(thread.getImageUrl()));
                    return;
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
                    e.onError(new Throwable("Thread users have no valid avatar URLs"));
                }
                else if (urls.size() == 1) {
                    e.onSuccess(Uri.parse(urls.get(0)));
                }
                else {
                    combineBitmaps(context, urls).subscribe(new Consumer<Bitmap>() {
                        @Override
                        public void accept(Bitmap bitmap) throws Exception {
                            File file = saveImageToCache(context, bitmap);
                            if(file != null) {
                                e.onSuccess(Uri.fromFile(file));
                            }
                            else {
                                e.onError(new Throwable("Could not save composite thread image to file"));
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            e.onError(throwable);
                        }
                    });
                }
            }
        });
    }

    public static Single<Bitmap> combineBitmaps (final Context context, final List<String> urls) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull final SingleEmitter<Bitmap> e) throws Exception {

                final ArrayList<Bitmap> bitmaps = new ArrayList<>();
                ArrayList<Single<Bitmap>> singles = new ArrayList<>();

                for(String url : urls) {
                    if(singles.size() >= 4) {
                        break;
                    }
                    singles.add(bitmapForURL(context, url).onErrorResumeNext(new Function<Throwable, SingleSource<? extends Bitmap>>() {
                        @Override
                        public SingleSource<? extends Bitmap> apply(@NonNull Throwable throwable) throws Exception {
                            return null;
                        }
                    }));
                }

                Single.merge(singles).observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                int size = context.getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                                Bitmap bitmap = ImageUtils.getMixImagesBitmap(size, size, bitmaps);

                                if(bitmap == null) {
                                    e.onError(new Throwable("Thread image could not be created"));
                                }
                                else {
                                    e.onSuccess(bitmap);
                                }
                            }
                        })
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(@NonNull Bitmap bitmap) throws Exception {
                                bitmaps.add(bitmap);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                e.onError(throwable);
                                throwable.printStackTrace();
                            }
                        });
            }
        });
    }

    public static int defaultBitmapResId (Thread thread) {
        if (thread.typeIs(ThreadType.Private1to1)) {
            return R.drawable.icn_100_private_thread;
        }
        else {
            return R.drawable.icn_100_public_thread;
        }
    }

    public static Uri defaultBitmapUri (Context context, Thread thread) {
        return new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(defaultBitmapResId(thread)))
                .build();
    }

    public static Bitmap defaultBitmap (Context context, Thread thread) {
        return BitmapFactory.decodeResource(context.getResources(), defaultBitmapResId(thread));
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public static File saveImageToCache (Context context, Bitmap image) {
        File cache = getDiskCacheDir(context, NM.config().stringForKey(BaseConfigurationHandler.ImageDirectoryName));


        if(!cache.exists()) {
            cache.mkdirs();
        }

        File file = new File(cache, UUID.randomUUID() + ".png");
        while (file.exists()) {
            file = new File(cache, UUID.randomUUID() + ".png");
        }

        try {

            OutputStream outStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Log.e("file", "" + file);
        return file;
    }


}

