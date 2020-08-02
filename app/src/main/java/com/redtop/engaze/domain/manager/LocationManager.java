package com.redtop.engaze.domain.manager;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.redtop.engaze.Interface.OnAPICallCompleteListener;
import com.redtop.engaze.Interface.OnActionFailedListner;
import com.redtop.engaze.common.enums.Action;
import com.redtop.engaze.common.utility.AppUtility;
import com.redtop.engaze.common.utility.JsonParser;
import com.redtop.engaze.domain.UsersLocationDetail;
import com.redtop.engaze.webservice.ILocationWS;
import com.redtop.engaze.webservice.proxy.LocationWSProxy;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationManager {

    private final static String TAG = LocationManager.class.getName();
    private final static ILocationWS locationWS = new LocationWSProxy();

    public static void updateLocationToServer(final Context context, final Location location, final String loginId, final OnAPICallCompleteListener onAPICallCompleteListener) {
        //this will be called from background service, app may not be running at that time
        if (!AppUtility.isNetworkAvailable(context)) {
            Log.d(TAG, "No internet connection. Aborting location update to server.");
            return;
        }

        LocationWSProxy.location = location;
        UsersLocationDetail usersLocationDetail = new UsersLocationDetail(
                loginId,
                location.getLatitude(),
                location.getLongitude(), "1.0", "0");

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new JsonParser().Serialize(usersLocationDetail));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        locationWS.updateLocation(jsonObject, new OnAPICallCompleteListener<JSONObject>() {
            @Override
            public void apiCallSuccess(JSONObject response) {
                onAPICallCompleteListener.apiCallSuccess(response);
            }

            @Override
            public void apiCallFailure() {
                onAPICallCompleteListener.apiCallFailure();
            }
        });
    }

    public static void getLocationsFromServer(String userId, String eventId,
                                              final OnAPICallCompleteListener onAPICallCompleteListener) {


        locationWS.getLocationsFromServer(userId, eventId, new OnAPICallCompleteListener<JSONObject>() {
            @Override
            public void apiCallSuccess(JSONObject response) {
                onAPICallCompleteListener.apiCallSuccess(response);
            }

            @Override
            public void apiCallFailure() {
                onAPICallCompleteListener.apiCallFailure();
            }
        });

    }
}