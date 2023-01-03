package sdk.chat.core.hook;

import java.util.Map;

public interface Executor {
    void execute(Map<String, Object> data);
}
