package co.chatsdk.ui.utils;

import android.net.Uri;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import androidx.annotation.StringRes;

public class ViewHelper {

    // View

    public static void setVisibility(View view, int visibility) {
        if (view != null) view.setVisibility(visibility);
    }

    public static void setVisible(View view, boolean visible) {
        setVisibility(view, visible ? View.VISIBLE : View.INVISIBLE);
    }

    public static void setGone(View view, boolean gone) {
        setVisibility(view, gone ? View.GONE : View.VISIBLE);
    }

    public static void setOnClickListener(View view, View.OnClickListener listener) {
        if (view != null) view.setOnClickListener(listener);
    }

    public static void setEnabled(View view, boolean enabled) {
        if (view != null) view.setEnabled(enabled);
    }

    // TextView

    public static void setText(TextView textView, String text) {
        if (textView != null) textView.setText(text);
    }

    public static void setText(TextView textView, @StringRes int resid) {
        if (textView != null) textView.setText(resid);
    }

    public static CharSequence getText(TextView textView) {
        if (textView != null) return textView.getText();
        else return null;
    }

    public static String getTextString(TextView textView) {
        CharSequence charSequence = getText(textView);
        if (charSequence != null) return charSequence.toString();
        else return "";
    }

    public static void addTextChangedListener(TextView textView, TextWatcher watcher) {
        if (textView != null) textView.addTextChangedListener(watcher);
    }

    // Spinner

    public static int getCount(Spinner spinner) {
        if (spinner != null) return spinner.getCount();
        else return -1;
    }

    public static Object getSelectedItem(Spinner spinner) {
        if (spinner != null) return spinner.getSelectedItem();
        else return null;
    }

    public static String getSelectedString(Spinner spinner) {
        Object item = getSelectedItem(spinner);
        if (item != null) return item.toString();
        else return "";
    }

    public static Object getItemAtPosition(Spinner spinner, int position) {
        if (spinner != null) return spinner.getItemAtPosition(position);
        else return null;
    }

    public static String getStringAtPosition(Spinner spinner, int position) {
        Object item = getItemAtPosition(spinner, position);
        if (item != null) return item.toString();
        else return "";
    }

    public static void setSelection(Spinner spinner, int position) {
        if (spinner != null) spinner.setSelection(position);
    }

    // SimpleDraweeView

    public static void setImageURI(SimpleDraweeView draweeView, Uri uri) {
        if (draweeView != null) draweeView.setImageURI(uri);
    }

    public static void setImageURI(SimpleDraweeView draweeView, String uriString) {
        if (draweeView != null) draweeView.setImageURI(uriString);
    }

}
