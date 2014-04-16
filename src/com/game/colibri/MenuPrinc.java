package com.game.colibri;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.content.Intent;

/**
 * Menu principal : activité lancée au démarage.
 */
public class MenuPrinc extends Activity {
	
	
	
	public int ww,wh;
	private Intent jeu;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_princ);
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
	
	/**
	 * Fonctions appelées par le "onClick" des boutons définis dans activity_menu.xml
	 * 		@param v
	 */
	public void continuer(View v) {
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
	}
}