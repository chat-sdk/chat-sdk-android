package wanderingdevelopment.tk.chatsdkcoreadapters;

import android.content.Context;

import wanderingdevelopment.tk.chatsdkcore.db.DaoCore;

/**
 * Created by kykrueger on 2017-01-10.
 */

public class BaseAdapter {
    private Context context;

    BaseAdapter(Context context) {
        if(context != null) {
            this.context = context.getApplicationContext();
        }
    }

    Context getContext(){
        return context;
    }

    DaoCore getDaoCore(){
        return DaoCore.getDaoCore(context);
    }
}
