package wanderingdevelopment.tk.sdkbaseui.UiHelpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.facebook.LoginActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by kykrueger on 2016-11-06.
 */

/***
 * Convenience class for starting activities built into the Chat SDK.
 * Replaces god class ChatSDKUiHelper
 */
public class ActivityLauncher {

    private AppCompatActivity activity;

    public ActivityLauncher(AppCompatActivity activity){
        this.activity = activity;
    }

    private void startActivity(Intent intent){
        activity.startActivity(intent);
    }

//    public void loginScreen(){
//        Intent intent = new Intent(activity, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//    }
//
//    public void mainTabbedActivity(){
//        Intent intent = new Intent(activity, MainTabbedActivity.class);
//        startActivity(intent);
//    }
//
//
//    public void searchUsersActivity(){
//        Intent intent = new Intent(activity, SearchForUsersActivity.class);
//        startActivity(intent);
//    }

    public void thread(String id){

    }

    public void contactProfile(String id){

    }

    public void profile(){

    }

}
