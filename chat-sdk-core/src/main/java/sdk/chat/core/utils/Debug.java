package sdk.chat.core.utils;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;

public class Debug {

    public static String messageList(List<Message> list) {
        StringBuilder output = new StringBuilder();
        output.append("\n ");
        for(Message m: list) {
            output.append(m.getDate()).append(" : ").append(m.getEntityID()).append(" - ").append(m.getText()).append(" \n");
        }
        Logger.debug(output.toString());
        return output.toString();
    }

    public static List<String> messageText(List<Message> list) {
        List<String> text = new ArrayList<>();
        for (Message m: list) {
            text.add(m.getText());
        }
        return text;
    }


}
