package com.game.colibri;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.content.Intent;

public class MenuPrinc extends Activity {
	
	/**
	 * Menu principal : activit� lanc�e au d�marage.
	 */
	
	public int ww,wh;
	private Intent jeu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_princ);
	}

	// Le placement des boutons est calcul� ici en fonction des dimensions de l'�cran. (Astuce pour contourner le temps d'�tablissement de l'affichage emp�chant ces op�rations dans le onCreate)
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
	
	// Fonctions appel�es par le "onClick" des boutons d�finis dans activity_menu.xml
	public void continuer(View v) {
		jeu = new Intent(this, Jeu.class);
		startActivity(jeu);
	}
}