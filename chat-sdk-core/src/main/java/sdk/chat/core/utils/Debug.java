package sdk.chat.core.utils;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;

public class Debug {

    public static boolean enabled = false;

    public static List<String> messageList(List<Message> list) {
        if (enabled) {
            List<String> text = new ArrayList<>();
            for(Message m: list) {
                String t = m.getDate() + " : " + m.getEntityID() + " - " + m.getText();
                text.add(t);
                Logger.debug(t);
            }
            return text;
        }
        return new ArrayList<>();
    }

    public static List<String> messageText(List<Message> list) {
        if (enabled) {
            List<String> text = new ArrayList<>();
            for (Message m: list) {
                text.add(m.getText());
            }
            return text;
        }
        return new ArrayList<>();
    }


}
