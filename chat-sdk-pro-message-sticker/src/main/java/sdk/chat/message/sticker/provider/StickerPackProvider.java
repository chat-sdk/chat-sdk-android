package sdk.chat.message.sticker.provider;

import java.util.List;

import javax.inject.Singleton;

import io.reactivex.Single;
import sdk.chat.message.sticker.StickerPack;

@Singleton
public interface StickerPackProvider {
    void preload();
    Single<List<StickerPack>> getPacks();
    String imageURL(String name);
}
