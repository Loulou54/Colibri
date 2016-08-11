package com.game.colibri;

import java.util.Comparator;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Resultats extends Activity {
	
	public static int PERIODE = 1000/20;
	
	private RefreshHandler handler;
	private ResultatsAdapter adapt;
	private Niveau niv;
	private boolean display_res;
	private boolean has_won;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		display_res = (getIntent().getExtras()!=null);
		if(!display_res) { // Fin de campagne !
			setContentView(R.layout.fin_campagne);
			((Artifices) findViewById(R.id.artifices_doge)).doge = true;
			return;
		}
		setContentView(R.layout.activity_resultats);
		Defi defi;
		try {
			defi = Defi.DefiFromJSON(getIntent().getExtras().getString("defi"));
		} catch (JSONException e) {
			e.printStackTrace();
			finish();
			return;
		}
		ResultatsAdapter.prog=0;
		ResultatsAdapter.etape=0;
		handler = new RefreshHandler();
		Participation[] parts = new Participation[defi.participants.size()];
		for(int i=0; i<parts.length; i++) {
			parts[i] = defi.participants.valueAt(i);
		}
		adapt = new ResultatsAdapter(this, parts);
		ListView lv = (ListView) findViewById(R.id.listRes);
		lv.setAdapter(adapt);
		lv.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Joueur j = ((Participation) arg0.getAdapter().getItem(arg2)).joueur;
				(new DispJoueur(Resultats.this, j)).show();
			}
		});
		((TextView) findViewById(R.id.nomDefiRes)).setText(defi.nom);
		handler.sendMessageDelayed(handler.obtainMessage(0), 1200); // Commence les animations après 1200ms
		niv = new Niveau(defi.matchFini.mode, defi.matchFini.seed, defi.matchFini.param, defi.matchFini.avancement);
		has_won = (defi.participants.get(MyApp.id).gagne==1);
	}
	
	@Override
	protected void onStart() {
		MyApp.resumeActivity();
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		MyApp.stopActivity();
		super.onStop();
	}
	
	public interface callBackInterface {
		void suite();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus) {
			if(!display_res) {
				((TextView) findViewById(R.id.the_end)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.artifice_anim_spin));
				Toast toast = Toast.makeText(this, R.string.toast_fin, Toast.LENGTH_SHORT);
		    	TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
		    	if( v != null) v.setGravity(Gravity.CENTER);
		    	toast.show();
			} else {
				((Carte) findViewById(R.id.apercuResMini)).loadNiveau(niv);
				Carte c = ((Carte) findViewById(R.id.apercuResMaxi));
				c.loadNiveau(niv);
				c.setVisibility(View.GONE);
			}
	    	
		}
	}
	
	public void clickApercu(View v) {
		View c = findViewById(R.id.apercuResMaxi);
		if(c.getVisibility()==View.GONE) {
			c.setVisibility(View.VISIBLE);
			c.startAnimation(AnimationUtils.loadAnimation(this, R.anim.aleat_opt_anim));
		} else {
			c.setVisibility(View.GONE);
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
					sendMessageDelayed(obtainMessage(1), 600);
			} else {
				ResultatsAdapter.etape = msg.what;
				if(msg.what==4) {
					if(has_won)
						((Artifices) findViewById(R.id.artifices)).setVisibility(View.VISIBLE);
				}
				if(msg.what==5) {
					adapt.sort(new Comparator<Participation>() {
						@Override
						public int compare(Participation lhs, Participation rhs) {
							return lhs.t_fini+lhs.penalite_fini - (rhs.t_fini+rhs.penalite_fini);
						}
					});
				} else {
					sendMessageDelayed(obtainMessage(msg.what+1), 600);
					adapt.notifyDataSetChanged();
				}
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
			if(display_res && findViewById(R.id.apercuResMaxi).getVisibility()==View.VISIBLE) {
				findViewById(R.id.apercuResMaxi).setVisibility(View.GONE);
			} else {
				finish();
			}
			return true;
		}
		return false;
	}
    
}
