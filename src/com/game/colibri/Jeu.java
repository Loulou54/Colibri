package com.game.colibri;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

public class Jeu extends Activity {
	
	/**
	 * Activit� g�rant le jeu � proprement dit. Elle affiche notamment la View "Carte" en plein �cran.
	 */
	
	public Carte carte;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jeu);
		carte = (Carte) findViewById(R.id.carte);
		carte.loadNiveau(Niveaux.getNiveau(1));
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent ev) {
		
		return true;
	}
}
