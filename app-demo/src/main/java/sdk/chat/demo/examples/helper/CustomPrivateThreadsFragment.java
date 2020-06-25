package sdk.chat.demo.examples.helper;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;

import sdk.chat.demo.pre.R;
import sdk.chat.ui.fragments.PrivateThreadsFragment;

public class CustomPrivateThreadsFragment extends PrivateThreadsFragment {

    // You can customize the layout file here
    @Override
    protected @LayoutRes
    int getLayout() {
        return R.layout.fragment_threads;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Success");
        builder.setMessage("View is overridden successfully!");

        builder.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
        });

        // create alert dialog
        AlertDialog alertDialog = builder.create();

        // show it
        alertDialog.show();

        return view;
    }

}
