package co.chatsdk.core.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import co.chatsdk.core.R;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 10/14/17.
 */

public class ImageBuilder {

    public static Single<Bitmap> bitmapForURL (final String url) {
        return bitmapForURL(url, null, null);
    }

    // Use Picasso instead
    @Deprecated
    public static Single<Bitmap> bitmapForURL (final String url, Integer width, Integer height) {
        // Picasso needs to run on the main thread
        return Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            RequestCreator creator = Picasso.get().load(url);
            if (width != null && height != null) {
                creator = creator.resize(width, height);
            }
            creator.into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    emitter.onSuccess(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                   emitter.onError(e);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
}
