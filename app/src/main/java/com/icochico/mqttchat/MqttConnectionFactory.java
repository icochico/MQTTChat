package com.icochico.mqttchat;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

/**
 * MqttConnectionFactory.java
 *
 * <p>
 * Created by Enrico Casini on 2/17/18. (enrico.casini@gmail.com)
 */

public class MqttConnectionFactory {

    private static final String TAG = "MqttConnectionFactory";

    /**
     * Creates a new instance of the MqttAndroidClient
     *
     * @param context the Android Context object
     * @param brokerUrl the URL at which the broker resides
     * @param clientId a client id
     *
     * @return a new instance of the MqttAndroidClient
     */
    static MqttAndroidClient newClient(Context context, String brokerUrl, String clientId) {

        final MqttAndroidClient client = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = client.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable t) {
                    Log.e(TAG, "Failure ", t);
                    t.printStackTrace();
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error during connection ", e);
        }

        return client;
    }

    /**
     * Disconnects the client from the broker.
     *
     * @param client the client to be disconnected
     *
     * @throws MqttException if failure
     */
    public static void disconnect(@NonNull MqttAndroidClient client) throws MqttException {
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
            }
        });
    }

    /**
     * Creates a new instance of DisconnectedBufferOptions with a common options.
     *
     * @return a new instance of DisconnectedBufferOptions with a common options
     */
    @NonNull
    private static DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    /**
     * Creates a new instance of MqttConnectOptions with common options.
     *
     * @return a new instance of MqttConnectOptions with common options
     */
    @NonNull
    private static MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        return mqttConnectOptions;
    }


    /**
     *
     * Publishes a message through the following MqttClient.
     *
     *
     * @param client the client that needs to publish the message
     * @param msg a String representing the message
     * @param qos the desired QoS
     * @param topic the topic where we want to publish the message
     * @throws MqttException if failure in publishing
     * @throws UnsupportedEncodingException if the chosen coding for the message is unsupported.
     */
    public static void publishMessage(@NonNull MqttAndroidClient client, @NonNull String msg, int qos, @NonNull String topic)
            throws MqttException, UnsupportedEncodingException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(320);
        message.setRetained(true);
        message.setQos(qos);
        client.publish(topic, message);
    }

    /**
     * Subscribes the client to the specified topic at the specified QoS.
     *
     * @param client the client that needs to subscribe
     * @param topic the topic for the subscription
     * @param qos the desired QoS
     * @throws MqttException if failure in subscribing
     */
    public static void subscribe(@NonNull MqttAndroidClient client, @NonNull final String topic, int qos) throws MqttException {
        IMqttToken token = client.subscribe(topic, qos);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Subscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed " + topic);

            }
        });
    }

    /**
     * Unsubscribes the client from the specified topic.
     *
     * @param client the client to be unsubscribed
     * @param topic the topic from which we want unsubscribe
     * @throws MqttException
     */
    public static void unSubscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {

        IMqttToken token = client.unsubscribe(topic);

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "UnSubscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "UnSubscribe Failed " + topic);
            }
        });
    }

}

