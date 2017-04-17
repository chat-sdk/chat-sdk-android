package wanderingdevelopment.tk.chatsdkcore;

/**
 * Created by kykrueger on 2017-01-30.
 */

public class EqualsWithNulls {
    public static final boolean equalsWithNulls(Object a, Object b) {
        if (a==b) return true;
        if ((a==null)||(b==null)) return false;
        return a.equals(b);
    }
}
