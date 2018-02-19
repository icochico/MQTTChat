package com.icochico.mqttchat;

/**
 * List of Actions supported by the app's LocalBroadcastManagers;
 *
 * @author Enrico Casini (enrico.casini@gmail.com)
 */

interface Actions {

    String ACTION_SEND_MESSAGE = "com.icochico.mqttchat.actionSendMessage";
    String ACTION_RECEIVE_MESSAGE = "com.icochico.mqttchat.actionReceiveMessage";
}
