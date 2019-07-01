package com.example;

import android.content.Context;
import android.content.SharedPreferences;

public class GeofenceSharedPreferences {
    private static SharedPreferences sharedPreferences;

    public static void initFromContext(Context androidContext){
        sharedPreferences = androidContext.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void StoreValue(boolean disableButton){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(Constants.PREF_GEOFENCE_KEY_DISABLE_BUTTON, disableButton);
        editor.apply();
    }

    public static boolean readStoredValue(){
        return sharedPreferences.getBoolean(Constants.PREF_GEOFENCE_KEY_DISABLE_BUTTON, true);
    }
}
