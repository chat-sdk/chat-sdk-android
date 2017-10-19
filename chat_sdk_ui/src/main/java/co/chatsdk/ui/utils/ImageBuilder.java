package co.chatsdk.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import co.chatsdk.core.utils.StringChecker;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 10/14/17.
 */

public class ImageBuilder {

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
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
}
