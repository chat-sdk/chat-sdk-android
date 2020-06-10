package sdk.chat.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import sdk.chat.core.dao.User;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.module.UIModule;
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
        return BitmapFactory.decodeResource(ChatSDK.ctx().getResources(), UIModule.config().defaultProfilePlaceholder);
    }

}
