
package com.braunster.chatsdk.Utils.volley;


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
public class VolleyUtils {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static BitmapCache bitmapCache;

    private VolleyUtils() {
        // no instances
    }


    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        int cacheSize = maxMemory / 8 ;

        mRequestQueue.start();

        bitmapCache = new BitmapCache(cacheSize);
        mImageLoader = new ImageLoader(mRequestQueue, bitmapCache);

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

    public static BitmapCache getBitmapCache() {
        return bitmapCache;
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

        /**
         * Creates a cache key for use with the L1 cache.
         * @param url The URL of the request.
         * @param maxWidth The max-width of the output.
         * @param maxHeight The max-height of the output.
         */
        public static String getCacheKey(String url, int maxWidth, int maxHeight) {
            return new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                    .append("#H").append(maxHeight).append(url).toString();
        }

        /**
         * Creates a cache key for use with the L1 cache.
         * @param url The URL of the request.
         */
        public static String getCacheKey(String url) {
            return new StringBuilder(url.length() + 12).append("#W").append(0)
                    .append("#H").append(0).append(url).toString();
        }

        /**
         * Creates a cache key for use with the L1 cache.
         * @param url The URL of the request.
         * @param maxWidth The max-width of the output.
         * @param maxHeight The max-height of the output.
         */
        public static String getCacheKey(StringBuilder builder, String url, int maxWidth, int maxHeight) {
            builder.setLength(0);
            builder.setLength(url.length() + 12);
            return builder.append("#W").append(maxWidth)
                    .append("#H").append(maxHeight).append(url).toString();
        }
        

    }
}