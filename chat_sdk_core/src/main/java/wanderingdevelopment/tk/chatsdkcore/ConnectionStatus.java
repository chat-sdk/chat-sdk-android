package wanderingdevelopment.tk.chatsdkcore;

/**
 * Created by kykrueger on 2017-02-20.
 */

public enum ConnectionStatus {
    NONE(0),
    CONNECTED(2),
    AUTHENTICATED(4),
    DISCONNECTED(5),
    RECONNECTING(6),
    ERROR(7);

    private int value;
    ConnectionStatus(int value){
        this.value = value;
    }
}
