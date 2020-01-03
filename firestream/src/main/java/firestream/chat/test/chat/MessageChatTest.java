package firestream.chat.test.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import firestream.chat.interfaces.IChat;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Message;
import firestream.chat.message.Sendable;
import firestream.chat.message.TextMessage;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.types.SendableType;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class MessageChatTest extends Test {

    public MessageChatTest() {
        super("MessageChat");
    }

    @Override
    public Observable<Result> run() {
        return Observable.create(emitter -> {
            manage(emitter);

            // Modify the chat
            List<IChat> chats = Fire.Stream.getChats();

            if (chats.size() == 0) {
                failure("Chat doesn't exist");
            } else {
                IChat chat = chats.get(0);

                ArrayList<Message> messages = new ArrayList<>();
                ArrayList<DeliveryReceipt> receipts = new ArrayList<>();

                dm.add(chat.getSendableEvents().getMessages().pastAndNewEvents().subscribe(message -> {
                    messages.add(message);
                }, this));

                dm.add(chat.getSendableEvents().getDeliveryReceipts().pastAndNewEvents().subscribe(deliveryReceipt -> {
                    receipts.add(deliveryReceipt);
                }, this));

                // Send a message
                dm.add(chat.send(message()).subscribe(() -> {
                    // The chat should not yet contain the message - messages are only received via events
                    if(chat.getSendables(SendableType.message()).size() != 0) {
                        failure("Chat should contain no messages so far");
                    }
                }, this));

                dm.add(Completable.timer(2, TimeUnit.SECONDS).subscribe(() -> {
                    // Check that the chat now has the message
                    List<Sendable> sendables = chat.getSendables(SendableType.message());
                    if (sendables.size() != 0) {
                        TextMessage message = TextMessage.fromSendable(sendables.get(0));
                        if (!message.getText().equals(messageText())) {
                            failure("Message text incorrect");
                        }
                    } else {
                        failure("Chat doesn't contain message");
                    }

                    if (messages.size() != 0) {
                        TextMessage message = TextMessage.fromSendable(messages.get(0));
                        if (!message.getText().equals(messageText())) {
                            failure("Message text incorrect");
                        }
                    } else {
                        failure("Chat doesn't contain message");
                    }

                    complete();
                }));

            }
        });
    }

    public static Message message() {
        return new TextMessage(messageText());
    }

    public static String messageText() {
        return "Test";
    }

}
