package com.game.colibri;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Activité gérant le jeu proprement dit. Elle affiche notamment la View "Carte" en plein écran.
 */
public class Jeu extends Activity {
	
	
	public static Bundle opt;
	public Niveau niv;
	public Carte carte;
	public MoteurJeu play;
	public RelativeLayout lay;
	public RelativeLayout pause;
	public RelativeLayout perdu; 
	public RelativeLayout gagne; 
	public Button bout_dyna;
	private boolean brandNew=true;
	public int n_niv=1;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jeu);
		carte = (Carte) findViewById(R.id.carte);
		lay = (RelativeLayout) findViewById(R.id.lay);
		bout_dyna = (Button) findViewById(R.id.bout_dyna);
		pause= (RelativeLayout) findViewById(R.id.pause);
        perdu= (RelativeLayout) findViewById(R.id.perdu);
		gagne= (RelativeLayout) findViewById(R.id.gagner);
		play = new MoteurJeu(this,carte);
		Log.i("onCreate","FINI");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (brandNew) { // événement appelé lorsque le RelativeLayout "lay" est prêt ! C'est ici que l'on peut charger le niveau et ajouter les View "Animal".
			launch_niv();
			brandNew=false;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent (MotionEvent ev) {
		if(play.isRunning)
			play.onTouch(ev);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			play.pause();
			pause.setVisibility(View.VISIBLE);
		}
		if(play.isRunning) { // Commande en cours de jeu
			if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				play.direction(MoteurJeu.UP);
			} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				play.direction(MoteurJeu.RIGHT);
			} else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				play.direction(MoteurJeu.LEFT);
			} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				play.direction(MoteurJeu.DOWN);
			} else if(keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_ESCAPE) {
				play.pause();
			}
		} else { // Jeu en pause
			if(keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_ESCAPE) {
				play.start();
			}
		}
	return true;
	}
	
	// Événement déclenché par "play" lorsque le niveau a été gagné.
	/**
	 * Affiche que le niveau à été gagné et charge le niveau suivant 
	 * 
	 */
	public void gagne() {
		play.pause();
		gagne.setVisibility(View.VISIBLE);
		if(carte.n_dyna>0) hideDyna();
		Log.i("C'est Gagné !","BRAVO !");
	}
	
	/**
	 * Le colibri est mort : affiche l'écran associé.
	 */
	public void mort() {
		perdu.setVisibility(View.VISIBLE);
		Log.i("Oh non !","Vous vous êtes fait écraser !");
	}
	
	/**
	 * Explose le menhir en face du colibri à l'aide d'une dynamite. Appelé par l'appui sur le bouton dédié.
	 * 	@param v le bouton
	 */
	public void exploser(View v) {
		if(play.isRunning && carte.n_dyna>0)
			play.dynamite();
	}
	
	/**
	 * Montre le bouton des dynamites avec une animation.
	 */
	public void showDyna() {
		bout_dyna.setVisibility(View.VISIBLE);
		bout_dyna.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bouton_dyna_down));
	}
	
	/**
	 * Cache le bouton des dynamites avec une animation.
	 */
	public void hideDyna() {
		bout_dyna.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bouton_dyna_up));
		bout_dyna.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * S'occupe de charger un niveau dans la "carte" et de lancer le moteur de jeu "play".
	 */
	private void launch_niv() {
		if(opt.getBoolean("isRandom")) {
			niv = new Niveau(opt.getInt("long"),opt.getInt("vari"));
		} else {
			try { // On ouvre le Niveau index_niv.
				niv = new Niveau(this.getAssets().open("niveaux/niveau"+n_niv+".txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		carte.loadNiveau(niv,lay);
		lay.removeView(bout_dyna);
		lay.addView(bout_dyna); // Astuce pour mettre le bouton au premier plan
		play.init();
		play.start();
	}
	
	/**
	 * Les fonctions suivantes sont déclenchées par les appuis sur les boutons des menus Pause, Gagné et Mort.
	 */
	
	public void reprendre(View v) {
		pause.setVisibility(View.INVISIBLE);
        play.start(); 
	}
	
	public void recommencer(View v) {
		pause.setVisibility(View.INVISIBLE);
		perdu.setVisibility(View.INVISIBLE);
    	if(carte.n_dyna>0) hideDyna();
    	launch_niv();
	}
	
	public void quitter(View v) {
    	Jeu.this.finish();
	}
	
	public void suivant(View v) {
		gagne.setVisibility(View.INVISIBLE);
		n_niv++;
		launch_niv();
	}
	
}
