package co.chatsdk.android.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;


public class BlurHelper {

    public static void blurBitmapWithRenderscript(RenderScript rs, Bitmap bitmap2) {
        //this will blur the bitmapOriginal with a radius of 25 and save it in bitmapOriginal
        final Allocation input = Allocation.createFromBitmap(rs, bitmap2); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        // must be >0 and <= 25
        script.setRadius(25f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap2);
    }

    public static Bitmap roundCorners(Bitmap bitmap,
                                      int cornerRadiusInPixels,
                                      boolean captureCircle) {
        Bitmap output = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(output);

        final int color = 0xffffffff;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = cornerRadiusInPixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        if (captureCircle) {
            canvas.drawCircle(rectF.centerX(),
                    rectF.centerY(),
                    bitmap.getWidth() / 2,
                    paint);
        } else {
            canvas.drawRoundRect(rectF,
                    roundPx,
                    roundPx,
                    paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getRoundedCornerAndLightenBitmap(Bitmap bitmap, int cornerRadiusInPixels, boolean captureCircle) {
        Bitmap output = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(output);

        final int color = 0xffffffff;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = cornerRadiusInPixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        if (captureCircle) {
            canvas.drawCircle(rectF.centerX(),
                    rectF.centerY(),
                    bitmap.getWidth() / 2,
                    paint);
        } else {
            canvas.drawRoundRect(rectF,
                    roundPx,
                    roundPx,
                    paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        ColorFilter filter = new LightingColorFilter(0xFFFFFFFF, 0x00222222); // lighten
        //ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}