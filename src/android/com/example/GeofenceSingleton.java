package com.example;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class GeofenceSingleton {
    public static final String TAG = "GeofenceSingleton";

    private Context appContext;
    private static final String ERR_MSG = "Application Context is not set!! " +
            "Please call GeofenceSngleton.init() with proper application context";
    private PendingIntent mGeofencePendingIntent;
    private static ArrayList<GeoDistanceCalculator.GeofenceWrapper> mGeofenceList;
    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient mFusedLocationClient;

    public GeofenceSingleton(Activity activity) {
        if (activity == null)
            throw new IllegalStateException(ERR_MSG);

        appContext = activity.getApplicationContext();
        mGeofenceList = new ArrayList<>();

        geofencingClient = LocationServices.getGeofencingClient(appContext);

    }

    public void addGeofence(double latitude, double longitude, int radius, String uid) {
        Log.d(TAG, "addGeofence -> " + latitude + ", " + longitude + ", " + radius + ", " + uid);

        GeoDistanceCalculator.GeofenceWrapper wrapper;
        wrapper = new GeoDistanceCalculator.GeofenceWrapper(uid, latitude, longitude, radius);
        mGeofenceList.add(wrapper);
    }

    private List<Geofence> wrapperToGeofenceList(){
        List<Geofence> list = new ArrayList<>();
        for (GeoDistanceCalculator.GeofenceWrapper wrapper : mGeofenceList){
            list.add(wrapper.toGeofenceInstance());
        }
        return list;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(wrapperToGeofenceList());
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            Log.d(TAG, "mGeofencePendingIntent nulo");
            return mGeofencePendingIntent;
        }

        Log.d(TAG, "mGeofencePendingIntent non-nulo");
        Intent intent = new Intent(appContext, BootReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent intent = new Intent(appContext, GeofenceTransitionsIntentService.class);
        //mGeofencePendingIntent = PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    public void startGeofencing(Activity appActivity) {

        if (checkPermission()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext);

            Task<Location> task = mFusedLocationClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null && this.checkPermission()){
                    GeoDistanceCalculator.GeofenceWrapper areaInside;
                    areaInside = GeoDistanceCalculator.isPositionInsideFence(location, mGeofenceList);
                    areaInside.name = mGeofenceList.get(0).name;

                    if (areaInside.isValid()){
                        this.executeServiceEnterArea(areaInside, "entrada");
                    }
                    else{
                        GeofenceSharedPreferences.StoreValue(true);
                        this.executeServiceEnterArea(areaInside, "salida");
                    }

                    Task<Void> innerTask = geofencingClient.addGeofences(
                            getGeofencingRequest(),
                            getGeofencePendingIntent()
                    );

                    innerTask = innerTask.addOnSuccessListener(aVoid -> {
                        Log.i(TAG, "Exito agregando todas las geofences");
                        initLocationRequest();
                    });

                    innerTask.addOnFailureListener(e -> {
                        Log.wtf(TAG, "Ocurrió un error agregando las geofences. WTF!!!!");
                    });
                }
            });

        } else {
            Log.d(TAG, "Not permissions");
        }
    }

    private void initLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(Constants.LOCATION_INTERVAL);
        locationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    Log.d(TAG, "Posición latitud actual -> " + location.getLatitude());
                    Log.d(TAG, "Posición longitud actual -> " + location.getLongitude());
                }
            }
        };

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(() -> {
            if (checkPermission())
                mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
            Log.d(TAG, "FusedLocationClient requestLocationUpdates");
        });
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        return (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    public void removeGeofence() {
        LocationServices.getGeofencingClient(appContext).removeGeofences(
                getGeofencePendingIntent()
        );
    }

    public void checkLocation(Activity activity) {
        LocationManager lm = (LocationManager)appContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        Log.d(TAG, "gps_enabled -> " + gps_enabled + ", network_enabled -> " + network_enabled);

    }

    public static boolean isServiceRunning(Activity activity, String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    private void executeServiceEnterArea(GeoDistanceCalculator.GeofenceWrapper wrapper, String action){
        String[] parts = wrapper.name.split("\\|");
        Log.i(TAG, "Llamando servicio de entrada de finmarkets desde la detección manual de distancia");
        Log.i(TAG, "Se encontró que se está dentro de la cerca con id: " + wrapper.name);
        Log.i(TAG, "Dado que estamos a " + wrapper.distanceFromClosest + " km del centro");

        Handler mHandler = new Handler(Looper.getMainLooper());

        mHandler.post(() -> {
            JSONParser jsonParser = new JSONParser();
            jsonParser.loadServiceFinmarkets(parts[0], parts[1], FirebaseInstanceId.getInstance().getToken(), action);
        });
    }
}