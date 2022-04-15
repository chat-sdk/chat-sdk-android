package sdk.chat.message.sticker;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sdk.chat.message.sticker.view.StickerListItem;

/**
 * Created by ben on 10/11/17.
 */

public class StickerPack implements StickerListItem {

    public interface StickerOnClickListener {
        void onClick (Sticker sticker);
    }

    public interface StickerPackOnClickListener {
        void onClick (StickerPack pack);
    }

    public String imageURL;

    protected List<Sticker> stickers = new ArrayList<>();

    public StickerOnClickListener stickerOnClickListener;
    public StickerPackOnClickListener onClickListener;

    public StickerPack(String imageURL) {
        this.imageURL = imageURL;
    }

    public StickerPack(String imageURL, List<Sticker> stickers) {
        this.imageURL = imageURL;
        this.stickers.addAll(stickers);
    }

    public boolean isValid () {
        return imageURL != null && stickers.size() > 0;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public void addSticker(String imageURL, String name) {
        addSticker(imageURL, name, null);
    }

    public void addSticker(String imageURL, String name, String sound) {
        Sticker sticker = new Sticker();

        sticker.imageURL = imageURL;
        sticker.imageName = name;
        sticker.sound = sound;
        sticker.pack = new WeakReference<>(this);

        stickers.add(sticker);
    }

    @Override
    public void click() {
        if(onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override
    public void load(ImageView view) {
        if (imageURL == null) {
            Glide.with(view).asGif().load(imageURL).into(view);
        } else {
            Glide.with(view).load(imageURL).into(view);
        }
    }

    public List<StickerListItem> getStickerItems() {
        List<StickerListItem> items = new ArrayList<>();
        for (Sticker s: stickers) {
            items.add(s);
        }
        return items;
    }

    public boolean isAnimated() {
        return false;
    }

}
