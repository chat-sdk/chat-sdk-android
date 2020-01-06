package firestream.chat.test.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import firestream.chat.interfaces.IChat;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Message;
import firestream.chat.message.Sendable;
import firestream.chat.message.TextMessage;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.SendableType;
import firestream.chat.types.TypingStateType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MessageChatTest extends Test {

    public MessageChatTest() {
        super("MessageChat");
    }

    /**
     * Test:
     * -
     */

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
                ArrayList<TypingState> typingStates = new ArrayList<>();

                dm.add(chat.getSendableEvents().getMessages().pastAndNewEvents().subscribe(message -> {
                    messages.add(message);
                }, this));

                dm.add(chat.getSendableEvents().getDeliveryReceipts().pastAndNewEvents().subscribe(deliveryReceipt -> {
                    receipts.add(deliveryReceipt);
                }, this));

                dm.add(chat.getSendableEvents().getTypingStates().pastAndNewEvents().subscribe(typingState -> {
                    typingStates.add(typingState);
                }, this));

                // Send a message
                dm.add(chat.send(message()).doOnComplete(() -> {
                    // The chat should not yet contain the message - messages are only received via events
                    if(chat.getSendables(SendableType.message()).size() != 1) {
                        failure("Message not in sendables when it should be");
                    } else {
                        TextMessage message = TextMessage.fromSendable(chat.getSendables(SendableType.message()).get(0));
                        if (!message.getText().equals(messageText())) {
                            failure("Message text mismatch");
                        }
                    }
                }).concatWith(chat.sendTypingIndicator(TypingStateType.typing()).doOnComplete(() -> {
                    if(chat.getSendables(SendableType.typingState()).size() != 1) {
                        failure("Typing state not in sendables when it should be");
                    } else {
                        TypingState state = TypingState.fromSendable((chat.getSendables(SendableType.typingState()).get(0)));
                        if (!state.getTypingStateType().equals(TypingStateType.typing())) {
                            failure("Typing state type mismatch");
                        }
                    }
                })).concatWith(chat.sendDeliveryReceipt(DeliveryReceiptType.received(), messageReceiptId()).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        if(chat.getSendables(SendableType.deliveryReceipt()).size() != 1) {
                            failure("delivery receipt not in sendables when it should be");
                        } else {
                            DeliveryReceipt receipt = DeliveryReceipt.fromSendable((chat.getSendables(SendableType.deliveryReceipt()).get(0)));
                            if (!receipt.getDeliveryReceiptType().equals(DeliveryReceiptType.received())) {
                                failure("Delivery receipt type mismatch");
                            }
                            if (!receipt.getMessageId().equals(messageReceiptId())) {
                                failure("Delivery receipt message id incorrect");
                            }
                        }
                    }
                })).subscribe(() -> {}, this));

                dm.add(Completable.timer(5, TimeUnit.SECONDS).subscribe(() -> {
                    // Check that the chat now has the message

                    if (messages.size() != 0) {
                        TextMessage message = TextMessage.fromSendable(messages.get(0));
                        if (!message.getText().equals(messageText())) {
                            failure("Message text incorrect");
                        }
                    } else {
                        failure("Chat doesn't contain message");
                    }
                    if (receipts.size() != 0) {
                        DeliveryReceipt receipt = DeliveryReceipt.fromSendable(receipts.get(0));
                        if (!receipt.getDeliveryReceiptType().equals(DeliveryReceiptType.received())) {
                            failure("Delivery receipt type incorrect");
                        }
                        if (!receipt.getMessageId().equals(messageReceiptId())) {
                            failure("Delivery receipt message id incorrect");
                        }
                    } else {
                        failure("Chat doesn't contain delivery receipt");
                    }
                    if (typingStates.size() != 0) {
                        TypingState state = TypingState.fromSendable(typingStates.get(0));
                        if (!state.getTypingStateType().equals(TypingStateType.typing())) {
                            failure("Typing state type incorrect");
                        }
                    } else {
                        failure("Chat doesn't contain typing state");
                    }

                    // Send 10 messages
                    ArrayList<Completable> completables = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        completables.add(chat.sendMessageWithText(i + ""));
                    }

                    dm.add(Completable.concat(completables).subscribe(() -> {

                        // The messages should have been delivered by now
                        // Make a query to get all but the first and last messages in order
                        List<Sendable> sendables = chat.getSendables();
                        if (sendables.size() != 13) {
                            failure("There should be 13 messages and there are not");
                            complete();
                        } else {

                            DateFormat format = new SimpleDateFormat("yyyy", Locale.ENGLISH);

                            dm.add(chat.loadMoreMessages(new Date(0), format.parse("3000")).subscribe(sendablesAll -> {

                                Sendable allFirst = sendablesAll.get(0);
                                Sendable allSecond = sendablesAll.get(1);
                                Sendable allLast = sendablesAll.get(sendablesAll.size() - 1);

                                // Check first and last messages
                                if (!allFirst.id.equals(sendables.get(0).id)) {
                                    failure("All first message incorrect");
                                }
                                if (!allLast.id.equals(sendables.get(sendables.size()-1).id)) {
                                    failure("All last message incorrect");
                                }
                                if (sendablesAll.size() != sendables.size()) {
                                    failure("All size mismatch");
                                }

                                int indexOfFirst = 0;
                                int indexOfLast = sendablesAll.size()-1;
                                int limit = 5;

                                Sendable fromSendable = sendablesAll.get(indexOfFirst);
                                Sendable toSendable = sendablesAll.get(indexOfLast);

                                // Get the date of the second and penultimate
                                Date from = fromSendable.date;
                                Date to = toSendable.date;

                                ArrayList<Completable> completables1 = new ArrayList<>();


                                // There is a timing issue here in that the date of the sendable
                                // will actually be a Firebase prediction rather than the actual time recorded on the server
                                completables1.add(chat.loadMoreMessages(from, to).doOnSuccess(sendablesFromTo -> {
                                    if (sendablesFromTo.size() != 12) {
                                        failure("From/To Sendable size incorrect");
                                    }

                                    Sendable first = sendablesFromTo.get(0);
                                    Sendable second = sendablesFromTo.get(1);
                                    Sendable last = sendablesFromTo.get(sendablesFromTo.size() - 1);

                                    // First message should be the same as the second overall message
                                    if (!first.id.equals(allSecond.id)) {
                                        failure("From/To First message incorrect");
                                    }
                                    if (!last.id.equals(toSendable.id)) {
                                        failure("From/To Last message incorrect");
                                    }
                                    // Check the first message is on or after the from date
                                    if (first.date.getTime() <= from.getTime()) {
                                        failure("From/To First message is before from time");
                                    }
                                    if (last.date.getTime() > to.getTime()) {
                                        failure("From/To Last message is after to time");
                                    }
                                    if (second.date.getTime() < first.date.getTime()) {
                                        failure("From/To Messages order incorrect");
                                    }
                                }).ignoreElement().concatWith(chat.loadMoreMessagesFrom(from, limit).doOnSuccess(sendablesFrom -> {
                                    if (sendablesFrom.size() != limit) {
                                        failure("From Sendable size incorrect");
                                    }

                                    Sendable first = sendablesFrom.get(0);
                                    Sendable second = sendablesFrom.get(1);
                                    Sendable last = sendablesFrom.get(sendablesFrom.size() - 1);

                                    if (!allSecond.id.equals(first.id)) {
                                        failure("From First message incorrect");
                                    }
                                    if (!sendablesAll.get(limit).id.equals(last.id)) {
                                        failure("From Last message incorrect");
                                    }

                                    // Check the first message is on or after the from date
                                    if (first.date.getTime() <= from.getTime()) {
                                        failure("From First message is before from time");
                                    }
                                    if (second.date.getTime() < first.date.getTime()) {
                                        failure("From Messages order incorrect");
                                    }

                                }).ignoreElement().concatWith(chat.loadMoreMessagesTo(to, limit).doOnSuccess(sendablesTo -> {

                                    Sendable first = sendablesTo.get(0);
                                    Sendable second = sendablesTo.get(1);
                                    Sendable last = sendablesTo.get(sendablesTo.size() - 1);

                                    if (sendablesTo.size() != limit) {
                                        failure("To Sendable size incorrect");
                                    }
                                    if (!first.id.equals(sendablesAll.get(sendablesAll.size() - limit).id)) {
                                        failure("To First message incorrect");
                                    }
                                    if (!toSendable.id.equals(last.id)) {
                                        failure("To Last message incorrect");
                                    }
                                    if (last.date.getTime() > to.getTime()) {
                                        failure("To Last message is after to time");
                                    }
                                    if (second.date.getTime() < first.date.getTime()) {
                                        failure("To Messages order incorrect");
                                    }


                                }).ignoreElement())));

                                dm.add(Completable.concat(completables1).subscribe(() -> {

                                    //
                                    complete();

                                }, this));

                            }, this));
                        }
                    }, this));

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

    public static String messageReceiptId() {
        return "XXX";
    }

}
