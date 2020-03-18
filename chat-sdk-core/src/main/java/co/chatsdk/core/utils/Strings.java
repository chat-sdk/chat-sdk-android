package co.chatsdk.core.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.chatsdk.core.R;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 07/06/2017.
 */

public class Strings {

    public static String t (Context context, int resId) {
        return context.getString(resId);
    }

    public static String t (int resId) {
        return t(ChatSDK.shared().context(), resId);
    }

    public static String dateTime (Date date) {
        return new SimpleDateFormat("HH:mm dd/MM/yy", CurrentLocale.get()).format(date);
    }

    public static String date (Date date) {
        return new SimpleDateFormat("dd/MM/yy", CurrentLocale.get()).format(date);
    }

    public static String nameForThread (Thread thread) {
        if (thread == null) return null;

        String displayName = thread.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }

        // Due to the bundle printing when the app execute on debug this sometime is null.
        User currentUser = ChatSDK.currentUser();
        String name = "";

        if (thread.typeIs(ThreadType.Private)) {

            for (User user : thread.getUsers()){
                if (!user.getId().equals(currentUser.getId())) {
                    String n = user.getName();

                    if (n != null && !n.isEmpty()) {
                        name += (!name.equals("") ? ", " : "") + n;
                    }
                }
            }
        }

        if(name.length() == 0) {
            name = Strings.t(R.string.no_name);
        }
        return name;
    }

}
