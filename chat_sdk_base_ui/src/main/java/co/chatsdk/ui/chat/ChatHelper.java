/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BMessageDao;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.types.Defines;
import co.chatsdk.ui.R;
import co.chatsdk.ui.UiHelpers.UIHelper;
import co.chatsdk.core.dao.sorter.MessageSorter;
import co.chatsdk.core.dao.DaoCore;
import com.github.johnpersano.supertoasts.SuperCardToast;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.query.QueryBuilder;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

@Deprecated
public class ChatHelper {

    /** The key to get the shared file uri. This is used when the activity is opened to share and imageView or a file with the chat users.
     *  Example can be found in ContactsFragment that use click mode share with contact. */
    public static final String SHARED_FILE_URI = "share_file_uri";

    /** The key to get shared text, this is used when the activity is open to share text with the chat user.
     *  Example can be found in ContactsFragment that use click mode share with contact. */
    public static final String SHARED_TEXT = "shared_text";

    public static final String READ_COUNT = "read_count";

    public static final String FILE_NAME = "file_name";
    
    /** The key to get the path of the last captured imageView path in case the activity is destroyed while capturing.*/
    public static final String SELECTED_FILE_PATH = "captured_photo_path";

    /** The amount of messages that was loaded for this thread,
     *  When we load more then the default messages amount we want to keep the amount so we could load them again if the list needs to be re-created.*/
    public static final String LOADED_MESSAGES_AMOUNT = "LoadedMessagesAmount";
    private int loadedMessagesAmount = 0;

    /** The selected file that is picked to be sent.
     *  This is also the path to the camera output.*/
    private String selectedFilePath = "";


}
