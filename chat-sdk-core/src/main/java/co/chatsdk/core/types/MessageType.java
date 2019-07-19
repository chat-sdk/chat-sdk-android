package co.chatsdk.core.types;

/**
 * Created by ben on 9/29/17.
 */

//public enum MessageType {
//
//    Text,
//    Location,
//    Image,
//    Audio,
//    Video,
//    System,
//    Sticker,
//    File,
//    Custom,
//    None,
//
//}

public class MessageType {

    public static final int None = -1;
    public static final int Text = 0;
    public static final int Location = 1;
    public static final int Image = 2;
    public static final int Audio = 3;
    public static final int Video = 4;
    public static final int System = 5;
    public static final int Sticker = 6;
    public static final int File = 7;
    public static final int Contact = 8;
    public static final int Snap = 9;
    public static final int Max = 100000;

    private int value;

    public MessageType (int type) {
        assert (type < MessageType.Max);
        value = type;
    }

    public int value () {
        return value;
    }

    public int ordinal () {
        return value;
    }

    public boolean is (MessageType type) {
        return is(type.value());
    }

    public boolean is (MessageType... types) {
        for (MessageType type : types) {
            if (value() == type.value()) {
                return true;
            }
        }
        return false;
    }

    public boolean is (int type) {
        return value() == type;
    }

    public boolean is (int... types) {
        for (int type : types) {
            if (value() == type) {
                return true;
            }
        }
        return false;
    }

}
