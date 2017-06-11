/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

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
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.utils.volley.ImageUtils;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.core.utils.volley.VolleyUtils;
import com.braunster.chatsdk.thread.ChatSDKImageMessagesThreadPool;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class ChatBubbleImageView extends AppCompatImageView /*implements View.OnTouchListener */{

    public static final boolean DEBUG = Debug.ChatBubbleImageView;

    /** The size in pixels of the chat bubble point. i.e the the start of the bubble.*/
    private float tipSize = 4.2f * getResources().getDisplayMetrics().density;

    private int imagePadding = (int) (10 * getResources().getDisplayMetrics().density);

    public ChatBubbleImageView(Context context) {
        super(context);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatBubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    public int getImagePadding() {
        return imagePadding;
    }

    public float getTipSize() {
        return tipSize;
    }
}
