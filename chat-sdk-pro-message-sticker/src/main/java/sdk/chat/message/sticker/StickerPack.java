package sdk.chat.message.sticker;

import androidx.annotation.DrawableRes;

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

    @DrawableRes public int icon;
    private List<Sticker> stickers = new ArrayList<>();

    public StickerOnClickListener stickerOnClickListener;
    public StickerPackOnClickListener onClickListener;

    public StickerPack(@DrawableRes int icon) {
        this.icon = icon;
    }

    public StickerPack(@DrawableRes int icon, List<Sticker> stickers) {
        this.icon = icon;
        this.stickers.addAll(stickers);
    }

    public boolean isValid () {
        return icon != 0 && stickers.size() > 0;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public void addSticker(@DrawableRes int image, String name) {
        addSticker(image, name, null);
    }

    public void addSticker(@DrawableRes int image, String name, String sound) {
        Sticker sticker = new Sticker();

        sticker.imageName = name;
        sticker.image = image;
        sticker.sound = sound;
        sticker.pack = new WeakReference<>(this);

        stickers.add(sticker);
    }

    @Override
    public @DrawableRes int getIcon() {
        return icon;
    }

    @Override
    public void click() {
        if(onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    public List<StickerListItem> getStickerItems() {
        List<StickerListItem> items = new ArrayList<>();
        for (Sticker s: stickers) {
            items.add(s);
        }
        return items;
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

}
