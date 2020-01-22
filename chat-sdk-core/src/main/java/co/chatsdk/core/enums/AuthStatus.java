package co.chatsdk.core.enums;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public enum AuthStatus {
    Idle {
        @Override
        public String toString() {
            return "Idle";
        }
    },
    Authenticating {
        @Override
        public String toString() {
            return "Authenticating";
        }
    }
}
