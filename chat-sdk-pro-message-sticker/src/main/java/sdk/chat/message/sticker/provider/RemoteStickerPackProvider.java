package sdk.chat.message.sticker.provider;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import sdk.chat.message.sticker.StickerPack;

public class RemoteStickerPackProvider implements StickerPackProvider {

    protected List<StickerPack> stickerPacks;

    @Override
    public void preload() {
        getPacks().subscribe();
    }

    @Override
    public Single<List<StickerPack>> getPacks() {
        return Single.create(emitter -> {
            stickerPacks = new ArrayList<>();

            emitter.onSuccess(stickerPacks);
        });
    }}
