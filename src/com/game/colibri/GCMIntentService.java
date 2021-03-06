package com.game.colibri;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.game.colibri.R;
import com.google.android.gcm.GCMBaseIntentService;

import static com.network.colibri.CommonUtilities.SENDER_ID;
import static com.network.colibri.CommonUtilities.broadcastMessage;

public class GCMIntentService extends GCMBaseIntentService {
 
    private static final String TAG = "GCMIntentService";
 
    public GCMIntentService() {
        super(SENDER_ID);
    }
 
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        broadcastMessage(context, "GCMRegistartion");
    }
 
    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        broadcastMessage(context, "GCMUnRegistartion");
    }
 
    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("msg");
         
        broadcastMessage(context, message);
        // notifies user
        if(!Multijoueur.active)
        	generateNotification(context, message);
    }
 
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
    }
 
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }
 
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		int id = 3 + (int) (Math.random()*(Integer.MAX_VALUE-3));
		long when = System.currentTimeMillis();
		String title = context.getString(R.string.app_name), msg=message;
		SharedPreferences pref = MyApp.getApp().pref;
		SharedPreferences.Editor editor = MyApp.getApp().editor;
		int nNewM = pref.getInt("nNewM", 0), nRes = pref.getInt("nRes", 0);
		long lastNotif = pref.getLong("lastNotif", 0);
		try {
			JSONObject o = new JSONObject(message);
			String typ = o.getString("type");
			if(typ.equals("newMatch")) {
				id = 1;
				nNewM++;
				if(nNewM>1) {
					title = context.getString(R.string.nouveau_defi);
					msg = context.getString(R.string.notif_n_newmatch, nNewM);
				} else {
					title = o.getString("nomDefi");
					msg = context.getString(R.string.notif_newdefi, o.getString("initPlayer"));
				}
			} else if(typ.equals("results")) {
				id = 2;
				nRes++;
				if(nRes>1) {
					title = context.getString(R.string.etat_resultats);
					msg = context.getString(R.string.notif_n_results, nRes);
				} else {
					title = o.getString("nomDefi");
					if(o.has("initPlayer"))
						msg = context.getString(R.string.notif_results, o.getString("initPlayer"));
					else
						msg = context.getString(R.string.notif_results_exp);
				}
			} else if(typ.equals("message")) {
				if(o.has("title"))
					title = o.getString("title");
				msg = o.getString("message");
			}
			editor.putInt("nNewM", nNewM)
				.putInt("nRes", nRes)
				.putLong("lastNotif", when)
				.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(context, MenuPrinc.class);
		// On ajoute les infos dans l'Intent :
		notificationIntent.putExtra("com.game.colibri.notification", message);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		// The notification :
		Notification notification = new NotificationCompat.Builder(context)
			.setContentIntent(intent)
			.setSmallIcon(icon)
			.setContentTitle(title)
		    .setContentText(msg)
		    .setTicker(msg)
		    .setWhen(when)
		    .setAutoCancel(true)
		    .build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// Play default notification sound
		//notification.defaults |= Notification.DEFAULT_SOUND;
		// Vibrate if vibrate is enabled
		if(when - lastNotif > 5000) // Pour ne pas faire vibrer le téléphone à chaque notif lors de rafales !
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(id, notification);
	}

}
