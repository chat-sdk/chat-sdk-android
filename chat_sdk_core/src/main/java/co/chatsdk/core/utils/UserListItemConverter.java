package co.chatsdk.core.utils;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;

/**
 * Created by ben on 10/9/17.
 */

public class UserListItemConverter {
    public static List<UserListItem> toUserItemList (List<User> users) {
        ArrayList<UserListItem> userItemList = new ArrayList<>();
        for(User u : users) {
            userItemList.add(u);
        }
        return userItemList;
    }

    public static List<User> toUserList (List<UserListItem> items) {
        ArrayList<User> users = new ArrayList<>();
        for(UserListItem u : items) {
            if(u instanceof User) {
                users.add((User) u);
            }
        }
        return users;
    }

    public static User toUser (UserListItem item) {
        if(item instanceof User) {
            return (User) item;
        }
        return null;
    }

}
