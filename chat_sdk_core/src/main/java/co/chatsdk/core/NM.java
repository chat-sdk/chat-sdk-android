package co.chatsdk.core;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.AudioMessageHandler;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.handlers.BlockingHandler;
import co.chatsdk.core.handlers.ConfigurationHandler;
import co.chatsdk.core.handlers.ContactHandler;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.handlers.LastOnlineHandler;
import co.chatsdk.core.handlers.PublicThreadHandler;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.handlers.SocialLoginHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.handlers.VideoMessageHandler;

/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public class NM {

    public static CoreHandler core () {
        return NetworkManager.shared().a.core;
    }

    public static AuthenticationHandler auth () {
        return NetworkManager.shared().a.auth;
    }

    public static ThreadHandler thread () {
        return NetworkManager.shared().a.thread;
    }

    public static PublicThreadHandler publicThread () {
        return NetworkManager.shared().a.publicThread;
    }

    public static PushHandler push () {
        return NetworkManager.shared().a.push;
    }

    public static UploadHandler upload () {
        return NetworkManager.shared().a.upload;
    }

    public static EventHandler events () {
        return NetworkManager.shared().a.events;
    }

    public static User currentUser () {
        return NM.core().currentUserModel();
    }

    public static SearchHandler search () {
        return NetworkManager.shared().a.search;
    }

    public static ContactHandler contact () {
        return NetworkManager.shared().a.contact;
    }

    public static BlockingHandler blocking () {
        return NetworkManager.shared().a.blocking;
    }

    public static LastOnlineHandler lastOnline () {
        return NetworkManager.shared().a.lastOnline;
    }

    public static ConfigurationHandler config () {
        return NetworkManager.shared().a.config;
    }

    public static AudioMessageHandler audioMessage () {
        return NetworkManager.shared().a.audioMessage;
    }

    public static VideoMessageHandler videoMessage () {
        return NetworkManager.shared().a.videoMessage;
    }

    public static HookHandler hook () {
        return NetworkManager.shared().a.hook;
    }

    public static SocialLoginHandler socialLogin () {
        return NetworkManager.shared().a.socialLogin;
    }

    public static ReadReceiptHandler readReceipts () {
        return NetworkManager.shared().a.readReceipts;
    }

    public static TypingIndicatorHandler typingIndicator () {
        return NetworkManager.shared().a.typingIndicator;
    }

}
