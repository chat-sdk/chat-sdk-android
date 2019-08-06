package co.chatsdk.core.session;

public class MethodArgument {

    public Class<?> type;
    public Object value;

    public MethodArgument(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }


}
