package sdk.chat.message.sticker.provider;

import java.util.List;

import io.reactivex.Single;
import sdk.chat.message.sticker.StickerPack;

public class MemoryStickerPackProvider implements StickerPackProvider {

    protected List<StickerPack> packs;

    public MemoryStickerPackProvider(List<StickerPack> stickerPacks) {
        this.packs = stickerPacks;
    }

    @Override
    public void preload() {

    }

    @Override
    public Single<List<StickerPack>> getPacks() {
        return Single.just(packs);
    }

    @Override
    public String imageURL(String name) {
        return null;
    }
}
