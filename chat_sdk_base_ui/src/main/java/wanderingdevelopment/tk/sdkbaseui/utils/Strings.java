package wanderingdevelopment.tk.sdkbaseui.utils;

import android.support.annotation.StringRes;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.AppContext;
import wanderingdevelopment.tk.sdkbaseui.R;

/**
 * Created by benjaminsmiley-andrews on 07/06/2017.
 */

public class Strings {

    public static String t (@StringRes int resId) {
        return AppContext.context.getString(resId);
    }

    public static String payloadAsString (BMessage message) {
            if (message.getType() != null) {
                switch (message.getType()) {
                    case BMessage.Type.TEXT:
                        return message.getTextString();
                    case BMessage.Type.IMAGE:
                        return t(R.string.not_image_message);
                    case BMessage.Type.LOCATION:
                        return t(R.string.not_location_message);
                }
            }
            return t(R.string.not_unknown_message);
    }

    public static String dateTime (Date date) {
        return new SimpleDateFormat("HH:mm dd/MM/yy").format(date);
    }

    public static String date (Date date) {
        return new SimpleDateFormat("dd/MM/yy").format(date);
    }

    public static String nameForThread (BThread thread) {

        if (StringUtils.isNotEmpty(thread.getName()))
            return thread.getName();

        // Due to the bundle printing when the app run on debug this sometime is null.
        BUser currentUser = NM.currentUser();
        String name = "";

        if (thread.typeIs(ThreadType.Private)) {

            for (BUser user : thread.getUsers()){
                if (!user.getId().equals(currentUser.getId())) {
                    String n = user.getMetaName();

                    if (StringUtils.isNotEmpty(n)) {
                        name += (!name.equals("") ? ", " : "") + n;
                    }
                }
            }
        }

        if(name.length() == 0) {
            name = Strings.t(R.string.not_thread);
        }
        return name;
    }

}
