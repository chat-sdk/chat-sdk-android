package sdk.chat.message.sticker.view;

import androidx.annotation.DrawableRes;

/**
 * Created by ben on 10/11/17.
 */

public interface StickerListItem {

    @DrawableRes int getIcon();
    void click();
    boolean isAnimated();

}
