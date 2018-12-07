package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Key;

import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class ConfirmUsernameActivity extends AppCompatActivity {

    private String username;
    private String stageName;
    private String presentedStageName;
    private String city;
    private TextView confirmUsername;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmusername);

        //Retrieve the data from the last activity
        Intent i = getIntent();
        username = (String) i.getSerializableExtra(Keys.Username);
        stageName = (String) i.getSerializableExtra(Keys.StageName);
        presentedStageName = (String) i.getSerializableExtra(Keys.PresentedStageName);
        city = (String) i.getSerializableExtra(Keys.City);

        //The confirmUsername is the same as the presentedStageName, it just needs to be written on two lines.
        confirmUsername = (TextView) findViewById(R.id.confirm_username);
        confirmUsername.setText(username + "\n" + city);
    }
    public void continue_click(View v) {

        //If the user clicks this we start the main activity and write their stage name, city and presented stage name to firebase.
        ChatSDK.currentUser().setMetaString(Keys.PresentedStageName, presentedStageName);
        ChatSDK.currentUser().setMetaString(Keys.StageName, stageName);
        ChatSDK.currentUser().setMetaString(Keys.City, city);
        ChatSDK.core().pushUser().subscribe(new Action() {
            @Override
            public void run() throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });

        ChatSDK.ui().startMainActivity(ConfirmUsernameActivity.this);
    }
    public void back_click(View v) {
        Intent i = new Intent (ConfirmUsernameActivity.this, GossipGirlUsernameActivity.class);
        startActivity(i);
    }
}
