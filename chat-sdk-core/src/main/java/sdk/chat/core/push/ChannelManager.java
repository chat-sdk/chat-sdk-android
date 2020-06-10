package sdk.chat.core.push;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sdk.chat.core.session.ChatSDK;

public class ChannelManager {

    public static String PushChannelsKey = "PushChannelsKey";

    public interface Executor {
        void run(String channel);
    }

    public List<String> getChannelsForUser(String userEntityID) {
        SharedPreferences preferences = ChatSDK.shared().getPreferences();
        Set<String> channelSet = preferences.getStringSet(userKey(userEntityID), null);
        List<String> channels = new ArrayList<>();
        if (channelSet != null) {
            channels.addAll(channelSet);
        }
        return channels;
    }

    public void setChannelsForUser(String userEntityID, List<String> channels) {
        SharedPreferences.Editor editor = ChatSDK.shared().getPreferences().edit();
        editor.putStringSet(userKey(userEntityID), new HashSet<>(channels)).apply();
    }

    public String userKey(String userEntityID) {
        return PushChannelsKey + userEntityID;
    }

    public void addChannel(String channel) {
        addChannel(ChatSDK.currentUserID(), channel);
    }

    public void removeChannel(String channel) {
        removeChannel(ChatSDK.currentUserID(), channel);
    }

    public void addChannel(String userEntityID, String channel) {
        List<String> channels = getChannelsForUser(userEntityID);
        channels.add(channel);
        setChannelsForUser(userEntityID, channels);
    }

    public void removeChannel(String userEntityID, String channel) {
        List<String> channels = getChannelsForUser(userEntityID);
        channels.remove(channel);
        setChannelsForUser(userEntityID, channels);
    }

    public List<String> getUserEntityIDs() {
        SharedPreferences preferences = ChatSDK.shared().getPreferences();
        Map<String, ?> all = preferences.getAll();
        List<String> userIDs = new ArrayList<>();
        for (String key: all.keySet()) {
            if (key.contains(PushChannelsKey)) {
                userIDs.add(key.replace(PushChannelsKey, ""));
            }
        }
        return userIDs;
    }

    public void channelsForUsersExcludingCurrent(Executor executor) {
        for (String entityID: getUserEntityIDs()) {
            if (!entityID.equals(ChatSDK.currentUserID())) {
                for (String channel: getChannelsForUser(entityID)) {
                    executor.run(channel);
                }
            }
        }
    }

    public boolean isSubscribed(String userEntityID, String channel) {
        return getChannelsForUser(userEntityID).contains(channel);
    }

    public boolean isSubscribed(String channel) {
        return isSubscribed(ChatSDK.currentUserID(), channel);
    }

}
