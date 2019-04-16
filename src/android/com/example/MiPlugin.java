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

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class MiPlugin extends CordovaPlugin {
    private static final String TAG = "MiPlugin";
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
        Log.d(TAG, "Inicializando MiPlugin");
        geofenceSingleton = new GeofenceSingleton(this.cordova.getActivity());
        this.cordova.getActivity().startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+this.cordova.getActivity().getPackageName())));
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
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
                    String id = args.optJSONObject(0).optString("id");
                    double latitud = args.optJSONObject(0).optDouble("latitud");
                    double longitud = args.optJSONObject(0).optDouble("longitud");
                    int radius = args.optJSONObject(0).optInt("radius");

                    String[] parts = id.split("\\|");

                    String idFence = parts[0] + "|" + parts[1];
                    geofenceSingleton.addGeofence(latitud, longitud, radius, idFence);
                    geofenceSingleton.startGeofencing(cordova.getActivity());

                    final PluginResult result = new PluginResult(PluginResult.Status.OK,
                            "Hola todo addOrUpdateFence... ");
                    callbackContext.sendPluginResult(result);
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
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
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
