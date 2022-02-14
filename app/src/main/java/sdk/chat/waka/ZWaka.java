package sdk.chat.waka;

import java.util.concurrent.TimeUnit;

public class ZWaka {

    protected static final ZWaka instance = new ZWaka();
    public static ZWaka shared() {
        return instance;
    }

    // Threads are added to the user in order. So this is how many of the most recently added threads should be live
    public int mostRecent = 1;

    // Total max live threads. Threads will be added in order of last message added
    public int maxThreadCount = 2;

    public long maxThreadAgeInMillis = TimeUnit.DAYS.toMillis(10);

}
