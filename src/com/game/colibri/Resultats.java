package com.game.colibri;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

public class Resultats extends Activity {
	
	public static boolean DISPLAY_RES = false; // Détermine s'il faut afficher les résultats ou juste le feu d'artifices
	public static callBackInterface callback;
	public static Multijoueur multi;
	public static int PERIODE = 1000/20;
	
	private Participation[] participations;
	private int t1,t2;
	private double prog; // Progression
	private TextView j1,j2,r1,r2,score;
	private RefreshHandler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!DISPLAY_RES) { // Fin de campagne !
			setContentView(R.layout.fin_campagne);
			((Artifices) findViewById(R.id.artifices_doge)).doge = true;
			return;
		}
		setContentView(R.layout.activity_resultats);
		participations = new Participation[multi.defi.participants.size()+1]; // TODO /!\
		int i = 0;
		for(Participation part : multi.defi.participants.values()) {
			participations[i++] = part;
		}
		participations[1] = participations[0]; // TODO /!\
		t1=participations[0].t_fini;
		t2=participations[1].t_fini;
		prog=0;
		handler = new RefreshHandler();
		score = (TextView) findViewById(R.id.score_multi);
		int s1=participations[0].win,s2=participations[1].win;
		if(participations[0].gagne)
			s1--;
		else if(participations[1].gagne)
			s2--;
		score.setText(s1+" -=- "+s2);
		j1 = (TextView) findViewById(R.id.joueur1);
		j2 = (TextView) findViewById(R.id.joueur2);
		j1.setText(participations[0].joueur.getPseudo());
		j2.setText(participations[1].joueur.getPseudo());
		r1 = (TextView) findViewById(R.id.resultats1);
		r2 = (TextView) findViewById(R.id.resultats2);
		r1.setText((t1==Integer.MAX_VALUE) ? getString(R.string.forfait)+" !" : Jeu.getFormattedTime(0)+"\n\n");
		r2.setText((t2==Integer.MAX_VALUE) ? getString(R.string.forfait)+" !" : Jeu.getFormattedTime(0)+"\n\n");
		if(t1!=Integer.MAX_VALUE || t2!=Integer.MAX_VALUE)
			handler.sendMessageDelayed(handler.obtainMessage(0), 1200); // Pour différer le démarrage du comptage
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
	
	private void updateTime() {
		prog+=0.02;
		if(t1!=Integer.MAX_VALUE)
			r1.setText(Jeu.getFormattedTime((int) (t1*prog))+"\n\n");
		if(t2!=Integer.MAX_VALUE)
			r2.setText(Jeu.getFormattedTime((int) (t2*prog))+"\n\n");
		if(prog<1)
			handler.sleep(PERIODE);
		else
			handler.sendMessageDelayed(handler.obtainMessage(1), 800);
	}
	
	private void solution(int what) {
		if(participations[0].t_fini!=Integer.MAX_VALUE)
			r1.setText(Jeu.getFormattedTime(t1)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(participations[0].penalite_fini));
		if(participations[1].t_fini!=Integer.MAX_VALUE)
			r2.setText(Jeu.getFormattedTime(t2)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(participations[1].penalite_fini));
		if(what==1)
			handler.sendMessageDelayed(handler.obtainMessage(2), 800);
		else
			handler.sendMessageDelayed(handler.obtainMessage(3), 800);
	}
	
	private void updateScore() {
		if(participations[0].t_fini!=Integer.MAX_VALUE)
			r1.setText(Jeu.getFormattedTime(t1)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(participations[0].penalite_fini)+"\n"+getString(R.string.exp)+" : "+participations[0].exp);
		if(participations[1].t_fini!=Integer.MAX_VALUE)
			r2.setText(Jeu.getFormattedTime(t2)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(participations[1].penalite_fini)+"\n"+getString(R.string.exp)+" : "+participations[1].exp);
		score.setText(participations[0].win+" -=- "+participations[1].win);
		score.startAnimation(AnimationUtils.loadAnimation(this, R.anim.aleat_opt_anim));
		handler.sendMessageDelayed(handler.obtainMessage(4), 800);
	}
	
	private void gagnant() {
		Artifices a=null;
		TextView g=null;
		if(participations[0].gagne) {
			a = (Artifices) findViewById(R.id.artifices1);
			g = (TextView) findViewById(R.id.gagne1);
		} else if(participations[1].gagne) {
			a = (Artifices) findViewById(R.id.artifices2);
			g = (TextView) findViewById(R.id.gagne2);
		}
		if(a!=null) {
			a.setVisibility(View.VISIBLE);
			g.setVisibility(View.VISIBLE);
			g.startAnimation(AnimationUtils.loadAnimation(this, R.anim.aleat_opt_anim));
		}
	}
	
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==0) {
				updateTime();
			} else if(msg.what==1) {
				solution(1);
			} else if(msg.what==2) {
				t1+=participations[0].penalite_fini;
				t2+=participations[1].penalite_fini;
				solution(2);
			} else if(msg.what==3) {
				updateScore();
			} else if(msg.what==4) {
				gagnant();
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
