package tk.wanderingdevelopment.chatsdk.core.abstracthandlers;

import android.content.Context;

import co.chatsdk.core.defines.Debug;

import tk.wanderingdevelopment.chatsdk.core.interfaces.CoreInterface;

/**
 * Created by KyleKrueger on 11.04.2017.
 */

public abstract class CoreManager implements CoreInterface {

    public CoreManager authInterface;
    protected boolean DEBUG = Debug.CoreManager;
    private Context context;
    //private AbstractEventManager eventManager;

    public CoreManager(Context context){
        this.context = context;
    }






}
