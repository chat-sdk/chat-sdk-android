/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.core.image;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.net.Uri;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.types.FileUploadResult;
import sdk.guru.common.RX;


public class ImageUtils {

    public static File saveBitmapToFile(Bitmap bitmap) {
        FileManager fm = ChatSDK.shared().fileManager();
        File dir = fm.imageCache();
        File outFile = fm.newDatedFile(dir, "image", "png");
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile;
    }

    /**
     * Constructing a bitmap that contains the given bitmaps(max is three).
     *
     * For given two bitmaps the result will be a half and half bitmap.
     *
     * For given three the result will be a half of the first bitmap and the second
     * half will be shared equally by the two others.
     *
     * @param  bitmaps Array of bitmaps to use for the final image.
     * @param  w width of the final image, A positive number.
     * @param  h height of the final image, A positive number.
     *
     * @return A Bitmap containing the given images.
     * */
    public static Bitmap getMixImagesBitmap(int w, int h, Bitmap...bitmaps){

        if (h == 0 || w == 0 || bitmaps.length == 0) {
            return null;
        }

        Bitmap finalImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        finalImage.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(finalImage);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        int margin = 1;

        int w_2 = w/2 - margin;
        int x_2 = w/2 + margin;

        int h_2 = h/2 - margin;
        int y_2 = h/2 + margin;

        if(bitmaps.length == 1) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], w, h), 0, 0, paint);
        }
        else if (bitmaps.length == 2) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], w_2, h), 0, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[1], w_2, h), x_2, 0, paint);
        }
        else if (bitmaps.length == 3) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], w_2, h), 0, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[1], w_2, h_2), x_2, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[2], w_2, h_2), x_2, y_2, paint);
        }
        else {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], w_2, h_2), 0, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], w_2, h_2), 0, y_2, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[1], w_2, h_2), x_2, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[2], w_2, h_2), x_2, y_2, paint);
        }

        return finalImage;
    }

    public static Bitmap getMixImagesBitmap(int width, int height, List<Bitmap> bitmaps) {
        return getMixImagesBitmap(width, height, bitmaps.toArray(new Bitmap[0]));
    }

    public static Bitmap scaleImage(Bitmap bitmap, int boxSize) {
        if (boxSize == 0)
            return null;

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boxSize) / bitmap.getWidth();
        float yScale = ((float) boxSize) / bitmap.getHeight();
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static byte[] getImageByteArray(Bitmap bitmap){
        return getImageByteArray(bitmap, 50);
    }

    public static byte[] getImageByteArray(Bitmap bitmap, int quality) {
        // Converting file to a JPEG and then to byte array.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    public static Uri uriForResourceId(Context context, @DrawableRes int resourceId) {
        Resources resources = context.getResources();
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resourceId))
                .appendPath(resources.getResourceTypeName(resourceId))
                .appendPath(resources.getResourceEntryName(resourceId))
                .build();
    }

    public static Single<Bitmap> bitmapForURL (final String url) {
        return bitmapForURL(url, null, null);
    }

    public static Single<Bitmap> bitmapForURL(final String url, Integer width, Integer height) {
        return Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            RequestBuilder<Bitmap> requestBuilder = Glide.with(ChatSDK.ctx()).asBitmap().dontAnimate().load(url);
            if (width != null && height != null) {
                requestBuilder = requestBuilder.override(width, height);
            }
            Bitmap bitmap = requestBuilder.submit().get();
            emitter.onSuccess(bitmap);
        }).subscribeOn(RX.io()).observeOn(RX.main());
    }

    public static Single<ImageUploadResult> uploadImageFile(File file) {
        return Observable.defer((Callable<ObservableSource<FileUploadResult>>) () -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            final Bitmap image = BitmapFactory.decodeFile(file.getPath(), options);
            return ChatSDK.upload().uploadImage(image);
        }).subscribeOn(RX.computation())
                .flatMapMaybe(fileUploadResult -> {
            if (fileUploadResult.urlValid()) {
                return Maybe.just(new ImageUploadResult(fileUploadResult.url, file.getPath()));
            } else {
                return Maybe.empty();
            }
        }).firstElement().toSingle();
    }
}


