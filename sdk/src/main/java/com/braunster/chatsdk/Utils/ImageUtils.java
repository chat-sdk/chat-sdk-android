package com.braunster.chatsdk.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.braunster.chatsdk.network.BDefines;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by braunster on 10/07/14.
 */
public class ImageUtils {
    public static final String TAG = ImageUtils.class.getSimpleName();
    public static final boolean DEBUG = true;

    public static Bitmap decodeSampledBitmapFromFile(String path,
                                                     int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSampledBitmapFromString(String path,
                                                     int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * returns the bytesize of the give bitmap
     */
    public static int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    public static Bitmap decodeFrom64(byte[] bytesToDecode){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        byte[] bytes = Base64.decode(bytesToDecode, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }

    public static Bitmap loadBitmapFromFile(String photoPath){
        if (DEBUG) Log.v(TAG, "loadBitmapFromFile, Path: " + photoPath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        return bitmap;
    }

    public static String BitmapToString(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp){
        if (boundBoxInDp == 0)
            return null;

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return bitmap;
    }

    public static int[] calcNewImageSize(int[] imgDimensions, int bounds){
        int[] dimestions = new int[2];

        // Get current dimensions
        int width = imgDimensions[0];
        int height = imgDimensions[1];

        if (DEBUG) Log.v(TAG, "calcNewImageSize, W: " + width + ", H: " + height);

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounds) / width;
        float yScale = ((float) bounds) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        if (DEBUG) Log.v(TAG, "calcNewImageSize, Scale: "  + scale);

        dimestions[0] = (int) (width * scale);
        dimestions[1] = (int) (height * scale);

        if (DEBUG) Log.v(TAG, "calcNewImageSize, After W: " + dimestions[0] + ", H: " + dimestions[1]);

        return dimestions;
    }

    public static Bitmap getCompressed(String filePath){
        return getCompressed(filePath, BDefines.ImageProperties.MAX_WIDTH_IN_PX, BDefines.ImageProperties.MAX_HEIGHT_IN_PX);
    }

    /*http://voidcanvas.com/whatsapp-like-image-compression-in-android/*/
    public static Bitmap getCompressed(String filePath, float maxWidth, float maxHeight){
        if (DEBUG) Log.d(TAG, "Max Width: " + maxWidth+ ", Max Height: " + maxHeight);

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (DEBUG) Log.d(TAG, "Actual Width: " + actualWidth + ", Actual Height: " + actualHeight);

//      max Height and width values of the compressed image is taken as 816x612
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio =  maxWidth / maxHeight;

        if (DEBUG) Log.d(TAG, "Image Ratio: " + imgRatio + ", Max Ratio: " + maxRatio);

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth)
        {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            }
            else if (imgRatio > maxRatio)
            {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else
            {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }


//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        if (DEBUG) Log.d(TAG, "Actual Width: " + actualWidth + ", Actual Height: " + actualHeight);
        if (actualHeight <= 0 || actualWidth <= 0)
            return null;

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            return null;
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }

            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

    public static void saveBitmapToFile(File file, Bitmap bitmap){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if (out != null)
                    out.close();
            } catch(Throwable ignore) {}
        }
    }

    public static String getDimensionString(Bitmap bitmap){
        if (bitmap == null)
            throw  new NullPointerException("Bitmap cannot be null");

        return WIDTH + bitmap.getWidth() +  DIVIDER + HEIGHT + bitmap.getHeight();
    }

    public static int[] getDimentionsFromString(String dimensions){
        if (StringUtils.isEmpty(dimensions))
            throw new IllegalArgumentException("dimensions cannot be empty");

        String[] dimen = dimensions.split(DIVIDER);

        if (dimen.length != 2)
            throw new IllegalArgumentException("The dimensions string us invalid.");

        // Removing the letters from the String.
        dimen[0] = dimen[0].substring(1);
        dimen[1] = dimen[1].substring(1);

        return new int[]{ Integer.parseInt(dimen[0]), Integer.parseInt(dimen[1]) };
    }

    public static final String DIVIDER = "&", HEIGHT = "H", WIDTH = "W";
}
