package com.network.colibri;

import android.content.Context;
import android.content.Intent;

public final class CommonUtilities {
     
    // give your server registration url here
    public static final String SERVER_URL = "https://ssl17.ovh.net/~louisworix/colibri"; 
 
    // Google project id
    public static final String SENDER_ID = "533995009920"; 
    
    // Communication token
    public static final String APP_TOKEN = "az-79A_uG7*uYf42v2"; 
 
    /**
     * Tag used on log messages.
     */
    public static final String TAG = "Colibri GCM";
 
    public static final String BROADCAST_MESSAGE_ACTION =
            "com.game.colibri.BROADCAST_MESSAGE";
 
    public static final String EXTRA_MESSAGE = "message";
 
    /**
     * Envoie un message au BroadcastReceiver de Multijoueur
     *
     * @param context application's context.
     * @param message message to be broadcasted.
     */
    public static void broadcastMessage(Context context, String message) {
        Intent intent = new Intent(BROADCAST_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
