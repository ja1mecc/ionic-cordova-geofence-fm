package com.example;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "Geofence-Service";

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand DESDE SERVICE BACKGROUND!!!******");

        String action = intent.getStringExtra("action");

        Log.i(TAG, "onReceive -> action -> " + action);

        if(action != null && action.equals("STOP_SERVICE")){
            stopSelf();
            return START_NOT_STICKY;
        }

        final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error in geofencing event");
            return START_NOT_STICKY;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        Log.i(TAG, "FenceTransition -> " + transitionType);

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();

            for (Geofence fence : triggerList) {
                String fenceId = fence.getRequestId();

                Log.i(TAG, "Entered -> " + fenceId);

                String[] parts = fenceId.split("\\|");

                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Entered -> " + fenceId, Toast.LENGTH_SHORT).show();
                        JSONParser jsonParser = new JSONParser();
                        jsonParser.loadServiceFinmarkets(parts[0], parts[1], FirebaseInstanceId.getInstance().getToken());
                    }
                });

            }
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();

            for (Geofence fence : triggerList) {
                String fenceId = fence.getRequestId();

                Log.i(TAG, "Exit -> " + fenceId);

                String[] parts = fenceId.split("\\|");

                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Exit -> " + fenceId, Toast.LENGTH_SHORT).show();
                        JSONParser jsonParser = new JSONParser();
                        //jsonParser.loadServiceFinmarkets("-1", parts[1], "OTZlZDMzYWFkZjgzNDQ2NDY4MWNmYjlkYmMyMWRjNWQ0YmY4OTI3YmQyZGJiYjAwYTU0ZGI5NzlmZDQ1ZDFmNhp7hJYQY8nX6jsOWH6yzfIhV9qR5c82aCzfYb+MAroP");
                        jsonParser.loadServiceFinmarkets(parts[0], parts[1], FirebaseInstanceId.getInstance().getToken());
                    }
                });

            }
        } else {
            String error = "Geofence transition error: " + transitionType;
            Log.e(TAG, error);
        }

        // sendBroadcast(broadcastIntent);


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(Constants.LOCATION_INTERVAL);
        locationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        Intent intentAction = new Intent(getApplicationContext(), GeofenceTransitionsIntentService.class);
        intentAction.putExtra("action","STOP_SERVICE");
        PendingIntent pIntentlogin = PendingIntent.getService(getApplicationContext(),1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "Entro a uno -> **********");

            String NOTIFICATION_CHANNEL_ID = "com.example.MiPlugin";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            String packageName = getApplicationContext().getPackageName();
            Intent resultIntent = getApplicationContext().getPackageManager()
                    .getLaunchIntentForPackage(packageName);


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    1, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(getApplicationContext().getApplicationInfo().icon)
                    .setContentTitle("App ejecutandose en segundo plano")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(resultPendingIntent)
                    .addAction(getApplicationContext().getApplicationInfo().icon,"APAGAR", pIntentlogin)
                    .build();



            startForeground(1, notification);

        } else {
            Log.i(TAG, "Entro a dos -> **********");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("App ejecutandose en segundo plano")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(getApplicationContext().getApplicationInfo().icon,"APAGAR", pIntentlogin)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }

        return START_NOT_STICKY;
    }

}