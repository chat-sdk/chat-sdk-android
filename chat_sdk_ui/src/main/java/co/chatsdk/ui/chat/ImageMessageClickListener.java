package co.chatsdk.ui.chat;

import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import org.apache.commons.lang3.StringUtils;
import co.chatsdk.core.types.Defines;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.ui.utils.ToastHelper;

/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class ImageMessageClickListener implements View.OnClickListener {

    private String url;
    private String imageName;
    private AppCompatActivity activity;

    public ImageMessageClickListener (AppCompatActivity activity, String  url, String imageName) {
        this.url = url;
        this.imageName = imageName;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        UIHelper.hideSoftKeyboard(activity);

        if (StringUtils.isNotBlank(url)) {


            PopupWindow popupWindow;

            // Telling the popup window to save the messageImageView after it was open.
            if (!Defines.Options.SaveImagesToDir) {
                popupWindow = DialogUtils.getImageDialog(activity, url, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_URL);
            }
            else {
                popupWindow = DialogUtils.getImageMessageDialog(activity, url, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_URL, imageName);
            }

            if (popupWindow == null)
                ToastHelper.show(R.string.message_adapter_load_image_fail);
            else popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        }
        else {
            ToastHelper.show(activity.getString(R.string.message_adapter_load_image_fail));
        }
    }

    private UIHelper helper () {
        return UIHelper.shared();
    }

}
