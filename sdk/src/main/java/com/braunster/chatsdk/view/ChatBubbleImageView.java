/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.thread.ChatSDKImageMessagesThreadPool;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class ChatBubbleImageView extends ImageView /*implements View.OnTouchListener */{

    public static final boolean DEBUG = Debug.ChatBubbleImageView;

    private WeakReference<Bitmap>image;

    private Loader loader = new Loader();
    private LoadDone loadDone ;

    /** The size in pixels of the chat bubble point. i.e the the start of the bubble.*/
    private float tipSize = 4.2f * getResources().getDisplayMetrics().density;

    private int imagePadding = (int) (10 * getResources().getDisplayMetrics().density);

    private float cornerRadius = /*18.5f*/ 6f * getResources().getDisplayMetrics().density;

    private boolean pressed = false;

    private boolean fromUrl = true;
    
    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_RIGHT = 1;

    public static final int BubbleDefaultPressedColor = Color.parseColor(BDefines.Defaults.BubbleDefaultColor);
    public static final int BubbleDefaultColor = Color.parseColor(BDefines.Defaults.BubbleDefaultPressedColor);

    private boolean showClickIndication = false;

    private int bubbleGravity = GRAVITY_LEFT, bubbleColor = Color.BLACK, pressedColor = BubbleDefaultPressedColor;

    private Drawable bubbleBackground = null;

    public static final String URL_FIX = "fix";

    private String bubbleImageUrl = "";
    private int imgWidth, imgHeight;
    
    private LoadAndFixImageFromFileTask loadAndFixImageFromFileTask;
    
    public ChatBubbleImageView(Context context) {
        super(context);
        init();
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(attrs);

        init();
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(attrs);

        init();
        // Note style not supported.
    }

    
    public interface LoadDone{
        public void onDone();
        public void immediate(boolean immediate);
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

            bubbleBackground = a.getDrawable(R.styleable.ChatBubbleImageView_bubble_background);

            tipSize = a.getDimensionPixelSize(R.styleable.ChatBubbleImageView_bubble_tip_size, (int) tipSize);

            cornerRadius = a.getDimensionPixelSize(R.styleable.ChatBubbleImageView_bubble_image_corner_radius, (int) cornerRadius);
        } finally {
            a.recycle();
        }

    }

    private void init(){
        if (bubbleBackground!=null){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                setBackgroundDrawable(bubbleBackground);
            else setBackground(bubbleBackground);
        }
        else
        {
            if (bubbleGravity == GRAVITY_RIGHT)
            {
                setBackgroundResource(R.drawable.bubble_right);
            }
            else
            {
                setBackgroundResource(R.drawable.bubble_left);
            }
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

        if (image == null || image.get() == null)
        {
            if (fromUrl)
                loadFromUrl(bubbleImageUrl, loadDone, imgWidth, imgHeight);
            else
                loadFromPath(bubbleImageUrl, loadDone, imgWidth, imgHeight);
            return;
        }

        if (bubbleGravity == GRAVITY_RIGHT)
        {
            canvas.drawBitmap(image.get(),  imagePadding /2 , imagePadding /2 , null);
        }
        else
        {
            canvas.drawBitmap(image.get(), imagePadding /2 + tipSize, imagePadding /2 , null);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (!showClickIndication)
            return;

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

    public void clearCanvas(){
        image = null;
        init();
    }

    
    
    public void loadFromUrl(String url, final LoadDone loadDone, int width, int height){
      
        boolean isCachedWithSize = StringUtils.isNotEmpty(url) && VolleyUtils.getImageLoader().isCached(url + URL_FIX, 0, 0);

        if (DEBUG) Timber.v("loadFromUrl, Url: %s, Width: %s, Height: %s", url, width, height);
        
        fromUrl = true;
        
        if (isCachedWithSize)
            url += URL_FIX;

        this.bubbleImageUrl = url;
        this.imgHeight = height;
        this.imgWidth = width;
        this.loadDone = loadDone;

        if (loader != null)
            loader.setKilled(true);

        loader = new Loader(height, width, url, loadDone, isCachedWithSize);

        VolleyUtils.getImageLoader().get(url, loader, 0, 0);
    }

    class Loader implements ImageLoader.ImageListener{
        private boolean isKilled = false, isCachedWithSize = false;
        private LoadDone loadDone;
        private String imageUrl = "";
        private int width, height;

        private FixImageAsyncTask fixImageAsyncTask;

        private Loader(){

        }

        Loader(int height, int width, String imageUrl, LoadDone loadDone, boolean isCachedWithSize) {
            this.height = height;
            this.width = width;
            this.imageUrl = imageUrl;
            this.loadDone = loadDone;
            this.isCachedWithSize = isCachedWithSize;
        }

        public void setKilled(boolean isKilled) {
            this.isKilled = isKilled;

            // Cancel the previous task so it wont affect the the UI when the task finished.
            if (fixImageAsyncTask != null)
            {
                fixImageAsyncTask.cancel(true);
                fixImageAsyncTask = null;
            }
        }

        @Override
        public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
            if (isImmediate)
            {
                if (loadDone != null)
                    loadDone.immediate(response.getBitmap() != null);
            }

            if (response.getBitmap() != null) {

                if (isCachedWithSize)
                {
                    if (isKilled)
                        return;

                    image = new WeakReference<Bitmap>(response.getBitmap());
                    invalidate();

                    if (loadDone != null)
                        loadDone.onDone();
                }
                else
                {
                    // If the image was already in the cache that means that there is a task to fix the image.
                    if (isImmediate)
                        return;

                    // Create a new task to fix the image size.
                    fixImageAsyncTask = new FixImageAsyncTask(loadDone, this.imageUrl, width, height, isKilled);
                    fixImageAsyncTask.execute(response.getBitmap());
                }
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (DEBUG){
                Timber.e("Image Load Error: %s", error.getMessage());
                error.printStackTrace();
            }
        }
    }

    
    public void loadFromPath(String path, final LoadDone loadDone, int width, int height){
        if (DEBUG) Timber.v("loadFromPath, Path: %s, Width: %s, Height: %s", path, width, height);

        fromUrl = false;
        
        this.bubbleImageUrl = path;
        this.imgHeight = height;
        this.imgWidth = width;
        this.loadDone = loadDone;

        if (loader != null)
            loader.setKilled(true);

        loadImageFromFile(path, loadDone, width, height);
    }

    private void loadImageFromFile(final String path, final LoadDone loadDone, int width, int height){

        if (DEBUG) Timber.v("loadImageFromFile, Path: %s", path);

        image = new WeakReference<Bitmap>(VolleyUtils.getBitmapCache().getBitmap(
                VolleyUtils.BitmapCache.getCacheKey(path + URL_FIX)));
        
        if (image.get() != null)
        {
            if (loadDone != null)
            {
                invalidate();

                loadDone.immediate(true);

                loadDone.onDone();
            }

            return;
        }
        else if (loadDone != null)
            loadDone.immediate(false);

        // Cancelling the old task.
        if (loadAndFixImageFromFileTask != null)
            loadAndFixImageFromFileTask.isKilled = true;

        loadAndFixImageFromFileTask = new LoadAndFixImageFromFileTask(path, loadDone, width, height);
        ChatSDKImageMessagesThreadPool.getInstance().execute(loadAndFixImageFromFileTask);
    }

    private class LoadAndFixImageFromFileTask implements Runnable{
        private boolean isKilled = false;
        
        private LoadDone loadDone;
        private FixImageAsyncTask fixImageAsyncTask;
        private int width, height;
        private String path;

        private LoadAndFixImageFromFileTask (String path, LoadDone loadDone, int width, int height) {
            this.loadDone = loadDone;
            this.height = height;
            this.width = width;
            this.path = path;
        }
        
        @Override
        public void run() {

            Bitmap b;
            if (!VolleyUtils.getBitmapCache().contains(VolleyUtils.BitmapCache.getCacheKey(path)))
            {
                b = ImageUtils.getCompressed(path,
                        BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE,
                        BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);

                if (b != null)
                    VolleyUtils.getBitmapCache().put(VolleyUtils.BitmapCache.getCacheKey(path), b);
            }
            else b =  VolleyUtils.getBitmapCache().getBitmap(VolleyUtils.BitmapCache.getCacheKey(path));

            if (b == null)
            {
                if (loadDone != null)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            loadDone.onDone();
                        }
                    });

                return;
            }

            if (isKilled)
                return;

            // Create a new task to fix the image size.
            fixImageAsyncTask = new FixImageAsyncTask(loadDone, path, width, height, false);
            fixImageAsyncTask.execute(b);
        }
    }

    
    private class FixImageAsyncTask extends AsyncTask<Bitmap, Void, WeakReference<Bitmap>>{
        private String imageUrl = "";
        private int width, height;
        private LoadDone loadDone;
        private boolean killed = false;

        private FixImageAsyncTask(LoadDone loadDone, String imageUrl, int width, int height, boolean killed) {
            this.imageUrl = imageUrl;
            this.width = width;
            this.height = height;
            this.loadDone = loadDone;
            this.killed = killed;
        }

        @Override
        protected WeakReference<Bitmap> doInBackground(Bitmap... params) {
            Bitmap img;
            
            // scaling the image to the needed width.
            // rounding the corners of the image.
            img = getRoundedCornerBitmap(Bitmap.createScaledBitmap(params[0], width, height, true),
                    cornerRadius);

            if (img == null)
                return null;
            
            // Out with the old
            VolleyUtils.getBitmapCache().remove(VolleyUtils.BitmapCache.getCacheKey(this.imageUrl));
            
            // In with the new.
            VolleyUtils.getBitmapCache().put(VolleyUtils.BitmapCache.getCacheKey(this.imageUrl + URL_FIX), img);

            return new WeakReference<Bitmap>(img);
        }

        @Override
        protected void onPostExecute(WeakReference<Bitmap> bitmap) {
            super.onPostExecute(bitmap);

            // Validating the data so we wont show the wrong image.
            if (isCancelled() || bitmap == null || killed || !imageUrl.equals(bubbleImageUrl))
            {
                if (DEBUG) Timber.d("Async task is dead? %s", isCancelled());
                return;
            }

            image = bitmap;
            invalidate();

            if (loadDone != null)
                loadDone.onDone();
        }
    }
    
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);

        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
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

    public float getTipSize() {
        return tipSize;
    }
}
