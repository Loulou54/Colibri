package com.game.colibri;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Activité gérant le jeu  proprement dit. Elle affiche notamment la View "Carte" en plein écran.
 */
public class Jeu extends Activity {
	
	
	
	public Carte carte;
	public MoteurJeu play;
	public RelativeLayout lay;
	public Animal colibri;
	private boolean brandNew=true;
	public int n_niv=0;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jeu);
		carte = (Carte) findViewById(R.id.carte);
		lay = (RelativeLayout) findViewById(R.id.lay);
		play = new MoteurJeu(this,carte); // TODO : r�cup�rer le niveau � jouer (� travers un Intent ?)
		Log.i("onCreate","FINI");
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (brandNew) { // �v�nement appel� lorsque le RelativeLayout "lay" est pr�t ! C'est ici que l'on peut charger le niveau et ajouter les View "Animal".
			carte.loadNiveau(Niveaux.getNiveau(n_niv),lay);
			play.init();
			play.start();
			brandNew=false;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent (MotionEvent ev) {
	    play.onTouch(ev);
		return true;
	}
	
	// �v�nement d�clench� par "play" lorsque le niveau a �t� gagn�.
	/**
	 * Affiche que le niveau à été gagné et charge le niveau suivant 
	 * 
	 */
	public void gagne() {
		n_niv++;
		carte.loadNiveau(Niveaux.getNiveau(n_niv),lay);
		play.init();
		Log.i("C'est Gagn� !","BRAVO !");
	}
}
