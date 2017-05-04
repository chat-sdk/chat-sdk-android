package co.chatsdk.core.enums;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public enum AuthStatus {
    IDLE {
        @Override
        public String toString() {
            return "Idle";
        }
    },
    AUTH_WITH_MAP{
        @Override
        public String toString() {
            return "Auth with map";
        }
    },
    HANDLING_F_USER{
        @Override
        public String toString() {
            return "Handling F user";
        }
    },
    UPDATING_USER{
        @Override
        public String toString() {
            return "Updating user";
        }
    },
    PUSHING_USER{
        @Override
        public String toString() {
            return "Pushing user";
        }
    },
    CHECKING_IF_AUTH{
        @Override
        public String toString() {
            return "Checking if Authenticated";
        }
    }
}
