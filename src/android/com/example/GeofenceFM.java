/**
 */
package com.example;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.util.DisplayMetrics;

import java.io.IOException;

public class GeofenceFM extends CordovaPlugin {
    private static final String TAG = "GeofenceFM";
    private GeofenceSingleton geofenceSingleton;
    private Context context;

    private class Action {
        public String action;
        public JSONArray args;
        public CallbackContext callbackContext;

        public Action(String action, JSONArray args, CallbackContext callbackContext) {
            this.action = action;
            this.args = args;
            this.callbackContext = callbackContext;
        }
    }

    private Action executedAction;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = this.cordova.getActivity().getApplicationContext();
        Log.d(TAG, "Inicializando GeofenceFM");
        geofenceSingleton = new GeofenceSingleton(this.cordova.getActivity());
        this.cordova.getActivity().startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+this.cordova.getActivity().getPackageName())));
    }

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        executedAction = new Action(action, args, callbackContext);

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if (action.equals("init")) {
                    if (callbackContext == null)
                        Log.d(TAG, "Callback nulo 0");
                    else
                        Log.d(TAG, "Callback non-nulo 0");
                    initialize(callbackContext);
                }

                if (action.equals("addOrUpdateFence")) {
                    try {
                        for (int i = 0; i < args.optJSONArray(0).length(); i++) {

                            Log.d(TAG, "args.getJSONObject -> " + args.optJSONArray(0).optJSONObject(i));

                            String id = args.optJSONArray(0).optJSONObject(i).optString("id");
                            double latitud = args.optJSONArray(0).optJSONObject(i).optDouble("latitud");
                            double longitud = args.optJSONArray(0).optJSONObject(i).optDouble("longitud");
                            int radius = args.optJSONArray(0).optJSONObject(i).optInt("radius");

                            String[] parts = id.split("\\|");

                            String idFence = parts[0] + "|" + parts[1];
                            geofenceSingleton.addGeofence(latitud, longitud, radius, idFence);
                        }

                        geofenceSingleton.startGeofencing(cordova.getActivity());

                        final PluginResult result = new PluginResult(PluginResult.Status.OK);
                        callbackContext.sendPluginResult(result);

                    } catch (Exception e) {

                        Log.e(TAG, "execute: Error " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                }

                if (action.equals("removeAllFences")) {
                    try {
                        Log.d(TAG, "removeAllFences");
                        geofenceSingleton.removeGeofence();
                        final PluginResult result = new PluginResult(PluginResult.Status.OK);
                        callbackContext.sendPluginResult(result);
                    } catch (Exception e) {
                        Log.e(TAG, "removeAllFences: Error " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                }

                if (action.equals("getScreenSize")) {
                    Log.d(TAG, "getScreenSize");
                    DisplayMetrics dm = new DisplayMetrics();
                    this.cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

                    int width = dm.widthPixels;
                    int height = dm.heightPixels;

                    double xdpi = (double)dm.xdpi;
                    double ydpi = (double)dm.ydpi;
                    double wi = (double)width / xdpi;
                    double he = (double)height / ydpi;
                    double x = Math.pow(wi, 2);
                    double y = Math.pow(he, 2);

                    double screenInches = Math.round(Math.sqrt(x + y) * 100.0) / 100.0;

                    double density_value = (double)dm.density;
                    String density_bucket = "";

                    // https://design.google.com/devices/
                    // https://developer.android.com/guide/practices/screens_support.html

                    if (density_value < 1.0) {
                        density_bucket = "ldpi";
                    } else if (density_value < 1.5) {
                        density_bucket = "mdpi";
                    } else if (density_value < 2.0) {
                        density_bucket = "hdpi";
                    } else if (density_value < 3.0) {
                        density_bucket = "xhdpi";
                    } else if (density_value < 4.0) {
                        density_bucket = "xxhdpi";
                    } else {
                        density_bucket = "xxxhdpi";
                    }

                    JSONObject result = new JSONObject();

                    try {
                        result.put("width", width);
                        result.put("height", height);
                        result.put("diameter", screenInches);
                        result.put("xdpi", xdpi);
                        result.put("ydpi", ydpi);
                        result.put("densityValue", density_value);
                        result.put("densityBucket", density_bucket);
                    } catch (JSONException e) {}

                    callbackContext.success(result);
                }
                
            }
        });
        return true;
    }

    public boolean execute(Action action) throws JSONException {
        return execute(action.action, action.args, action.callbackContext);
    }

    private void initialize(CallbackContext callbackContext) {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        if (!hasPermissions(permissions)) {
            Log.d(TAG, "Solicitando Permisos");
            PermissionHelper.requestPermissions(this, 0, permissions);
        } else {
            if (callbackContext == null) {
                Log.d(TAG, "Callback nulo 1");
                return;
            }
            callbackContext.success();
        }
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (!PermissionHelper.hasPermission(this, permission))
                return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
            throws JSONException {

        PluginResult result;

        if (executedAction != null) {
            for (int r:grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "Permission Denied!");
                    result = new PluginResult(PluginResult.Status.OK, "PERMISSION_DENIED");
                    executedAction.callbackContext.sendPluginResult(result);
                    executedAction = null;
                    return;
                }
            }
            Log.d(TAG, "Permission Granted!");
            execute(executedAction);
            executedAction = null;
        }

    }

}
