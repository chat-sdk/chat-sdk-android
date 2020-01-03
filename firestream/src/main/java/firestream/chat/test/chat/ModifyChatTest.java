package firestream.chat.test.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import firestream.chat.interfaces.IChat;
import firestream.chat.chat.User;
import firestream.chat.events.EventType;
import firestream.chat.events.UserEvent;
import firestream.chat.namespace.Fire;
import firestream.chat.test.Result;
import firestream.chat.test.Test;
import firestream.chat.test.TestScript;
import firestream.chat.types.RoleType;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class ModifyChatTest extends Test {

    public ModifyChatTest() {
        super("ModifyChat");
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

                ArrayList<String> nameEvents = new ArrayList<>();
                ArrayList<String> imageURLEvents = new ArrayList<>();
                ArrayList<UserEvent> userEvents = new ArrayList<>();

                dm.add(chat.getNameChangeEvents().subscribe(s -> {
                    nameEvents.add(s);
                }, this));

                dm.add(chat.getNameChangeEvents().subscribe(s -> {
                    imageURLEvents.add(s);
                }, this));

                dm.add(chat.getUserEvents().subscribe(userEvent -> {
                    if (userEvent.type == EventType.Modified) {
                        userEvents.add(userEvent);
                    } else {
                        failure("Add or Remove User event when modify expected");
                    }
                }, this));

                dm.add(chat.setName(chatName()).subscribe(() -> {
                    if (!chat.getName().equals(chatName())) {
                        failure("Chat name not updated");
                    }
                }, this));

                dm.add(chat.setImageURL(chatImageURL()).subscribe(() -> {
                    if (!chat.getImageURL().equals(chatImageURL())) {
                        failure("Chat image URL not updated");
                    }
                }, this));

                for (User u: users()) {
                    if (!u.isMe()) {
                        dm.add(chat.setRole(u, u.roleType).subscribe(() -> {
                            // Check the user's role
                            if (!u.roleType.equals(chat.getRoleTypeForUser(u))) {
                                failure("User role updated not correct");
                            }
                        }, this));
                    }
                }

                dm.add(Completable.timer(2, TimeUnit.SECONDS).subscribe(() -> {

                    // Check the chat is correct
                    // Check the name matches
                    if (!chat.getName().equals(chatName())) {
                        failure("Name mismatch");
                    }

                    if (!chat.getImageURL().equals(chatImageURL())) {
                        failure("Image URL mismatch");
                    }

                    // Check the users
                    for (User user: chat.getUsers()) {
                        for (User u: users()) {
                            if (user.equals(u) && !user.isMe()) {
                                if (!user.roleType.equals(u.roleType)) {
                                    failure("Role type mismatch");
                                }
                            }
                        }
                        if (user.isMe() && !user.roleType.equals(RoleType.owner())) {
                            failure("Creator user not owner");
                        }
                    }

                    if (nameEvents.size() == 0) {
                        failure("Name not set from stream");
                    } else {
                        if (!nameEvents.get(nameEvents.size() - 1).equals(chatName())) {
                            failure("Name from stream incorrect");
                        }
                    }

                    if (imageURLEvents.size() == 0) {
                        failure("ImageURL not set from stream");
                    } else {
                        if (!imageURLEvents.get(imageURLEvents.size() - 1).equals(chatImageURL())) {
                            failure("ImageURL from stream incorrect");
                        }
                    }

                    if (userEvents.size() == 0) {
                        failure("User events not received");
                    } else {
                        for (UserEvent ue: userEvents) {
                            for (User u: users()) {
                                if (ue.user.equals(u) && !ue.user.roleType.equals(u.roleType)) {
                                    failure("Role type not updated correctly");
                                }
                            }
                        }
                    }

                    complete();
                }));

            }

        });
    }

    public static List<User> users() {
        ArrayList<User> users = new ArrayList<>();
        for (User u: CreateChatTest.users()) {
            if (u.equals(TestScript.testUser1())) {
                u.roleType = RoleType.banned();
            }
            if (u.equals(TestScript.testUser2())) {
                u.roleType = RoleType.watcher();
            }
            if (u.equals(TestScript.testUser3())) {
                u.roleType = RoleType.admin();
            }
            users.add(u);
        }
        return users;
    }

    public static String chatName() {
        return "Test2";
    }

    public static String chatImageURL() {
        return "http://chatsdk.co/wp-content/uploads/2019/03/ic_launcher_big.png";
    }

}
