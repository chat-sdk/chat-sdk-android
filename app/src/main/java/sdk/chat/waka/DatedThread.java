package sdk.chat.waka;

public class DatedThread {

    public DatedThread(String entityID, Long timestamp) {
        this.entityID = entityID;
        this.timestamp = timestamp;
    }

    public String entityID;
    public Long timestamp;

}
