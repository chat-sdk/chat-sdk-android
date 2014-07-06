package com.braunster.chatsdk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.braunster.chatsdk.R;

/**
 * Created by braunster on 04/07/14.
 */
public class ChatBubbleImageView extends ImageView {

    private String data;

    public ChatBubbleImageView(Context context, String data) {
        super(context);
        this.data = data;
    }

    public ChatBubbleImageView(Context context) {
        super(context);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

//        if (!isInEditMode())
//        {
//            setImageBitmap(getChatBubleBitmapForImage());
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Bitmap image = Utils.decodeFrom64(data.getBytes());
//        Bitmap buble = getChatBubleBitmapForImage();
//
//        image = Bitmap.createScaledBitmap(image, getWidth() /2, getHeight()/2, true);
//
//        canvas.drawBitmap(buble,0,0, null);
//        canvas.drawBitmap(image,buble.getWidth() /4, buble.getHeight()/4 , null);

        Bitmap buble =  BitmapFactory.decodeResource(getContext().getResources(), R.drawable.chat_bubble_reply);
        String text = "Some nice piece of text.";
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
//        float width = paint.measureText("Some Text");
        Rect bounds = new Rect();
        paint.getTextBounds(text,0, text.length(), bounds);
        buble = Bitmap.createScaledBitmap(buble, bounds.width() + 50 , bounds.height() + 50, true);

        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawBitmap(buble, 0, 0, null);
        canvas.drawText("Some Text", 50, 50, paint);
    }

    public void setData(String data) {
        this.data = data;
    }

    private Bitmap getChatBubleBitmapForImage(){
        Bitmap buble =  BitmapFactory.decodeResource(getContext().getResources(), R.drawable.chat_bubble_reply);

        buble = Bitmap.createScaledBitmap(buble, getWidth() - (getWidth()/6), getHeight() - (getWidth()/6), true);
        return buble;
    }
}
