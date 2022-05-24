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

package com.stfalcon.chatkit.commons;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.stfalcon.chatkit.R;

/**
 * Base class for chat component styles
 */
public abstract class Style {

    public Context context;
    public Resources resources;
    public AttributeSet attrs;

    public Style(Context context, AttributeSet attrs) {
        this.context = context;
        this.resources = context.getResources();
        this.attrs = attrs;
    }

    public final int getSystemAccentColor() {
        return getSystemColor(R.attr.colorAccent);
    }

    public final int getSystemPrimaryColor() {
        return getSystemColor(R.attr.colorPrimary);
    }

    public final int getSystemPrimaryDarkColor() {
        return getSystemColor(R.attr.colorPrimaryDark);
    }

    public final int getSystemPrimaryTextColor() {
        return getSystemColor(android.R.attr.textColorPrimary);
    }

    public final int getSystemHintColor() {
        return getSystemColor(android.R.attr.textColorHint);
    }

    public final int getSystemColor(@AttrRes int attr) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();

        return color;
    }

    public final int getDimension(@DimenRes int dimen) {
        return resources.getDimensionPixelSize(dimen);
    }

    public final int getColor(@ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    public final Drawable getDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public final Drawable getVectorDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(context, drawable);
    }

}
