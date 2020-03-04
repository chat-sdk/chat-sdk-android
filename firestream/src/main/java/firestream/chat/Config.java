package firestream.chat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import firefly.sdk.chat.R;
import firestream.chat.namespace.Fire;
import io.reactivex.functions.Predicate;

public class Config {

    public enum DatabaseType {
        Firestore,
        Realtime
    }

    public static class TimePeriod {

        long seconds;

        protected TimePeriod(long seconds) {
            this.seconds = seconds;
        }

        public static TimePeriod seconds(long seconds) {
            return new TimePeriod(seconds);
        }

        public static TimePeriod minutes(int minutes) {
            return seconds(TimeUnit.MINUTES.toSeconds(minutes));
        }

        public static TimePeriod hours(long hours) {
            return seconds(TimeUnit.HOURS.toSeconds(hours));
        }

        public static TimePeriod days(long days) {
            return seconds(TimeUnit.DAYS.toSeconds(days));
        }

        public static TimePeriod weeks(long weeks) {
            return days(weeks * 7);
        }

        public static TimePeriod months(long months) {
            return weeks(months * 4);
        }

        public static TimePeriod infinite() {
            return seconds(-1);
        }

        public Date getDate() {
            if (seconds < 0) {
                return new Date(0);
            } else {
                return new Date(new Date().getTime() - TimeUnit.SECONDS.toMillis(seconds));
            }
        }
    }

    /**
     * Should the framework automatically send a delivery receipt when
     * a message type received
     */
    public boolean deliveryReceiptsEnabled = true;

    /**
     * Should the framework send the received receipt automatically
     */
    public boolean autoMarkReceived = true;

    /**
     * Are chat chat invites accepted automatically
     */
    public boolean autoAcceptChatInvite = true;

    /**
     * If this type enabled, each time a message type received, it will be
     * deleted from our inbound message queue childOn Firestore. Even if this
     * type set to false, typing indicator messages and presence messages will
     * always be deleted as they don't have any use in the message archive
     * this flag only affects 1-to-1 messages.
     */
    public boolean deleteMessagesOnReceipt = false;

    /**
     * How many historic messages should we retrieve?
     */
    public int messageHistoryLimit = 100;

    /**
     * This will be the root of the FireStream Firebase database i.e.
     * /root/[sandbox]/users
     */
    protected String root = "firestream";

    /**
     * This will be the sandbox of the FireStream Firebase database i.e.
     * /root/[sandbox]/users
     */
    protected String sandbox = "prod";

    /**
     * When should we add the message listener from? By default
     * we set this to the date of the last message or receipt
     * we sent. This is the most efficient way because each message
     * will be downloaded exactly once.
     *
     * In some situations it may not be desirable. Especially because
     * clients will only pick up remote delete events since the last
     * sent date.
     *
     * If you want messages to be retrieved for a longer history, you
     * can set this to false.
     *
     * If this is set to false, you will need to be careful if you are
     * using read receipts because the framework won't know whether it
     * has already sent an automatic receipt for a message. To resolve
     * this there are two options, you can set {@link Config#autoMarkReceived}
     * to false or you can use the set the read receipt filter
     * {@link FireStream#setMarkReceivedFilter(Predicate)}
     *
     * Fire.stream().setMarkReceivedFilter(event -> {
     *     return !YourMessageStore.getMessageById(event.get().getId()).isMarkedReceived();
     * });
     *
     * So if the message receipt has been sent already return false, otherwise
     * return true
     *
     */
    public boolean startListeningFromLastSentMessageDate = true;

    /**
     * This will listen to messages with a duration before
     * the current date. For example, if we set the duration to 1 week,
     * we will start listening to messages that have been received in
     * the last week. If it is set to null there will be no limit,
     * we will listed to all historic messages
     *
     * This also is in effect in the case that the {@link Config#startListeningFromLastSentMessageDate }
     * is set to true, in that case, if there are no messages or receipts in the queue,
     * the listener will be set with this duration ago
     * */
    public TimePeriod listenToMessagesWithTimeAgo = TimePeriod.days(10);

    /**
     * Which database to use - Firestore or Realtime database
     */
    public DatabaseType database = DatabaseType.Firestore;

    /**
     * Should debug log messages be shown?
     */
    public boolean debugEnabled = false;

    public void setRoot(String root) throws Exception {
        if (pathValid(root)) {
            this.root = root;
        } else {
            throw new Exception(Fire.internal().context().getString(R.string.error_invalid_path));
        }
    }

    public void setSandbox(String sandbox) throws Exception {
        if (pathValid(sandbox)) {
            this.sandbox = sandbox;
        } else {
            throw new Exception(Fire.internal().context().getString(R.string.error_invalid_path));
        }
    }

    protected boolean pathValid(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if(!Character.isLetterOrDigit(c) && !String.valueOf(c).equals("_")) {
                return false;
            }
        }
        return true;
    }

    public String getRoot() {
        return root;
    }

    public String getSandbox() {
        return sandbox;
    }

}
