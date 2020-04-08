package co.chatsdk.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.image.ImageUtils;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.module.DefaultUIModule;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public class UserImageBuilder {

    public static Disposable loadAvatar(final User user, final ImageView imageView, int width, int height) {
        return getAvatarBitmap(user, width, height).subscribe(imageView::setImageBitmap);
    }

    public static Single<Bitmap> getAvatarBitmap(final User user, int width, int height) {
        return ImageUtils.bitmapForURL(user.getAvatarURL(), width, height).onErrorReturnItem(defaultAvatar());
    }

    public static Bitmap defaultAvatar() {
        return BitmapFactory.decodeResource(ChatSDK.ctx().getResources(), DefaultUIModule.config().defaultProfileImage);
    }

}
