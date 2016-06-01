package com.game.colibri;

import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Resultats extends Activity {
	
	public static boolean DISPLAY_RES = false; // Détermine s'il faut afficher les résultats ou juste le feu d'artifices
	public static callBackInterface callback;
	public static Multijoueur multi;
	public static int PERIODE = 1000/20;
	
	private RefreshHandler handler;
	private ResultatsAdapter adapt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!DISPLAY_RES) { // Fin de campagne !
			setContentView(R.layout.fin_campagne);
			((Artifices) findViewById(R.id.artifices_doge)).doge = true;
			return;
		}
		setContentView(R.layout.activity_resultats);
		ResultatsAdapter.prog=0;
		ResultatsAdapter.etape=0;
		handler = new RefreshHandler();
		Participation[] parts = new Participation[multi.defi.participants.size()];
		for(int i=0; i<parts.length; i++) {
			parts[i] = multi.defi.participants.valueAt(i);
		}
		adapt = new ResultatsAdapter(this, parts);
		((ListView) findViewById(R.id.listRes)).setAdapter(adapt);
		((TextView) findViewById(R.id.nomDefiRes)).setText(multi.defi.nom);
		handler.sendMessageDelayed(handler.obtainMessage(0), 1200); // Commence les animations après 1200ms
	}
	
	public interface callBackInterface {
		void suite();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus && !DISPLAY_RES) {
			((TextView) findViewById(R.id.the_end)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.artifice_anim_spin));
			Toast toast = Toast.makeText(this, R.string.toast_fin, Toast.LENGTH_SHORT);
	    	TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
	    	if( v != null) v.setGravity(Gravity.CENTER);
	    	toast.show();
		}
	}
	
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==0) {
				ResultatsAdapter.prog+=0.02;
				adapt.notifyDataSetChanged();
				if(ResultatsAdapter.prog<1)
					sleep(PERIODE);
				else
					sendMessageDelayed(obtainMessage(1), 800);
			} else {
				ResultatsAdapter.etape = msg.what;
				if(msg.what==4) {
					if(multi.defi.participants.get(multi.user.getId()).gagne==1)
						((Artifices) findViewById(R.id.artifices)).setVisibility(View.VISIBLE);
				}
				if(msg.what==5) {
					adapt.sort(new Comparator<Participation>() {
						@Override
						public int compare(Participation lhs, Participation rhs) {
							return lhs.t_cours+lhs.penalite_cours - (rhs.t_cours+rhs.penalite_cours);
						}
					});
				} else
					sendMessageDelayed(obtainMessage(msg.what+1), 800);
				adapt.notifyDataSetChanged();
			}
		}
		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
	    	finish();
	    	callback.suite();
	    	DISPLAY_RES = false;
	        return true;
    	}
    	return false;
    }
    
}
