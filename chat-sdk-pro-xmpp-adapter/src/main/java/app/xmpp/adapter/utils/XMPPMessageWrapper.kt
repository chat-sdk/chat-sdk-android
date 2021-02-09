package app.xmpp.adapter.utils

import app.xmpp.adapter.XMPPManager
import app.xmpp.adapter.defines.XMPPDefines
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.StandardExtensionElement
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smackx.address.packet.MultipleAddresses
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension
import org.jivesoftware.smackx.delay.packet.DelayInformation
import org.jivesoftware.smackx.receipts.DeliveryReceipt
import org.jxmpp.jid.impl.JidCreate
import org.pmw.tinylog.Logger
import sdk.chat.core.dao.Thread
import sdk.chat.core.dao.User
import sdk.chat.core.interfaces.ThreadType
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.types.MessageType
import java.util.*

open class XMPPMessageWrapper(val message: Stanza) {

    open fun threadEntityID(): String {
        return if (isGroupChat()) {
            from()
        } else {
            var from = message.extFrom() ?: from()
            if (from == ChatSDK.currentUserID()) {
                return to()
            }
            return from
        }
    }

    open fun userEntityID(): String? {
        return message.extFrom()
//        return if(isGroupChat()) {
//            val from = delayInformation()?.from ?: XMPPManager.shared().mucManager.userJID(message.from)
//            return JidCreate.bareFrom(from).toString()
//        } else {
//            from()
//        }
    }

    open fun hasAction(action: Int): Boolean {
        return if (isSilent()) {
            action() == action
        } else false
    }

    open fun isSilent(): Boolean {
        return type() == MessageType.Silent
    }

    open fun extras(): StandardExtensionElement? {
        val element = message.getExtension<ExtensionElement>(XMPPDefines.Extras, XMPPDefines.MessageNamespace)
        return if (element is StandardExtensionElement) {
            element
        } else null
    }

    open fun from(): String {
        return message.from.asBareJid().toString()
    }

    open fun to(): String {
        return message.to.asBareJid().toString()
    }

    open fun user(): User? {
        val entityID = userEntityID()
        if (entityID != null) {
            return ChatSDK.core().getUserNowForEntityID(userEntityID())
        }
        return null;
    }

    open fun type(): Int {
        return extras()?.getFirstElement(XMPPDefines.Type)?.text?.toInt() ?: MessageType.None
    }

    open fun action(): Int {
        return extras()?.getFirstElement(XMPPDefines.Action)?.text?.toInt() ?: MessageType.None
    }

    open fun isGroupChat(): Boolean {
        return getType() == Message.Type.groupchat
    }

    open fun getType(): Message.Type {
        if (message is Message) {
            return message.type
        }
        return Message.Type.normal
    }

    open fun isOneToOne(): Boolean {
        return ((getType() == Message.Type.chat || getType() == Message.Type.normal)
                && body() != null)
    }

    open fun getThread(): Thread? {

        // There are three options:
        // 1-to-1 message incoming - from other user, to me
        // 1-to-1 message outcoming - from me to other user
        // Group chat = from group chat to me
        return if (isGroupChat()) {
            ChatSDK.db().fetchThreadWithEntityID(threadEntityID())
        } else {
            val threadID = threadEntityID()
            val currentUser = ChatSDK.currentUser()

            // Set the thread
            var thread = ChatSDK.db().fetchThreadWithEntityID(threadID)
            if (thread == null) {
                thread = ChatSDK.db().createEntity(Thread::class.java)
                thread.entityID = threadID
                thread.type = ThreadType.Private1to1
                thread.creationDate = Date()

                // The thread ID will always be the ID of the other user
                val otherUser = ChatSDK.db().fetchOrCreateEntityWithEntityID(User::class.java, threadID)
                ChatSDK.core().userOn(otherUser).subscribe(ChatSDK.events())
                if (currentUser.entityID == threadID) {
                    thread.creator = currentUser
                } else {
                    thread.creator = otherUser
                }
                thread.addUsers(currentUser, otherUser)
            }
            thread
        }
    }

    fun isReadExtension(): Boolean {
        return message.hasExtension(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace)
    }

    fun isChatState(): Boolean {
        return message.hasExtension(ChatStateExtension.NAMESPACE)
    }

    fun isDeliveryReceipt(): Boolean {
        return message.hasExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE)
    }

    fun isDeliveryRequest(): Boolean {
        return message.hasExtension(XMPPDefines.Request, DeliveryReceipt.NAMESPACE);
    }

    fun chatStateExtension(): ChatStateExtension? {
        val element = message.getExtension(ChatStateExtension.NAMESPACE)
        return if (element is ChatStateExtension) {
            element
        } else null
    }

    open fun deliveryReceipt(): DeliveryReceipt? {
        val element: ExtensionElement = message.getExtension(DeliveryReceipt.NAMESPACE)
        if (element is DeliveryReceipt) {
            return element
        }
        return null
    }

    open fun delayInformation(): DelayInformation? {
        val element = message.getExtension<ExtensionElement>(DelayInformation.ELEMENT, DelayInformation.NAMESPACE)
        if (element is DelayInformation) {
            return element
        } else return null
    }

    open fun date(): Date {
        val delay = delayInformation()?.stamp ?: Date()
        return XMPPManager.shared().serverToClientTime(delay);
    }

    open fun delayExtra(): StandardExtensionElement? {
        val element = message.getExtension<ExtensionElement>(XMPPDefines.Extras, XMPPDefines.DelayNamespace)
        if (element is StandardExtensionElement) {
            return element
        } else return null
    }

    open fun body(): String? {
        if (message is Message) {
            if (message.body != null && message.body.isNotEmpty()) {
                return message.body
            }
        }
        return null
    }

    open fun debug() {
        Logger.debug(prettyXML())
    }

    open fun prettyXML(): String {
        return XML.prettyFormatXml(message.toXML(""))
    }

//    open fun fromIds(): List<String> {
//        return message.fromJIDS()
//    }

}

//fun Stanza.fromJIDS(): List<String> {
//    var ids = mutableListOf<String>()
//    val addressesElement = getExtension<MultipleAddresses>(MultipleAddresses.ELEMENT, MultipleAddresses.NAMESPACE)
//    if (addressesElement != null) {
//        val addresses = addressesElement.getAddressesOfType(MultipleAddresses.Type.ofrom)
//        for (address in addresses) {
//            ids.add(address.jid.asBareJid().toString())
//        }
//    } else {
//        ids.add(from.asBareJid().toString())
//    }
//    return ids
//}

fun Stanza.addressJIDs(): List<String> {
    var ids = mutableListOf<String>()
    val addressesElement = getExtension<MultipleAddresses>(MultipleAddresses.ELEMENT, MultipleAddresses.NAMESPACE)
    if (addressesElement != null) {
        val addresses = addressesElement.getAddressesOfType(MultipleAddresses.Type.ofrom)
        for (address in addresses) {
            ids.add(address.jid.asBareJid().toString())
        }
    }
    return ids
}

fun Stanza.extIsOneToOneMessage(): Boolean {
    if (getType() == Message.Type.groupchat || hasExtension(XMPPDefines.MUCUserNamespace)) {
        return false
    }
    return true
}

fun Stanza.getType(): Message.Type {
    if (this is Message) {
        return this.type
    }
    return Message.Type.normal
}


fun Stanza.extFrom(): String? {
    var fromJID: String?
    if (extIsOneToOneMessage()) {
        fromJID = from.asBareJid().toString()
    } else {
        fromJID = delayInformation()?.from
        if (fromJID == null) {
            val jids = addressJIDs()
            if (jids.size >= 1) {
                fromJID = jids[0]
            }
            if (jids.size > 1) {
                Logger.debug("Something went wrong")
            }
        }
        if (fromJID == null) {
            fromJID = XMPPManager.shared().mucManager.userJID(from)
        }
    }
    fromJID = JidCreate.bareFrom(fromJID).toString()
    return fromJID;
}

fun Stanza.delayInformation(): DelayInformation? {
    val element = getExtension<ExtensionElement>(DelayInformation.ELEMENT, DelayInformation.NAMESPACE)
    if (element is DelayInformation) {
        return element
    } else return null
}

fun Stanza.date(): Date {
    val delay = delayInformation()?.stamp ?: Date()
    return XMPPManager.shared().serverToClientTime(delay);
}

