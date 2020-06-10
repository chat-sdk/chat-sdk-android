package sdk.chat.core.utils;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;

public class Debug {

    public static List<String> messageList(List<Message> list) {
        List<String> text = new ArrayList<>();
        for(Message m: list) {
            String t = m.getDate() + " : " + m.getEntityID() + " - " + m.getText();
            text.add(t);
            Logger.debug(t);
        }
        return text;
    }

    public static List<String> messageText(List<Message> list) {
        List<String> text = new ArrayList<>();
        for (Message m: list) {
            text.add(m.getText());
        }
        return text;
    }


}
