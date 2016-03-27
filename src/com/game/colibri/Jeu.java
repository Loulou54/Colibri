package com.game.colibri;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activité gérant le jeu proprement dit. Elle affiche notamment la View "Carte" en plein écran.
 */
public class Jeu extends Activity {
	
	
	public static Bundle opt;
	public static MenuPrinc menu;
	public static Multijoueur multi = null;
	public static final int NIV_MAX=36;
	public static String startMsg = null; // Message avant le lancement du jeu
	
	public Niveau niv;
	public Carte carte;
	public MoteurJeu play;
	public View menu_lateral;
	public View pause;
	public View perdu; 
	public View gagne; 
	public Button bout_dyna;
	private boolean brandNew=true, solUsed=false, solvedBySol=false, solved=false, music=false, popup=false, finipartous=false;
	public int n_niv;
	
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
		menu_lateral = findViewById(R.id.menu_lateral);
		menu_lateral.findViewById(R.id.av_rapide).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					MoteurJeu.PERIODE = 1000/50; // Vitesse * 2
					break;
				case MotionEvent.ACTION_UP:
					MoteurJeu.PERIODE = 1000/25;
					v.performClick();
					break;
				default:
					break;
				}
				return true;
			}
		});
		pause= findViewById(R.id.pause);
		((ViewStub) pause).setOnInflateListener(new ViewStub.OnInflateListener() {
			@Override
			public void onInflate(ViewStub stub, View inflated) {
				pause = inflated;
			}
		});
        perdu= findViewById(R.id.perdu);
		gagne= findViewById(R.id.gagner);
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
				if(play.isRunning) {
					play.pause();
					pause.setVisibility(View.VISIBLE);
				}
				if(music && !popup)
					MenuPrinc.stopMusic();
				popup=false;
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
			menuLateral(play.onTouch(ev), ev);
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
			} else if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_ESCAPE) {
				play.pause();
				menuLateral(0,null);
				pause.setVisibility(View.VISIBLE);
			} else
				return false;
		} else if(pause.getVisibility()==View.VISIBLE) { // Jeu en pause
			if(keyCode == KeyEvent.KEYCODE_BACK)
				reprendre(null);
			else
				return false;
		} else {
			return false;
		}
	return true;
	}
	
	private void setMenuLateral() {
		TextView mode = (TextView) findViewById(R.id.mode);
		TextView niv = (TextView) findViewById(R.id.niveau_courant);
		mode.setText(R.string.aleatoire);
		switch(opt.getInt("mode", 0)) {
		case Niveau.CAMPAGNE:
			mode.setText(R.string.campagne);
			niv.setText(n_niv+"/"+NIV_MAX);
			break;
		case Niveau.FACILE:
			niv.setText(R.string.fac);
			break;
		case Niveau.MOYEN:
			niv.setText(R.string.moy);
			break;
		case Niveau.DIFFICILE:
			niv.setText(R.string.dif);
			break;
		default:
			niv.setText(R.string.perso);
		}
	}
	
	public void updateMenuLateral() {
		if(menu_lateral.getVisibility()==View.VISIBLE) {
			int t_ms = (play.total_frames+play.frame)*MoteurJeu.PERIODE;
			((TextView) menu_lateral.findViewById(R.id.temps)).setText(
					String.format("%d.%02d s", t_ms/1000, (t_ms%1000)/10)
			);
		}
	}
	
	// Événement déclenché par "play" lorsque le niveau a été gagné.
	/**
	 * Affiche que le niveau a été gagné et charge le niveau suivant 
	 * 
	 */
	public void gagne(int temps_total_ms) {
		if(solvedBySol && !solved) {
			recommencer(null);
			return;
		}
		gagne.setVisibility(View.VISIBLE);
		play.pause();
		play.frame = 0;
		play.total_frames = 0;
		if(carte.n_dyna>0) hideDyna();
		menuLateral(0,null);
		TextView txt = (TextView) findViewById(R.id.resultats);
		if (multi != null) { // Mode multijoueur
			if(!solved) {
				finipartous = multi.defi.finMatch(multi.user.getPseudo(), temps_total_ms, (solUsed) ? niv.solution.length*500 : 0);
				multi.syncData();
			} else {
				Participation p = multi.defi.participants.get(multi.user.getPseudo());
				temps_total_ms = finipartous ? p.t_fini : p.t_cours;
				solUsed = (finipartous ? p.penalite_fini : p.penalite_cours)!=0;
			}
			String s;
			if(temps_total_ms==Participation.FORFAIT)
				s=getString(R.string.forfait)+" !";
			else
				s=getString(R.string.temps)+" : "+getFormattedTime(temps_total_ms)
						+"\n"+getString(R.string.aide)+" : "+(solUsed ? getString(R.string.oui) : getString(R.string.non));
			txt.setText(s+(!finipartous ? "\n"+getString(R.string.joueur_suivant) : "\n"+getString(R.string.resultats)));
		}
		else {
			int exp=0;
			if(!solved) {
				if(opt.getInt("mode",0)>0) {
					exp=(solUsed) ? play.niv.experience/2 : play.niv.experience;
				} else {
					if(n_niv==menu.avancement) {
						menu.avancement++;
						exp=n_niv*(50+n_niv/4);
					} else
						exp=(solUsed) ? n_niv*(10+n_niv/8)/2 : n_niv*(10+n_niv/8);
				}
				menu.experience+=exp;
				menu.expToSync+=exp;
				menu.saveData(); // On sauvegarde la progression.
			}
			txt.setText(getString(R.string.temps)+" : "+getFormattedTime(temps_total_ms)
					+"\n"+getString(R.string.exp)+" : + "+exp
					+"\n"+getString(R.string.aide)+" : "+(solUsed ? getString(R.string.oui) : getString(R.string.non)));
		}
		solved = true;
	}
	
	public static String getFormattedTime(int time) {
		return String.format(Locale.FRANCE, (time>=60000 ? (time/60000)+" min " : "")+"%d.%02d s", (time%60000)/1000, (time%1000)/10);
	}
	
	/**
	 * Le colibri est mort : affiche l'écran associé.
	 */
	public void mort() {
		if(!play.isRunning && pause.getVisibility()!=View.VISIBLE) {
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
		bout_dyna.setVisibility(View.GONE);
	}
	
	/**
	 * Affiche ou cache le menu latéral par dessus le jeu.
	 * @param disp 0:cacher ; 1:afficher ; 2:rien
	 */
	private void menuLateral(int disp, MotionEvent ev) {
		if(disp==1 && menu_lateral.getVisibility()!=View.VISIBLE) { // Afficher
			int cote = (int) Math.signum(2*ev.getX()-menu.ww);
			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) menu_lateral.getLayoutParams();
			p.addRule(10-cote, 0); // pour annuler l'autre contrainte
			p.addRule(10+cote); // car RelativeLayout.ALIGN_PARENT_LEFT = 9 & RelativeLayout.ALIGN_PARENT_RIGHT = 11
			menu_lateral.setLayoutParams(p);
			TranslateAnimation anim = new TranslateAnimation(cote*menu_lateral.getWidth(),0,0,0);
		    anim.setDuration(250);
		    anim.setInterpolator(new AccelerateDecelerateInterpolator());
			menu_lateral.setVisibility(View.VISIBLE);
			menu_lateral.startAnimation(anim);
		} else if(disp<=1 && menu_lateral.getVisibility()==View.VISIBLE) { // Cacher
			int cote = (((RelativeLayout.LayoutParams) menu_lateral.getLayoutParams()).getRules()[RelativeLayout.ALIGN_PARENT_LEFT]==0) ? 1 : -1;
			TranslateAnimation anim = new TranslateAnimation(0,cote*menu_lateral.getWidth(),0,0);
		    anim.setDuration(250);
			menu_lateral.startAnimation(anim);
			menu_lateral.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * S'occupe de charger un niveau dans la "carte" et de lancer le moteur de jeu "play".
	 */
	private void launch_niv(boolean replay) {
		if(replay) {
			niv.replay();
		} else {
			solved = false;
			solUsed = false;
			if(opt.getInt("mode", 0)>0) {
				if(multi!=null) { // Mode multijoueur
					niv = new Niveau(opt.getInt("mode"), opt.getLong("seed"), opt.getIntArray("param"), opt.getInt("progressMin"));
					if(multi.defi.match==null) {
						multi.defi.match = new Defi.Match(opt.getInt("mode"), opt.getLong("seed"), opt.getIntArray("param"), opt.getInt("progressMin"), niv.experience);
						multi.defi.limite = System.currentTimeMillis()/1000 + multi.defi.t_max;
						multi.base.updateDefi(multi.defi);
					}
				} else // Mode Carte Aléatoire
					niv = new Niveau(opt.getInt("mode"), (new Random()).nextLong(), ParamAleat.param, menu.avancement);
			} else {
				try { // On ouvre le Niveau index_niv.
					niv = new Niveau(this.getAssets().open("niveaux/niveau"+n_niv+".txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		solvedBySol = false;
		setMenuLateral();
		carte.loadNiveau(niv);
		play.init(replay);
		if(startMsg!=null) { // Affichage d'un message avant le démarrage du jeu.
			Toast.makeText(this, startMsg, Toast.LENGTH_SHORT).show();
			startMsg = null;
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
			  @Override
			  public void run() {
			    play.start();
			  }
			}, 2000);
		} else
			play.start();
	}
	
	/*
	 * Les fonctions suivantes sont déclenchées par les appuis sur les boutons des menus Pause, Gagné et Mort.
	 */
	
	public void reprendre(View v) {
		pause.setVisibility(View.GONE);
        play.start(); 
	}
	
	public void recommencer(View v) {
		pause.setVisibility(View.GONE);
		gagne.setVisibility(View.GONE);
		perdu.setVisibility(View.GONE);
    	if(carte.n_dyna>0) hideDyna();
    	menuLateral(0,null);
    	launch_niv(true);
	}
	
	public void quitter(View v) {
		music=false;
		multi=null;
    	this.finish();
	}
	
	public void solution(View v) {
		if(niv.solution==null) // il n'y a pas de solution
			return;
		if(opt.getInt("mode", 0)==Niveau.CAMPAGNE && n_niv==menu.avancement) {
			Toast.makeText(this, R.string.solution_bloque, Toast.LENGTH_SHORT).show();
			return;
		}
		recommencer(v);
		solUsed=true;
		solvedBySol=true;
    	play.solution();
	}
	
	public void suivant(View v) {
		gagne.setVisibility(View.GONE);
		if(bout_dyna.getVisibility()==View.VISIBLE) hideDyna();
		if(play.isRunning)
			play.pause();
		if(opt.getInt("mode", 0)==Niveau.CAMPAGNE) {
			if(n_niv==NIV_MAX) {
				quitter(v);
				if(menu.screen==0)
					menu.findViewById(R.id.coupe).setVisibility(View.VISIBLE);
				menu.finCampagne(v);
				return;
			}
			if(n_niv<menu.avancement)
				n_niv++;
			else {
				Toast.makeText(this, R.string.bloque, Toast.LENGTH_SHORT).show();
				play.start();
				return;
			}
		} else if(multi!=null) {
			if(!solved) { // j1 ou j2 appui bouton Suivant => Forfait
				forfaitBox();
			} else{
				if(v.getId()==R.id.continuer) { // appui sur continuer
					if(finipartous) {
						finDefi();
					} else {
						quitter(null);
					}
				} else // le joueur a recommencé et appuyé sur Suivant
					gagne(10);
			}
			return;
		}
		launch_niv(false);
	}
	
	@SuppressLint("InlinedApi")
	private void forfaitBox() {
		play.start(); // Pour que le menu pause se déclenche ensuite.
		DialogInterface.OnClickListener check = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pause.setVisibility(View.GONE);
				solvedBySol=false;
				solUsed = false;
				gagne(Participation.FORFAIT);
			}
		};
		AlertDialog.Builder forfait;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			forfait = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
		} else {
			forfait = new AlertDialog.Builder(this);
		}
		forfait.setTitle(R.string.forfait);
		forfait.setMessage(R.string.forfait_msg);
		forfait.setPositiveButton(R.string.accept, check);
		forfait.setNegativeButton(R.string.annuler, null);
		popup = true;
		forfait.show();
	}
	
	private void finDefi() {
		Resultats.callback = new Resultats.callBackInterface() {
			@Override
			public void suite() {
				multi.base.setResultatsVus(multi.defi.id,multi.defi.nMatch);
				quitter(null);
				//multi.choixNiveau();
			}
		};
		Resultats.multi = multi;
		Resultats.DISPLAY_RES = true;
		popup = true;
		startActivity(new Intent(this, Resultats.class));
	}
}
