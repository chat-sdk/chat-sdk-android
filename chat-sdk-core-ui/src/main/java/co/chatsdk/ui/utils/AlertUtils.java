package co.chatsdk.ui.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

import co.chatsdk.core.session.ChatSDK;
import io.reactivex.functions.Consumer;

public class AlertUtils implements Consumer<Throwable> {

    protected Snackbar snackbar;
    protected ProgressDialog progressDialog;
    protected Provider provider;

    public interface Provider {
        Context getContext();
        View getRootView();
    }

    public AlertUtils(Provider provider) {
        this.provider = provider;
    }

    /** Show a SuperToast with the given text. */
    public void showToast(@StringRes int textResourceId){
        showToast(provider.getContext().getString(textResourceId));
    }

    public void showToast(String text){
        if (!text.isEmpty()) {
            ToastHelper.show(provider.getContext(), text);
        }
    }

    public void showSnackbar(@StringRes int textResourceId, int duration){
        showSnackbar(provider.getContext().getString(textResourceId), duration);
    }

    public void showSnackbar(@StringRes int textResourceId){
        showSnackbar(provider.getContext().getString(textResourceId), Snackbar.LENGTH_SHORT);
    }

    public void showSnackbar (String text) {
        showSnackbar(text, Snackbar.LENGTH_SHORT);
    }

    public void showSnackbar (String text, int duration) {
        if (!text.isEmpty() && provider.getRootView() != null) {
            if (snackbar == null) {
                snackbar = Snackbar.make(provider.getRootView(), text, duration);
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        AlertUtils.this.snackbar = null;
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                    }
                });
                snackbar.show();

            }
        }
    }

    public Consumer<? super Throwable> toastOnErrorConsumer () {
        return (Consumer<Throwable>) throwable -> showToast(throwable.getLocalizedMessage());
    }

    public Consumer<? super Throwable> snackbarOnErrorConsumer () {
        return (Consumer<Throwable>) throwable -> showSnackbar(throwable.getLocalizedMessage());
    }

    public void showProgressDialog(@StringRes int stringResId) {
        showProgressDialog(provider.getContext().getString(stringResId));
    }

    public void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(provider.getContext());
        }

        if (!progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(provider.getContext());
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    public void showOrUpdateProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(provider.getContext());
        }

        if (!progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(provider.getContext());
            progressDialog.setMessage(message);
            progressDialog.show();
        } else {
            progressDialog.setMessage(message);
        }
    }

    public void dismissProgressDialog() {
        // For handling orientation changed.
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public void onError(Throwable e) {
        showToast(e.getLocalizedMessage());
    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        onError(throwable);
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }
}
