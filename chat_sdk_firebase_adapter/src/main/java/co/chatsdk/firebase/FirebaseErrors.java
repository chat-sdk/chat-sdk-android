package co.chatsdk.firebase;

import co.chatsdk.core.types.ChatError;
import com.google.firebase.database.DatabaseError;

/**
 * Created by KyleKrueger on 11.04.2017.
 */

@Deprecated
public class FirebaseErrors {

    /** Convert the firebase error to a {@link ChatError ChatError} object. */
    public static ChatError getFirebaseError(DatabaseError error){
        String errorMessage = "";

        int code = 0;

        switch (error.getCode())
        {
            /*case DatabaseError.EMAIL_TAKEN:
                code = ChatError.Code.EMAIL_TAKEN;
                errorMessage = "Email is taken.";
                break;

            case DatabaseError.INVALID_EMAIL:
                code = ChatError.Code.INVALID_EMAIL;
                errorMessage = "Invalid Email.";
                break;

            case DatabaseError.INVALID_PASSWORD:
                code = ChatError.Code.INVALID_PASSWORD;
                errorMessage = "Invalid Password";
                break;

            case DatabaseError.USER_DOES_NOT_EXIST:
                code = ChatError.Code.USER_DOES_NOT_EXIST;
                errorMessage = "Account not found.";
                break;

            case DatabaseError.INVALID_CREDENTIALS:
                code = ChatError.Code.INVALID_CREDENTIALS;
                errorMessage = "Invalid credentials.";
                break;*/

            case DatabaseError.NETWORK_ERROR:
                code = ChatError.Code.NETWORK_ERROR;
                errorMessage = "Network Error.";
                break;

            case DatabaseError.EXPIRED_TOKEN:
                code = ChatError.Code.EXPIRED_TOKEN;
                errorMessage = "Expired Token.";
                break;

            case DatabaseError.OPERATION_FAILED:
                code = ChatError.Code.OPERATION_FAILED;
                errorMessage = "Operation failed";
                break;

            case DatabaseError.PERMISSION_DENIED:
                code = ChatError.Code.PERMISSION_DENIED;
                errorMessage = "Permission denied";
                break;

            case DatabaseError.DISCONNECTED:
                code = ChatError.Code.DISCONNECTED;
                errorMessage = "Disconnected.";
                break;

            case DatabaseError.INVALID_TOKEN:
                code = ChatError.Code.INVALID_TOKEN;
                errorMessage = "Invalid token.";
                break;

            case DatabaseError.MAX_RETRIES:
                code = ChatError.Code.MAX_RETRIES;
                errorMessage = "Max retries.";
                break;

            case DatabaseError.OVERRIDDEN_BY_SET:
                code = ChatError.Code.OVERRIDDEN_BY_SET;
                errorMessage = "Overridden by set.";
                break;

            case DatabaseError.UNAVAILABLE:
                code = ChatError.Code.UNAVAILABLE;
                errorMessage = "Unavailable.";
                break;

            case DatabaseError.UNKNOWN_ERROR:
                code = ChatError.Code.UNKNOWN_ERROR;
                errorMessage = "Unknown error.";
                break;

            case DatabaseError.USER_CODE_EXCEPTION:
                code = ChatError.Code.USER_CODE_EXCEPTION;

                String[] stacktrace = error.toException().getMessage().split(": ");

                String[] message = stacktrace[2].split("\\.");

                errorMessage = message[0];
                break;

            case DatabaseError.WRITE_CANCELED:
                code = ChatError.Code.WRITE_CANCELED;
                errorMessage = "Write canceled.";
                break;

            default: errorMessage = "An Error Occurred.";
        }

        return new ChatError(code, errorMessage, error);
    }
}
