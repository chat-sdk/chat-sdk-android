package co.chatsdk.android.app;

import co.chatsdk.core.session.ChatSDK;

public class GossipGirlUserHelper {

    // In firebase, only the stage name is stored. The stage name is presented as the city plus the username,
    // so it is shown as city-username. However, the stage name should be presented to the users as
    // username-city. This will be called the display stage name. This function creates the display stage name
    // from the stored stage name.
    public static String displayStageName () {
        String stageName = ChatSDK.currentUser().metaStringForKey(Keys.StageName);
        String [] split = stageName.split("-");
        return split[1] + "-" + split[0];
    }

    public static String displayStageName (String stageName) {
        String [] split = stageName.split("-");
        return split[1] + "-" + split[0];
    }

}
