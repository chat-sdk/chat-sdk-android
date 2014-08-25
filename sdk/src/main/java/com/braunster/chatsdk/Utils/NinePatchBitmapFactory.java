package com.braunster.chatsdk.Utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 https://gist.github.com/briangriffey/4391807
 */
public class NinePatchBitmapFactory {

    // The 9 patch segment is not a solid color.
    private static final int NO_COLOR = 0x00000001;

    // The 9 patch segment is completely transparent.
    private static final int TRANSPARENT_COLOR = 0x00000000;

    public static NinePatchDrawable createNinePathWithCapInsets(Resources res, Bitmap bitmap, int top, int left, int bottom, int right, String srcName) {
        ByteBuffer buffer = getByteBuffer(top, left, bottom, right);
        NinePatchDrawable drawable = new NinePatchDrawable(res, bitmap, buffer.array(), new Rect(), srcName);
        return drawable;
    }

    public static NinePatch createNinePatch(Resources res, Bitmap bitmap, int top, int left, int bottom, int right, String srcName) {
        ByteBuffer buffer = getByteBuffer(top, left, bottom, right);
        NinePatch patch = new NinePatch(bitmap, buffer.array(), srcName);
        return patch;
    }

    private static ByteBuffer getByteBuffer(int top, int left, int bottom, int right) {
        //Docs check the NinePatchChunkFile
        ByteBuffer buffer = ByteBuffer.allocate(56).order(ByteOrder.nativeOrder());
        //was translated
        buffer.put((byte)0x01);
        //divx size
        buffer.put((byte)0x02);
        //divy size
        buffer.put((byte)0x02);
        //color size
        buffer.put(( byte)0x02);

        //skip
        buffer.putInt(0);
        buffer.putInt(0);

        //padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);

        //skip 4 bytes
        buffer.putInt(0);

        buffer.putInt(left);
        buffer.putInt(right);
        buffer.putInt(top);
        buffer.putInt(bottom);
        buffer.putInt(NO_COLOR);
        buffer.putInt(NO_COLOR);

        return buffer;
    }

}