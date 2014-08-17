package com.braunster.chatsdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.network.BDefines;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by braunster on 04/07/14.
 */
public class ChatBubbleImageView2 extends ImageView /*implements View.OnTouchListener */{

    public static final String TAG = ChatBubbleImageView2.class.getSimpleName();
    public static final boolean DEBUG = Debug.ChatBubbleImageView;

    private Bitmap image;

    /** The max size that we would use for the image.*/
    public final float MAX_WIDTH = 200 * getResources().getDisplayMetrics().density;

    /** The size in pixels of the chat bubble point. i.e the the start of the bubble.*/
    private float pointSize = 4.2f * getResources().getDisplayMetrics().density;

    private int imagePadding = (int) (10 * getResources().getDisplayMetrics().density);

    private float roundRadius = /*18.5f*/ 6f * getResources().getDisplayMetrics().density;

    private boolean pressed = false;

    private String imageUrl = "";

    private Loader loader;

    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_RIGHT = 1;

    public static final int BubbleDefaultPressedColor = Color.parseColor(BDefines.Defaults.BubbleDefaultColor);
    public static final int BubbleDefaultColor = Color.parseColor(BDefines.Defaults.BubbleDefaultPressedColor);


    private boolean showClickIndication = false;

    private int bubbleGravity = GRAVITY_LEFT, bubbleColor = Color.BLACK, pressedColor = BubbleDefaultPressedColor;

    public ChatBubbleImageView2(Context context) {
        super(context);

        init();
    }

    public ChatBubbleImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(attrs);

        init();
    }

    public ChatBubbleImageView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(attrs);

        init();
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

            showClickIndication = a.getBoolean(R.styleable.ChatBubbleImageView_bubble_with_click_indicator, false);
        } finally {
            a.recycle();
        }

    }

    private void init(){
        if (bubbleGravity == GRAVITY_RIGHT)
        {
            setBackgroundResource(R.drawable.bubble_right);
        }
        else
        {
            setBackgroundResource(R.drawable.bubble_left);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode())
            return;

        /*if (showClickIndication)
        {
            if (pressed)
            {
            }
            else {
            }
        }*/

        if (image == null)
        {
            return;
        }

        if (bubbleGravity == GRAVITY_RIGHT)
        {
            canvas.drawBitmap(image,  imagePadding /2 , imagePadding /2 , null);
        }
        else
        {
            canvas.drawBitmap(image, imagePadding /2 +  pointSize, imagePadding /2 , null);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (!showClickIndication)
            return;

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

    private void clearCanvas(){
        image = null;
        if (DEBUG) Log.i(TAG, "Clearing canvas");
        init();
    }

    private void setImage(Bitmap image) {
        this.image = image;
    }

    public void loadFromUrl(final String url, final LoadDone loadDone){
        imageUrl = url;

        if (StringUtils.isNotEmpty(imageUrl) && VolleyUtills.getImageLoader().isCached(this.imageUrl, 0, 0))
        {
            if (DEBUG) Log.d(TAG, "image is cached: " + imageUrl);
        }
        else if (DEBUG) Log.d(TAG, "image isn't cached: " + imageUrl);

        clearCanvas();

        if (loader != null)
            loader.setKilled(true);

        loader = new Loader(loadDone);
        VolleyUtills.getImageLoader().get(url, loader);
    }

    class Loader implements ImageLoader.ImageListener{
        private boolean firstOnResponse = true, isKilled = false;
        private LoadDone loadDone;

        public Loader(LoadDone loadDone){
            this.loadDone = loadDone;
        }

        public void setKilled(boolean isKilled) {
            this.isKilled = isKilled;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

            if (isKilled)
                return;

            if (firstOnResponse){
                if (loadDone != null)
                    loadDone.immediate(response.getBitmap() != null);

                firstOnResponse = false;
            }

            if (response.getBitmap() != null) {

                // The image bitmap from Volley.
                Bitmap img = response.getBitmap();

                // scaling the image to the needed width.
                img = ImageUtils.scaleImage(img, (int) MAX_WIDTH);

                // rounding the corners of the image.
                img = getRoundedCornerBitmap(img, roundRadius);

                // Setting the image bitmap. It will be used in onDraw
                setImage(img);

                if (loadDone != null)
                    loadDone.onDone();

                invalidate();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (DEBUG){
                Log.e(TAG, "Image Load Error: " + error.getMessage());
                error.printStackTrace();
            }
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public interface LoadDone{
        public void onDone();
        public void immediate(boolean immediate);
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

    public float getPointSize() {
        return pointSize;
    }
}
