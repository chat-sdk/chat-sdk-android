/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;

import static android.os.Environment.isExternalStorageRemovable;


public class ImageUtils {

    static {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static final String DIVIDER = "&", HEIGHT = "H", WIDTH = "W";

    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        if (uniqueName == null || uniqueName.isEmpty()) {
            uniqueName = context.getResources().getString(R.string.app_name);
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static File createEmptyFileInCacheDirectory(File dir, String name, String ext) {
        if (!dir.exists()) dir.mkdirs();
        if (name.contains(ext)) {
            return new File(dir, name);
        }
        String fileName = (name += UUID.randomUUID()).replace("@", "_");
        File file = new File(dir, fileName + ext);
        while (file.exists()) {
            fileName = (name += UUID.randomUUID()).replace("@", "_");
            file = new File(dir, fileName + ext);
        }
        return file;
    }

    public static File createEmptyFileInCacheDirectory(Context context, String name, String ext) {
        File imageDir = getDiskCacheDir(context, ChatSDK.config().imageDirectoryName);
        return createEmptyFileInCacheDirectory(imageDir, name, ext);
    }

    public static File compressImageToFile(Bitmap bitmap, File outFile, Bitmap.CompressFormat format) {
        try {
            OutputStream outStream = new FileOutputStream(outFile);
            bitmap.compress(format, 100, outStream);
            outStream.flush();
            outStream.close();
        }
        catch (Exception e) {
            ChatSDK.logError(e);
            return null;
        }
        return outFile;
    }

    public static File compressImageToFile(Bitmap bitmap, File outFile) {
        String path = outFile.getPath();
        String ext = path.substring(path.lastIndexOf(".")).toLowerCase();
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        if (ext == "png") {
            format = Bitmap.CompressFormat.PNG;
        }
        return compressImageToFile(bitmap, outFile, format);
    }

    public static File compressImageToFile(Context context, Bitmap bitmap, String name, String ext) {
        File outFile = createEmptyFileInCacheDirectory(context, name, ext);
        return compressImageToFile(bitmap, outFile);
    }

    public static File compressImageToFile(Context context, String filePath, String name, String ext) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        return compressImageToFile(context, bitmap, name, ext);
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
     * @param  width width of the final image, A positive number.
     * @param  height height of the final image, A positive number.
     *
     * @return A Bitmap containing the given images.
     * */
    public static Bitmap getMixImagesBitmap(int width, int height, Bitmap...bitmaps){

        if (height == 0 || width == 0 || bitmaps.length == 0) {
            return null;
        }

        Bitmap finalImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalImage);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        if(bitmaps.length == 1) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], width, height), 0, 0, paint);
        }
        else if (bitmaps.length == 2) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], width/2, height), 0, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[1], width/2, height), width/2, 0, paint);
        }
        else if (bitmaps.length == 3) {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], width/2, height), 0, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[1], width/2, height/2), width/2, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[2], width/2, height/2), width/2, height/2, paint);
        }
        else {
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], width/2, height/2), 0, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[0], width/2, height/2), 0, height/2, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[1], width/2, height/2), width/2, 0, paint);
            canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmaps[2], width/2, height/2), width/2, height/2, paint);
        }

        return finalImage;
    }

    public static Bitmap getMixImagesBitmap(int width, int height, List<Bitmap> bitmaps) {
        return getMixImagesBitmap(width, height, bitmaps.toArray(new Bitmap[bitmaps.size()]));
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

    public static String getDimensionAsString(Bitmap bitmap){
        if (bitmap == null)
            throw  new NullPointerException("Bitmap cannot be null");

        return getDimensionAsString(bitmap.getWidth(), bitmap.getHeight());
    }

    public static String getDimensionAsString(int width, int height){
        return WIDTH + width +  DIVIDER + HEIGHT + height;
    }

    public static void scanFilePathForGallery(Context context, String path) {
        if (context == null)
            return;
        
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
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

}


