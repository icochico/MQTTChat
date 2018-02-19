package com.icochico.mqttchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;

/**
 * Simple Activity that uses a LocalBroadcastManager that uses an Android Service to connect
 * to a MQTT broker (e.g. mosquitto) and execute the following actions:
 *  - subscribe to a topic
 *  - publish a message on that topic
 *  - displays the message
 *
 * @author Enrico Casini (enrico.casini@gmail.com) 02/17/2018
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Views
    private EditText mEtEditMessage;
    private TextView mTvViewMessage;

    // LocalBroadcastManager
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mBtnSendMessage = findViewById(R.id.btn_send_message);
        mEtEditMessage = findViewById(R.id.et_edit_message);
        mTvViewMessage = findViewById(R.id.tv_view_message);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Register with local action
        mLocalBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "Received message with action: " + intent.getAction());

                String msg = intent.getStringExtra(IntentExtras.MESSAGE);
                if (msg == null) {
                    Log.e(TAG, "Received null message from "
                            + MqttService.class.getSimpleName());
                    return;
                }

                mTvViewMessage.setText(msg);

            }
        }, new IntentFilter(Actions.ACTION_RECEIVE_MESSAGE));

        // Send message
        mBtnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mEtEditMessage.getText().toString().trim();

                // don't send if msg is empty
                if (msg.isEmpty()) return;

                mLocalBroadcastManager
                        .sendBroadcast(new Intent(Actions.ACTION_SEND_MESSAGE)
                                .putExtra(IntentExtras.MESSAGE, msg));

                //clear message after sent
                mEtEditMessage.getText().clear();
            }

        });

        // Start service
        startService(new Intent(this, MqttService.class));
    }
}
