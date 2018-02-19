package com.icochico.mqttchat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MqttService extends Service {

    private static final String TAG = "MqttService";

    // MqttClient
    private MqttAndroidClient mMqttClient;

    // Service needs the LocalBroadcastManager, too
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // instantiate LocalBroadcastManager
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received message with action: " + intent.getAction());

                String msg = intent.getStringExtra(IntentExtras.MESSAGE);
                publishMessage(msg);

            }
        }, new IntentFilter(Actions.ACTION_SEND_MESSAGE));

        connectAndSubscribe();
    }

    /**
     * Attempts a connection to the MQTT broker at the URL specified in Config.MQTT_BROKER_URL.
     *
     * If connection is successful, subscribes via callback.
     */
    private void connectAndSubscribe() {
        // connect to server
        mMqttClient = MqttConnectionFactory.newClient(getApplicationContext(),
                Config.MQTT_BROKER_URL,
                Config.CLIENT_ID);

        mMqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                // connection was successful
                String greetMsg = "Connected to " + serverURI;
                Toast.makeText(getApplicationContext(), greetMsg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, greetMsg);

                try {
                    mMqttClient.subscribe(Config.TOPIC, 0, null,
                            new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d(TAG, "Subscribed successfully to topic " + Config.TOPIC);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.e(TAG, "Subscribe failed for topic " + Config.TOPIC);
                        }
                    });

                } catch (MqttException ex){
                    Log.e(TAG, "Exception while subscribing ", ex);
                    ex.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable t) {
                Log.e(TAG, "Connection lost ", t);

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                String message = new String(mqttMessage.getPayload());
                Log.d(TAG, "Received message: " + message + " for topic: " + topic);
                mLocalBroadcastManager
                        .sendBroadcast(new Intent(Actions.ACTION_RECEIVE_MESSAGE)
                                .putExtra(IntentExtras.MESSAGE, message));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    /**
     *  Publishes a message or displays a Toast for the user if failure.
     *
     * @param msg a String containing the message
     */
    private void publishMessage(String msg) {

        // if mqtt client is null, handle gracefully for the user
        if (mMqttClient == null || !mMqttClient.isConnected()) {
            Toast.makeText(getApplicationContext(), "Client is not connected. Unable to send "
                    , Toast.LENGTH_LONG).show();
            return;
        }

        try {
            MqttConnectionFactory.publishMessage(mMqttClient, msg, 1, Config.TOPIC);
        } catch (UnsupportedEncodingException e) {
            String errMsg = "Unsupported encoding ";
            Log.e(TAG, errMsg, e);
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            String errMsg = "MQTT error while publishing ";
            Log.e(TAG, errMsg , e);
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
        // if service is killed, tells the OS to recreate the service after it has enough memory
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
