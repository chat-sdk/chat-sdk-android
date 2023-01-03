package sdk.chat.sinch.calling;

import java.util.Locale;

public class CarCallListItem {

    private String name;
    private String email;
    private String uid;
    private String avatarURL;
    private long duration;
    private boolean initiator;

    public CarCallListItem(String name, String email, String uid, String avatarURL, long duration, boolean initiator) {
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.avatarURL = avatarURL;
        this.duration = duration;
        this.initiator = initiator;
    }

    public CarCallListItem(String name, String email, String uid, String avatarURL, long duration) {
        this(name, email, uid, avatarURL, duration, false);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public long getDuration() {
        return duration;
    }

    public String getDurationFormatted() {
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public boolean isInitiator() {
        return initiator;
    }

}
