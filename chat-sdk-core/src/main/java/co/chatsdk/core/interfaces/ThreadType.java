package co.chatsdk.core.interfaces;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class ThreadType {

//    ThreadType

//    public static int Private2 = 0x1;
//    public static int _1to1 = 0x2;
//    public static int Public2 = 0x4;
//    public static int Group2 = 0x8;
//
//    public static int PrivateGroup2 = Private2 | Group2;
//    public static int Private1to12 = _1to1 | Private2;
//    public static int PublicGroup2 = Public2 | Group2;

    public static int Snap = 0x8;

    public static int PrivateGroup = 0x1;
    public static int Private1to1 = 0x2;
    public static int PublicGroup = 0x4;

    public static int Private1to1Snap = Private1to1 | Snap;

    // Descriptors
    public static int Private = Private1to1 | PrivateGroup;
    public static int Public = PublicGroup;
    public static int Group = PrivateGroup | PublicGroup;
    public static int All = 0xFF;

    // To maintain backwards compatibility
    public static int PrivateV3 = 0;
    public static int PublicV3 = 1;

    public static boolean isPublic (int type) {
        return (type & Public) > 0;
    }

    public static boolean isPrivate (int type) {
        return (type & Private) > 0;
    }

}
