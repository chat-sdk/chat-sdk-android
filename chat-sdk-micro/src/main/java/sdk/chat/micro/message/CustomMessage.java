package sdk.chat.micro.message;

import java.util.HashMap;

public class CustomMessage extends Message {

    public CustomMessage (HashMap<String, Object> body) {
        super();
        this.body = body;
    }

}
