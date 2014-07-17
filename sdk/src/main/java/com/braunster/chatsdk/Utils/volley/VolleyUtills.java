package com.braunster.chatsdk.Utils.volley;


import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 *
 * @author Ognyan Bankov
 *
 */
public class VolleyUtills {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;


    private VolleyUtills() {
        // no instances
    }


    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // Use 1/6th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memClass / 6;
        mRequestQueue.start();

        mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache(cacheSize));

    }


    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }


    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static class BitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
        public BitmapCache(int maxSize) {
            super(maxSize);
        }


        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            int size = bitmap.getByteCount() / 1024;
            return size;
        }

        public boolean contains(String key){
            return get(key) != null;
        }

        @Override
        public Bitmap getBitmap(String key) {
            return get(key);
        }


        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }
    }
}