package com.game.colibri;

import java.io.IOException;
import java.util.GregorianCalendar;

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
	public static MenuPrinc menu;
	public static Multijoueur multi = null;
	
	public Niveau niv;
	public Carte carte;
	public MoteurJeu play;
	public RelativeLayout pause;
	public RelativeLayout perdu; 
	public RelativeLayout gagne; 
	public Button bout_dyna;
	private boolean brandNew=true, solUsed=false, music=false;
	public int n_niv;
	public GregorianCalendar debutPause;
	private long temps = 0;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jeu);
		n_niv=opt.getInt("n_niv", 1);
		carte = (Carte) findViewById(R.id.carte);
		bout_dyna = (Button) findViewById(R.id.bout_dyna);
		pause= (RelativeLayout) findViewById(R.id.pause);
        perdu= (RelativeLayout) findViewById(R.id.perdu);
		gagne= (RelativeLayout) findViewById(R.id.gagner);
		play = new MoteurJeu(this,carte);
		if(MenuPrinc.intro!=null)
			music=MenuPrinc.intro.isPlaying();
		else
			music=MenuPrinc.boucle.isPlaying();
		Log.i("onCreate","FINI");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (brandNew) { // événement appelé lorsque le RelativeLayout "lay" est prêt ! C'est ici que l'on peut charger le niveau et ajouter les View "Animal".
			launch_niv(false);
			brandNew=false;
		} else {
			if(!hasFocus) {
				debutPause = new GregorianCalendar();
				play.pause();
				pause.setVisibility(View.VISIBLE);
				if(music)
					MenuPrinc.stopMusic();
			} else {
				if(music)
					MenuPrinc.startMusic();
			}
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
		if(play.isRunning) { // Commande en cours de jeu
			if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				play.direction(MoteurJeu.UP);
			} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				play.direction(MoteurJeu.RIGHT);
			} else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				play.direction(MoteurJeu.LEFT);
			} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				play.direction(MoteurJeu.DOWN);
			} else if((keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_ESCAPE) && niv.solution!=null && n_niv<menu.avancement && multi==null) { // Solution !
				if(carte.n_dyna>0) hideDyna();
		    	launch_niv(true);
				solUsed=true;
		    	play.solution();
			} else if(keyCode == KeyEvent.KEYCODE_BACK) {
				debutPause = new GregorianCalendar();
				play.pause();
				pause.setVisibility(View.VISIBLE);
			}
		} else if(pause.getVisibility()==View.VISIBLE) { // Jeu en pause
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				GregorianCalendar finpause = new GregorianCalendar();
				GregorianCalendar debut = menu.getDebut();
				long diff = (finpause.getTimeInMillis()-debutPause.getTimeInMillis());
				debut.setTimeInMillis(diff+debut.getTimeInMillis());
				menu.setDebut(debut);
				pause.setVisibility(View.INVISIBLE);
		        play.start(); 
			}
		} else {
			recommencer(gagne);
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
		if(carte.n_dyna>0) hideDyna();
		GregorianCalendar fin = new GregorianCalendar();
		temps = fin.getTimeInMillis() - menu.getDebut().getTimeInMillis();
		if (multi != null) { // Mode multijoueur
			if (multi.temps1 == 0) {
				multi.temps1 = temps;
				RelativeLayout gagneMulti = (RelativeLayout)findViewById(R.id.gain);
				gagneMulti.setVisibility(View.VISIBLE);
			}
			else {
				multi.temps2 = temps;
				RelativeLayout gagneMulti2 = (RelativeLayout)findViewById(R.id.fin);
				gagneMulti2.setVisibility(View.VISIBLE);
			}
		}
		else {
			gagne.setVisibility(View.VISIBLE);
			if(!solUsed) {
				if(opt.getBoolean("isRandom")) {
					menu.experience+=100+play.niv.solution.length*10;
				} else {
					if(n_niv==menu.avancement) {
						menu.avancement++;
						menu.experience+=100+n_niv*40;
					} else
						menu.experience+=n_niv*10;
				}
				menu.saveData(); // On sauvegarde la progression.
			}
		}
		Log.i("C'est Gagné !","BRAVO !");
	}
	
	public void secondJoueur(View v) {
		menu.setDebut(new GregorianCalendar());
		RelativeLayout gagneMulti = (RelativeLayout)findViewById(R.id.gain);
		gagneMulti.setVisibility(View.INVISIBLE);
		recommencer(v);
	}
	
	public void fin(View v){
		multi.finDefi(play.niv.solution.length);
		music=false;
		this.finish();
	}
	
	/**
	 * Le colibri est mort : affiche l'écran associé.
	 */
	public void mort() {
		if(!play.isRunning && pause.getVisibility()==View.INVISIBLE) {
			perdu.setVisibility(View.VISIBLE);
		}
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
	private void launch_niv(boolean replay) {
		if(replay) {
			niv.replay();
		} else {
			if(opt.getBoolean("isRandom")) {
				niv = new Niveau(opt.getInt("long"),opt.getInt("vari"));
			} else {
				try { // On ouvre le Niveau index_niv.
					niv = new Niveau(this.getAssets().open("niveaux/niveau"+n_niv+".txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		solUsed=false;
		carte.loadNiveau(niv);
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
		gagne.setVisibility(View.INVISIBLE);
		perdu.setVisibility(View.INVISIBLE);
    	if(carte.n_dyna>0) hideDyna();
    	launch_niv(true);
	}
	
	public void quitter(View v) {
		music=false;
    	this.finish();
	}
	
	public void suivant(View v) {
		gagne.setVisibility(View.INVISIBLE);
		if(!opt.getBoolean("isRandom")) n_niv++;
		launch_niv(false);
	}
	
	public long getTemps() {
		return temps;
	}
}
