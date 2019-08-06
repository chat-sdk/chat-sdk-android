package co.chatsdk.core.interfaces;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class ThreadType {

    // Thread Type
    public static int None = -1;
    public static int PrivateGroup = 0x1;
    public static int Private1to1 = 0x2;
    public static int PublicGroup = 0x4;
    public static int Snap = 0x8;

    // Snaps
    public static int Private1to1Snap = Private1to1 | Snap;
    public static int PrivateGroupSnap = PrivateGroup | Snap;

    // Descriptors
    public static int Private = Private1to1 | PrivateGroup;
    public static int Public = PublicGroup;
    public static int Group = PrivateGroup | PublicGroup;
    public static int All = 0xFF;

    public static boolean isPublic (int type) {
        return is(type, Public);
    }

    public static boolean isPrivate (int type) {
        return is(type, Private);
    }

    public static boolean is (int type1, int type2) {
        return (type1 & type2) > 0;
    }

}
