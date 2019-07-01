package com.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        //Do whatever you did in your Service handleIntent function here.
        Log.i(TAG, "BootReceiver -> onReceive -> " + geofencingEvent.getGeofenceTransition());

        int transitionType = geofencingEvent.getGeofenceTransition();
        Log.i(TAG, "FenceTransition -> " + transitionType);

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {

            List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();

            for (Geofence fence : triggerList) {
                String fenceId = fence.getRequestId();

                Log.i(TAG, "Entered -> " + fenceId);

                String[] parts = fenceId.split("\\|");

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(getApplicationContext(), "Entered -> " + fenceId, Toast.LENGTH_SHORT).show();
                        JSONParser jsonParser = new JSONParser();
                        jsonParser.loadServiceFinmarkets(parts[0], parts[1], FirebaseInstanceId.getInstance().getToken(), "entrada");
                    }
                });

            }
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();

            for (Geofence fence : triggerList) {
                String fenceId = fence.getRequestId();

                Log.i(TAG, "Exit -> " + fenceId);

                String[] parts = fenceId.split("\\|");

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(getApplicationContext(), "Exit -> " + fenceId, Toast.LENGTH_SHORT).show();
                        JSONParser jsonParser = new JSONParser();
                        jsonParser.loadServiceFinmarkets(parts[0], parts[1], FirebaseInstanceId.getInstance().getToken(), "salida");
                    }
                });

            }
        }
    }
}
