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
	
	public static Jeu jeu=null;
	public static Multijoueur multi;
	public static int PERIODE = 1000/20;
	
	private int t1,t2;
	private double prog; // Progression
	private TextView j1,j2,r1,r2,score;
	private RefreshHandler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(jeu==null) { // Fin de campagne !
			setContentView(R.layout.fin_campagne);
			((Artifices) findViewById(R.id.artifices_doge)).doge = true;
			return;
		}
		setContentView(R.layout.activity_resultats);
		t1=multi.temps1;
		t2=multi.temps2;
		prog=0;
		handler = new RefreshHandler();
		score = (TextView) findViewById(R.id.score_multi);
		int s1=multi.j.getLost(),s2=multi.j.getWin();
		if(multi.gagne==1)
			s1--;
		else if(multi.gagne==2)
			s2--;
		score.setText(s1+" -=- "+s2);
		j1 = (TextView) findViewById(R.id.joueur1);
		j2 = (TextView) findViewById(R.id.joueur2);
		j1.setText(multi.user.getPseudo());
		j2.setText(multi.j.getPseudo());
		r1 = (TextView) findViewById(R.id.resultats1);
		r2 = (TextView) findViewById(R.id.resultats2);
		r1.setText((t1==Integer.MAX_VALUE) ? getString(R.string.forfait)+" !" : Jeu.getFormattedTime(0)+"\n\n");
		r2.setText((t2==Integer.MAX_VALUE) ? getString(R.string.forfait)+" !" : Jeu.getFormattedTime(0)+"\n\n");
		if(t1!=Integer.MAX_VALUE || t2!=Integer.MAX_VALUE)
			handler.sendMessageDelayed(handler.obtainMessage(0), 1200); // Pour différer le démarrage du comptage
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus && jeu==null) {
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
		if(multi.temps1!=Integer.MAX_VALUE)
			r1.setText(Jeu.getFormattedTime(t1)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(multi.penalite1));
		if(multi.temps2!=Integer.MAX_VALUE)
			r2.setText(Jeu.getFormattedTime(t2)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(multi.penalite2));
		if(what==1)
			handler.sendMessageDelayed(handler.obtainMessage(2), 800);
		else
			handler.sendMessageDelayed(handler.obtainMessage(3), 800);
	}
	
	private void updateScore() {
		if(multi.temps1!=Integer.MAX_VALUE)
			r1.setText(Jeu.getFormattedTime(t1)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(multi.penalite1)+"\n"+getString(R.string.exp)+" : "+multi.exp1);
		if(multi.temps2!=Integer.MAX_VALUE)
			r2.setText(Jeu.getFormattedTime(t2)+"\n"+getString(R.string.penalite_aide)+" :\n + "+Jeu.getFormattedTime(multi.penalite2)+"\n"+getString(R.string.exp)+" : "+multi.exp2);
		score.setText(multi.j.getLost()+" -=- "+multi.j.getWin());
		score.startAnimation(AnimationUtils.loadAnimation(this, R.anim.aleat_opt_anim));
		handler.sendMessageDelayed(handler.obtainMessage(4), 800);
	}
	
	private void gagnant() {
		Artifices a=null;
		TextView g=null;
		if(multi.gagne==1) {
			a = (Artifices) findViewById(R.id.artifices1);
			g = (TextView) findViewById(R.id.gagne1);
		} else if(multi.gagne==2) {
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
				t1+=multi.penalite1;
				t2+=multi.penalite2;
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
	    	if(jeu!=null)
	    		jeu.quitter(null);
	    	else {
	    		Toast toast = Toast.makeText(this, R.string.toast_fin2, Toast.LENGTH_LONG);
		    	TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
		    	if( v != null) v.setGravity(Gravity.CENTER);
		    	toast.show();
	    	}
	    	jeu=null;
	        return true;
    	}
    	return false;
    }
    
}
