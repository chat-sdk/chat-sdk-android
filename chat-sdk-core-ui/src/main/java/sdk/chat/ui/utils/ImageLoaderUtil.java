package sdk.chat.ui.utils;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;

import org.pmw.tinylog.Logger;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.Size;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;


public class ImageLoaderUtil {

    public void load(ImageView imageView, String url, Drawable placeholder, Size size) {
        load(imageView, url, placeholder, size, false);
    }

    public void load(ImageView imageView, String url, @DrawableRes int placeholder, Size size) {
        load(imageView, url, placeholder, size, false);
    }

    public void loadAvatar(ImageView imageView, String url) {
        int size = Dimen.from(imageView.getContext(), R.dimen.large_avatar_height);
        load(imageView, url, UIModule.config().defaultProfilePlaceholder, new Size(size));
    }

    public void loadSmallAvatar(ImageView imageView, String url) {
        int size = Dimen.from(imageView.getContext(), R.dimen.small_avatar_height);
        load(imageView, url, UIModule.config().defaultProfilePlaceholder, new Size(size));
    }

    public void loadThread(ImageView imageView, String url, boolean isGroup, @DimenRes int sizeRes) {
        int size = Dimen.from(imageView.getContext(), sizeRes);
        Drawable placeholder = ResourcesCompat.getDrawable(imageView.getResources(), UIModule.config().defaultProfilePlaceholder, null);
        if (isGroup) {
            placeholder = ChatSDKUI.icons().group_100;
        }
        load(imageView, url, placeholder, new Size(size));
    }

    public void loadThread(ImageView imageView, Thread thread, @DimenRes int sizeRes) {
        int size = Dimen.from(imageView.getContext(), sizeRes);
        String url = thread.getImageUrl();
        if (url == null && thread.typeIs(ThreadType.Private1to1)) {
            User user = thread.otherUser();
            if (user != null) {
                url = user.getAvatarURL();
            }
        }
        Drawable placeholder = ResourcesCompat.getDrawable(imageView.getResources(), UIModule.config().defaultProfilePlaceholder, null);
        if (thread.typeIs(ThreadType.Group)) {
            placeholder = ChatSDKUI.icons().group_100;
        }
        load(imageView, url, placeholder, new Size(size));
    }

    public void loadReply(ImageView imageView, String url, Drawable placeholder) {

        int maxWidth = Dimen.from(imageView.getContext(), R.dimen.reply_image_width);
        int maxHeight = Dimen.from(imageView.getContext(), R.dimen.reply_image_height);

        load(imageView, url, placeholder, new Size(maxWidth, maxHeight));
    }

    public void loadIcon(ImageView imageView, String url) {
        int size = Dimen.from(imageView.getContext(), R.dimen.large_icon_width);
        load(imageView, url, R.drawable.icn_200_image_message_placeholder, new Size(size));
    }

    public void load(ImageView imageView, String url, @DrawableRes int placeholder, Size size, boolean isAnimated) {
        load(imageView, url, ResourcesCompat.getDrawable(ChatSDK.ctx().getResources(), placeholder, null), size, isAnimated);
    }

    public void load(ImageView imageView, String url, Drawable placeholder, Size size, boolean isAnimated) {

        if (url == null) {
            Logger.debug("Stop here");
            imageView.setImageDrawable(placeholder);
            return;
        }

        if (imageView == null || imageView.getContext() == null) {
            return;
        }

        RequestManager request = Glide.with(imageView);
        RequestBuilder<?> builder;
        if (isAnimated) {
            builder = request.asGif();
        } else {
            builder = request.asDrawable().dontAnimate();
        }

        // If this is a local image
        Uri uri = Uri.parse(url);
        if (uri != null && uri.getScheme() != null && uri.getScheme().equals("android.resource")) {
            builder = builder.load(uri);
        } else {
            builder = builder.load(url);
        }

        if (placeholder != null) {
            builder = builder.placeholder(placeholder);
        }

        builder.override(size.widthInt(), size.heightInt())
                .centerCrop()
                .into(imageView);
    }

}
