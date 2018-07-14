package com.skynet.aptosense;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class WearActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static String SensorType = "HR";
    public TextView mTextView;
    Node mNode;
    GoogleApiClient mGoogleApiClient;

    SensorManager mSensorManager;

    Sensor mHeartRateSensor;
    Sensor mGyroscopeSensor;
    Sensor mAccelerometerSensor;
    boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {

            //
            //
            //
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mTextView = (TextView) stub.findViewById(R.id.textHR);
                mTextView.setText(R.string.app_name);
                getHR();

            }
        });

    }

    public void getHR() {

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            String hRate = "" + (int) event.values[0];
            SensorType = "HR";
            sendMessage(hRate);
            ImageView imageView = (ImageView) findViewById(R.id.imgHeart);
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            imageView.startAnimation(pulse);

        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {


            final float RAD_TO_DEGREES = (float) (180.0f / Math.PI);

            String gyroX = "" + (int) event.values[0] * RAD_TO_DEGREES;
            String gyroY = "" + (int) event.values[1] * RAD_TO_DEGREES;
            String gyroZ = "" + (int) event.values[2] * RAD_TO_DEGREES;
            SensorType = "GYRO";
            sendMessage(gyroX + "," + gyroY + "," + gyroZ);

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float[] gravity = new float[3];
            float[] linear_acceleration = new float[3];
            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            String accX = "" + (int) linear_acceleration[0];
            String accY = "" + (int) linear_acceleration[1];
            String accZ = "" + (int) linear_acceleration[2];
            SensorType = "ACC";
            sendMessage(accX + "," + accY + "," + accZ);


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();

        }
    }


    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {

        resolveNode();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void StopHR(View view) {
        SensorType = "None";
        sendMessage("Disconnected");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
        this.finish();
        System.exit(0);
    }

    private void sendMessage(String Key) {

        if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SensorType + "--" + Key, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        }
                    }
            );
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}

// Connecting guide
// adb forward tcp:4444 localabstract:/adb-hub
//adb connect 127.0.0.1:4444