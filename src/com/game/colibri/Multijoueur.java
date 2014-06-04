package com.game.colibri;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class Multijoueur extends Activity {
	
	public static MenuPrinc menu;
	
	private ListView lv;
	private DefiAdapter adapt;
	private Joueur user;
	private ArrayList<Joueur> adversaires;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multijoueur);
		lv = (ListView) findViewById(R.id.listView1);
		loadPlayers();
	}
	
	/**
	 * Crée toutes les instances de Joueur pour l'utilisateur du jeu et pour tous ses adversaires en liste.
	 */
	private void loadPlayers() {
		user=new Joueur(menu.pref);
		// TODO : tester si user non inscrit et afficher procédure d'inscription.
		adversaires = new ArrayList<Joueur>();
		// TODO : charger la liste des défis en cours.
		adversaires.add(new Joueur("Jacky"));
		adversaires.add(new Joueur("Joseph"));
		adversaires.add(new Joueur("Jacob"));
		adversaires.add(new Joueur("Jacky"));
		adversaires.add(new Joueur("Joseph"));
		adversaires.add(new Joueur("Jacob"));
		adversaires.add(new Joueur("Jacky"));
		adversaires.add(new Joueur("Joseph"));
		adversaires.add(new Joueur("Jacob"));
		adapt = new DefiAdapter(this, adversaires);
		lv.setAdapter(adapt);
	}
	
	public void nouveauDefi(View v) {
		Log.i("CLIC !","CLIC !");
	}
	
}
