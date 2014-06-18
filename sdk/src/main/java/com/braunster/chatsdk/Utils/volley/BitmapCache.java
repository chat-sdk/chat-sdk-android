package com.braunster.chatsdk.Utils.volley;

/**
 * Created by braunster on 18/06/14.
 */

import com.android.volley.toolbox.ImageLoader.ImageCache;

        import android.graphics.Bitmap;
        import android.support.v4.util.LruCache;


public class BitmapCache extends LruCache<String, Bitmap> implements ImageCache {
    public BitmapCache(int maxSize) {
        super(maxSize);
    }


    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }


    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }


    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
