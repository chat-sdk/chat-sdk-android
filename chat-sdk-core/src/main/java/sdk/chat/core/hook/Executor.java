package sdk.chat.core.hook;

import java.util.HashMap;

public interface Executor {
    void execute(HashMap<String, Object> data);
}
