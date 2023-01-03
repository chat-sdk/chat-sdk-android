package firestream.chat.test.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import io.reactivex.ObservableOnSubscribe;
import sdk.guru.common.RX;

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
        return Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            manage(emitter);

            // Modify the chat
            List<IChat> chats = Fire.stream().getChats();

            if (chats.size() == 0) {
                failure("Chat doesn't exist");
            } else {
                IChat chat = chats.get(0);

                dm.add(chat.getSendableEvents().getErrors().subscribe(throwable -> {
                    //
                    MessageChatTest.this.accept(throwable);
                }));

                ArrayList<Message> messages = new ArrayList<>();
                ArrayList<DeliveryReceipt> receipts = new ArrayList<>();
                ArrayList<TypingState> typingStates = new ArrayList<>();

                dm.add(chat.getSendableEvents().getMessages().pastAndNewEvents().subscribe(event -> {
                    messages.add(event.get());
                }, this));

                dm.add(chat.getSendableEvents().getDeliveryReceipts().pastAndNewEvents().subscribe(event -> {
                    receipts.add(event.get());
                }, this));

                dm.add(chat.getSendableEvents().getTypingStates().pastAndNewEvents().subscribe(event -> {
                    typingStates.add(event.get());
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
                })
                        /*
                        .concatWith(chat.sendTypingIndicator(TypingStateType.typing()).doOnComplete(() -> {
                    if(chat.getSendables(SendableType.typingState()).size() != 1) {
                        failure("Typing state not in sendables when it should be");
                    } else {
                        TypingState state = TypingState.fromSendable((chat.getSendables(SendableType.typingState()).get(0)));
                        if (!state.getTypingStateType().equals(TypingStateType.typing())) {
                            failure("Typing state type mismatch");
                        }
                    }
                })).concatWith(chat.sendDeliveryReceipt(DeliveryReceiptType.received(), messageReceiptId()).doOnComplete(() -> {
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
                })) */
                .subscribe(() -> {

                }, this));
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

                            dm.add(chat.loadMoreMessages(new Date(0), format.parse("3000"), true).subscribe(sendablesAll -> {

                                Sendable allFirst = sendablesAll.get(0);
                                Sendable allSecond = sendablesAll.get(1);
                                Sendable allLast = sendablesAll.get(sendablesAll.size() - 1);

                                // Check first and last messages
                                if (!allFirst.equals(sendables.get(0))) {
                                    failure("All first message incorrect");
                                }
                                if (!allLast.equals(sendables.get(sendables.size()-1))) {
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
                                Date from = fromSendable.getDate();
                                Date to = toSendable.getDate();

                                ArrayList<Completable> completables1 = new ArrayList<>();


                                // There type a timing issue here in that the date of the sendable
                                // will actually be a Firebase prediction rather than the actual time recorded on the server
                                completables1.add(chat.loadMoreMessages(from, to, true).doOnSuccess(sendablesFromTo -> {
                                    if (sendablesFromTo.size() != 12) {
                                        failure("From/To Sendable size incorrect");
                                    }

                                    Sendable first = sendablesFromTo.get(0);
                                    Sendable second = sendablesFromTo.get(1);
                                    Sendable last = sendablesFromTo.get(sendablesFromTo.size() - 1);

                                    // First message should be the same as the second overall message
                                    if (!first.equals(allSecond)) {
                                        failure("From/To First message incorrect");
                                    }
                                    if (!last.equals(toSendable)) {
                                        failure("From/To Last message incorrect");
                                    }
                                    // Check the first message type on or after the from date
                                    if (first.getDate().getTime() <= from.getTime()) {
                                        failure("From/To First message type before from time");
                                    }
                                    if (last.getDate().getTime() > to.getTime()) {
                                        failure("From/To Last message type after to time");
                                    }
                                    if (second.getDate().getTime() < first.getDate().getTime()) {
                                        failure("From/To Messages order incorrect");
                                    }
                                }).ignoreElement().concatWith(chat.loadMoreMessagesFrom(from, limit, true).doOnSuccess(sendablesFrom -> {
                                    if (sendablesFrom.size() != limit) {
                                        failure("From Sendable size incorrect");
                                    }

                                    Sendable first = sendablesFrom.get(0);
                                    Sendable second = sendablesFrom.get(1);
                                    Sendable last = sendablesFrom.get(sendablesFrom.size() - 1);

                                    if (!allSecond.equals(first)) {
                                        failure("From First message incorrect");
                                    }
                                    if (!sendablesAll.get(limit).equals(last)) {
                                        failure("From Last message incorrect");
                                    }

                                    // Check the first message type on or after the from date
                                    if (first.getDate().getTime() <= from.getTime()) {
                                        failure("From First message type before from time");
                                    }
                                    if (second.getDate().getTime() < first.getDate().getTime()) {
                                        failure("From Messages order incorrect");
                                    }

                                }).ignoreElement().concatWith(chat.loadMoreMessagesTo(to, limit, true).doOnSuccess(sendablesTo -> {

                                    Sendable first = sendablesTo.get(0);
                                    Sendable second = sendablesTo.get(1);
                                    Sendable last = sendablesTo.get(sendablesTo.size() - 1);

                                    if (sendablesTo.size() != limit) {
                                        failure("To Sendable size incorrect");
                                    }
                                    if (!first.equals(sendablesAll.get(sendablesAll.size() - limit))) {
                                        failure("To First message incorrect");
                                    }
                                    if (!toSendable.equals(last)) {
                                        failure("To Last message incorrect");
                                    }
                                    if (last.getDate().getTime() > to.getTime()) {
                                        failure("To Last message type after to time");
                                    }
                                    if (second.getDate().getTime() < first.getDate().getTime()) {
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
        }).subscribeOn(RX.io());
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
