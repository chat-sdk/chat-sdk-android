package co.chatsdk.xmpp.utils;

import androidx.annotation.Nullable;

import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.xmpp.R;

public class Role {

    public static int None = 0x0;

    public static int Owner = 0x1;
    public static int Admin = 0x2;
    public static int Member = 0x4;
    public static int Outcast = 0x8;

    public static int Moderator = 0x10;
    public static int Participant = 0x20;
    public static int Visitor = 0x40;

    public static String toString(int roleInt) {
        MUCAffiliation affiliation = intToAffiliation(roleInt);
        MUCRole role = intToRole(roleInt);

        if (affiliation == MUCAffiliation.owner) {
            return ChatSDK.shared().getString(R.string.owner__);
        }
        if (affiliation == MUCAffiliation.admin) {
            return ChatSDK.shared().getString(R.string.admin);
        }
        if (role == MUCRole.moderator) {
            return ChatSDK.shared().getString(R.string.moderator);
        }
        if (affiliation == MUCAffiliation.member) {
            return ChatSDK.shared().getString(R.string.member);
        }
        if (role == MUCRole.participant) {
            return ChatSDK.shared().getString(R.string.participant);
        }
        if (role == MUCRole.visitor) {
            return ChatSDK.shared().getString(R.string.visitor);
        }
        if (affiliation == MUCAffiliation.outcast) {
            return ChatSDK.shared().getString(R.string.outcast);
        }
        return ChatSDK.shared().getString(R.string.none);
    }

    public static String toString(MUCAffiliation affiliation) {
        return toString(affiliation, null);
//
//        if (affiliation == MUCAffiliation.owner) {
//            return ChatSDK.shared().getString(R.string.owner);
//        }
//        if (affiliation == MUCAffiliation.admin) {
//            return ChatSDK.shared().getString(R.string.admin);
//        }
//        if (affiliation == MUCAffiliation.member) {
//            return ChatSDK.shared().getString(R.string.member);
//        }
//        if (affiliation == MUCAffiliation.outcast) {
//            return ChatSDK.shared().getString(R.string.outcast);
//        }
//        return ChatSDK.shared().getString(R.string.none);
    }

    public static String toString(MUCAffiliation affiliation, @Nullable MUCRole role) {
        String affiliationString;
        if (affiliation == MUCAffiliation.owner) {
            affiliationString = ChatSDK.shared().getString(R.string.owner__);
        }
        else if (affiliation == MUCAffiliation.admin) {
            affiliationString = ChatSDK.shared().getString(R.string.admin__);
        }
        else if (affiliation == MUCAffiliation.member) {
            affiliationString = ChatSDK.shared().getString(R.string.member__);
        }
        else if (affiliation == MUCAffiliation.outcast) {
            affiliationString = ChatSDK.shared().getString(R.string.outcast__);
        } else {
//            affiliationString = ChatSDK.shared().getString(R.string.none);
            affiliationString = null;
        }
//        if (role != null) {
//            return String.format(affiliationString, " (" + toString(role) + ")");
//        } else {
//            return String.format(affiliationString, "");
//        }
        return String.format(affiliationString, "");
    }

    public static String toString(MUCRole role) {
        if (role == MUCRole.moderator) {
            return ChatSDK.shared().getString(R.string.moderator);
        }
        if (role == MUCRole.participant) {
            return ChatSDK.shared().getString(R.string.participant);
        }
        if (role == MUCRole.visitor) {
            return ChatSDK.shared().getString(R.string.visitor);
        }
        return ChatSDK.shared().getString(R.string.none);
    }
//    public static int fromString(String string) {
//        if (string != null) {
//            if (string.equals(ChatSDK.shared().getString(R.string.owner))) {
//                return Owner;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.admin))) {
//                return Admin;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.moderator))) {
//                return Moderator;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.member))) {
//                return MUCAffiliation.outcast;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.participant))) {
//                return MUCAffiliation.outcast;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.visitor))) {
//                return MUCAffiliation.outcast;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.outcast))) {
//                return MUCAffiliation.outcast;
//            }
//            if (string.equals(ChatSDK.shared().getString(R.string.outcast))) {
//                return MUCAffiliation.outcast;
//            }
//        }
//        return None;
//    }



    public static MUCAffiliation affiliationFromString(String string) {
        if (string != null) {
            if (string.equals(ChatSDK.shared().getString(R.string.owner))) {
                return MUCAffiliation.owner;
            }
            if (string.equals(ChatSDK.shared().getString(R.string.admin))) {
                return MUCAffiliation.admin;
            }
            if (string.equals(ChatSDK.shared().getString(R.string.member))) {
                return MUCAffiliation.member;
            }
            if (string.equals(ChatSDK.shared().getString(R.string.outcast))) {
                return MUCAffiliation.outcast;
            }
            return MUCAffiliation.none;
        }
        return null;
    }

    public static MUCRole roleFromString(String string) {
        if (string != null) {
            if (string.equals(ChatSDK.shared().getString(R.string.moderator))) {
                return MUCRole.moderator;
            }
            if (string.equals(ChatSDK.shared().getString(R.string.participant))) {
                return MUCRole.participant;
            }
            if (string.equals(ChatSDK.shared().getString(R.string.visitor))) {
                return MUCRole.visitor;
            }
            return MUCRole.none;
        }
        return null;
    }

    public static int role(MUCAffiliation affiliation, MUCRole role) {
        return affiliationToInt(affiliation) | roleToInt(role);
    }

    public static int affiliationToInt(MUCAffiliation affiliation) {
        if (affiliation == null) {
            return None;
        }
        switch (affiliation) {
            case owner:
                return Owner;
            case admin:
                return Admin;
            case member:
                return Member;
            case outcast:
                return Outcast;
            case none:
            default:
                return None;
        }
    }

    public static int roleToInt(MUCRole role) {
        if (role == null) {
            return None;
        }
        switch (role) {
            case moderator:
                return Moderator;
            case participant:
                return Participant;
            case visitor:
                return Visitor;
            case none:
            default:
                return None;
        }
    }

    public static MUCRole intToRole(int role) {
        if ((role & Moderator) != 0) {
            return MUCRole.moderator;
        }
        if ((role & Participant) != 0) {
            return MUCRole.participant;
        }
        if ((role & Visitor) != 0) {
            return MUCRole.visitor;
        }
        return MUCRole.none;
    }

    public static MUCAffiliation intToAffiliation(int role) {
        if ((role & Owner) != 0) {
            return MUCAffiliation.owner;
        }
        if ((role & Admin) != 0) {
            return MUCAffiliation.admin;
        }
        if ((role & Member) != 0) {
            return MUCAffiliation.member;
        }
        if ((role & Outcast) != 0) {
            return MUCAffiliation.outcast;
        }
        return MUCAffiliation.none;
    }

    public static int fromAffiliate(Affiliate affiliate) {
        if (affiliate != null) {
            return affiliationToInt(affiliate.getAffiliation()) | roleToInt(affiliate.getRole());
        }
        return None;
    }

    public static boolean is(int role, int type) {
        return (role & type) != 0;
    }

    public static boolean isAnd(int role, Integer... types) {
        boolean is = true;
        for (Integer type: types) {
            is = is && is(role, type);
        }
        return is;
    }

    public static boolean isOr(int role, Integer... types) {
        boolean is = false;
        for (Integer type: types) {
            is = is || is(role, type);
        }
        return is;
    }
}
