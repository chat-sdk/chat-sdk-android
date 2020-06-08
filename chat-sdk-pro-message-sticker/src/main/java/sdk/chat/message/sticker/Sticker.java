package co.chatsdk.message.sticker;

import java.lang.ref.WeakReference;

import co.chatsdk.message.sticker.view.StickerListItem;

/**
 * Created by ben on 10/11/17.
 */

public class Sticker implements StickerListItem {

    public int imageResourceId;
    public String imageName;
    public WeakReference<StickerPack> pack;

    public void setImageName (String imageName) {
        this.imageName = imageName;
    }

    @Override
    public int getImageResourceId() {
        return imageResourceId;
    }

    @Override
    public void click () {
        if(pack.get().stickerOnClickListener != null) {
            pack.get().stickerOnClickListener.onClick(this);
        }
    }

}
