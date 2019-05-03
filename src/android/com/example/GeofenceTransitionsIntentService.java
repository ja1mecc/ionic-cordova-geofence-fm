package com.example;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import io.ionic.finmarkets.R;

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
        Intent broadcastIntent = new Intent("com.example.ACTION_RECEIVE_GEOFENCE");


        int transitionType = geofencingEvent.getGeofenceTransition();
        Log.i(TAG, "FenceTransition -> " + transitionType);

        broadcastIntent.putExtra("transitionType", transitionType);
        sendBroadcast(broadcastIntent);

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

                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(getApplicationContext(), "Exit -> " + fenceId, Toast.LENGTH_SHORT).show();
                        JSONParser jsonParser = new JSONParser();
                        jsonParser.loadServiceFinmarkets(parts[0], parts[1], FirebaseInstanceId.getInstance().getToken(), "salida");
                    }
                });

            }
        } else {

//            LocationRequest locationRequest = LocationRequest.create();
//            locationRequest.setInterval(Constants.LOCATION_INTERVAL);
//            locationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


            Intent intentAction = new Intent(getApplicationContext(), GeofenceTransitionsIntentService.class);
            intentAction.putExtra("action", "STOP_SERVICE");
            PendingIntent pIntentlogin = PendingIntent.getService(getApplicationContext(), 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);


            Log.i(TAG, "Build.VERSION_CODES.LOLLIPOP -> " + Build.VERSION_CODES.LOLLIPOP);
            Log.i(TAG, "Build.VERSION_CODES.O -> " + Build.VERSION_CODES.O);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i(TAG, "Entro a uno -> ********** -> " + Build.VERSION.SDK_INT);

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
                        .setSmallIcon(getResIdForDrawable("notification_icon"))
                        .setSmallIcon(getResIdForDrawable("notification_icon_large"))
                        .setColor(NotificationCompat.COLOR_DEFAULT)
                        .setContentTitle("App ejecutandose en segundo plano")
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentIntent(resultPendingIntent)
                        .addAction(getResIdForDrawable("notification_icon"), "APAGAR", pIntentlogin)
                        .build();


                startForeground(1, notification);

            } else {
                Log.i(TAG, "Entro a dos -> ********** -> " + Build.VERSION.SDK_INT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setContentTitle("App ejecutandose en segundo plano")
                        .setSmallIcon(getResIdForDrawable("notification_icon"))
                        .setSmallIcon(getResIdForDrawable("notification_icon_large"))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .addAction(getResIdForDrawable("icon"), "APAGAR", pIntentlogin)
                        .setAutoCancel(true);

                Notification notification = builder.build();

                startForeground(1, notification);
            }
        }

        return START_NOT_STICKY;
    }

    int getResIdForDrawable(String resPath) {
        int resId = getResIdForDrawable(getPkgName(), resPath);

        if (resId == 0) {
            resId = getResIdForDrawable("android", resPath);
        }

        return resId;
    }

    int getResIdForDrawable(String clsName, String resPath) {
        String drawable = extractResourceName(resPath);
        int resId = 0;

        try {
            Class<?> cls  = Class.forName(clsName + ".R$drawable");

            resId = (Integer) cls.getDeclaredField(drawable).get(Integer.class);
        } catch (Exception ignore) {}

        return resId;
    }

    private String extractResourceName (String resPath) {
        String drawable = resPath;

        if (drawable.contains("/")) {
            drawable = drawable.substring(drawable.lastIndexOf('/') + 1);
        }

        if (resPath.contains(".")) {
            drawable = drawable.substring(0, drawable.lastIndexOf('.'));
        }

        return drawable;
    }

    private String getPkgName () {
        return getApplicationContext().getPackageName();
    }

    Bitmap getIconFromDrawable (String drawable) {
        Resources res = getApplicationContext().getResources();
        int iconId;

        iconId = getResIdForDrawable(getPkgName(), drawable);

        if (iconId == 0) {
            iconId = getResIdForDrawable("android", drawable);
        }

        if (iconId == 0) {
            iconId = android.R.drawable.ic_menu_info_details;
        }

        return BitmapFactory.decodeResource(res, iconId);
    }
}
