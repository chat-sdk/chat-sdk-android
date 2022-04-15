package sdk.chat.ui.provider;

import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.options.MediaChatOption;
import sdk.chat.ui.chat.options.MediaType;
import sdk.chat.ui.module.UIModule;

public class ChatOptionProvider {

    public ChatOption image() {
        return new MediaChatOption(R.string.image_or_photo,
                R.drawable.icn_100_gallery,
                MediaType.choosePhoto(),
                UIModule.config().cropType);
    }

}
