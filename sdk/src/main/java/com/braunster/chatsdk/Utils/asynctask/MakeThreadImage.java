/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.asynctask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractThreadsListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/** Async task that craet mix images bitmap for the thread image.
 *
 *  Before creating the new image the task will check to see if there is an image cached for this thread for the given amount of urls,
 *  This way if there was two images mix and now there need to be three the task will create a new image.
 *
 *  */
public class MakeThreadImage extends AsyncTask<Bitmap, Void, Bitmap> {

    protected static final String TAG = ChatSDKAbstractThreadsListAdapter.class.getSimpleName();
    protected static final boolean DEBUG = Debug.ThreadsListAdapter;

    private int width, height;
    private WeakReference<LoadBitmapsForThreadImage> loadBitmapsForThreadImage;
    private String cacheKey;

    private int urlsLength = 0;

    private ImageView image;

    private ProgressBar progressBar;

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (loadBitmapsForThreadImage!=null && loadBitmapsForThreadImage.get() != null)
            loadBitmapsForThreadImage.get().kill();
    }

    public MakeThreadImage(String[] urls, int width, int height, String cacheKey, ImageView image) {
        this.width = width;
        this.height = height;
        this.cacheKey = cacheKey;
        this.image = image;

        urlsLength= urls.length;

        // Loading from cache
        if (VolleyUtils.getBitmapCache().contains(getCacheKey(cacheKey, urlsLength)))
        {
            image.setImageBitmap(VolleyUtils.getBitmapCache().getBitmap(getCacheKey(cacheKey, urlsLength)));
            image.setVisibility(View.VISIBLE);
            image.bringToFront();
        }
        // Creating an image.
        else
        {
            loadBitmapsForThreadImage = new WeakReference<LoadBitmapsForThreadImage>(new LoadBitmapsForThreadImage(urls));
            loadBitmapsForThreadImage.get().run();
        }
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        return ImageUtils.getMixImagesBitmap(width, height, params);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (bitmap != null)
        {
            // Save image to cache.
            VolleyUtils.getBitmapCache().put(getCacheKey(cacheKey, urlsLength), bitmap);
        }

        if (!isCancelled())
        {
            if (bitmap != null) {
                {
                    image.setImageBitmap(bitmap);

                    if (progressBar!=null)
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        image.setVisibility(View.VISIBLE);
                    }
                }
            } else setMultipleUserDefaultImg();
        }
    }

    
    
    /** 
     * Load images for given urls array.
     * After all images are loaded the runnable will execute the makeThreadImage AsyncTask.
     * * */
    private class LoadBitmapsForThreadImage implements Runnable {

        private LoadBitmapsForThreadImage(String[] urls) {
            this.urls = urls;
        }

        private boolean killed = false;

        private int loadedCount = 0;

        /** Bitmaps that was loaded*/
        private List<Bitmap> bitmaps = new ArrayList<Bitmap>();

        /** Given urls to load*/
        private String[] urls;

        /** To keep track of urls that caused an error so we wont count them twice.*/
        private List<String> errorUrls = new ArrayList<String>();

        public void kill() {
            this.killed = true;
        }

        private void load(final String... urls) {
            this.urls = urls;

            for (final String url : urls) {
                VolleyUtils.getImageLoader().get(url, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (killed)
                            return;

                        if (response.getBitmap() != null) {
                            bitmaps.add(response.getBitmap());
                            loadedCount++;

                            dispatchFinishedIfDid();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (killed)
                            return;

                        if (!errorUrls.contains(url))
                        {
                            loadedCount++; 
                            if (DEBUG) Timber.e("Image Load Error: %s", error.getMessage());
                            errorUrls.add(url);
                            dispatchFinishedIfDid();
                        }
                    }
                });
            }
        }

        private void dispatchFinishedIfDid(){
            if (killed)
                return;

            if (urls.length == loadedCount)
            {
                MakeThreadImage.this.execute(bitmaps.toArray(new Bitmap[bitmaps.size()]));
            }
        }

        @Override
        public void run() {
            load(urls);
        }

    }

    
    
    
    
    public void setMultipleUserDefaultImg(){
        image.setImageResource(R.drawable.ic_users);
    }

    public static String getCacheKey(String key, int size){
        return key + "S" + size;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
