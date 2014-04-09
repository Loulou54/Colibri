package com.game.colibri;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Jeu extends Activity {
	
	/**
	 * Activité gérant le jeu à proprement dit. Elle affiche notamment la View "Carte" en plein écran.
	 */
	
	public Carte carte;
	public ImageView colibri;
	private RelativeLayout.LayoutParams layoutParams;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jeu);
		carte = (Carte) findViewById(R.id.carte);
		carte.loadNiveau(Niveaux.getNiveau(0));
		colibri = (ImageView) findViewById(R.id.colibri);
		layoutParams = (RelativeLayout.LayoutParams) colibri.getLayoutParams();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		layoutParams.width=5*carte.cw/4;
		layoutParams.height=5*carte.ch/4;
		layoutParams.leftMargin = carte.ww/2;
	    layoutParams.topMargin = carte.wh/2;
	    colibri.setLayoutParams(layoutParams);
		((AnimationDrawable) colibri.getBackground()).start();
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent ev) {
		layoutParams.leftMargin = (int) ev.getX();
	    layoutParams.topMargin = (int) ev.getY();
	    colibri.setLayoutParams(layoutParams);
		return true;
	}
}
