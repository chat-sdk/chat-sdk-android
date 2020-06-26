package sdk.chat.ui.icons;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.ui.R;

public class Icons {

    public static final Icons instance = new Icons();

    public static Icons shared() {
        return instance;
    }

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
        add = context.getResources().getDrawable(R.drawable.icn_18_plus);
        microphone = new IconicsDrawable(context, FontAwesome.Icon.faw_microphone);
        cancel = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_cancel);
        play = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_play_arrow);
        pause = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_pause);
        send = context.getResources().getDrawable(R.drawable.ic_send);
        options = new IconicsDrawable(context, FontAwesome.Icon.faw_ellipsis_h);

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
    public Drawable send;

    public static Icons choose() {
        return shared();
    }

    public static Drawable get(IconicsDrawable icon, @ColorRes int colorRes) {
        return get(icon, colorRes, 0, 0);
    }

    public static Drawable get(Context context, IconicsDrawable icon, @ColorRes int colorRes) {
        return get(context, icon, colorRes, 0, 0);
    }

    public static Drawable get(Drawable icon, @ColorRes int colorRes) {
        return get(context(), icon, colorRes);
    }

    public static Drawable get(Context context, Drawable icon, @ColorRes int colorRes) {
        icon.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.MULTIPLY);
        return icon;
    }

    public static Drawable get(IconicsDrawable icon, int colorRes, int width, int height) {
        return get(context(), icon, colorRes, width, height);
    }

    public static Drawable getLarge(IconicsDrawable icon, int colorRes) {
        return get(context(), icon, colorRes, Dimen.from(R.dimen.large_icon_width), Dimen.from(R.dimen.large_icon_height));
    }

    public static Drawable get(Context context, IconicsDrawable drawable, int colorRes, int width, int height) {
        int color = ContextCompat.getColor(context, colorRes);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_OVER));

        if (width > 0) {
            drawable.setSizeXPx(width);
        }
        if (height > 0) {
            drawable.setSizeYPx(height);
        }

        return new BitmapDrawable(context.getResources(), drawable.toBitmap());
    }

    public static Context context() {
        return ChatSDK.ctx();
    }

}
