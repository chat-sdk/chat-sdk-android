package com.braunster.androidchatsdk.firebaseplugin.firebase;

import com.braunster.chatsdk.object.BError;
import com.google.firebase.database.DatabaseError;

/**
 * Created by KyleKrueger on 11.04.2017.
 */

public class FirebaseErrors {

    /** Convert the firebase error to a {@link com.braunster.chatsdk.object.BError BError} object. */
    public static BError getFirebaseError(DatabaseError error){
        String errorMessage = "";

        int code = 0;

        switch (error.getCode())
        {
            /*case DatabaseError.EMAIL_TAKEN:
                code = BError.Code.EMAIL_TAKEN;
                errorMessage = "Email is taken.";
                break;

            case DatabaseError.INVALID_EMAIL:
                code = BError.Code.INVALID_EMAIL;
                errorMessage = "Invalid Email.";
                break;

            case DatabaseError.INVALID_PASSWORD:
                code = BError.Code.INVALID_PASSWORD;
                errorMessage = "Invalid Password";
                break;

            case DatabaseError.USER_DOES_NOT_EXIST:
                code = BError.Code.USER_DOES_NOT_EXIST;
                errorMessage = "Account not found.";
                break;

            case DatabaseError.INVALID_CREDENTIALS:
                code = BError.Code.INVALID_CREDENTIALS;
                errorMessage = "Invalid credentials.";
                break;*/

            case DatabaseError.NETWORK_ERROR:
                code = BError.Code.NETWORK_ERROR;
                errorMessage = "Network Error.";
                break;

            case DatabaseError.EXPIRED_TOKEN:
                code = BError.Code.EXPIRED_TOKEN;
                errorMessage = "Expired Token.";
                break;

            case DatabaseError.OPERATION_FAILED:
                code = BError.Code.OPERATION_FAILED;
                errorMessage = "Operation failed";
                break;

            case DatabaseError.PERMISSION_DENIED:
                code = BError.Code.PERMISSION_DENIED;
                errorMessage = "Permission denied";
                break;

            case DatabaseError.DISCONNECTED:
                code = BError.Code.DISCONNECTED;
                errorMessage = "Disconnected.";
                break;

            case DatabaseError.INVALID_TOKEN:
                code = BError.Code.INVALID_TOKEN;
                errorMessage = "Invalid token.";
                break;

            case DatabaseError.MAX_RETRIES:
                code = BError.Code.MAX_RETRIES;
                errorMessage = "Max retries.";
                break;

            case DatabaseError.OVERRIDDEN_BY_SET:
                code = BError.Code.OVERRIDDEN_BY_SET;
                errorMessage = "Overridden by set.";
                break;

            case DatabaseError.UNAVAILABLE:
                code = BError.Code.UNAVAILABLE;
                errorMessage = "Unavailable.";
                break;

            case DatabaseError.UNKNOWN_ERROR:
                code = BError.Code.UNKNOWN_ERROR;
                errorMessage = "Unknown error.";
                break;

            case DatabaseError.USER_CODE_EXCEPTION:
                code = BError.Code.USER_CODE_EXCEPTION;

                String[] stacktrace = error.toException().getMessage().split(": ");

                String[] message = stacktrace[2].split("\\.");

                errorMessage = message[0];
                break;

            case DatabaseError.WRITE_CANCELED:
                code = BError.Code.WRITE_CANCELED;
                errorMessage = "Write canceled.";
                break;

            default: errorMessage = "An Error Occurred.";
        }

        return new BError(code, errorMessage, error);
    }
}
