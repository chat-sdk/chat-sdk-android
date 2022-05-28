package sdk.chat.custom;

import java.util.ArrayList;

import feather.Provides;
import feather.Replaces;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.sticker.provider.MemoryStickerPackProvider;
import sdk.chat.message.sticker.provider.StickerPackProvider;

public class CustomProvider extends StickerMessageModule {

    @Replaces
    @Provides
    public StickerPackProvider getStickerPack() {
        return new MemoryStickerPackProvider(new ArrayList<>());
    }

}
