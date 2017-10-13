package co.chatsdk.ui.threads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.NM;
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

/**
 * Created by benjaminsmiley-andrews on 12/06/2017.
 */

public class ThreadImageBuilder {

    // TODO: Localize
    public static Single<Bitmap> bitmapForURL (final Context context, final String url) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Bitmap> e) throws Exception {
                if(!StringChecker.isNullOrEmpty(url)) {
                    Uri uri = Uri.parse(url);
                    ImageRequest request = ImageRequestBuilder
                            .newBuilderWithSource(uri)
                            .build();
                    ImagePipeline pipeline = Fresco.getImagePipeline();
                    DataSource dataSource = pipeline.fetchDecodedImage(request, context);
                    dataSource.subscribe(new BaseBitmapDataSubscriber() {
                        @Override
                        protected void onNewResultImpl(Bitmap bitmap) {
                            e.onSuccess(bitmap);
                        }

                        @Override
                        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                            e.onError(new Throwable("Unable to load image"));
                        }
                    }, CallerThreadExecutor.getInstance());
                }
                else {
                    e.onError(new Throwable("Unable to load image"));
                }
            }
        });
    }

    public static void load (final SimpleDraweeView imageView, final Thread thread) {

                if(thread.getImageUrl() != null) {
                    imageView.setImageURI(thread.getImageUrl());
                    return;
                }

                List<User> users = thread.getUsers();
                users.remove(NM.currentUser());

                // If the URL is empty
                if (users.size() == 0) {
                    imageView.setImageBitmap(defaultBitmap(imageView.getContext(), thread));
                    return;
                }
                else {
                    getBitmapForThread(imageView.getContext(), thread).subscribe(new Consumer<Bitmap>() {
                        @Override
                        public void accept(@NonNull Bitmap bitmap) throws Exception {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
    }

    public static Single<Bitmap> getBitmapForThread(final Context context, final Thread thread) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Bitmap> e) throws Exception {

                List<User> users = thread.getUsers();

                List<String> urls = new ArrayList<>();
                for(User u : users) {
                    if(!StringChecker.isNullOrEmpty(u.getAvatarURL()) && !u.isMe()) {
                        urls.add(u.getAvatarURL());
                    }
                }

                // If the URL is empty
                if (urls.size() == 0) {
                    e.onSuccess(defaultBitmap(context, thread));
                    return;
                }

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
                                Bitmap bitmap = defaultBitmap(context, thread);
                                if (bitmaps.size() == 1) {
                                    bitmap = bitmaps.get(0);
                                }
                                else if (bitmaps.size() > 1) {
                                    int size = context.getResources().getDimensionPixelSize(R.dimen.chat_sdk_chat_action_barcircle_image_view_size);
                                    bitmap = ImageUtils.getMixImagesBitmap(size, size, bitmaps);
                                }
                                e.onSuccess(bitmap);
                            }
                        })
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(@NonNull Bitmap bitmap) throws Exception {
                                if(bitmap != null) {
                                    bitmaps.add(bitmap);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
            }
        });
    }

    public static Bitmap defaultBitmap (Context context, Thread thread) {
        if (thread.typeIs(ThreadType.Private1to1)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icn_100_private_thread);
        }
        else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icn_100_public_thread);
        }
    }


}

