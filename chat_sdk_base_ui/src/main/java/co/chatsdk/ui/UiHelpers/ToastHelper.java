package co.chatsdk.ui.UiHelpers;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import co.chatsdk.ui.R;

/**
 * Created by kykrueger on 2016-11-06.
 */

public class ToastHelper {

    private SuperCardToast superCardToast;
    private Activity activity;

    public ToastHelper(Activity activity){
        this.activity = activity;
    }

    private SuperToast getDefaultAlertToast(){

        SuperToast alertToast = new SuperToast(this.activity);
        alertToast.setDuration(SuperToast.Duration.MEDIUM);
        //alertToast.setBackground(ContextCompat.getColor(activity, R.color.sdkAlertToastColor));
        //alertToast.setTextColor(ContextCompat.getColor(activity, R.color.sdkAlertToastTextColor));
        alertToast.setAnimations(SuperToast.Animations.FLYIN);
        return alertToast;
    }

    private SuperToast getDefaultToast(){

        SuperToast toast = new SuperToast(this.activity);
        toast.setDuration(SuperToast.Duration.MEDIUM);
        //toast.setBackground(ContextCompat.getColor(activity, R.color.sdkToastColor));
        //toast.setTextColor(ContextCompat.getColor(activity, R.color.sdkToastTextColor));
        toast.setAnimations(SuperToast.Animations.FLYIN);
        return toast;
    }

    /***
     * 
     * @return returns null if another card is outstanding, otherwise returns an initialized card.
     */
    @Nullable
    private SuperCardToast initCardToast(){
        
        if (superCardToast != null)
            return null;
    
        try {
            superCardToast = new SuperCardToast(this.activity, SuperToast.Type.PROGRESS);
            superCardToast.setIndeterminate(true);
            superCardToast.setProgressIndeterminate(true);
            superCardToast.setBackground(activity.getResources().getColor(R.color.sdkToastColor));
            superCardToast.setTextColor(activity.getResources().getColor(R.color.sdkToastTextColor));
            superCardToast.setSwipeToDismiss(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return superCardToast;
    }

    public void dismissProgressCard(){
        dismissProgressCard(0);

    }

    public void dismissProgressCard(long delay){
        if (superCardToast == null)
            return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                superCardToast.dismiss();
                superCardToast = null;
            }
        }, delay);
    }

    public void dismissProgressCardImmediately(){
        if (superCardToast == null)
            return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                superCardToast.dismissImmediately();
                superCardToast = null;
            }
        }, 0);
    }

    private void showProgressCard(String text){


        SuperCardToast cardToastReturn = initCardToast();
        if(cardToastReturn == null) return;

        View decorView = (this.activity).getWindow().getDecorView().findViewById(android.R.id.content);
        ViewGroup viewGroup = superCardToast.getViewGroup();

        if (viewGroup!=null && superCardToast.getView()!= null && viewGroup.findViewById(superCardToast.getView().getId()) != null)
            viewGroup.clearChildFocus(superCardToast.getView());

        decorView.findViewById(R.id.card_container).bringToFront();

        superCardToast.setText(text);

        if (!superCardToast.isShowing())
            superCardToast.show();

    
    }

    public void showProgressCard(@StringRes int resourceId){
        showProgressCard(this.activity.getString(resourceId));
    }

    /*Getters and Setters*/
    private void showToast(String text, SuperToast toast){
        toast.setText(text);
        toast.show();
    }

    public void showToast(@StringRes int resourceId){
        if (this.activity == null)
            return;

        SuperToast toast = getDefaultAlertToast();
        showToast(this.activity.getResources().getString(resourceId), toast);
    }

    public void showToast(@StringRes int resourceId, SuperToast toast){
        if (this.activity == null)
            return;

        showToast(this.activity.getResources().getString(resourceId), toast);
    }

}
