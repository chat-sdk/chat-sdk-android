package com.braunster.chatsdk.dao.entities;

import android.graphics.Color;

import java.util.Random;

/**
 * Created by braunster on 13/07/14.
 */
public abstract class BMessageEntity extends Entity {

    public static final class Type{
        public static final int TEXT = 0, IMAGE = 2, LOCATION = 1;
    }

    public static final class Status{
        public static final int NULL = 0, SENDING = 1, SENT = 2, SENT_FAILED = 3;
    }

    public abstract String color();

    public abstract String textColor();

    public abstract int fontSize();

    public abstract String fontName();

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

    /*-(float) getTextHeightWithFont: (UIFont *) font withWidth: (float) width {
        return [self.text sizeWithFont:font constrainedToSize:CGSizeMake(width, 999999)].height;
    }

    +(NSString *) colorToString: (UIColor *) color {
        return [CIColor colorWithCGColor:color.CGColor].stringRepresentation;
    }

    +(UIColor *) stringToColor: (NSString *) color {
        return [UIColor colorWithCIColor:[CIColor colorWithString:color]];
    }*/
    /*-(UIImage *) thumbnail {
        if (self.type.intValue != bMessageTypeImage) {
            return Nil;
        }

        UIImage * thumbnail = objc_getAssociatedObject(self, bThumbnailKey);
        if(!thumbnail) {
            UIImage * image = [self textAsImage];
            thumbnail = [image resizedImage:CGSizeMake(bMaxMessageWidth, bMaxMessageWidth * image.size.height / image.size.width)
            interpolationQuality:kCGInterpolationHigh];
            objc_setAssociatedObject(self, bThumbnailKey, thumbnail, OBJC_ASSOCIATION_RETAIN);
        }
        return thumbnail;
    }*/
/*
    (UIFont *) fontWithName: (NSString *) name size: (float) size {
        if ([name isEqualToString:bSystemFont]) {
            return [UIFont systemFontOfSize:size];
        }
        else {
            return [UIFont fontWithName:name size:size];
        }
    }
*/
}
