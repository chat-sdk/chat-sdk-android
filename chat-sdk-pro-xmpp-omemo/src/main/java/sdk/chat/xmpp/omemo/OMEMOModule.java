package sdk.chat.xmpp.omemo;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.carbons.packet.CarbonExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;

import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.OmemoMessage;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
import org.jivesoftware.smackx.omemo.listener.OmemoMessageListener;
import org.jivesoftware.smackx.omemo.listener.OmemoMucMessageListener;
import org.jivesoftware.smackx.omemo.signal.SignalCachingOmemoStore;
import org.jivesoftware.smackx.omemo.signal.SignalFileBasedOmemoStore;
import org.jivesoftware.smackx.omemo.signal.SignalOmemoService;
import org.jivesoftware.smackx.omemo.trust.OmemoFingerprint;
import org.jivesoftware.smackx.omemo.trust.OmemoTrustCallback;
import org.jivesoftware.smackx.omemo.trust.TrustState;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.pmw.tinylog.Logger;

import java.util.UUID;

import app.xmpp.adapter.XMPPManager;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;

public class OMEMOModule implements OmemoTrustCallback, OmemoMessageListener, OmemoMucMessageListener {

    public static final OMEMOModule instance = new OMEMOModule();
    public static OMEMOModule shared() {
        return instance;
    }

    TrustState trustState;

    public OMEMOModule() {

    }

    public void start() {
        SignalOmemoService.acknowledgeLicense();
        SignalOmemoService.setup();

        SignalOmemoService service = (SignalOmemoService) SignalOmemoService.getInstance();
        service.setOmemoStoreBackend(new SignalCachingOmemoStore(new SignalFileBasedOmemoStore(ChatSDK.ctx().getFilesDir())));

        OmemoManager omemoManager = OmemoManager.getInstanceFor(XMPPManager.shared().getConnection());

        omemoManager.setTrustCallback(this);
        omemoManager.addOmemoMessageListener(this);
        omemoManager.addOmemoMucMessageListener(this);

        try {
            omemoManager.purgeDeviceList();
            omemoManager.initialize();

            ChatSDK.hook().addHook(Hook.sync(data -> {
                Object messageObject = data.get(HookEvent.Message);
                if (messageObject instanceof sdk.chat.core.dao.Message) {
                    sdk.chat.core.dao.Message message = (sdk.chat.core.dao.Message) messageObject;

                    try {
                        EntityBareJid bareJid = JidCreate.entityBareFrom(message.getThread().getEntityID());
                        OmemoMessage.Sent encMessage = omemoManager.encrypt(bareJid, message.getText());

                        Message newMessage = encMessage.buildMessage(MessageBuilder.buildMessage(UUID.randomUUID().toString()), bareJid);
                        XMPPManager.shared().sendStanza(newMessage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }), HookEvent.MessageWillSend);

        } catch (Exception e) {

        }
    }

    @Override
    public TrustState getTrust(OmemoDevice device, OmemoFingerprint fingerprint) {
        return TrustState.trusted;
    }

    @Override
    public void setTrust(OmemoDevice device, OmemoFingerprint fingerprint, TrustState state) {
        this.trustState = state;
    }

    @Override
    public void onOmemoMessageReceived(Stanza stanza, OmemoMessage.Received decryptedMessage) {
        Logger.info(decryptedMessage);
    }

    @Override
    public void onOmemoCarbonCopyReceived(CarbonExtension.Direction direction, Message carbonCopy, Message wrappingMessage, OmemoMessage.Received decryptedCarbonCopy) {
        Logger.info(decryptedCarbonCopy);
    }

    @Override
    public void onOmemoMucMessageReceived(MultiUserChat muc, Stanza stanza, OmemoMessage.Received decryptedOmemoMessage) {
        Logger.info(decryptedOmemoMessage);
    }
}
