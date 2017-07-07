package co.chatsdk.core.types;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

@Deprecated
public class AccountType {

    /**
     * Each type get his own prefix by using the private constructor.
     * This is the place to change the prefix if wanted.
     * */
    public static final int Password = 1;
    public static final int Facebook = 2;
    public static final int Twitter = 3;
    public static final int Anonymous = 4;
    public static final int Google = 5;
    public static final int Custom = 6;
    public static final int Register = 99;

}
