package sdk.chat.ui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.ui.activities.thread.details.ThreadUser;

public class UserSorter {


    /**
     * Sorting a given list using the internal comparator.
     * <p>
     * This will be used each time after setting the user item
     * *
     */
    public static void sortUsers(List<User> list) {
        Comparator<UserListItem> comparator = (u1, u2) -> {
            boolean u1online = u1.getIsOnline();
            boolean u2online = u2.getIsOnline();
            if (u1online != u2online) {
                if (u1online) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                String s1 = u1.getName() != null ? u1.getName() : "";
                String s2 = u2.getName() != null ? u2.getName() : "";

                return s1.compareToIgnoreCase(s2);
            }
        };
        Collections.sort(list, comparator);
    }

    public static void sortThreadUsers(List<ThreadUser> list) {
        Comparator<ThreadUser> comparator = (u1, u2) -> {
            boolean u1online = u1.isActive();
            boolean u2online = u2.isActive();
            if (u1online != u2online) {
                if (u1online) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                String s1 = u1.getUser().getName() != null ? u1.getUser().getName() : "";
                String s2 = u2.getUser().getName() != null ? u2.getUser().getName() : "";

                return s1.compareToIgnoreCase(s2);
            }
        };
        Collections.sort(list, comparator);
    }
}
