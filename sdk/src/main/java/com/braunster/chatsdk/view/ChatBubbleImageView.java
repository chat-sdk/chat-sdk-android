package com.braunster.chatsdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;

/**
 * Created by braunster on 04/07/14.
 */
public class ChatBubbleImageView extends ImageView /*implements View.OnTouchListener */{

    public static final String TAG = ChatBubbleImageView.class.getSimpleName();
    public static final boolean DEBUG = true;

    private Bitmap bubble, image;

    /** The max size that we would use for the image.*/
    public final float MAX_WIDTH = 200 * getResources().getDisplayMetrics().density;

    /** The size in pixels of the chat bubble point. i.e the the start of the bubble.*/
    private float pointSize = 6.5f * getResources().getDisplayMetrics().density;

    private int imagePadding = (int) (10 * getResources().getDisplayMetrics().density);

    private float roundRadius = /*18.5f*/ 12f * getResources().getDisplayMetrics().density;

    private boolean pressed = false;

    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_RIGHT = 1;

    public static final int BubbleDefaultPressedColor = Color.parseColor("#27ae60");
    public static final int BubbleDefaultColor = Color.parseColor("#3498db");

    private int bubbleGravity = GRAVITY_LEFT, bubbleColor = 0, pressedColor = BubbleDefaultPressedColor;

    public ChatBubbleImageView(Context context) {
        super(context);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(attrs);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(attrs);
        // Note style not supported.
    }

    private void getAttrs(AttributeSet attrs){
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.ChatBubbleImageView);

        try {
            // Gravity of the bubble. Left or Right.
            bubbleGravity = a.getInt(
                    R.styleable.ChatBubbleImageView_bubble_gravity, GRAVITY_LEFT);

            // Bubble color. The color could be changed when loading the the image url.
            bubbleColor = a.getColor(R.styleable.ChatBubbleImageView_bubble_color, BubbleDefaultColor);

            // The color of the bubble when pressed.
            pressedColor = a.getColor(R.styleable.ChatBubbleImageView_bubble_pressed_color, BubbleDefaultPressedColor);

            imagePadding = a.getDimensionPixelSize(R.styleable.ChatBubbleImageView_image_padding, imagePadding);
        } finally {
            a.recycle();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode())
            return;

        if (image == null)
            return;

        if (pressed)
            bubble = setBubbleColor(bubble, pressedColor);
        else bubble = setBubbleColor(bubble, bubbleColor);

        if (bubbleGravity == GRAVITY_RIGHT)
        {
            canvas.drawBitmap(bubble, getMeasuredWidth() - bubble.getWidth(), 0 , null);
            canvas.drawBitmap(image, getMeasuredWidth() - bubble.getWidth() + imagePadding /2 +  pointSize, imagePadding /2 , null);
        }
        else
        {
            canvas.drawBitmap(bubble,0, 0 , null);
            canvas.drawBitmap(image, imagePadding /2 +  pointSize, imagePadding /2 , null);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (DEBUG) Log.v(TAG, "drawableStateChanged, "
                + (isPressed()?"Pressed":"Not Pressed")
                + ", " + (isFocused()?"Focused":"Not Focused")
                + ", " + (isEnabled()?"Enabled":"Not Enabled")
                + ".");

        if (!pressed && isPressed())
        {
            pressed = true;
            invalidate();
        }
        else if (pressed && !isPressed())
        {
            pressed = false;
            invalidate();
        }
    }

    private void setImage(Bitmap image) {
        this.image = image;
    }

    private void setBubble(Bitmap bubble) {
        this.bubble = bubble;
    }

    public void loadFromUrl(String url, int maxWidth, LoadDone loadDone){
       loadFromUrl(url, Color.parseColor("#F20D08"), maxWidth, loadDone);
    }

    public void loadFromUrl(String url, String color, int maxWidth, LoadDone loadDone){
        int bubbleColor = -1;
        try{
            bubbleColor = Color.parseColor(color);
        }
        catch (Exception e){}

        if (bubbleColor == -1)
        {
            bubbleColor = Color.parseColor(Float.toHexString(Float.parseFloat(color)));
        }

        loadFromUrl(url, bubbleColor, maxWidth, loadDone);
    }

    public void loadFromUrl(String url, final int color,final int maxWidth, final LoadDone loadDone){
        VolleyUtills.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {

                if (response.getBitmap() != null) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (DEBUG) Log.d(TAG, "MaxWidth = " + maxWidth + " , MAX_WIDTH = " + MAX_WIDTH);

                            bubbleColor = color;

                            // Calculating the image width so we could scale it.
                            // If the wanted width is bigger then MAX_WIDTH we will use MAX_WIDTH not the given width.
                            final int width;
                            if (maxWidth > MAX_WIDTH) {
                                width = (int) (MAX_WIDTH - imagePadding - pointSize);
                            }
                            else {
                                width = (int) (maxWidth - imagePadding - pointSize);
                            }

                            if (DEBUG) Log.d(TAG, "new image size: " + width);

                            // The image bitmap from Volley.
                            Bitmap img = response.getBitmap();
                            Bitmap bubble;

                            // scaling the image to the needed width.
                            img = ImageUtils.scaleImage(img, width);

                            // Getting the bubble nine patch image for given size.
                            if (bubbleGravity == GRAVITY_LEFT)
                                bubble = get_ninepatch(R.drawable.bubble_left_2, (int) (img.getWidth() + imagePadding + pointSize), (int) (img.getHeight() + imagePadding), getContext());
                            else
                                bubble = get_ninepatch(R.drawable.bubble_right_2, (int) (img.getWidth() + imagePadding + pointSize), (int) (img.getHeight() + imagePadding), getContext());

                            // Replacing the defualt color of the bubble.
                            bubble = replaceIntervalColor(bubble, 40, 75, 130, 140, 190, 210, color);

                            // Setting the bubble bitmap. It will be used in onDraw
                            setBubble(bubble);

                            // rounding the corners of the image.
                            img = getRoundedCornerBitmap(img, roundRadius);

                            // Setting the image bitmap. It will be used in onDraw
                            setImage(img);

                            // Notifying the view that we are done.
                            Message message = new Message();
                            message.arg1 = bubble.getWidth();
                            message.arg2 = bubble.getHeight();
                            message.obj = loadDone;
                            handler.sendMessage(message);
                        }
                    }).start();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (DEBUG){
                    Log.e(TAG, "Image Load Error: " + error.getMessage());
                    error.printStackTrace();
                }
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = msg.arg1;
            params.height = msg.arg2;
            // existing height is ok as is, no need to edit it
            setLayoutParams(params);

//            setOnTouchListener(ChatBubbleImageView.this);

            ((LoadDone) msg.obj).onDone();

            invalidate();
        }
    };

    public static Bitmap get_ninepatch(int id,int x, int y, Context context){
        // id is a resource id for a valid ninepatch
        Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), id);

        byte[] chunk = bitmap.getNinePatchChunk();
        NinePatchDrawable np_drawable = new NinePatchDrawable(context.getResources(), bitmap,
                chunk, new Rect(), null);
        np_drawable.setBounds(0, 0,x, y);

        Bitmap output_bitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output_bitmap);
        np_drawable.draw(canvas);

        return output_bitmap;
    }

    public static Bitmap setBubbleColor(Bitmap bubble, int color){
        if (DEBUG) Log.v(TAG, "setBubbleColor, color: " + color);
        return replaceIntervalColor(bubble, 40, 75, 130, 140, 190, 210, color);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        final Rect strikeRect = new Rect(0, 0, bitmap.getWidth() + 10, bitmap.getHeight() + 10);
        final RectF strokeRectF = new RectF(strikeRect);
        final Paint strokePaint = new Paint();
        strokePaint.setColor(0xff000000);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

//        canvas.drawRoundRect(strokeRectF, roundPx, roundPx, strokePaint);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap replaceIntervalColor(Bitmap bitmap, int oldColor, int newColor){
        return replaceIntervalColor(bitmap,
                Color.red(oldColor), Color.red(oldColor),
                Color.green(oldColor), Color.green(oldColor),
                Color.blue(oldColor), Color.blue(oldColor),
                newColor);
    }

    public static Bitmap replaceIntervalColor(Bitmap bitmap,
                                              int redStart, int redEnd,
                                              int greenStart, int greenEnd,
                                              int blueStart, int blueEnd,
                                              int colorNew) {
        if (bitmap != null) {
            int picw = bitmap.getWidth();
            int pich = bitmap.getHeight();
            int[] pix = new int[picw * pich];
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);
            for (int y = 0; y < pich; y++) {
                for (int x = 0; x < picw; x++) {
                    int index = y * picw + x;
                    if (
                            ((Color.red(pix[index]) >= redStart)&&(Color.red(pix[index]) <= redEnd))&&
                                    ((Color.green(pix[index]) >= greenStart)&&(Color.green(pix[index]) <= greenEnd))&&
                                    ((Color.blue(pix[index]) >= blueStart)&&(Color.blue(pix[index]) <= blueEnd)) ||
                                    Color.alpha(pix[index]) > 0
                            ){

                        // If the alpha is not full that means we are on the edges of the bubbles so we create the new color with the old alpha.
                        if (Color.alpha(pix[index]) > 0)
                        {
//                            Log.i(TAG, "PIX: " + Color.alpha(pix[index]));
                            pix[index] = Color.argb(Color.alpha(pix[index]), Color.red(colorNew), Color.green(colorNew), Color.blue(colorNew));
                        }
                        else
                            pix[index] = colorNew;
                    }
                }
            }

            return Bitmap.createBitmap(pix, picw, pich,Bitmap.Config.ARGB_8888);
        }
        return null;
    }

    public interface LoadDone{
        public void onDone();
    }

    public void setBubbleGravity(int bubbleGravity) {
        this.bubbleGravity = bubbleGravity;
    }

    public void setImagePadding(int imagePadding) {
        this.imagePadding = imagePadding;
    }

    public void setBubbleColor(int bubbleColor) {
        this.bubbleColor = bubbleColor;
    }

    public int getBubbleGravity() {
        return bubbleGravity;
    }

    public int getBubbleColor() {
        return bubbleColor;
    }

    public int getImagePadding() {
        return imagePadding;
    }
}
