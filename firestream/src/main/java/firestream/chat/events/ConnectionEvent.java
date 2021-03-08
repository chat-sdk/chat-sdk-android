package firestream.chat.events;

public class ConnectionEvent {

    public enum Type {
        WillConnect,
        DidConnect,
        WillDisconnect,
        DidDisconnect
    }

    protected Type type;

    protected ConnectionEvent(Type type) {
        this.type = type;
    }

    public static ConnectionEvent willConnect() {
        return new ConnectionEvent(Type.WillConnect);
    }

    public static ConnectionEvent didConnect() {
        return new ConnectionEvent(Type.DidConnect);
    }

    public static ConnectionEvent willDisconnect() {
        return new ConnectionEvent(Type.WillDisconnect);
    }

    public static ConnectionEvent didDisconnect() {
        return new ConnectionEvent(Type.DidDisconnect);
    }

    public Type getType() {
        return type;
    }

}
