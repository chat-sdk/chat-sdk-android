package sdk.chat.demo;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.fragment.app.Fragment;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import sdk.chat.core.Tab;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.pre.R;
import sdk.chat.ui.fragments.ChatFragment;
import sdk.chat.ui.icons.Icons;

public class CustomizeTabsExample {

    public static void run(Context context) {

        String title = "EditTab";

        // Use FontAwesome or Google Material icons
        IconicsDrawable fawIcon = new IconicsDrawable(context, FontAwesome.Icon.faw_user);
        IconicsDrawable gmdIcon = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_blur_on);

        Drawable drawable = Icons.get(fawIcon, R.color.red);

        Fragment fragment = new ChatFragment();

        // Create and add the tab
        Tab tab = new Tab(title, drawable, fragment);
        ChatSDK.ui().setTab(tab, 1);

        // Or add the tab like this
        ChatSDK.ui().setTab(title, drawable, fragment, 1);

        // Remove the tab
        ChatSDK.ui().removeTab(1);


    }

}
