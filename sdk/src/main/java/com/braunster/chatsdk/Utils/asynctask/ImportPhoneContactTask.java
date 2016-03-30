/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:23 PM
 */

package com.braunster.chatsdk.Utils.asynctask;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;

import org.jdeferred.DoneCallback;

import java.util.List;

import timber.log.Timber;

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

        if (DEBUG) Timber.d("CountryCode: %s", countryCode);

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

                        if (DEBUG) Timber.d("Name: %s, Phone Number: %s", name, phoneNo);

                        BNetworkManager.sharedManager().getNetworkAdapter().usersForIndex(BDefines.Keys.BPhone, phoneNo)
                                .done(new DoneCallback<List<BUser>>() {
                                    @Override
                                    public void onDone(List<BUser> users) {
                                        for (BUser u : users)
                                        {
                                            if (DEBUG) Timber.d("User found: %s", u.getMetaName());
                                            BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().addContact(u);
                                        }
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
