package sdk.chat.core.base;

import java.util.HashMap;

import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.handlers.BlockingHandler;
import sdk.chat.core.handlers.ContactHandler;
import sdk.chat.core.handlers.ContactMessageHandler;
import sdk.chat.core.handlers.CoreHandler;
import sdk.chat.core.handlers.EncryptionHandler;
import sdk.chat.core.handlers.EventHandler;
import sdk.chat.core.handlers.FileMessageHandler;
import sdk.chat.core.handlers.HookHandler;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.chat.core.handlers.LocationMessageHandler;
import sdk.chat.core.handlers.ModerationHandler;
import sdk.chat.core.handlers.NearbyUsersHandler;
import sdk.chat.core.handlers.ProfilePicturesHandler;
import sdk.chat.core.handlers.PublicThreadHandler;
import sdk.chat.core.handlers.PushHandler;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.handlers.SearchHandler;
import sdk.chat.core.handlers.StickerMessageHandler;
import sdk.chat.core.handlers.ThreadHandler;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.handlers.UploadHandler;
import sdk.chat.core.handlers.VideoMessageHandler;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class BaseNetworkAdapter {

    public CoreHandler core;
    public AuthenticationHandler auth;
    public PushHandler push;
    public UploadHandler upload;
    public ThreadHandler thread;
    public VideoMessageHandler videoMessage;
    public AudioMessageHandler audioMessage;
    public ImageMessageHandler imageMessage = new BaseImageMessageHandler();
    public LocationMessageHandler locationMessage = new BaseLocationMessageHandler();
    public ContactHandler contact = new BaseContactHandler();
    public TypingIndicatorHandler typingIndicator;
    public ModerationHandler moderation;
    public SearchHandler search;
    public PublicThreadHandler publicThread;
    public ProfilePicturesHandler profilePictures;
    public BlockingHandler blocking;
    public LastOnlineHandler lastOnline;
    public NearbyUsersHandler nearbyUsers;
    public ReadReceiptHandler readReceipts;
    public StickerMessageHandler stickerMessage;
    public FileMessageHandler fileMessage;
    public EventHandler events;
    public HookHandler hook = new BaseHookHandler();
    public EncryptionHandler encryption;
    public ContactMessageHandler contactMessage;

    private HashMap<String, Object> handlers = new HashMap<>();

    public void setHandler(Object handler, String name) {
        handlers.put(name, handler);
    }

    public Object getHandler (String name) {
        return handlers.get(name);
    }

    public void stop() {
        if (auth != null) {
            auth.stop();
        }
        if (events != null) {
            events.stop();
        }
    }

}
