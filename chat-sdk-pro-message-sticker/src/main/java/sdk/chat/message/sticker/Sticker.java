package sdk.chat.message.sticker;

import androidx.annotation.DrawableRes;

import java.lang.ref.WeakReference;

import sdk.chat.message.sticker.view.StickerListItem;

/**
 * Created by ben on 10/11/17.
 */

public class Sticker implements StickerListItem {

    public @DrawableRes int image;
    public String imageName;
    public String sound;
    public WeakReference<StickerPack> pack;

    public void setName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public @DrawableRes int getIcon() {
        return image;
    }

    @Override
    public void click () {
        if(pack.get().stickerOnClickListener != null) {
            pack.get().stickerOnClickListener.onClick(this);
        }
    }

    @Override
    public boolean isAnimated() {
        return imageName.contains(".gif");
    }

}
