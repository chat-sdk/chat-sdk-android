package com.braunster.chatsdk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;

/**
 * Created by braunster on 04/07/14.
 */
public class ImageBubleDrawable extends Drawable {
    private Context context;
    private String data;

    public ImageBubleDrawable(Context context, String data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap image = Utils.decodeFrom64(data.getBytes());
        Bitmap buble = getChatBubleBitmapForImage(image);
        canvas.drawBitmap(buble,0,0, null);
//        canvas.drawBitmap(image,0, 0 , null);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    private Bitmap getChatBubleBitmapForImage(Bitmap image){
        Bitmap buble =  BitmapFactory.decodeResource(context.getResources(), R.drawable.chat_bubble_reply);

        buble = Bitmap.createScaledBitmap(buble, image.getWidth(), image.getHeight(), true);
        return buble;
    }
}
