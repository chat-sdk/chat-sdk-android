package firestream.chat.test;

import android.content.Context;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import firestream.chat.Config;
import firestream.chat.chat.User;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.firebase.rx.DisposableMap;
import firestream.chat.namespace.Fire;
import firestream.chat.test.chat.CreateChatTest;
import firestream.chat.test.chat.MessageChatTest;
import firestream.chat.test.chat.ModifyChatTest;
import firestream.chat.test.contact.AddContactTest;
import firestream.chat.test.contact.DeleteContactTest;
import firestream.chat.test.contact.GetContactAddedTest;
import firestream.chat.test.contact.GetContactRemovedTest;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class TestScript {

    DisposableMap dm = new DisposableMap();

    public ArrayList<Test> tests = new ArrayList<>();

    public ArrayList<Result> results = new ArrayList<>();

    public TestScript(Context context, String rootPath) {

        Config config = new Config();
        try {
            config.setRoot(rootPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.deleteMessagesOnReceipt = false;
        config.debugEnabled = true;
        config.database = Config.DatabaseType.Realtime;

        Fire.internal().initialize(context, config);

        tests.add(new AddContactTest());
        tests.add(new GetContactAddedTest());
        tests.add(new DeleteContactTest());
        tests.add(new GetContactRemovedTest());
        tests.add(new CreateChatTest());
        tests.add(new ModifyChatTest());
        tests.add(new MessageChatTest());

        Disposable d = Fire.stream().getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {
                start();
            }
            if (connectionEvent.getType() == ConnectionEvent.Type.WillDisconnect) {
                stop();
            }
        });
    }

    public void start() {

        ArrayList<Observable<Result>> testObservables = new ArrayList<>();
        for (Test test: tests) {
            testObservables.add(test.run());
        }

        dm.add(Observable.concat(testObservables).doOnComplete(() -> {

            String output = "\n";

            for (Result result : results) {
                if (result.isSuccess()) {
                    output += result.test.name + " - success";
                } else {
                    output += result.test.name + " - fail: " + result.errorMessage;
                }
                output += "\n";
            }

            log(output);
        }).subscribe(result -> {
            results.add(result);
        }));
    }

    public void stop() {
        dm.dispose();
        log("Stop");
    }

    public void log(String text) {
        Logger.debug(text);
    }

    public static User testUser1() {
        return new User("6TB34PbMqdU67KHvIuGrekMCIOk2");
    }

    public static User testUser2() {
        return new User("9tJUx1iT5LQ1bC1J952No6sNZjZ2");
    }

    public static User testUser3() {
        return new User("NX6mGPCkR3NcNiJJxBzNrsR2Anz2");
    }

    public static List<User> usersNotMe() {
        ArrayList<User> users = new ArrayList<>();
        for (User u: users()) {
            if (!u.getId().equals(Fire.stream().currentUserId())) {
                users.add(u);
            }
        }
        return users;
    }

    public static ArrayList<User> users() {
        ArrayList<User> users = new ArrayList<>();
        users.add(TestScript.testUser1());
        users.add(TestScript.testUser2());
        users.add(TestScript.testUser3());
        return users;
    }


    public static void run(Context context, String rootPath) {
        new TestScript(context, rootPath);
    }
}
