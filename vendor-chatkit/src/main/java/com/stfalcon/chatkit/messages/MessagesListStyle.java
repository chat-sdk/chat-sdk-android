/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.messages;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import com.stfalcon.chatkit.R;
import com.stfalcon.chatkit.commons.Style;

/**
 * Style for MessagesListStyle customization by xml attributes
 */
@SuppressWarnings("WeakerAccess")
public class MessagesListStyle extends Style {

    public int textAutoLinkMask;
    public int incomingTextLinkColor;
    public int outcomingTextLinkColor;

    public int incomingAvatarWidth;
    public int incomingAvatarHeight;

    public int incomingBubbleDrawable;
    public int incomingDefaultBubbleColor;
    public int incomingDefaultBubblePressedColor;
    public int incomingDefaultBubbleSelectedColor;

    public int incomingImageOverlayDrawable;
    public int incomingDefaultImageOverlayPressedColor;
    public int incomingDefaultImageOverlaySelectedColor;

    public int incomingDefaultBubblePaddingLeft;
    public int incomingDefaultBubblePaddingRight;
    public int incomingDefaultBubblePaddingTop;
    public int incomingDefaultBubblePaddingBottom;

    public int incomingTextColor;
    public int incomingTextSize;
    public int incomingTextStyle;

    public int incomingTimeTextColor;
    public int incomingTimeTextSize;
    public int incomingTimeTextStyle;

    public int incomingImageTimeTextColor;
    public int incomingImageTimeTextSize;
    public int incomingImageTimeTextStyle;

    public int outcomingBubbleDrawable;
    public int outcomingDefaultBubbleColor;
    public int outcomingDefaultBubblePressedColor;
    public int outcomingDefaultBubbleSelectedColor;

    public int outcomingImageOverlayDrawable;
    public int outcomingDefaultImageOverlayPressedColor;
    public int outcomingDefaultImageOverlaySelectedColor;

    public int outcomingDefaultBubblePaddingLeft;
    public int outcomingDefaultBubblePaddingRight;
    public int outcomingDefaultBubblePaddingTop;
    public int outcomingDefaultBubblePaddingBottom;

    public int outcomingTextColor;
    public int outcomingTextSize;
    public int outcomingTextStyle;

    public int outcomingTimeTextColor;
    public int outcomingTimeTextSize;
    public int outcomingTimeTextStyle;

    public int outcomingImageTimeTextColor;
    public int outcomingImageTimeTextSize;
    public int outcomingImageTimeTextStyle;

    public int dateHeaderPadding;
    public String dateHeaderFormat;
    public int dateHeaderTextColor;
    public int dateHeaderTextSize;
    public int dateHeaderTextStyle;

    public static MessagesListStyle parse(Context context, AttributeSet attrs) {
        MessagesListStyle style = new MessagesListStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MessagesList);

        style.textAutoLinkMask = typedArray.getInt(R.styleable.MessagesList_textAutoLink, 0);
        style.incomingTextLinkColor = typedArray.getColor(R.styleable.MessagesList_incomingTextLinkColor,
                style.getSystemAccentColor());
        style.outcomingTextLinkColor = typedArray.getColor(R.styleable.MessagesList_outcomingTextLinkColor,
                style.getSystemAccentColor());

        style.incomingAvatarWidth = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingAvatarWidth,
                style.getDimension(R.dimen.message_avatar_width));
        style.incomingAvatarHeight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingAvatarHeight,
                style.getDimension(R.dimen.message_avatar_height));

        style.incomingBubbleDrawable = typedArray.getResourceId(R.styleable.MessagesList_incomingBubbleDrawable, -1);
        style.incomingDefaultBubbleColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultBubbleColor,
                style.getColor(R.color.white_two));
        style.incomingDefaultBubblePressedColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultBubblePressedColor,
                style.getColor(R.color.white_two));
        style.incomingDefaultBubbleSelectedColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultBubbleSelectedColor,
                style.getColor(R.color.cornflower_blue_two_24));

        style.incomingImageOverlayDrawable = typedArray.getResourceId(R.styleable.MessagesList_incomingImageOverlayDrawable, -1);
        style.incomingDefaultImageOverlayPressedColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultImageOverlayPressedColor,
                style.getColor(R.color.transparent));
        style.incomingDefaultImageOverlaySelectedColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultImageOverlaySelectedColor,
                style.getColor(R.color.cornflower_blue_light_40));

        style.incomingDefaultBubblePaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingLeft,
                style.getDimension(R.dimen.message_padding_left));
        style.incomingDefaultBubblePaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingRight,
                style.getDimension(R.dimen.message_padding_right));
        style.incomingDefaultBubblePaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingTop,
                style.getDimension(R.dimen.message_padding_top));
        style.incomingDefaultBubblePaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingBottom,
                style.getDimension(R.dimen.message_padding_bottom));
        style.incomingTextColor = typedArray.getColor(R.styleable.MessagesList_incomingTextColor,
                style.getColor(R.color.dark_grey_two));
        style.incomingTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTextSize,
                style.getDimension(R.dimen.message_text_size));
        style.incomingTextStyle = typedArray.getInt(R.styleable.MessagesList_incomingTextStyle, Typeface.NORMAL);

        style.incomingTimeTextColor = typedArray.getColor(R.styleable.MessagesList_incomingTimeTextColor,
                style.getColor(R.color.warm_grey_four));
        style.incomingTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTimeTextSize,
                style.getDimension(R.dimen.message_time_text_size));
        style.incomingTimeTextStyle = typedArray.getInt(R.styleable.MessagesList_incomingTimeTextStyle, Typeface.NORMAL);

        style.incomingImageTimeTextColor = typedArray.getColor(R.styleable.MessagesList_incomingImageTimeTextColor,
                style.getColor(R.color.warm_grey_four));
        style.incomingImageTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingImageTimeTextSize,
                style.getDimension(R.dimen.message_time_text_size));
        style.incomingImageTimeTextStyle = typedArray.getInt(R.styleable.MessagesList_incomingImageTimeTextStyle, Typeface.NORMAL);

        style.outcomingBubbleDrawable = typedArray.getResourceId(R.styleable.MessagesList_outcomingBubbleDrawable, -1);
        style.outcomingDefaultBubbleColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultBubbleColor,
                style.getColor(R.color.cornflower_blue_two));
        style.outcomingDefaultBubblePressedColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultBubblePressedColor,
                style.getColor(R.color.cornflower_blue_two));
        style.outcomingDefaultBubbleSelectedColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultBubbleSelectedColor,
                style.getColor(R.color.cornflower_blue_two_24));

        style.outcomingImageOverlayDrawable = typedArray.getResourceId(R.styleable.MessagesList_outcomingImageOverlayDrawable, -1);
        style.outcomingDefaultImageOverlayPressedColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultImageOverlayPressedColor,
                style.getColor(R.color.transparent));
        style.outcomingDefaultImageOverlaySelectedColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultImageOverlaySelectedColor,
                style.getColor(R.color.cornflower_blue_light_40));

        style.outcomingDefaultBubblePaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingLeft,
                style.getDimension(R.dimen.message_padding_left));
        style.outcomingDefaultBubblePaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingRight,
                style.getDimension(R.dimen.message_padding_right));
        style.outcomingDefaultBubblePaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingTop,
                style.getDimension(R.dimen.message_padding_top));
        style.outcomingDefaultBubblePaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingBottom,
                style.getDimension(R.dimen.message_padding_bottom));
        style.outcomingTextColor = typedArray.getColor(R.styleable.MessagesList_outcomingTextColor,
                style.getColor(R.color.white));
        style.outcomingTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTextSize,
                style.getDimension(R.dimen.message_text_size));
        style.outcomingTextStyle = typedArray.getInt(R.styleable.MessagesList_outcomingTextStyle, Typeface.NORMAL);

        style.outcomingTimeTextColor = typedArray.getColor(R.styleable.MessagesList_outcomingTimeTextColor,
                style.getColor(R.color.white60));
        style.outcomingTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTimeTextSize,
                style.getDimension(R.dimen.message_time_text_size));
        style.outcomingTimeTextStyle = typedArray.getInt(R.styleable.MessagesList_outcomingTimeTextStyle, Typeface.NORMAL);

        style.outcomingImageTimeTextColor = typedArray.getColor(R.styleable.MessagesList_outcomingImageTimeTextColor,
                style.getColor(R.color.warm_grey_four));
        style.outcomingImageTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingImageTimeTextSize,
                style.getDimension(R.dimen.message_time_text_size));
        style.outcomingImageTimeTextStyle = typedArray.getInt(R.styleable.MessagesList_outcomingImageTimeTextStyle, Typeface.NORMAL);

        style.dateHeaderPadding = typedArray.getDimensionPixelSize(R.styleable.MessagesList_dateHeaderPadding,
                style.getDimension(R.dimen.message_date_header_padding));
        style.dateHeaderFormat = typedArray.getString(R.styleable.MessagesList_dateHeaderFormat);
        style.dateHeaderTextColor = typedArray.getColor(R.styleable.MessagesList_dateHeaderTextColor,
                style.getColor(R.color.warm_grey_two));
        style.dateHeaderTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_dateHeaderTextSize,
                style.getDimension(R.dimen.message_date_header_text_size));
        style.dateHeaderTextStyle = typedArray.getInt(R.styleable.MessagesList_dateHeaderTextStyle, Typeface.NORMAL);

        typedArray.recycle();

        return style;
    }

    public MessagesListStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Drawable getMessageSelector(@ColorInt int normalColor, @ColorInt int selectedColor,
                                            @ColorInt int pressedColor, @DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_selected},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
                        },
                        new int[]{selectedColor, pressedColor, normalColor}
                ));
        return drawable;
    }

    public int getTextAutoLinkMask() {
        return textAutoLinkMask;
    }

    public int getIncomingTextLinkColor() {
        return incomingTextLinkColor;
    }

    public int getOutcomingTextLinkColor() {
        return outcomingTextLinkColor;
    }

    public int getIncomingAvatarWidth() {
        return incomingAvatarWidth;
    }

    public int getIncomingAvatarHeight() {
        return incomingAvatarHeight;
    }

    public int getIncomingDefaultBubblePaddingLeft() {
        return incomingDefaultBubblePaddingLeft;
    }

    public int getIncomingDefaultBubblePaddingRight() {
        return incomingDefaultBubblePaddingRight;
    }

    public int getIncomingDefaultBubblePaddingTop() {
        return incomingDefaultBubblePaddingTop;
    }

    public int getIncomingDefaultBubblePaddingBottom() {
        return incomingDefaultBubblePaddingBottom;
    }

    public int getIncomingTextColor() {
        return incomingTextColor;
    }

    public int getIncomingTextSize() {
        return incomingTextSize;
    }

    public int getIncomingTextStyle() {
        return incomingTextStyle;
    }

    public Drawable getOutcomingBubbleDrawable() {
        if (outcomingBubbleDrawable == -1) {
            return getMessageSelector(outcomingDefaultBubbleColor, outcomingDefaultBubbleSelectedColor,
                    outcomingDefaultBubblePressedColor, R.drawable.shape_outcoming_message);
        } else {
            return getDrawable(outcomingBubbleDrawable);
        }
    }

    public Drawable getOutcomingImageOverlayDrawable() {
        if (outcomingImageOverlayDrawable == -1) {
            return getMessageSelector(Color.TRANSPARENT, outcomingDefaultImageOverlaySelectedColor,
                    outcomingDefaultImageOverlayPressedColor, R.drawable.shape_outcoming_message);
        } else {
            return getDrawable(outcomingImageOverlayDrawable);
        }
    }

    public int getOutcomingDefaultBubblePaddingLeft() {
        return outcomingDefaultBubblePaddingLeft;
    }

    public int getOutcomingDefaultBubblePaddingRight() {
        return outcomingDefaultBubblePaddingRight;
    }

    public int getOutcomingDefaultBubblePaddingTop() {
        return outcomingDefaultBubblePaddingTop;
    }

    public int getOutcomingDefaultBubblePaddingBottom() {
        return outcomingDefaultBubblePaddingBottom;
    }

    public int getOutcomingTextColor() {
        return outcomingTextColor;
    }

    public int getOutcomingTextSize() {
        return outcomingTextSize;
    }

    public int getOutcomingTextStyle() {
        return outcomingTextStyle;
    }

    public int getOutcomingTimeTextColor() {
        return outcomingTimeTextColor;
    }

    public int getOutcomingTimeTextSize() {
        return outcomingTimeTextSize;
    }

    public int getOutcomingTimeTextStyle() {
        return outcomingTimeTextStyle;
    }

    public int getOutcomingImageTimeTextColor() {
        return outcomingImageTimeTextColor;
    }

    public int getOutcomingImageTimeTextSize() {
        return outcomingImageTimeTextSize;
    }

    public int getOutcomingImageTimeTextStyle() {
        return outcomingImageTimeTextStyle;
    }

    public int getDateHeaderTextColor() {
        return dateHeaderTextColor;
    }

    public int getDateHeaderTextSize() {
        return dateHeaderTextSize;
    }

    public int getDateHeaderTextStyle() {
        return dateHeaderTextStyle;
    }

    public int getDateHeaderPadding() {
        return dateHeaderPadding;
    }

    public String getDateHeaderFormat() {
        return dateHeaderFormat;
    }

    public int getIncomingTimeTextSize() {
        return incomingTimeTextSize;
    }

    public int getIncomingTimeTextStyle() {
        return incomingTimeTextStyle;
    }

    public int getIncomingTimeTextColor() {
        return incomingTimeTextColor;
    }

    public int getIncomingImageTimeTextColor() {
        return incomingImageTimeTextColor;
    }

    public int getIncomingImageTimeTextSize() {
        return incomingImageTimeTextSize;
    }

    public int getIncomingImageTimeTextStyle() {
        return incomingImageTimeTextStyle;
    }

    public Drawable getIncomingBubbleDrawable() {
        if (incomingBubbleDrawable == -1) {
            return getMessageSelector(incomingDefaultBubbleColor, incomingDefaultBubbleSelectedColor,
                    incomingDefaultBubblePressedColor, R.drawable.shape_incoming_message);
        } else {
            return getDrawable(incomingBubbleDrawable);
        }
    }

    public Drawable getIncomingImageOverlayDrawable() {
        if (incomingImageOverlayDrawable == -1) {
            return getMessageSelector(Color.TRANSPARENT, incomingDefaultImageOverlaySelectedColor,
                    incomingDefaultImageOverlayPressedColor, R.drawable.shape_incoming_message);
        } else {
            return getDrawable(incomingImageOverlayDrawable);
        }
    }
}
