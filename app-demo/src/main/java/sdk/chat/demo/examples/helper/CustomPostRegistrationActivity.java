package sdk.chat.demo.examples.helper;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;

import sdk.chat.demo.pre.R;
import sdk.chat.ui.activities.PostRegistrationActivity;

public class CustomPostRegistrationActivity extends PostRegistrationActivity {

    // Override the getLayout method. We copy/pasted the contents of the activity_post_registration
    // Layout into the new custom layout. We can then modify it to modify the view
    @Override
    protected int getLayout() {
        return R.layout.activity_custom_post_registration;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success");
        builder.setMessage("View is overridden successfully!");

        builder.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
        });

        // create alert dialog
        AlertDialog alertDialog = builder.create();

        // show it
        alertDialog.show();
    }


}
