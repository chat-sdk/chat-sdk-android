package com.braunster.chatsdk.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BLinkedContactDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BNetworkManager;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.concurrent.Callable;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class BaseFragment extends DialogFragment implements BaseFragmentInterface {

    // TODO refresh on background method.
    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    SuperActivityToast superActivityToast;

    View mainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
//        mainView = inflater.inflate(resourceID, null);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        loadData();
        // TODO handle network call more efficiantly check time intervals and mabey listen to data coming.
    }

    @Override
    public void refresh() {
        loadData();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void refreshOnBackground() {
        loadDataOnBackground();
    }

    @Override
    public void loadDataOnBackground() {

    }

    @Override
    public void initViews() {

    }


    void initToast(){
        superActivityToast = new SuperActivityToast(getActivity());
        superActivityToast.setDuration(SuperToast.Duration.MEDIUM);
        superActivityToast.setBackground(SuperToast.Background.BLUE);
        superActivityToast.setTextColor(Color.WHITE);
        superActivityToast.setAnimations(SuperToast.Animations.FLYIN);
        superActivityToast.setTouchToDismiss(true);
    }

    void showToast(String text){
        superActivityToast.setText(text);
        superActivityToast.show();
    }

    void startChatActivityForID(long id){
        if (getActivity() == null);
            Log.e(TAG, "Activitu is nyll!!!!!!");
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.THREAD_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    void createAndOpenThreadWithUsers(String name, BUser...users){
        createAndOpenThreadWithUsers(name, null, users);
    }

    void createAndOpenThreadWithUsers(String name, final DoneListener doneListener, BUser...users){
        BNetworkManager.sharedManager().getNetworkAdapter().createThreadWithUsers(name, new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object>() {

            BThread thread = null;

            @Override
            public boolean onMainFinised(BThread bThread, Object o) {
                if (o != null)
                {
                    Toast.makeText(getActivity(), "Failed to start chat.", Toast.LENGTH_SHORT).show();
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
                    startChatActivityForID(thread.getId());
                    if (doneListener != null)
                        doneListener.onDone();
                }
            }

            @Override
            public void onItemError(BUser user, Object o) {
                if (DEBUG) Log.d(TAG, "Failed to add user to thread, User name: " +user.getName());
            }
        }, users);
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
                    Toast.makeText(getActivity(), "Thread is deleted.", Toast.LENGTH_SHORT).show();
                    refreshOnBackground();
                }

                @Override
                public void onDoneWithError() {
                    Toast.makeText(getActivity(), "Unable to delete thread.", Toast.LENGTH_SHORT).show();
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
            BLinkedContact linkedContact = DaoCore.fetchEntityWithProperty(BLinkedContact.class, BLinkedContactDao.Properties.EntityID, userID);
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


    interface DoneListener{
        public void onDone();
    }
}

interface BaseFragmentInterface{
    public void refresh();

    public void refreshOnBackground();

    public void loadData();

    public void loadDataOnBackground();

    public void initViews();
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
