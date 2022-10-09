package sdk.chat.message.sticker.keyboard

import sdk.chat.message.sticker.StickerPack
import sdk.chat.ui.recycler.SmartViewModel

open class StickerPackModel(val pack: StickerPack, val size: Int): SmartViewModel() {

    public var selected: Boolean = false

}