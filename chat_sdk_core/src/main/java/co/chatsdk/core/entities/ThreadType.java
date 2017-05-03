package co.chatsdk.core.entities;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class ThreadType {

    public static int PrivateGroup = 0x1;
    public static int Private1to1 = 0x2;
    public static int PublicGroup = 0x4;

    // Descriptors
    public static int Private = Private1to1 | PrivateGroup;
    public static int Public = PublicGroup;
    public static int Group = PrivateGroup | PublicGroup;

    // To maintain backwards compatibility
    public static int PrivateV3 = 0;
    public static int PublicV3 = 1;

}
