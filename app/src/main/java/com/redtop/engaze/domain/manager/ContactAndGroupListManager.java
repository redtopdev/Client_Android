package com.redtop.engaze.domain.manager;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnRefreshMemberListCompleteListner;
import com.redtop.engaze.R;
import com.redtop.engaze.app.AppContext;
import com.redtop.engaze.common.cache.InternalCaching;
import com.redtop.engaze.common.utility.BitMapHelper;
import com.redtop.engaze.common.utility.MaterialColor;
import com.redtop.engaze.common.utility.PreffManager;
import com.redtop.engaze.domain.ContactOrGroup;
import com.redtop.engaze.webservice.IUserWS;
import com.redtop.engaze.webservice.UserWS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ContactAndGroupListManager {

    private final static String TAG = ContactAndGroupListManager.class.getName();

    private final static IUserWS userWS = new UserWS();

    public static ContactOrGroup getContact(String userId) {
        ContactOrGroup cg = null;
        if (userId == null) {
            return cg;
        }
        HashMap<String, ContactOrGroup> table = InternalCaching.getRegisteredContactListFromCache();
        if (table != null) {
            cg = table.get(userId);
        }

        return cg;
    }

    public static void cacheGroupList() {
        PreffManager.setPrefArrayList("Groups", getAllGroups());
    }

    public static ArrayList<ContactOrGroup> sortContacts(ArrayList<ContactOrGroup> contactsAndGroups) {
        if (contactsAndGroups.size() > 0) {
            Collections.sort(contactsAndGroups, (lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()));
        }
        return contactsAndGroups;
    }

   /* public static ArrayList<ContactOrGroup> getAllRegisteredContacts() {
        ArrayList<ContactOrGroup> contactsAndGroups = new ArrayList<ContactOrGroup>(InternalCaching.getRegisteredContactListFromCache().values());
        AppContext.context.setRegisteredContactList(sortContacts(contactsAndGroups));
        return AppContext.context.sortedRegisteredContacts;
    }*/

    public static  ArrayList<ContactOrGroup> getSortedContacts(){
        ArrayList<ContactOrGroup> contactsAndGroups = new ArrayList<>(InternalCaching.getContactListFromCache().values());
        ArrayList<ContactOrGroup> registered = new ArrayList<>(InternalCaching.getRegisteredContactListFromCache().values());

        ArrayList<ContactOrGroup> finalContacts = new ArrayList<>();
       for(ContactOrGroup rcg: registered){
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
               contactsAndGroups.removeIf(cg->cg.getName().equals(rcg.getName()));
           }
       }

        finalContacts.addAll(sortContacts(registered));
        finalContacts.addAll(sortContacts(contactsAndGroups));

        return finalContacts;
    }

    public static HashMap<String, ContactOrGroup> getAllContactsFromCache() {
        return InternalCaching.getContactListFromCache();
    }

    public static ArrayList<ContactOrGroup> getGroups() {
        return PreffManager.getPrefArrayList("Groups");
    }

    public static void initializedRegisteredUser(final OnRefreshMemberListCompleteListner listnerOnSuccess, final OnRefreshMemberListCompleteListner listnerOnFailure) {
        cacheRegisteredContacts(getAllContactsFromCache(), listnerOnSuccess, listnerOnFailure);
    }

    public static HashMap<String, ContactOrGroup> getAllContactsFromDeviceContactList() {
        Cursor cursor = null;
        HashMap<String, ContactOrGroup> contacts = new HashMap<String, ContactOrGroup>();
        try {
            ContactOrGroup cg;
            String[] columns = {ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
            cursor = AppContext.context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, columns, null, null, null);

            int ColumeIndex_THUMBNAIL = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
            int ColumeIndex_ID = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int ColumeIndex_DISPLAY_NAME = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int ColumeIndex_HAS_PHONE_NUMBER = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

            //Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " DESC");


            if (cursor.getCount() > 0) {

                while (cursor.moveToNext()) {
                    String thumbnail_uri = cursor.getString(ColumeIndex_THUMBNAIL);
                    String has_phone = cursor.getString(ColumeIndex_HAS_PHONE_NUMBER);

                    if (!has_phone.endsWith("0")) {
                        ArrayList<String> phoneNumbers = getPhoneNumbers(cursor.getString(ColumeIndex_ID));
                        for (String phoneNumber : phoneNumbers) {
                            cg = new ContactOrGroup();
                            cg.setMobileNumber(phoneNumber);
                            cg.setName(cursor.getString(ColumeIndex_DISPLAY_NAME));

                            cg.setThumbnailUri(thumbnail_uri);

                            if (thumbnail_uri == null || thumbnail_uri == "") {
                                cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
                                String startingchar = cg.getName().substring(0, 1);
                                if (!(startingchar.matches("[0-9]") || startingchar.startsWith("+"))) {
                                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(cg.getName()), 40, startingchar.toUpperCase()));
                                } else {
                                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForIcon(MaterialColor.getColor(cg.getName()), 40, Uri.parse("android.resource://com.redtop.engaze/drawable/ic_person_white_24dp")));
                                }
                            } else {
                                Bitmap pofilePicBitmap = BitMapHelper.generateCircleBitmapForImage(54, Uri.parse(cg.getThumbnailUri()));
                                cg.setImageBitmap(pofilePicBitmap);
                                cg.setIconImageBitmap(pofilePicBitmap);
                            }
                            contacts.put(phoneNumber, cg);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contacts;
    }

    public static void cacheRegisteredContacts(final HashMap<String, ContactOrGroup> contactsAndgroups,
                                               final OnRefreshMemberListCompleteListner listnerOnSuccess,
                                               final OnRefreshMemberListCompleteListner listnerOnFailure) {
        if (!AppContext.context.isInternetEnabled) {
            String message = AppContext.context.getResources().getString(R.string.message_general_no_internet_responseFail);
            //Toast.makeText(mContext,	message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, message);
            listnerOnFailure.RefreshMemberListComplete(null);
            return;
        }

        userWS.AssignUserIdToRegisteredUser(contactsAndgroups, new OnAPICallCompleteListener<JSONArray>() {
            @Override
            public void apiCallSuccess(JSONArray response) {
                try {
                    HashMap<String, ContactOrGroup> registeredContacts = prepareRegisteredContactList(response, contactsAndgroups);
                    InternalCaching.saveRegisteredContactListToCache(registeredContacts);
                    listnerOnSuccess.RefreshMemberListComplete(registeredContacts);

                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());
                    ex.printStackTrace();
                    listnerOnFailure.RefreshMemberListComplete(null);
                }
            }

            @Override
            public void apiCallFailure() {
                listnerOnFailure.RefreshMemberListComplete(null);
            }
        });

    }

    private static HashMap<String, ContactOrGroup> prepareRegisteredContactList(JSONArray response, HashMap<String, ContactOrGroup> contactsAndgroups) throws JSONException, IOException, ClassNotFoundException {

        HashMap<String, ContactOrGroup> registeredContacts = new HashMap<>();
        JSONArray jUsers = response;
        if (jUsers.length() == 0) {
            return registeredContacts;
        }

        for (ContactOrGroup cg : contactsAndgroups.values()) {
            if (cg.getThumbnailUri() == null || cg.getThumbnailUri() == "") {
                cg.setIconImageBitmap(ContactOrGroup.getAppUserIconBitmap());
                String startingchar = cg.getName().substring(0, 1);
                if (!(startingchar.matches("[0-9]") || startingchar.startsWith("+"))) {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForText(MaterialColor.getColor(cg.getName()), 40, startingchar.toUpperCase()));
                } else {
                    cg.setImageBitmap(BitMapHelper.generateCircleBitmapForIcon(MaterialColor.getColor(cg.getName()), 40, Uri.parse("android.resource://com.redtop.engaze/drawable/ic_person_white_24dp")));
                }
            } else {
                Bitmap pofilePicBitmap = BitMapHelper.generateCircleBitmapForImage(54, Uri.parse(cg.getThumbnailUri()));
                cg.setImageBitmap(pofilePicBitmap);
                cg.setIconImageBitmap(pofilePicBitmap);
            }
        }

        String userId = "";
        ContactOrGroup cg = null;
        for (int i = 0, size = jUsers.length(); i < size; i++) {
            JSONObject jsonObj = jUsers.getJSONObject(i);

            cg = contactsAndgroups.get(jsonObj.getString("mobileNumberStoredInRequestorPhone"));
            if (cg != null) {
                userId = jsonObj.get("userId").toString();
                cg.setUserId(userId);
                registeredContacts.put(userId, cg);
                contactsAndgroups.put(jsonObj.getString("mobileNumberStoredInRequestorPhone"), cg);
            }
        }
        return registeredContacts;
    }

    private static ArrayList<String> getPhoneNumbers(String id) {
        ArrayList<String> numbers = new ArrayList<String>();
        //Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
        Cursor phones = AppContext.context.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
        while (phones.moveToNext()) {

            numbers.add(phones.getString(phones.getColumnIndex(Phone.NUMBER)).replaceAll("\\s", ""));
        }

        phones.close();

        return numbers;
    }

    private static ArrayList<ContactOrGroup> getAllGroups() {

        ArrayList<ContactOrGroup> groups = new ArrayList<ContactOrGroup>();
        new ContactOrGroup("group", 123, null);

        return groups;
    }
}