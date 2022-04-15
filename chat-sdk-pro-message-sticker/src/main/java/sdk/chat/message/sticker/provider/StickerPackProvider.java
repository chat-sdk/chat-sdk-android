package sdk.chat.message.sticker.provider;

import java.util.List;

import io.reactivex.Single;
import sdk.chat.message.sticker.StickerPack;

public interface StickerPackProvider {
    void preload();
    Single<List<StickerPack>> getPacks();
}
