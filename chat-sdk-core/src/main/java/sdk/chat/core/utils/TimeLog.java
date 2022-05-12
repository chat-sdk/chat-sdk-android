package sdk.chat.core.utils;

import org.pmw.tinylog.Logger;

public class TimeLog {

    protected long start;
    protected long end;
    protected String name;

    public TimeLog(String name) {
        this(name, true);
    }

    public TimeLog(String name, boolean startNow) {
        this.name = name;
        if (startNow) {
            start();
        }
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void end() {
        end = System.currentTimeMillis();
        long diff = end - start;
        Logger.warn(name + " diff: " + diff);
    }

}
