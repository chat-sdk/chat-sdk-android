/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.dao.entities;

import android.graphics.Color;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Random;

/**
 * Created by braunster on 13/07/14.
 */
public abstract class BMessageEntity extends Entity {

    @IntDef({Type.TEXT, Type.IMAGE, Type.LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MessageType{}
    
    public static final class Type{
        public static final int TEXT = 0, IMAGE = 2, LOCATION = 1;
    }

    public static final class Status{
        public static final int NULL = 0, SENDING = 1, SENT = 2, FAILED = 3;
    }

    public static final class Delivered{
        public static final int Yes = 0, No= 1;
    }

    public abstract void setType(@MessageType Integer type);
    
    @MessageType
    public abstract Integer getType();
    
    public abstract String color();

    public static String colorToString(int color){
        return Integer.toHexString(color);
    }

    public static int stringToColor(String color){
        return Color.parseColor(color);
    }

    public static int randomColor(){
        Random random = new Random();
        switch (random.nextInt(9))
        {
            case 0:
                return Color.parseColor("#eea9a4");

            case 1:
                return Color.parseColor("#e2b27b");

            case 2:
                return Color.parseColor("#a28daf");

            case 3:
                return Color.parseColor("#bcc9ab");

            case 4:
                return Color.parseColor("#f4e6b8");

            case 5:
                return Color.parseColor("#8ebdd1");

            case 6:
                return Color.parseColor("#c0d2a1");

            case 7:
                return Color.parseColor("#9acccb");

            case 8:
                return Color.parseColor("#9ccaa7");
        }

        return 0;
    }

}
