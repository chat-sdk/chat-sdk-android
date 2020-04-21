package sdk.chat.ui.extras;

import com.mikepenz.materialdrawer.holder.StringHolder;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import sdk.chat.core.Tab;
import sdk.chat.core.session.ChatSDK;

public class KotlinHelper {

    public static Single<StringHolder> privateTabName() {
        return ChatSDK.thread().getUnreadMessagesAmount(false).map(integer -> {
            Tab tab = ChatSDK.ui().privateThreadsTab();
            if (integer == 0) {
                return new StringHolder(tab.title);
            } else {
                return new StringHolder(String.format(tab.title, " (" + integer + ")"));
            }
        });
    }

}
