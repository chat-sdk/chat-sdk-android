package com.braunster.chatsdk.object;

/**
 * Created by braunster on 27/06/14.
 */
public class BError {

    public Object tag = null;
    public int code = -1;
    public String message;

    public BError(int code){
        this.code = code;
        this.message = Message.getMessageForCode(code);
    }

    public BError(int code, String message){
        this.code = code;
        this.message = message;
    }

    public BError(int code, Object tag){
        this.code = code;
        this.tag = tag;
        this.message = Message.getMessageForCode(code);
    }

    public BError(int code, String message, Object tag){
        this.code = code;
        this.message = message;
        this.tag = tag;
    }

    /*Static Initializer's*/
    public static BError getError(int code, String message){
        return new BError(code, message);
    }

    public static BError getNoPathError(){
        return new BError(Code.NO_PATH);
    }

    public static BError getExceptionError(Exception e){
        return new BError(Code.EXCEPTION, e.getMessage(), e);
    }

    public static BError getExceptionError(Exception e, String message){
        return new BError(Code.EXCEPTION, message, e);
    }

    public static final class Code{

        // General Errors
        public static final int SESSION_CLOSED = 10;
        public static final int TAGGED = 11; // All error details are inside the tag object.
        public static final int EXCEPTION = 12; // When an exception occurred that cause the error.
        public static final int NO_PATH = 13; // When an entity does not have valid path to push to.
        public static final int NETWORK_ERROR = 14;
        public static final int PARSE_EXCEPTION = 15; // When a parse exception occurs the tag will contain the exception object.
        public static final int NULL = 16; // If something that related to the wanted method was null.
        public static final int NO_LOGIN_INFO = 17; // When there is no available login info to use when login.
        public static final int ACCESS_TOKEN_REFUSED = 18;
        public static final int BAD_RESPONSE = 19;
        public static final int NO_LOGIN_TYPE = 20;
        public static final int EXPIRED_TOKEN = 21;
        public static final int INVALID_CREDENTIALS = 22;
        public static final int OPERATION_FAILED = 23;
        public static final int PREMISSION_DENIED = 24;

        // Login Errors
        public static final int USER_ALREADY_EXIST = 100;
        public static final int INVALID_EMAIL = 101;
        public static final int EMAIL_TAKEN = 102;
        public static final int INVALIDE_PASSWORD = 103;
        public static final int USER_DOES_NOT_EXIST = 104;


    }

    public static final class Message{
        static String getMessageForCode(int code){
            switch (code)
            {
                case Code.EXCEPTION:
                    return ExcpetionOccurred;

                case Code.NO_LOGIN_INFO:
                    return NoLoginInfo;

                case Code.NO_PATH:
                    return NoPath;

                default: return "";
            }
        }

        public static final String ExcpetionOccurred = "Exception occurred";
        public static final String NoLoginInfo = "No older login data is save in the preferences.";
        public static final String NoPath = "Entity Path is null";
        public static final String Tagged = "Tagged";
    }
}
