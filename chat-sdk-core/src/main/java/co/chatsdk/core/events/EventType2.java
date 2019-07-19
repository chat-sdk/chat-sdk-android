package co.chatsdk.core.events;

public class EventType2 {

    public static int Added = 0x1;
    public static int Removed = 0x2;
    public static int Updated = 0x4;
    public static int Deleted = 0x8;

    public static int Thread = 0x10;
    public static int Message = 0x20;
    public static int User = 0x40;
    public static int Follower = 0x80;
    public static int Following = 0x100;
    public static int Contact = 0x200;

    public static int Logout = 10001;

    public static int ThreadAdded = Thread | Added;
    public static int ThreadRemoved = Thread | Removed;
    public static int ThreadUpdated = Thread | Updated;

    public static int FollowerAdded = Follower | Added;
    public static int FollowerRemoved = Follower | Removed;

    public static int FollowingAdded = Following | Added;
    public static int FollowingRemoved = Following | Removed;

    public static int MessageAdded = Message | Added;
    public static int MessageRemoved = Message | Removed;

    public static int UserUpdated = User | Updated;

    public static int ContactAdded = Contact | Added;
    public static int ContactRemoved = Contact | Removed;
    public static int ContactUpdated = Contact | Updated;

}
