package com.braunster.chatsdk.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.listeners.AuthListener;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends ActionBarActivity{

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private ProgressDialog progressDialog;

    public void authenticate(AuthListener listener){
        BNetworkManager.sharedManager().getNetworkAdapter().checkUserAuthenticatedWithCallback(listener);
    }

    void createAndOpenThreadWithUsers(String name, BUser...users){
        BNetworkManager.sharedManager().getNetworkAdapter().createThreadWithUsers(name, new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object>() {

            BThread thread = null;

            @Override
            public boolean onMainFinised(BThread bThread, Object o) {
                if (o != null)
                {
                    Toast.makeText(BaseActivity.this, "Failed to start chat.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (DEBUG) Log.d(TAG, "New thread is created.");

                thread = bThread;

                return false;
            }

            @Override
            public boolean onItem(BUser item) {
                return false;
            }

            @Override
            public void onDone() {
                Log.d(TAG, "On done.");
                if (thread != null)
                {
                    Log.d(TAG, "Stating chat for thread.");
                    startChatActivityForID(thread.getId());
                }
                else if (DEBUG) Log.e(TAG, "thread added is null");
            }

            @Override
            public void onItemError(BUser user, Object o) {
                if (DEBUG) Log.d(TAG, "Failed to add user to thread, User name: " +user.getName());
            }
        }, users);
    }

    void startChatActivityForID(long id){
        Intent intent = new Intent(BaseActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.THREAD_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    protected void showProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void showOrUpdateProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        } else progressDialog.setMessage(message);
    }

    protected void dismissProgDialog(){
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
            // For handling orientation changed.
            e.printStackTrace();
        }
    }

    Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp){
        if (DEBUG) Log.v(TAG, "scaleImage, Size: " + boundBoxInDp);
        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return scaledBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgDialog();
    }
}
