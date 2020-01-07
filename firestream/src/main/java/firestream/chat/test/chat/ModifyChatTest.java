package firestream.chat.test.chat;

import java.util.ArrayList;
import java.util.HashMap;
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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

public class ModifyChatTest extends Test {

    public ModifyChatTest() {
        super("ModifyChat");
    }

    /**
     * We ge:
     * - We can get the chat that has been created
     * - Name change events correct
     * - Image url change events correct
     * - Custom data change events correct
     * - We can update the name
     * - We can update the image url
     * - We can update the custom data
     * - We can update the user roles
     * - We can add a user
     * - We can remove a user
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

                ArrayList<String> nameEvents = new ArrayList<>();
                ArrayList<String> imageURLEvents = new ArrayList<>();
                ArrayList<HashMap<String, Object>> customDataEvents = new ArrayList<>();
                ArrayList<UserEvent> userEvents = new ArrayList<>();

                ArrayList<User> removedUsers = new ArrayList<>();
                ArrayList<User> addedUsers = new ArrayList<>();

                dm.add(chat.getNameChangeEvents().subscribe(s -> {
                    nameEvents.add(s);
                }, this));

                dm.add(chat.getImageURLChangeEvents().subscribe(s -> {
                    imageURLEvents.add(s);
                }, this));

                dm.add(chat.getCustomDataChangedEvents().subscribe(map -> {
                    customDataEvents.add(map);
                }, this));

                final Disposable userEventsDisposable = chat.getUserEvents().newEvents().subscribe(userEvent -> {
                    if (userEvent.typeIs(EventType.Modified)) {
                        userEvents.add(userEvent);
                    } else {
                        failure("Add or Remove User event when modify expected");
                    }
                }, this);

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

                dm.add(chat.setCustomData(customData()).subscribe(() -> {
                    if (!chat.getCustomData().equals(customData())) {
                        failure("Chat custom data not updated");
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

                dm.add(Completable.timer(4, TimeUnit.SECONDS).subscribe(() -> {

                    // Check the chat isType correct
                    // Check the name matches
                    if (!chat.getName().equals(chatName())) {
                        failure("Name mismatch");
                    }

                    if (!chat.getImageURL().equals(chatImageURL())) {
                        failure("Image URL mismatch");
                    }

                    if (!chat.getCustomData().equals(customData())) {
                        failure("Custom data mismatch");
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

                    if (customDataEvents.size() == 0) {
                        failure("Custom data not set from stream");
                    } else {
                        if (!customDataEvents.get(customDataEvents.size() - 1).equals(customData())) {
                            failure("Custom data from stream incorrect");
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

                    userEventsDisposable.dispose();

                    dm.add(chat.getUserEvents().newEvents().subscribe(userEvent -> {
                        if (userEvent.typeIs(EventType.Added)) {
                            addedUsers.add(userEvent.user);
                        }
                        else if (userEvent.typeIs(EventType.Removed)) {
                            removedUsers.add(userEvent.user);
                        }
                        else {
                            failure("Modify event when added or removed expected");
                        }
                    }, this));

                    // Now try to add one user and remove another user
                    User u1 = usersNotMe().get(0);

                    dm.add(chat.removeUser(u1).subscribe(() -> {
                        RoleType role = chat.getRoleTypeForUser(u1);
                        if (role != null) {
                            failure("User removed but still exists in chat");
                        }
                    }));

                    dm.add(Completable.timer(2 , TimeUnit.SECONDS).subscribe(() -> {
                        if (removedUsers.size() == 0) {
                            failure("User removed event didn't fire");
                        } else {
                            if (!removedUsers.get(0).equals(u1)) {
                                failure("Removed user mismatch");
                            }
                        }

                        dm.add(chat.addUser(false, u1).subscribe(() -> {
                            RoleType role = chat.getRoleTypeForUser(u1);
                            if (!role.equals(u1.roleType)) {
                                failure("Added user has wrong role");
                            }
                        }));

                        dm.add(Completable.timer(2, TimeUnit.SECONDS).subscribe(() -> {
                            if (addedUsers.size() == 0) {
                                failure("User added event didn't fire");
                            } else {
                                if (!addedUsers.get(0).equals(u1)) {
                                    failure("Added user mismatch");
                                }
                            }

                            complete();

                        }));
                    }));

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

    public List<User> usersNotMe() {
        ArrayList<User> users = new ArrayList<>();
        for (User u: users()) {
            if (!u.isMe()) {
                users.add(u);
            }
        }
        return users;
    }

    public static String chatName() {
        return "Test2";
    }

    public static String chatImageURL() {
        return "http://chatsdk.co/wp-content/uploads/2019/03/ic_launcher_big.png";
    }

    public static HashMap<String, Object> customData() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("TestKey3", "TestValuexx");
        data.put("Key4", 88L);

        return data;
    }



}
