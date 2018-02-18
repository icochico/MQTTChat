package com.icochico.mqttchat;

/**
 * Config.java
 *
 * Simple interfaces holding connection constants.
 */

public interface Config {

    //String MQTT_BROKER_URL = "tcp://preprod-mqtt.aira.io:1883";
    String MQTT_BROKER_URL = "tcp://10.100.0.184:1883";
    String TOPIC = "chat/EnricoTest";
    String CLIENT_ID = "EnricoTest";
}
