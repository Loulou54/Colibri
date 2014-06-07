package com.game.colibri;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class Multijoueur extends Activity {
	
	public static MenuPrinc menu;
	
	private ListView lv;
	private DefiAdapter adapt;
	private Joueur user;
	private ArrayList<Joueur> adversaires;
	public long temps1;
	public long temps2;
	public Joueur j;
	
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
	
	public void newJoueur(View v) {
		EditText getName = (EditText) findViewById(R.id.nomJoueur);
		String name = getName.getText().toString();
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.INVISIBLE);
		j = new Joueur(name);
		adversaires.add(j);
		//adapt = new DefiAdapter(this, adversaires);
		//lv.setAdapter(adapt);
		Jeu.multi=this;
		menu.launchAleat(18,8);
	}
	
	public void finDefi() {
		Jeu.multi=null;
		j.defi();
		user.defi();
		if (temps1 > temps2)
			j.win();
		else user.win();

	}
	
	public void cancel(View v) {
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.INVISIBLE);
	}
	
	public void nouveauDefi(View v) {
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.VISIBLE);
	}
	
}
