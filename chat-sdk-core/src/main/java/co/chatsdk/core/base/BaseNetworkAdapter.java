package co.chatsdk.core.base;

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
import co.chatsdk.core.handlers.ModerationHandler;
import co.chatsdk.core.handlers.NearbyUsersHandler;
import co.chatsdk.core.handlers.PublicThreadHandler;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.handlers.SocialLoginHandler;
import co.chatsdk.core.handlers.StickerMessageHandler;
import co.chatsdk.core.handlers.FileMessageHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.handlers.VideoMessageHandler;

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
    public BlockingHandler blocking;
    public LastOnlineHandler lastOnline;
    public NearbyUsersHandler nearbyUsers;
    public ReadReceiptHandler readReceipts;
    public StickerMessageHandler stickerMessage;
    public FileMessageHandler fileMessage;
    public SocialLoginHandler socialLogin;
    public EventHandler events;
    public HookHandler hook = new BaseHookHandler();

}
