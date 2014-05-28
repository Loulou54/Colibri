package com.game.colibri;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.content.Intent;

/**
 * Menu principal : activité lancée au démarage.
 */
public class MenuPrinc extends Activity {
	
	
	
	public int ww,wh;
	private Intent jeu;
	LinearLayout opt_aleat;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_princ);
		opt_aleat = (LinearLayout) findViewById(R.id.opt_aleat);
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
		Button btn_lay = (Button)findViewById(R.id.bout1);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) btn_lay.getLayoutParams();
		layoutParams.leftMargin = ww*4/9;
	    layoutParams.topMargin = wh/3;
	    btn_lay.setLayoutParams(layoutParams);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(opt_aleat.getVisibility()==View.INVISIBLE) {
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
		}
		return true;
	}
	
	/**
	 * Fonctions appelées par le "onClick" des boutons définis dans activity_menu.xml
	 * 		@param v le bouton appuyé.
	 */
	public void continuer(View v) {
		Jeu.opt = new Bundle(); // On doit contourner le fait que startActivity(Intent i, Bundle b) ne soit pas supporté sur API < 16
		Jeu.opt.putBoolean("isRandom", false);
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
	}
	
	public void aleatoire(View v) {
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
	
	public void facile(View v) {
		launchAleat(8,14);
	}
	
	public void moyen(View v) {
		launchAleat(18,25);
	}

	public void difficile(View v) {
		launchAleat(30,40);
	}
	
	private void launchAleat(int lon, int var) {
		Jeu.opt = new Bundle();
		Jeu.opt.putBoolean("isRandom", true);
		Jeu.opt.putInt("long", lon);
		Jeu.opt.putInt("vari", var);
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
	}
}