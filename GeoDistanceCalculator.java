package com.example;

import android.location.Location;

import com.google.android.gms.location.Geofence;

import java.util.List;

public class GeoDistanceCalculator {
    public static class GeofenceWrapper {
        private boolean isValid;
        public String name;
        double lat;
        double lng;
        public float radius;
        int responsivenessMs = 1000 * 60 * 2;
        int transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
        public double distanceFromClosest = -1;

        public GeofenceWrapper(String name, double lat, double lng, float radius) {
            this.lat = lat;
            this.lng = lng;
            this.radius = radius;
            this.name = name;
            this.isValid = true;
        }

        public GeofenceWrapper() {
            this.isValid = false;
        }

        public Geofence toGeofenceInstance() {
            Geofence.Builder builder = new Geofence.Builder();
            return builder.setCircularRegion(this.lat, this.lng, this.radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setRequestId(this.name)
                    .setNotificationResponsiveness(this.responsivenessMs)
                    .setTransitionTypes(this.transitionTypes)
                    .build();
        }

        public boolean isValid(){
            return isValid;
        }
    }

    public static GeofenceWrapper isPositionInsideFence(Location currLocation,
                                                        List<GeofenceWrapper> geofencesList) {
//        double lat = -33.408172;
//        double lng = -70.5866;
//        double res = 1.61

        for (GeofenceWrapper wrapper : geofencesList) {
            double distance = calcDistance(currLocation.getLatitude(), currLocation.getLongitude(),
                    wrapper.lat, wrapper.lng);

            if (distance * 1000 < wrapper.radius ){
                wrapper.distanceFromClosest = distance;
                return wrapper;
            }
        }

        return new GeofenceWrapper(); // returns an invalid wrapper
    }

    // https://stackoverflow.com/a/27943
    private static double deg2rad(double deg) {
        return deg * (Math.PI / 180.0);
    }

    private static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        final int earthRadius = 6371;
        double difLat = deg2rad(lat2 - lat1);
        double difLng = deg2rad(lng2 - lng1);

        // así está en la respuesta de stackoverflow. Yo no entiendo nada, solo copio
        double a = Math.sin(difLat / 2.0) * Math.sin(difLat / 2.0) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(difLng / 2.0) * Math.sin(difLng / 2.0);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return  earthRadius * c;
    }
}
