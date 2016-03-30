package com.braunster.chatsdk.Utils.asynctask;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListener;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;

/**
 * Created by braunster on 17/12/14.
 *
 * This class imports users contact list and try to find other users with this phone number using indexing.
 *
 * The country code is added to a number if it does not have any other country code starting with '+'.
 *
 */
public class ImportPhoneContactTask extends AsyncTask<Void, Void, Void> {

    public static final String TAG = ImportPhoneContactTask.class.getSimpleName();
    public static final boolean DEBUG = false;

    private Context context;

    public ImportPhoneContactTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String countryCode = "+" + getCountryCode();

        if (DEBUG) Log.d(TAG, "CountryCode: " + countryCode);

        ContentResolver cr = context.getContentResolver();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        // Going thought all the user phone numbers.
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        // Adding the country code to the phone number if does not have any.
                        if (!phoneNo.startsWith(countryCode) && !phoneNo.startsWith("+"))
                            phoneNo = countryCode + phoneNo;

                        if (DEBUG) Log.d(TAG, "Name: " + name + ", Phone No: " + phoneNo);

                        BNetworkManager.sharedManager().getNetworkAdapter().usersForIndex(BDefines.Keys.BPhone, phoneNo, new RepetitiveCompletionListener<BUser>() {
                            @Override
                            public boolean onItem(BUser item) {
                                if (DEBUG) Log.d(TAG, "User found: " + item.getMetaName());
                                BNetworkManager.sharedManager().getNetworkAdapter().currentUser().addContact(item);
                                return false;
                            }

                            @Override
                            public void onDone() {

                            }

                            @Override
                            public void onItemError(BError object) {

                            }
                        });
                    }
                    pCur.close();
                }
            }
        }

        return null;
    }

    public String getCountryCode(){
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl= context.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                CountryZipCode=g[0];
                break;
            }
        }
        return CountryZipCode;
    }
}
