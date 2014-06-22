package com.braunster.chatsdk.activities;

import android.support.v7.app.ActionBarActivity;

import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.network.BFirebaseNetworkAdapter;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.tamplate.TestNetworkAdapter;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends ActionBarActivity{

    public void setNetworkAdapterAndSync(CompletionListener completionListener){
        //region TestAdapter
//        TestNetworkAdapter testNetworkAdapter = new TestNetworkAdapter();
//
//        BNetworkManager.getInstance().setNetworkAdapter(testNetworkAdapter);
//        BNetworkManager.getInstance().syncWithProgress(completionListener);
        //endregion -

        //region FirebaseAdapter
        BFirebaseNetworkAdapter firebaseNetworkAdapter = new BFirebaseNetworkAdapter();
        BNetworkManager.getInstance().setNetworkAdapter(firebaseNetworkAdapter);
        BNetworkManager.getInstance().syncWithProgress(completionListener);
        //endregion
    }

    public void setNetworkAdapterAndSync(){
        //region TestAdapter
//        TestNetworkAdapter testNetworkAdapter = new TestNetworkAdapter();
//
//        BNetworkManager.getInstance().setNetworkAdapter(testNetworkAdapter);
//        BNetworkManager.getInstance().syncWithProgress(new CompletionListener() {
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
        BNetworkManager.getInstance().setNetworkAdapter(firebaseNetworkAdapter);
        BNetworkManager.getInstance().syncWithProgress(new CompletionListener() {
            @Override
            public void onDone() {

            }

            @Override
            public void onDoneWithError() {

            }
        });
        //endregion
    }
}
