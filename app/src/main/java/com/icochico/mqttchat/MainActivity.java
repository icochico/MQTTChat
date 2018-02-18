package com.icochico.mqttchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

/**
 * Simple Activity that connects to a MQTT broker (e.g. mosquitto), subscribes to a topic, publishes
 * on the specified topic and displays the message
 *
 * @author Enrico Casini (enrico.casini@gmail.com) 02/17/2018
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Views
    private EditText mEtEditMessage;
    private Button mBtnSendMessage;
    private TextView mTvViewMessage;

    // MQTT Client
    private MqttAndroidClient mMqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnSendMessage = findViewById(R.id.btn_send_message);
        mEtEditMessage = findViewById(R.id.et_edit_message);
        mTvViewMessage = findViewById(R.id.tv_view_message);

        // attempt connection and subscription to MQTT broker
        connectAndSubscribe();

        // Send message
        mBtnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mEtEditMessage.getText().toString().trim();

                // don't send if msg is empty
                if (msg.isEmpty()) return;

                // if mqtt client is null, handle gracefully for the user
                if (mMqttClient == null || !mMqttClient.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Client is not connected. Unable to send " , Toast.LENGTH_LONG).show();
                    return;
                }

                publishMessage(msg);
            }

        });
    }

    /**
     *  Publishes a message or displays a Toast for the user if failure.
     *
     * @param msg a String containing the message
     */
    private void publishMessage(String msg) {
        try {
            MqttConnectionFactory.publishMessage(mMqttClient, msg, 1, Config.TOPIC);
            //clear message after sent
            mEtEditMessage.getText().clear();
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

    /**
     * Attempts a connection to the MQTT broker at the URL specified in Config.MQTT_BROKER_URL.
     *
     * If connection is successful, subscribes via callback.
     */
    private void connectAndSubscribe() {
        // connect to server
        mMqttClient = MqttConnectionFactory.newClient(getApplicationContext(), Config.MQTT_BROKER_URL, Config.CLIENT_ID);
        // display error if unsuccessful
        if (mMqttClient == null) {
            Toast.makeText(getApplicationContext(), "Unable to connect to " + Config.MQTT_BROKER_URL, Toast.LENGTH_LONG).show();
            return;
        }

        mMqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                // connection was successful
                String greetMsg = "Connected to " + serverURI;
                Toast.makeText(getApplicationContext(), greetMsg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, greetMsg);

                try {
                    mMqttClient.subscribe(Config.TOPIC, 0, null, new IMqttActionListener() {
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
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                String message = new String(mqttMessage.getPayload());
                Log.d(TAG, "Received message: " + message + " for topic: " + topic);
                mTvViewMessage.setText(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

}
