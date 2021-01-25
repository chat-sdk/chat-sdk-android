package app.xmpp.adapter.utils;

import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;

import app.xmpp.adapter.R;
import sdk.chat.core.session.ChatSDK;

public class Role {

    public static Boolean is(String role, MUCAffiliation affiliation) {
        return role.equals(affiliation.name());
    }

    public static Boolean is(String role, MUCRole mucRole) {
        return role.equals(mucRole.name());
    }

    public static Boolean isOwner(String role) {
        return is(role, MUCAffiliation.owner);
    }

    public static Boolean isAdmin(String role) {
        return is(role, MUCAffiliation.admin);
    }

    public static Boolean isOwnerOrAdmin(String role) {
        return isOwner(role) || isAdmin(role);
    }

    public static Boolean canRead(String role) {
        return isOwner(role) || isAdmin(role) || isMember(role);
    }

    public static Boolean isMember(String role) {
        return is(role, MUCAffiliation.member);
    }

    public static Boolean isOutcast(String role) {
        return is(role, MUCAffiliation.outcast);
    }

    public static Boolean isNone(String role) {
        return is(role, MUCAffiliation.none);
    }

    public static Boolean isMemberOrOutcast(String role) {
        return isMember(role) || isOutcast(role);
    }

    public static Integer level(String role) {
        if (isOwner(role)) {
            return 4;
        }
        if (isAdmin(role)) {
            return 3;
        }
        if (isMember(role)) {
            return 2;
        }
        if (isOutcast(role)) {
            return 1;
        }
        return 0;
    }

    public static Boolean isModerator(String role) {
        return is(role, MUCRole.moderator);
    }

    public static Boolean isVisitor(String role) {
        return is(role, MUCRole.visitor);
    }

    public static Boolean isParticipant(String role) {
        return is(role, MUCRole.participant);
    }

    public static String toString(String role) {
        String affiliationString;
        if (isOwner(role)) {
            affiliationString = ChatSDK.getString(R.string.owner);
        }
        else if (isAdmin(role)) {
            affiliationString = ChatSDK.getString(R.string.admin);
        }
        else if (isMember(role)) {
            affiliationString = ChatSDK.getString(R.string.member);
        }
        else if (isOutcast(role)) {
            affiliationString = ChatSDK.getString(R.string.outcast);
        }
        else if (isNone(role)) {
//            affiliationString = ChatSDK.getString(R.string.none);
            affiliationString ="";
        }
        else {
            affiliationString = role;
        }
        return affiliationString;
    }

}
