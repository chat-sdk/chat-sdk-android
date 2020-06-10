package sdk.chat.core.utils;

public class TimeLog {

    protected static long start;
    protected static String name;
    protected static boolean enabled = false;

    public static void startTimeLog(String theName) {
//        if (enabled && Looper.myLooper() == null || Looper.myLooper().equals(Looper.getMainLooper())) {
//            start = System.currentTimeMillis();
//            name = theName;
//        }
    }

    public static void endTimeLog() {
//        if (enabled && Looper.myLooper() == null || Looper.myLooper().equals(Looper.getMainLooper())) {
//            Logger.debug("TimeLog: " + name + ", duration: " + String.valueOf(System.currentTimeMillis() - start));
//            start = 0;
//        }
    }

}
