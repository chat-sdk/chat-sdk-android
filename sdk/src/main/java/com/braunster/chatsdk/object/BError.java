package com.braunster.chatsdk.object;

import android.os.Message;

import com.firebase.client.FirebaseError;

/**
 * Created by braunster on 27/06/14.
 */
public class BError {

    private Object tag = null;
    private int code = -1;
    private String message;

    public BError(int code){
        this.code = code;
    }

    public BError(int code, String message){
        this.code = code;
        this.message = message;
    }

    public BError(int code, Object tag){
        this.code = code;
        this.tag = tag;
    }

    public BError(int code, String message, Object tag){
        this.code = code;
        this.message = message;
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public String getMessage() {
        if (message == null)
            message = Message.getMessageForCode(code);

        return message;
    }

    /*Static Initializer's*/
    public static BError getFirebaseError(FirebaseError error){
        return new BError(Code.FIREBASE_ERROR, error);
    }

    public static BError getNoPathError(){
        return new BError(Code.NO_PATH);
    }

    public static BError getExceptionError(Exception e){
        return new BError(Code.EXCEPTION, e);
    }

    public static BError getExceptionError(Exception e, String message){
        return new BError(Code.EXCEPTION, message, e);
    }

    public static final class Code{
        public static final int SESSION_CLOSED = 10;
        public static final int TAGGED = 11; // All error details are inside the tag object.
        public static final int EXCEPTION = 12; // When an exception occurred that cause the error.
        public static final int NO_PATH = 13; // When an entity does not have valid path to push to.
        public static final int FIREBASE_ERROR = 14; // When a firebase error occurs the tag will contain the error object.
    }

    public static final class Message{
        static String getMessageForCode(int code){
            switch (code)
            {
                case Code.EXCEPTION:
                    return ExcpetionOccurred;

                case Code.FIREBASE_ERROR:
                    return FirebaseError;

                case Code.NO_PATH:
                    return NoPath;

                default: return "Error";
            }
        }

        public static final String FirebaseError = "Firebase error occurred";
        public static final String ExcpetionOccurred = "Exception occurred";
        public static final String NoPath = "Entity Path is null";
        public static final String Tagged = "Tagged";
    }
}
