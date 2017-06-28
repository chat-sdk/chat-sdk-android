package co.chatsdk.ui.UiHelpers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kykrueger on 2016-11-06.
 */

/***
 * Convenience class for starting activities built into the Chat SDK.
 * Replaces god class UIHelper
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
