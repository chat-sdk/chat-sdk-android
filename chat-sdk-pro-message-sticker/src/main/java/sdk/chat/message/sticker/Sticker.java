package sdk.chat.message.sticker;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

import sdk.chat.message.sticker.view.StickerListItem;

/**
 * Created by ben on 10/11/17.
 */

public class Sticker implements StickerListItem {

    public String imageURL;

    public String imageName;
    public String sound;
    public WeakReference<StickerPack> pack;

    public void setName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public void click () {
        if(pack.get().stickerOnClickListener != null) {
            pack.get().stickerOnClickListener.onClick(this);
        }
    }

    public boolean isAnimated() {
        return imageName.contains(".gif");
    }

    public void load(ImageView view) {
        if (imageURL == null) {
            Glide.with(view).asGif().load(imageURL).into(view);
        } else {
            Glide.with(view).load(imageURL).into(view);
        }
    }
}
