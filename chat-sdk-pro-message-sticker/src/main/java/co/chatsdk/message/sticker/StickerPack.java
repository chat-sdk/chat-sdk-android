package co.chatsdk.message.sticker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import co.chatsdk.message.sticker.view.StickerListItem;

/**
 * Created by ben on 10/11/17.
 */

public class StickerPack implements StickerListItem {

    public Integer imageResourceId;
    private ArrayList<Sticker> stickers = new ArrayList<>();

    public StickerOnClickListener stickerOnClickListener;
    public StickerPackOnClickListener onClickListener;

    public boolean isValid () {
        return imageResourceId != null && stickers.size() > 0;
    }

    public ArrayList<Sticker> getStickers() {
        return stickers;
    }

    public void addSticker(Sticker sticker) {
        sticker.pack = new WeakReference<>(this);
        stickers.add(sticker);
    }

    @Override
    public int getImageResourceId() {
        return imageResourceId;
    }

    @Override
    public void click() {
        if(onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    public interface StickerOnClickListener {
        void onClick (Sticker sticker);
    }

    public interface StickerPackOnClickListener {
        void onClick (StickerPack pack);
    }

    public ArrayList<StickerListItem> getStickerItems () {
        ArrayList<StickerListItem> items = new ArrayList<>();
        for(Sticker sticker : getStickers()) {
            items.add(sticker);
        }
        return items;
    }


}
