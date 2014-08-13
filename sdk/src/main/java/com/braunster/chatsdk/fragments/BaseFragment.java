package com.braunster.chatsdk.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.Utils.UiUtils;
import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BLinkedContactDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BNetworkManager;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.concurrent.Callable;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class BaseFragment extends DialogFragment implements BaseFragmentInterface{

    // TODO refresh on background method.
    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private ProgressDialog progressDialog;

    SuperActivityToast superActivityToast;

    View mainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void refresh() {
        loadData();
    }

    @Override
    public void refreshOnBackground() {
        loadDataOnBackground();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void loadDataOnBackground() {

    }

    @Override
    public void clearData() {

    }

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.*/
    public void setupTouchUIToDismissKeyboard(View view) {
        UiUtils.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UiUtils.hideSoftKeyboard(getActivity());
                return false;
            }
        });
    }

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        UiUtils.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UiUtils.hideSoftKeyboard(getActivity());
                return false;
            }
        }, exceptIDs);
    }

    @Override
    public void initViews() {

    }

    /** Init all the toast object.*/
    void initToast(){
        superActivityToast = new SuperActivityToast(getActivity());
        superActivityToast.setDuration(SuperToast.Duration.MEDIUM);
        superActivityToast.setBackground(SuperToast.Background.BLUE);
        superActivityToast.setTextColor(Color.WHITE);
        superActivityToast.setAnimations(SuperToast.Animations.FLYIN);
        superActivityToast.setTouchToDismiss(true);
    }

    /** Show a toast.*/
    void showToast(String text)
    {
        if (superActivityToast == null)
            return;

        superActivityToast.setText(text);
        superActivityToast.show();
    }

    /** Start the chat activity for given thread id.*/
    void startChatActivityForID(long id){
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.THREAD_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    /** Create or fetch chat for users, Opens the chat when done.*/
    void createAndOpenThreadWithUsers(String name, BUser...users){
        createAndOpenThreadWithUsers(name, null, true, users);
    }
    /** Create or fetch chat for users, Opens the chat when done.*/
    void createAndOpenThreadWithUsers(String name, final CompletionListenerWithData doneListener, BUser...users){
        createAndOpenThreadWithUsers(name, doneListener, true, users);
    }
    /** Create or fetch chat for users. Opens the chat if wanted.*/
    void createAndOpenThreadWithUsers(String name, final CompletionListenerWithData doneListener, final boolean openChatWhenDone, BUser...users){
        BNetworkManager.sharedManager().getNetworkAdapter().createThreadWithUsers(name, new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object>() {

            BThread thread = null;

            @Override
            public boolean onMainFinised(BThread bThread, Object o) {
                if (o != null)
                {
                    showToast("Failed to start chat.");
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
                    if (openChatWhenDone)
                        startChatActivityForID(thread.getId());

                    if (doneListener != null)
                        doneListener.onDone(thread);
                }
            }

            @Override
            public void onItemError(BUser user, Object o) {
                if (DEBUG) Log.d(TAG, "Failed to add user to thread, User name: " +user.getName());
            }
        }, users);
    }



    protected void showProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void showOrUpdateProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(getActivity());
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

    void showAlertDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title if not null
        if (title != null && !title.equals(""))
            alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(alert)
                .setCancelable(false)
                .setPositiveButton(p, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (pos != null)
                            try {
                                pos.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(n, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        if (neg != null)
                            try {
                                neg.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    class DeleteThread implements Callable{

        private String threadID;
        public DeleteThread(String threadID){
            this.threadID = threadID;
        }

        @Override
        public Object call() throws Exception {
            BNetworkManager.sharedManager().getNetworkAdapter().deleteThreadWithEntityID(threadID, new CompletionListener() {
                @Override
                public void onDone() {
                    showToast("Thread is deleted.");
                    refreshOnBackground();
                }

                @Override
                public void onDoneWithError() {
                    showToast("Unable to delete thread.");
                }
            });
            return null;
        }
    }

    class DeleteContact implements Callable{

        private String userID;

        public DeleteContact(String userID){
            this.userID = userID;
        }

        @Override
        public Object call() throws Exception {
            BLinkedContact linkedContact = DaoCore.<BLinkedContact>fetchEntityWithProperty(BLinkedContact.class, BLinkedContactDao.Properties.EntityID, userID);
            DaoCore.deleteEntity(linkedContact);
            loadData();
            return null;
        }
    }

    Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp){
        if (boundBoxInDp == 0)
            return null;

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
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return bitmap;
    }
}

interface BaseFragmentInterface{
    public void refresh();

    public void refreshOnBackground();

    public void loadData();

    public void loadDataOnBackground();

    public void initViews();

    public void clearData();
}
/*


    Bitmap setScaledPicToView(final String path, final int size, final ImageView imageView){
        imageView.setImageBitmap(null);

        // Reducing the size of the bitmap.
        Bitmap bitmap = DecodeUtils.decodeSampledBitmapFromFile(path, size, size);
        // Scaling to the needed size
        bitmap = scaleImage(bitmap, size);

        imageView.setImageBitmap(bitmap);

        return bitmap;
    }

    void setScaledPicToView(final Bitmap bitmap, final int size, final ImageView imageView){
        if (bitmap != null)
        {
            imageView.setImageBitmap(null);
            imageView.setImageBitmap(scaleImage(bitmap, size));
        }
    }*/
