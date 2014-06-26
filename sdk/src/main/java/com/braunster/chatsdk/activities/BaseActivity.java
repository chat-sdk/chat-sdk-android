package com.braunster.chatsdk.activities;

import android.support.v7.app.ActionBarActivity;

import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.network.BFirebaseNetworkAdapter;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends ActionBarActivity{

  /*  public void setNetworkAdapterAndSync(CompletionListener completionListener){
        //region TestAdapter
//        TestNetworkAdapter testNetworkAdapter = new TestNetworkAdapter();
//
//        BNetworkManager.sharedManager().setNetworkAdapter(testNetworkAdapter);
//        BNetworkManager.sharedManager().syncWithProgress(completionListener);
        //endregion -

        //region FirebaseAdapter
        BFirebaseNetworkAdapter firebaseNetworkAdapter = new BFirebaseNetworkAdapter();
        BNetworkManager.sharedManager().setNetworkAdapter(firebaseNetworkAdapter);
        BNetworkManager.sharedManager().syncWithProgress(completionListener);
        //endregion
    }

    public void setNetworkAdapterAndSync(){
        //region TestAdapter
//        TestNetworkAdapter testNetworkAdapter = new TestNetworkAdapter();
//
//        BNetworkManager.sharedManager().setNetworkAdapter(testNetworkAdapter);
//        BNetworkManager.sharedManager().syncWithProgress(new CompletionListener() {
//            @Override
//            public void onDone() {
//
//            }
//
//            @Override
//            public void onDoneWithError() {
//
//            }
//        });
        //endregion

        //region FirebaseAdapter
        BFirebaseNetworkAdapter firebaseNetworkAdapter = new BFirebaseNetworkAdapter();
        BNetworkManager.sharedManager().setNetworkAdapter(firebaseNetworkAdapter);
        BNetworkManager.sharedManager().getNetworkAdapter().(new CompletionListener() {
            @Override
            public void onDone() {

            }

            @Override
            public void onDoneWithError() {

            }
        });
        //endregion
    }*/

    public void authenticate(CompletionListenerWithDataAndError<BUser, Object> listener){
        BNetworkManager.sharedManager().getNetworkAdapter().checkUserAuthenticatedWithCallback(listener);
    }
}
