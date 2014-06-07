package com.game.colibri;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Menu principal : activité lancée au démarage.
 */
public class MenuPrinc extends Activity {
	
	
	
	public int ww,wh;
	public int avancement; // Progression du joueur dans les niveaux campagne.
	private int n_niv; // Niveau sélectionné dans Campagne
	public int experience; // L'expérience du joueur.
	private boolean brandNew=true;
	public SharedPreferences pref;
	public SharedPreferences.Editor editor;
	private Intent jeu,multi;
	private ViewFlipper MenuSel;
	private ViewFlipper vf; // ViewFlipper permettant de passer de l'écran de menu principal à celui des instrus ou infos
	private Carte carte; // L'instance de carte permettant de faire un apercu dans le menu de sélection de niveaux.
	private LinearLayout opt_aleat;
	private LinearLayout opt_reglages;
	private MediaPlayer intro=null,boucle=null;
	private float initialX;
	private double[][] points = new double[][] {{0.07625, 0.8145833333333333}, {0.18875, 0.7645833333333333}, {0.31625, 0.7354166666666667}, {0.24875, 0.8208333333333333}, {0.1125, 0.94375}, {0.25, 0.9458333333333333}, {0.405, 0.9208333333333333}, {0.52, 0.9416666666666667}, {0.6275, 0.9333333333333333}, {0.765, 0.9354166666666667}, {0.765, 0.8166666666666667}, {0.83, 0.74375}};
	private Calendar debut;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_princ);
		vf = (ViewFlipper) findViewById(R.id.flip);
		MenuSel = (ViewFlipper) findViewById(R.id.flipper);
        MenuSel.setInAnimation(this, android.R.anim.fade_in);
        MenuSel.setOutAnimation(this, android.R.anim.fade_out);
        carte = (Carte) findViewById(R.id.apercu);
		opt_aleat = (LinearLayout) findViewById(R.id.opt_aleat);
		opt_reglages = (LinearLayout) findViewById(R.id.opt_reglages);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		editor = pref.edit();
		Jeu.opt = new Bundle(); // On va contourner le fait que startActivity(Intent i, Bundle b) ne soit pas supporté sur API < 16, en utilisant un Bundle de classe.
		Jeu.menu=this;
		Multijoueur.menu=this;
		loadData();
		intro = MediaPlayer.create(this, R.raw.intro);
		intro.setLooping(false);
		boucle = MediaPlayer.create(this, R.raw.boucle);
		boucle.setLooping(true);
		intro.start();
		intro.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				intro.release();
			    intro = null;
				boucle.start();
			}
		});
	}

	// Le placement des boutons est calculé ici en fonction des dimensions de l'écran. (Astuce pour contourner le temps d'établissement de l'affichage empêchant ces opérations dans le onCreate)
	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		RelativeLayout root = (RelativeLayout) findViewById(R.id.root);
		ww = root.getWidth();
		wh = root.getHeight();
		if(brandNew) placeButton();
		if(vf.getDisplayedChild()==1) campagne(carte); // Permet de rafraîchir la progression lorsque l'on quitte le jeu et revient au menu de sélection.
	}
	
	/**
	 * Place le premier bouton à la position voulue. Les autres sont placés par rapport à lui.
	 */
	private void placeButton() {
		brandNew=false;
		Button btn_lay = (Button)findViewById(R.id.bout1);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) btn_lay.getLayoutParams();
		layoutParams.leftMargin = ww*4/9;
	    layoutParams.topMargin = 3*wh/8;
	    btn_lay.setLayoutParams(layoutParams);
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ww/2,wh/2);
		params.leftMargin = (int) (ww/4);
	    params.topMargin = (int) (wh/12);
	    carte.setLayoutParams(params);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(vf.getDisplayedChild()!=0) {
				vf.setDisplayedChild(0);
			}
			else if (opt_reglages.getVisibility()==View.VISIBLE) {
				opt_reglages.setVisibility(View.INVISIBLE);
			}
			else if(opt_aleat.getVisibility()==View.INVISIBLE) {
				if (intro!=null) {
				    intro.release();
				    intro = null;
				}
				if (boucle!=null) {
				    boucle.release();
				    boucle = null;
				}
				this.finish(); // On quitte le jeu !
			} else {
				// Animation pour rétablir le menu.
				Animation a2 = AnimationUtils.loadAnimation(MenuPrinc.this, R.anim.menu_right);
    			opt_aleat.startAnimation(a2);
				Button btn_aleat = (Button)findViewById(R.id.bout3);
				btn_aleat.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bouton_aleat_down));
				for(int i=0; i<opt_aleat.getChildCount(); i++) {
    				((Button) opt_aleat.getChildAt(i)).setClickable(false);
				}
    			a2.setAnimationListener(new AnimationListener() {
    	    		public void onAnimationStart(Animation an) {
    	    			
    	    		}
    	    		public void onAnimationRepeat(Animation an) {
    	    			
    	    		}
    	    		public void onAnimationEnd(Animation an) { // On réaffiche le menu principal une fois que les déplacements sont finis
						opt_aleat.setVisibility(View.INVISIBLE);
						Animation a = AnimationUtils.loadAnimation(MenuPrinc.this, R.anim.aleat_opt_anim);
						((Button)findViewById(R.id.bout1)).startAnimation(a);
						((Button)findViewById(R.id.bout2)).startAnimation(a);
						((Button)findViewById(R.id.bout4)).startAnimation(a);
						((Button)findViewById(R.id.bout5)).startAnimation(a);
						((Button)findViewById(R.id.bout1)).setClickable(true);
						((Button)findViewById(R.id.bout2)).setClickable(true);
						((Button)findViewById(R.id.bout3)).setClickable(true);
						((Button)findViewById(R.id.bout4)).setClickable(true);
						((Button)findViewById(R.id.bout5)).setClickable(true);
    	    		}
    	    	});
			}
			return true;
		} else
			return false;
	}
	
	/**
	 * Swipe de MenuSel
	 */
	@Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        if (vf.getDisplayedChild()==1) {
        int a=MenuSel.getDisplayedChild();
        switch (touchevent.getAction()) {
        
        case MotionEvent.ACTION_DOWN:
            initialX = touchevent.getX();
            break;
        case MotionEvent.ACTION_UP:
            float finalX = touchevent.getX();
            if (initialX-finalX > 10) {
                if (a == 2)
                    break;
                MenuSel.showNext();
            } else if (initialX-finalX < -10) {
                if (a == 0)
                    break;
                MenuSel.showPrevious();
            }
            break;
        }
        if(carte.getVisibility()==View.VISIBLE && MenuSel.getDisplayedChild()!=a) {
            carte.setAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
			carte.setVisibility(View.INVISIBLE);
        }
        }
        return false;
	}
	
	/**
	 * On récupère les préférences et l'avancement de l'utilisateur.
	 */
	private void loadData() {
		avancement=pref.getInt("niveau", 1);
		experience=pref.getInt("exp", 0);
		avancement=23;
		n_niv=avancement;
		Log.i("Avancement :","Niv "+avancement);
		Log.i("Experience :","Score :"+experience);
	}
	
	/**
	 * On sauve les préférences et l'avancement de l'utilisateur.
	 */
	public void saveData() {
		editor.putInt("niveau", avancement);
		editor.putInt("exp", experience);
		editor.commit();
	}
	
	/**
	 * Fonctions appelées par le "onClick" des boutons définis dans activity_menu.xml
	 * 		@param v le bouton appuyé.
	 */
	public void continuer(View v) {
		opt_reglages.setVisibility(View.INVISIBLE);
		Jeu.opt.putBoolean("isRandom", false);
		Jeu.opt.putInt("n_niv", avancement);
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
		debut = new GregorianCalendar();
	}
	
	public void campagne(View v) {
		opt_reglages.setVisibility(View.INVISIBLE);
		carte.setVisibility(View.INVISIBLE);
		vf.setDisplayedChild(1);
		RelativeLayout lay_sel;
		View img;
		RelativeLayout.LayoutParams params;
		int[] r = new int[] {R.id.menu_selection_1,R.id.menu_selection_2,R.id.menu_selection_3};
		for(int ecran=0; ecran<3; ecran++) {
			lay_sel = (RelativeLayout) findViewById(r[ecran]);
			lay_sel.removeAllViews();
			for(int i=0; i<points.length; i++) {
				img = new View(this);
				if(ecran*points.length+i+1>=avancement)
					img.setBackgroundResource(R.drawable.fleur);
				else if(Math.random()<0.5)
					img.setBackgroundResource(R.drawable.emplacement1);
				else
					img.setBackgroundResource(R.drawable.emplacement2);
				lay_sel.addView(img);
				int d;
				if(i<6) d=ww/24;
				else d=ww/20;
				params = new RelativeLayout.LayoutParams(d,d);
				params.leftMargin = (int) (ww*points[i][0]-d/2);
			    params.topMargin = (int) (wh*points[i][1]-d/2);
			    img.setLayoutParams(params);
			    img.setId(ecran*points.length+i+1);
			    img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(v.getId()<=avancement) setApercu(v.getId());
					}
			    });
			}
		}
		MenuSel.setDisplayedChild((avancement-1)/points.length);
	}
	
	/**
	 * Charge le niveau d'index n_niv dans l'aperçu.
	 * @param n_niv l'index du niveau à afficher
	 */
	private void setApercu(int n) {
		if(carte.getVisibility()==View.INVISIBLE) {
			carte.setAnimation(AnimationUtils.loadAnimation(this, R.anim.aleat_opt_anim));
			carte.setVisibility(View.VISIBLE);
		}
		n_niv=n;
		Niveau niv;
		try {
			niv = new Niveau(MenuPrinc.this.getAssets().open("niveaux/niveau"+n_niv+".txt"));
			carte.loadNiveau(niv);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void aleatoire(View v) {
		opt_reglages.setVisibility(View.INVISIBLE);
		((Button)findViewById(R.id.bout1)).setClickable(false);
		((Button)findViewById(R.id.bout2)).setClickable(false);
		((Button)findViewById(R.id.bout4)).setClickable(false);
		((Button)findViewById(R.id.bout5)).setClickable(false);
		Animation a = AnimationUtils.loadAnimation(this, R.anim.menu_right);
		((Button)findViewById(R.id.bout1)).startAnimation(a);
		((Button)findViewById(R.id.bout2)).startAnimation(a);
		((Button)findViewById(R.id.bout4)).startAnimation(a);
		((Button)findViewById(R.id.bout5)).startAnimation(a);
		Button btn_aleat = (Button)findViewById(R.id.bout3);
		btn_aleat.setClickable(false);
		btn_aleat.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bouton_aleat_up));
		(btn_aleat.getAnimation()).setAnimationListener(new AnimationListener() {
    		public void onAnimationStart(Animation an) {
    			
    		}
    		public void onAnimationRepeat(Animation an) {
    			
    		}
    		public void onAnimationEnd(Animation an) { // On affiche les options une fois que les déplacements sont finis
    			Button btn_aleat = (Button)findViewById(R.id.bout3);
    			int w=btn_aleat.getWidth(), h=btn_aleat.getHeight();
    			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) opt_aleat.getLayoutParams();
    			p.width=3*w/4;
    			p.height=3*h;
    			p.leftMargin=w/8;
    			opt_aleat.setLayoutParams(p);
    			Animation a2 = AnimationUtils.loadAnimation(MenuPrinc.this, R.anim.aleat_opt_anim);
    			opt_aleat.startAnimation(a2);
				for(int i=0; i<opt_aleat.getChildCount(); i++) {
    				((Button) opt_aleat.getChildAt(i)).setClickable(true);
				}
    			opt_aleat.setVisibility(View.VISIBLE);
    		}
    	});
	}
	
	public void launchNiv(View v) {
		Jeu.opt.putBoolean("isRandom", false);
		Jeu.opt.putInt("n_niv", n_niv);
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
		debut = new GregorianCalendar();
	}
	
	public void facile(View v) {
		launchAleat(8,7);
	}
	
	public void moyen(View v) {
		launchAleat(18,8);
	}

	public void difficile(View v) {
		launchAleat(30,11);
	}
	
	public void launchAleat(int lon, int var) {
		Jeu.opt = new Bundle();
		Jeu.opt.putBoolean("isRandom", true);
		Jeu.opt.putInt("long", lon);
		Jeu.opt.putInt("vari", var);
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
		debut = new GregorianCalendar();
	}
	
	public void multijoueur(View v) {
		opt_reglages.setVisibility(View.INVISIBLE);
		multi = new Intent(this, Multijoueur.class);
		startActivity(multi);
	}
	
	public void reglages(View v) {
		if (opt_reglages.getVisibility()==View.INVISIBLE)
			opt_reglages.setVisibility(View.VISIBLE);
		else opt_reglages.setVisibility(View.INVISIBLE);
		
	}
	
	public void retour(View v) {
		vf.setDisplayedChild(0);
	}
	
	public void info(View v) {
		vf.setDisplayedChild(2);
	}
	
	public void instru(View v) {
		vf.setDisplayedChild(3);
	}
	
	public void musique(View v) {
		Button son_off = (Button)findViewById(R.id.boutmusique);
		Button son_on = (Button) findViewById(R.id.boutmusique2);
		opt_reglages.setVisibility(View.INVISIBLE);
		if (son_on.getVisibility()==View.INVISIBLE) {
			son_on.setVisibility(View.VISIBLE);
			son_off.setVisibility(View.INVISIBLE);
			son_off.setClickable(false);
			son_on.setClickable(true);
			if(intro==null)
				boucle.start();
			else
				intro.start();
		}
		else {
			son_off.setVisibility(View.VISIBLE);
			son_on.setVisibility(View.INVISIBLE);
			son_on.setClickable(false);
			son_off.setClickable(true);
			if(intro==null)
				boucle.pause();
			else
				intro.pause();
		}
	}
	
	public Calendar getDebut() {
		return debut;
	}
	
	public void setDebut(Calendar debut){
		this.debut = debut;
	}
}