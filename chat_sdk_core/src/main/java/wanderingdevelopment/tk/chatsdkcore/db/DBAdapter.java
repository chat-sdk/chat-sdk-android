package wanderingdevelopment.tk.chatsdkcore.db;

import android.content.Context;

/**
 * Created by kykrueger on 2017-01-10.
 */

public class DBAdapter {
    private Context context;

    DBAdapter(Context context) {
        this.context = context;
    }

    Context getContext(){
        return context;
    }

    DaoCore getDaoCore(){
        return DaoCore.getDaoCore(context);
    }
}
