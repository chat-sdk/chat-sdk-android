package sdk.chat.ui.icons;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;

public class Icons {

    public @ColorRes int actionBarIconColor = R.color.app_bar_icon_color;
    public @ColorRes int chatOptionIconColor = R.color.white;
    public @ColorRes int tabIconColor = R.color.tab_icon_color;

    public void initialize(Context context) {

        // First icon doesn't load for some reason 🤷
        dummy = new IconicsDrawable(context, FontAwesome.Icon.faw_dumbbell);

        user = new IconicsDrawable(context, FontAwesome.Icon.faw_user);
        location = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_my_location);
        phone = new IconicsDrawable(context, FontAwesome.Icon.faw_phone);
        email = new IconicsDrawable(context, FontAwesome.Icon.faw_envelope);
        chat = new IconicsDrawable(context, FontAwesome.Icon.faw_comment);
        check = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_check);
        save = new IconicsDrawable(context, FontAwesome.Icon.faw_download);
        block = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_block);
        publicChat = new IconicsDrawable(context, FontAwesome.Icon.faw_users);
        contact = new IconicsDrawable(context, FontAwesome.Icon.faw_address_book);
        edit = new IconicsDrawable(context, FontAwesome.Icon.faw_user_edit);
        logout = new IconicsDrawable(context, FontAwesome.Icon.faw_sign_out_alt);
        search = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_search);
        users = new IconicsDrawable(context, FontAwesome.Icon.faw_user_friends);
        copy = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_content_copy);
        delete = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_delete);
        forward = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_forward);
        reply = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_reply);
        add = ContextCompat.getDrawable(context, R.drawable.icn_18_plus);
        microphone = new IconicsDrawable(context, FontAwesome.Icon.faw_microphone);
        cancel = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_cancel);
        play = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_play_arrow);
        pause = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_pause);
        send = ContextCompat.getDrawable(context, R.drawable.ic_send);
        options = new IconicsDrawable(context, FontAwesome.Icon.faw_ellipsis_h);
        drawer = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_list);
        refresh = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_sync);
        arrowRight = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_keyboard_arrow_right);
        user_100 = ContextCompat.getDrawable(context, R.drawable.icn_100_user);
        group_100 = ContextCompat.getDrawable(context, R.drawable.icn_100_group);

        call = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_call);
        download_arrow = ContextCompat.getDrawable(context, R.drawable.icn_60_download);

    }

    public IconicsDrawable dummy;
    public IconicsDrawable location;
    public IconicsDrawable user;
    public IconicsDrawable phone;
    public IconicsDrawable email;
    public IconicsDrawable chat;
    public IconicsDrawable check;
    public IconicsDrawable save;
    public IconicsDrawable block;
    public IconicsDrawable publicChat;
    public IconicsDrawable contact;
    public IconicsDrawable edit;
    public IconicsDrawable logout;
    public IconicsDrawable search;
    public IconicsDrawable users;
    public IconicsDrawable copy;
    public IconicsDrawable delete;
    public IconicsDrawable forward;
    public IconicsDrawable reply;
    public Drawable add;
    public IconicsDrawable microphone;
    public IconicsDrawable cancel;
    public IconicsDrawable play;
    public IconicsDrawable pause;
    public IconicsDrawable options;
    public IconicsDrawable drawer;
    public Drawable send;
    public IconicsDrawable refresh;
    public IconicsDrawable arrowRight;
    public Drawable group_100;
    public Drawable user_100;
    public IconicsDrawable call;
    public Drawable download_arrow;

    public Drawable get(IconicsDrawable icon, @ColorRes int colorRes) {
        return get(icon, colorRes, 0, 0);
    }

//    public Drawable getWithColor(IconicsDrawable icon, @ColorInt int colorInt) {
//        return getWithColor(icon, colorInt);
//    }

//    public Drawable getWithColor(Drawable icon, @ColorInt int colorInt) {
//        return getWithColor(icon, colorInt);
//    }

    public Drawable get(Context context, IconicsDrawable icon, @ColorRes int colorRes) {
        return get(context, icon, colorRes, 0, 0);
    }

//    public Drawable getWithColor(Context context, IconicsDrawable icon, @ColorInt int colorInt) {
//        return getWithColor(context, icon, colorInt, 0, 0);
//    }

    public Drawable get(Drawable icon, @ColorRes int colorRes) {
        return get(context(), icon, colorRes);
    }

    public Drawable get(Context context, Drawable icon, @ColorRes int colorRes) {
        if (colorRes != 0) {
            icon.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.MULTIPLY);
        }
        return icon;
    }

    public Drawable get(IconicsDrawable icon, int colorRes, int width, int height) {
        return get(context(), icon, colorRes, width, height);
    }

    public Drawable getWithColor(IconicsDrawable icon, @ColorInt int colorInt, int width, int height) {
        return getWithColor(context(), icon, colorInt, width, height);
    }

    public Drawable getLarge(IconicsDrawable icon, int colorRes) {
        return get(context(), icon, colorRes, Dimen.from(R.dimen.large_icon_width), Dimen.from(R.dimen.large_icon_height));
    }

    public Drawable get(Context context, IconicsDrawable drawable, @ColorRes int colorRes, int width, int height) {
        @ColorInt int color = 0;
        if (colorRes != 0) {
            color = ContextCompat.getColor(context, colorRes);
        }
        return getWithColor(context, drawable, color, width, height);
    }

    public Drawable getWithColor(Context context, IconicsDrawable drawable, @ColorInt int colorInt, int width, int height) {

        if (width > 0) {
            drawable.setSizeXPx(width);
        }
        if (height > 0) {
            drawable.setSizeYPx(height);
        }

        return getWithColor(context, drawable, colorInt);
    }

    public Drawable getWithColor(Context context, Drawable drawable, @ColorInt int colorInt) {

        if (colorInt != 0) {
            if (drawable instanceof IconicsDrawable) {
                drawable.setColorFilter(new PorterDuffColorFilter(colorInt, PorterDuff.Mode.SRC_OVER));
            } else {
                drawable.setColorFilter(colorInt, PorterDuff.Mode.MULTIPLY);
            }
        }

        if (drawable instanceof IconicsDrawable) {
            return new BitmapDrawable(context.getResources(), ((IconicsDrawable) drawable).toBitmap());
        } else if (drawable instanceof BitmapDrawable) {
            return new BitmapDrawable(context.getResources(), ((BitmapDrawable) drawable).getBitmap());
        } else {
            return drawable;
        }
    }

    public static Context context() {
        return ChatSDK.ctx();
    }

    /**
     * Use ChatSDKUI.icons()
     */
    @Deprecated
    public static Icons choose() {
        return ChatSDKUI.icons();
    }

    /**
     * Use ChatSDKUI.icons()
     */
    @Deprecated
    public static Icons shared() {
        return ChatSDKUI.icons();
    }

}
