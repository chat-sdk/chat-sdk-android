package co.chatsdk.core.types;

import java.util.Random;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

@Deprecated
public class Defines {

    /**
     * divide tat is used to divide b
     **/
    public static final String DIVIDER = ",";


    public static final class Time {
        public static final float Minutes = 60.0f;
        public static final float Hours = 60.0f * Minutes;
        public static final float Days = 24.0f * Hours;
        public static final float Months = 30.0f * Days;
        public static final float Years = 12.0f * Months;
    }

    public static final class MessageDateFormat {
        public static final String YearOldMessageFormat = "MM/yy";
        public static final String DayOldFormat = "MMM dd";
        public static final String LessThenDayFormat = "HH:mm";
    }

    public static final int MESSAGE_NOTIFICATION_ID = 1001;


    public static final String FROM_PUSH = "from_push";
    public static final String MSG_TIMESTAMP = "timestamp";

}
