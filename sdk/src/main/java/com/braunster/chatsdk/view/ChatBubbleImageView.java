package com.braunster.chatsdk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;

/**
 * Created by braunster on 04/07/14.
 */
public class ChatBubbleImageView extends ImageView {

    public static final String TAG = ChatBubbleImageView.class.getSimpleName();
    public static final boolean DEBUG = true;

    private String data;
    private Bitmap buble, image;
    private LoadDone loadDone;

    public ChatBubbleImageView(Context context, String data) {
        super(context);
        this.data = data;
    }

    public ChatBubbleImageView(Context context) {
        super(context);

//        buble =  BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bubble_left_2);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        buble =  BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bubble_left_2);

//        setBackgroundResource(R.drawable.bubble_left_2);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);



//        setBackgroundResource(R.drawable.bubble_left_2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        buble =  get_ninepatch(R.drawable.bubble_left_2, getMeasuredWidth(), getMeasuredHeight(), getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (image == null)
            return;
//        int cx = (mWidth - myBitmap.getWidth()) / 2;
//        int cy = (mHeight - myBitmap.getHeight()) / 2;
        canvas.drawBitmap(buble,0, 0 , null);

        if (image != null)
        {
            canvas.drawBitmap(image, pad/2 +  pointSize, pad/2 , null);
        }


//        Bitmap image = Utils.decodeFrom64(data.getBytes());
//        Bitmap buble = getChatBubleBitmapForImage();
//
//        image = Bitmap.createScaledBitmap(image, getWidth() /2, getHeight()/2, true);
//
//        canvas.drawBitmap(buble,0,0, null);
//        canvas.drawBitmap(image,buble.getWidth() /4, buble.getHeight()/4 , null);

//        if (!isInEditMode())
//            canvas.drawBitmap(buble, 0, 0, null);
/*
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

        canvas.drawText("Some Text", 50, 50, paint);*/
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setImage(Bitmap image) {
        this.image = image;
        invalidate();
    }

    public void loadFromUrl(String url, LoadDone loadDone){
       loadFromUrl(url, Color.parseColor("#F20D08"), loadDone);
    }

    public void loadFromUrl(String url, String color, LoadDone loadDone){
        loadFromUrl(url, Color.parseColor(color), loadDone);
    }

    public void loadFromUrl(String url, final int color, final LoadDone loadDone){
        VolleyUtills.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                if (response.getBitmap() != null) {
                    // load image into imageview
                    Bitmap bitmap = response.getBitmap();

                    bitmap = scaleImage(bitmap, 300);

                    buble = get_ninepatch(R.drawable.bubble_left_2, (int) (bitmap.getWidth() + pad + pointSize), (int) (bitmap.getHeight() + pad), getContext());

                    buble = replaceIntervalColor(buble, 40, 75, 130, 140, 190, 210, color);

                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.width = buble.getWidth();
                    params.height = buble.getHeight();
                    // existing height is ok as is, no need to edit it
                    setLayoutParams(params);

                    bitmap = getRoundedCornerBitmap(bitmap, roundRadius);
                    setImage(bitmap);

                    loadDone.onDone();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (DEBUG) Log.e(TAG, "Image Load Error: " + error.getMessage());
            }
        });
    }

    private float pointSize = 6f * getResources().getDisplayMetrics().density;
    private int pad = 40;
    private float roundRadius = /*18.5f*/ 12f * getResources().getDisplayMetrics().density;



    private Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp){
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

    public static Bitmap get_ninepatch(int id,int x, int y, Context context){
        // id is a resource id for a valid ninepatch

        if (x == 0 || y == 0)
        {
            x = 100; y = 100;
        }
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



    public static Bitmap setBubbleColor(Bitmap buble, int color){
        if (DEBUG) Log.v(TAG, "setBubbleColor, color: " + color);
        return replaceIntervalColor(buble, 40, 75, 130, 140, 190, 210, color);
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
}
