package com.game.colibri;

import java.io.IOException;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Activité gérant le jeu proprement dit. Elle affiche notamment la View "Carte" en plein écran.
 */
public class Jeu extends Activity {
	
	
	public static Bundle opt;
	public static MenuPrinc menu;
	public static Multijoueur multi = null;
	public static final int NIV_MAX=36;
	
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
				if(play.isRunning) {
					play.pause();
					pause.setVisibility(View.VISIBLE);
				}
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
			} else
				return false;
		} else if(pause.getVisibility()==View.VISIBLE) { // Jeu en pause
			if(keyCode == KeyEvent.KEYCODE_BACK)
				reprendre(gagne);
			else
				return false;
		} else {
			if(keyCode == KeyEvent.KEYCODE_BACK && multi==null) {
				menu.setDebut(new GregorianCalendar());
				recommencer(gagne);
			}
			else
				return false;
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
				TextView txt = (TextView) findViewById(R.id.multi_res);
				String tps1=multi.temps1/1000+"."+(multi.temps1%1000)/10;
				String tps2=multi.temps2/1000+"."+(multi.temps2%1000)/10;
				int exp1,exp2;
				if(multi.temps1 > multi.temps2) {
					exp1=play.niv.experience/2;
					exp2=play.niv.experience*2;
				} else {
					exp1=play.niv.experience*2;
					exp2=play.niv.experience/2;
				}
				multi.finDefi(exp1,exp2);
				txt.setText(getString(R.string.temps)+" : "+tps1+"  vs  "+tps2
						+"\n"+getString(R.string.exp)+" : + "+exp1+"  -=-  + "+exp2
						+"\n"+getString(R.string.score)+" "+(multi.j.getDefis()-multi.j.getWin())+"  -=-  "+multi.j.getWin());
				gagneMulti2.setVisibility(View.VISIBLE);
			}
		}
		else {
			int exp=0;
			if(!solUsed) {
				if(opt.getInt("mode",0)>0) {
					exp=play.niv.experience;
				} else {
					if(n_niv==menu.avancement) {
						menu.avancement++;
						exp=n_niv*(50+n_niv/4);
					} else
						exp=n_niv*(10+n_niv/8);
				}
				menu.experience+=exp;
				menu.saveData(); // On sauvegarde la progression.
			}
			TextView txt = (TextView) findViewById(R.id.resultats);
			txt.setText(getString(R.string.temps)+" : "+temps/1000+"."+(temps%1000)/10
					+"\n"+getString(R.string.exp)+" : + "+exp);
			gagne.setVisibility(View.VISIBLE);
		}
	}
	
	public void secondJoueur(View v) {
		menu.setDebut(new GregorianCalendar());
		RelativeLayout gagneMulti = (RelativeLayout)findViewById(R.id.gain);
		gagneMulti.setVisibility(View.INVISIBLE);
		recommencer(v);
	}
	
	public void fin(View v){
		music=false;
		multi=null;
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
			if(opt.getInt("mode", 0)>0) {
				niv = new Niveau(opt.getInt("mode"));
			} else {
				try { // On ouvre le Niveau index_niv.
					niv = new Niveau(this.getAssets().open("niveaux/niveau"+n_niv+".txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		solUsed=false;
		Button so=(Button) findViewById(R.id.but4);
		if(opt.getInt("mode", 0)>0 && multi==null) { // Pour niveaux : niv.solution!=null && n_niv<menu.avancement
			so.setVisibility(View.VISIBLE);
		} else {
			so.setVisibility(View.INVISIBLE);
		}
		carte.loadNiveau(niv);
		play.init();
		play.start();
	}
	
	/*
	 * Les fonctions suivantes sont déclenchées par les appuis sur les boutons des menus Pause, Gagné et Mort.
	 */
	
	public void reprendre(View v) {
		GregorianCalendar finpause = new GregorianCalendar();
		GregorianCalendar debut = menu.getDebut();
		long diff = (finpause.getTimeInMillis()-debutPause.getTimeInMillis());
		debut.setTimeInMillis(diff+debut.getTimeInMillis());
		menu.setDebut(debut);
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
		multi=null;
    	this.finish();
	}
	
	public void solution(View v) {
		recommencer(v);
		solUsed=true;
    	play.solution();
	}
		
	public void suivant(View v) {
		gagne.setVisibility(View.INVISIBLE);
		menu.setDebut(new GregorianCalendar());
		if(n_niv==NIV_MAX) {
			quitter(v);
			return;
		}
		if(opt.getInt("mode", 0)==Niveau.CAMPAGNE) n_niv++;
		launch_niv(false);
	}
	
	public long getTemps() {
		return temps;
	}
}
