package firestream.chat.test.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import firestream.chat.chat.User;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.test.TestScript;
import firestream.chat.types.RoleType;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import sdk.guru.common.RX;

public class CreateChatTest extends Test {

    public CreateChatTest() {
        super("CreateChat");
    }

    /**
     * We ge:
     * - Creating a chat
     * - Name correct
     * - Image URL correct
     * - Custom data correct
     * - Users and roles correct
     */
    @Override
    public Observable<Result> run() {
        return Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            manage(emitter);
            final List<User> users = users();
            dm.add(Fire.stream().createChat(chatName(), chatImageURL(), customData(), users).subscribe(chat -> {
                // Check the name matches
                if (!chat.getName().equals(chatName())) {
                    failure("Name mismatch");
                }

                if (!chat.getImageURL().equals(chatImageURL())) {
                    failure("Image url mismatch");
                }

                // Check the ID type set
                if (chat.getId() == null || chat.getId().isEmpty()) {
                    failure("Chat id not set");
                }

                if (chat.getCustomData() != null) {
                    if (!chat.getCustomData().equals(customData())) {
                        failure("Custom data value mismatch");
                    }
                } else {
                    failure("Custom data type null");
                }

                // Check the users
                for (User user: chat.getUsers()) {
                    for (User u: users) {
                        if (user.equals(u) && !user.isMe()) {
                            if (!user.equalsRoleType(u)) {
                                failure("Role type mismatch");
                            }
                        }
                    }
                    if (user.isMe() && !user.equalsRoleType(RoleType.owner())) {
                        failure("Creator user not owner");
                    }
                }

                complete();

            }, this));
        }).subscribeOn(RX.io());
    }

    public static String chatName() {
        return "Test";
    }

    public static String chatImageURL() {
        return "https://chatsdk.co/wp-content/uploads/2017/01/image_message-407x389.jpg";
    }

    public static HashMap<String, Object> customData() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("TestKey", "TestValue");
        data.put("Key2", 999L);

        return data;
    }

    public static List<User> users() {
        ArrayList<User> users = new ArrayList<>(TestScript.usersNotMe());
        for (User u: users) {
            if (u.equals(TestScript.testUser1())) {
                u.setRoleType(RoleType.watcher());
            }
            if (u.equals(TestScript.testUser2())) {
                u.setRoleType(RoleType.admin());
            }
            if (u.equals(TestScript.testUser3())) {
                u.setRoleType(RoleType.banned());
            }
        }
        return users;
    }
}
