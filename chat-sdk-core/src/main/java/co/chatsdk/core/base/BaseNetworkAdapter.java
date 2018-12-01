package co.chatsdk.core.base;

import java.util.HashMap;

import co.chatsdk.core.handlers.AudioMessageDisplayHandler;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.handlers.BlockingHandler;
import co.chatsdk.core.handlers.ContactHandler;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.EncryptionHandler;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.handlers.ImageMessageHandler;
import co.chatsdk.core.handlers.LastOnlineHandler;
import co.chatsdk.core.handlers.LocationMessageHandler;
import co.chatsdk.core.handlers.ModerationHandler;
import co.chatsdk.core.handlers.NearbyUsersHandler;
import co.chatsdk.core.handlers.ProfilePicturesHandler;
import co.chatsdk.core.handlers.PublicThreadHandler;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.handlers.SocialLoginHandler;
import co.chatsdk.core.handlers.StickerMessageDisplayHandler;
import co.chatsdk.core.handlers.FileMessageDisplayHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.handlers.VideoMessageDisplayHandler;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class BaseNetworkAdapter {

    public CoreHandler core;
    public AuthenticationHandler auth;
    public PushHandler push;
    public UploadHandler upload;
    public ThreadHandler thread;
    public VideoMessageDisplayHandler videoMessage;
    public AudioMessageDisplayHandler audioMessage;
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
    public StickerMessageDisplayHandler stickerMessage;
    public FileMessageDisplayHandler fileMessage;
    public SocialLoginHandler socialLogin;
    public EventHandler events;
    public HookHandler hook = new BaseHookHandler();
    public EncryptionHandler encryption;

    private HashMap<String, Object> handlers = new HashMap<>();

    public void setHandler(Object handler, String name) {
        handlers.put(name, handler);
    }

    public Object getHandler (String name) {
        return handlers.get(name);
    }

}
