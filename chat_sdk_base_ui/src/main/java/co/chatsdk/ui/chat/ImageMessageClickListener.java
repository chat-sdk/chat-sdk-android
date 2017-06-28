package co.chatsdk.ui.chat;

import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.types.Defines;
import co.chatsdk.ui.R;
import co.chatsdk.ui.UiHelpers.UIHelper;
import co.chatsdk.ui.UiHelpers.DialogUtils;

/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class ImageMessageClickListener implements View.OnClickListener {

    private MessageListItem messageItem;
    private AppCompatActivity activity;

    public ImageMessageClickListener (AppCompatActivity activity, MessageListItem messageItem) {
        this.messageItem = messageItem;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        UIHelper.hideSoftKeyboard(activity);

        if (StringUtils.isNotBlank(messageItem.resourcePath))
        {
            PopupWindow popupWindow;

            popupWindow = DialogUtils.getImageDialog(activity, messageItem.resourcePath, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_PATH);

            if (popupWindow == null) {
                helper().showProgressCard(activity.getString(R.string.message_adapter_load_image_fail));
            }
            else {
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        }
        else if (StringUtils.isNotBlank(messageItem.text))
        {
            String imageUrl = messageItem.text.split(Defines.DIVIDER)[0];
            // Saving the url so we could remove it later on.

            PopupWindow popupWindow;

            // Telling the popup window to save the imageView after it was open.
            if (!Defines.Options.SaveImagesToDir) {
                popupWindow = DialogUtils.getImageDialog(activity, imageUrl, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_URL);
            }
            else {
                popupWindow = DialogUtils.getImageMessageDialog(activity, imageUrl, DialogUtils.ImagePopupWindow.LoadTypes.LOAD_FROM_URL, messageItem.message);
            }

            if (popupWindow == null)
                helper().showToast(activity.getString(R.string.message_adapter_load_image_fail));
            else popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        }
        else
        {
            helper().showToast(activity.getString(R.string.message_adapter_load_image_fail));
        }
    }

    private UIHelper helper () {
        return UIHelper.getInstance();
    }

}
