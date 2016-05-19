package com.example.sergei.fastrun;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RunService extends Service implements SensorEventListener {
    public static final String PARAM_OUT_MSG_TIMER = "DONE_TIMER";
    public static final String PARAM_OUT_MSG_STEPS = "DONE_STEPS";
    private LocationManager locationManager;
    private List<Double> latitudes;
    private List<Double> longitudes;
    private static final int RUNNING_DISTANCE = 1;
    private RunTimer runTimer;
    private SensorManager sensorManager;
    private Sensor countSensor;
    private double startingCount;
    private static final int AVARAGE_STEPS = 47; // USAIN BOLT
    private double realCount;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
        startingCount = 0.0;

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_NOT_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, locationListener);

        // Start the timer
        runTimer = new RunTimer();
        runTimer.start();

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);

        return START_STICKY;
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            latitudes.add(location.getLatitude());
            longitudes.add(location.getLongitude());
            if(latitudes.size() > 1 && longitudes.size() > 1) {
                if(getDistance() > RUNNING_DISTANCE && realCount > AVARAGE_STEPS) {
                    runTimer.stop();
                    done();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    };

    private double getDistance() {
        float[] results = new float[1];
        if(latitudes.size() > 0 && longitudes.size() > 0) {
            Location.distanceBetween(
                    latitudes.get(0),
                    longitudes.get(0),
                    latitudes.get(latitudes.size() - 1),
                    longitudes.get(longitudes.size() - 1),
                    results
            );
            return results[0];
        } else {
            return 0.00;
        }


    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
        locationManager = null;
        done();
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    public void done() {
        runTimer.stop();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG_TIMER, runTimer.getTime() + "");
        broadcastIntent.putExtra(PARAM_OUT_MSG_STEPS, realCount + "");
        sendBroadcast(broadcastIntent);
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(startingCount < 1) {
            startingCount = event.values[0];
        }
        realCount = event.values[0] - startingCount;
        if(realCount > AVARAGE_STEPS && getDistance() > RUNNING_DISTANCE) {
            runTimer.stop();
            done();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
