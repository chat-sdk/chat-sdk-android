package co.chatsdk.core.session;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.AudioMessageHandler;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.handlers.BlockingHandler;
import co.chatsdk.core.handlers.ContactHandler;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.handlers.ImageMessageHandler;
import co.chatsdk.core.handlers.LastOnlineHandler;
import co.chatsdk.core.handlers.LocationMessageHandler;
import co.chatsdk.core.handlers.PublicThreadHandler;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.handlers.SocialLoginHandler;
import co.chatsdk.core.handlers.StickerMessageHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.handlers.VideoMessageHandler;

/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public class NM {

    public static CoreHandler core () {
        return a().core;
    }

    public static AuthenticationHandler auth () {
        return a().auth;
    }

    public static ThreadHandler thread () {
        return a().thread;
    }

    public static PublicThreadHandler publicThread () {
        return a().publicThread;
    }

    public static PushHandler push () {
        return a().push;
    }

    public static UploadHandler upload () {
        return a().upload;
    }

    public static EventHandler events () {
        return a().events;
    }

    public static User currentUser () {
        return NM.core().currentUserModel();
    }

    public static SearchHandler search () {
        return a().search;
    }

    public static ContactHandler contact () {
        return a().contact;
    }

    public static BlockingHandler blocking () {
        return a().blocking;
    }

    public static LastOnlineHandler lastOnline () {
        return a().lastOnline;
    }

    public static AudioMessageHandler audioMessage () {
        return a().audioMessage;
    }

    public static VideoMessageHandler videoMessage () {
        return a().videoMessage;
    }

    public static HookHandler hook () {
        return a().hook;
    }

    public static SocialLoginHandler socialLogin () {
        return a().socialLogin;
    }

    public static StickerMessageHandler stickerMessage () {
        return a().stickerMessage;
    }

    public static ImageMessageHandler imageMessage () {
        return a().imageMessage;
    }

    public static LocationMessageHandler locationMessage () {
        return a().locationMessage;
    }

    public static ReadReceiptHandler readReceipts () {
        return a().readReceipts;
    }

    public static TypingIndicatorHandler typingIndicator () {
        return a().typingIndicator;
    }

    public static BaseNetworkAdapter a() {
        return NetworkManager.shared().a;
    }

}
