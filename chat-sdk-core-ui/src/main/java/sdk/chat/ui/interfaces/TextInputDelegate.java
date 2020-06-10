package sdk.chat.ui.interfaces;

import java.io.File;

/**
 * Created by ben on 10/11/17.
 */

public interface TextInputDelegate {

//    void showOptions();
//    void hideOptions();
//    void startTyping();
    void sendAudio (final File file, String mimeType, long duration);
//    void stopTyping();
//    void onKeyboardShow();
//    void onKeyboardHide();
    void sendMessage(String text);

}
