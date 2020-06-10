package sdk.chat.firebase.adapter.moderation;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.R;

public class Permission {

    public static final String Owner = "owner";
    public static final String Admin = "admin";
    public static final String Member = "member";
    public static final String Watcher = "watcher";
    public static final String Banned = "banned";
    public static final String None = "none";

    public static boolean isOr(String permission, String... permissions) {
        if (permission == null) {
            permission = Permission.Member;
        }
        for (String p: permissions) {
            if (permission.equals(p)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAnd(String permission, String... permissions) {
        if (permission == null) {
            permission = Permission.Member;
        }
        for (String p: permissions) {
            if (!permission.equals(p)) {
                return false;
            }
        }
        return true;
    }

    public static boolean valid(String permission) {
        return isOr(Owner, Admin, Member, Watcher, Banned);
    }

    public static List<String> all() {
        return new ArrayList<String>() {{
            add(Owner);
            add(Admin);
            add(Member);
            add(Watcher);
            add(Banned);
        }};
    }

    public static List<String> allLocalized() {
        List<String> all = new ArrayList<>();
        for (String s: all()) {
            all.add(toLocalized(s));
        }
        return all;
    }

    public static int level(String permission) {
        int i = all().size();
        for (String p: all()) {
            // If no permission is specified we assume they are a member
            // Do this for backwards compatibility
            if ((permission == null && p.equals(Permission.Member)) || (permission != null && permission.equals(p))) {
                return i;
            }
            i--;
        }
        return 0;
    }

    public static String toLocalized(String permission) {
        int resId = -1;

        if (permission.equals(Owner)) {
            resId = R.string.owner;
        }
        else if (permission.equals(Admin)) {
            resId = R.string.admin;
        }
        else if (permission.equals(Member)) {
            resId = R.string.member;
        }
        else if (permission.equals(Watcher)) {
            resId = R.string.watcher;
        }
        else if (permission.equals(Banned)) {
            resId = R.string.banned;
        }

        if (resId != -1) {
            return ChatSDK.getString(resId);
        }

        return null;
    }

//    public static String fromLocalized(String localized) {
//        if (localized.equals(ChatSDK.getString(R.string.owner))) {
//            return Owner;
//        }
//        if (localized.equals(ChatSDK.getString(R.string.admin))) {
//            return Admin;
//        }
//        if (localized.equals(ChatSDK.getString(R.string.member))) {
//            return Member;
//        }
//        if (localized.equals(ChatSDK.getString(R.string.watcher))) {
//            return Watcher;
//        }
//        if (localized.equals(ChatSDK.getString(R.string.banned))) {
//            return Banned;
//        }
//        return null;
//    }
}
