package wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import wanderingdevelopment.tk.sdkbaseui.R;

/**
 * Created by kykrueger on 2016-12-31.
 */

public class FetchPhotoDialogFragment extends DialogFragment {

    public static FetchPhotoDialogFragment newInstance(){
        FetchPhotoDialogFragment dialogFragment = new FetchPhotoDialogFragment();

        return dialogFragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.fetchphoto_dialog_title)
                .setItems(R.array.fetchphoto_list_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if(which == 3){

                        }
                    }
                });
        return builder.create();
    }

}
