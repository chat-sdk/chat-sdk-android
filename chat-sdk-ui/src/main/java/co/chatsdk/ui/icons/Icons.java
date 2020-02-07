package co.chatsdk.ui.icons;

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

import java.lang.ref.WeakReference;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;

public class Icons {

    public static final Icons instance = new Icons();
    protected WeakReference<Context> context;

    public static Icons shared() {
        return instance;
    }

    public void setContext(Context context) {
        this.context = new WeakReference<>(context);
    }

    public GoogleMaterial.Icon location = GoogleMaterial.Icon.gmd_my_location;
    public FontAwesome.Icon user = FontAwesome.Icon.faw_user;
    public FontAwesome.Icon phone = FontAwesome.Icon.faw_phone;
    public FontAwesome.Icon email = FontAwesome.Icon.faw_envelope;
    public FontAwesome.Icon chat = FontAwesome.Icon.faw_comment;
    public GoogleMaterial.Icon check = GoogleMaterial.Icon.gmd_check;
    public FontAwesome.Icon save = FontAwesome.Icon.faw_download;

    public static Drawable get(FontAwesome.Icon icon, @ColorRes int colorRes) {
        Context context = context();
        int color = ContextCompat.getColor(context, colorRes);
        IconicsDrawable drawable = new IconicsDrawable(shared().context.get(), icon);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_OVER));
        return new BitmapDrawable(context.getResources(), drawable.toBitmap());
    }

    public static Drawable get(GoogleMaterial.Icon icon, @ColorRes int colorRes) {
        Context context = context();
        int color = ContextCompat.getColor(context, colorRes);
        IconicsDrawable drawable = new IconicsDrawable(shared().context.get(), icon);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_OVER));
        return new BitmapDrawable(context.getResources(), drawable.toBitmap());
    }

    public static Context context() {
        return shared().context.get();
    }

}
