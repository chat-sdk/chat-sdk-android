package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.carbons.CarbonCopyReceivedListener;
import org.jivesoftware.smackx.carbons.packet.CarbonExtension;
import org.pmw.tinylog.Logger;


public class XMPPCarbonCopyReceivedListener implements CarbonCopyReceivedListener {
    @Override
    public void onCarbonCopyReceived(CarbonExtension.Direction direction, Message carbonCopy, Message wrappingMessage) {
        Logger.debug("Carbon received");
    }
}
