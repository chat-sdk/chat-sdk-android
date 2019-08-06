package co.chatsdk.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import co.chatsdk.core.R;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 10/14/17.
 */

public class ImageBuilder {

    // TODO: Localize
    public static Single<Bitmap> bitmapForURL (final Context context, final String url) {
        return Single.create((SingleOnSubscribe<Bitmap>) e -> {
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
                        // We need to copy the bitmap because in some cases, Fresco is recycling the bitmap
                        // which causes a crash later on. Seen this problem in Android 5
                        e.onSuccess(bitmap.copy(bitmap.getConfig(), true));
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        e.onError(new Throwable(context.getString(R.string.unable_to_load_image)));
                    }
                }, CallerThreadExecutor.getInstance());
            }
            else {
                e.onError(new Throwable("Unable to load image"));
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
}
