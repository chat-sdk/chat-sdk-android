package sdk.chat.ui.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

import io.reactivex.functions.Consumer;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;

public class AlertUtils implements Consumer<Throwable> {

    protected class CustomProgressDialog {

        AlertDialog dialog;
        TextView textView;

        public CustomProgressDialog(Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(provider.getContext());
            builder.setCancelable(false); // if you want user to wait for some process to finish,
            LayoutInflater inflater = LayoutInflater.from(provider.getContext());
            View view = inflater.inflate(R.layout.dialog_progress, null);
            textView = view.findViewById(R.id.textView);
            builder.setView(view);
            dialog = builder.create();
        }

        public boolean isShowing() {
            return dialog.isShowing();
        }

        public void show() {
            dialog.show();
        }

        public void dismiss() {
            dialog.dismiss();
        }

        public void setMessage(String text) {
            textView.setText(text);
        }

        public AlertDialog getDialog() {
            return dialog;
        }
    }

    protected Snackbar snackbar;
    protected CustomProgressDialog progressDialog;
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
            progressDialog = new CustomProgressDialog(provider.getContext());
        }
        if (!progressDialog.isShowing()) {
//            progressDialog = newProgressDialog();
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    public void showOrUpdateProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(provider.getContext());
        }

        if (!progressDialog.isShowing()) {
//            progressDialog = newProgressDialog();
            progressDialog.show();
        }
        progressDialog.setMessage(message);
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

    public AlertDialog getProgressDialog() {
        return progressDialog.getDialog();
    }
}
