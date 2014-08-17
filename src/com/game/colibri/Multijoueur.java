package com.game.colibri;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    Multijoueur.this.j = adversaires.get(position);
			    Multijoueur.this.newDefi();
		}});
	}
	
	/**
	 * Crée toutes les instances de Joueur pour l'utilisateur du jeu et pour tous ses adversaires en liste.
	 */
	private void loadPlayers() {
		user=new Joueur(menu.pref);
		if(user.getPseudo()==null) {
			menu.editor.putString("pseudo", "Vous");
			menu.editor.commit();
			user=new Joueur(menu.pref);
		}
		dispUser();
		// TODO : tester si user non inscrit et afficher procédure d'inscription.
		loadAdv();
		Log.i("Adversaires:",adversaires.toString());
		adapt = new DefiAdapter(this, adversaires);
		lv.setAdapter(adapt);
	}
	
	public void newJoueur(View v) {
		EditText getName = (EditText) findViewById(R.id.nomJoueur);
		String name = getName.getText().toString();
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.INVISIBLE);
		if (getAdversaire(name) == -1 && !name.contains(",") && !name.contains(";") && name.length()>2 && name.length()<17) {
			j = new Joueur(name);
			adversaires.add(j);
			saveAdv();
		}
		else nouveauJoueur.setVisibility(View.VISIBLE);
	}
	
	public void newDefi() {
		Jeu.multi=this;
		temps1=0;
		temps2=0;
		menu.launchAleat(18,8);
	}
	
	public void finDefi(int exp1, int exp2) {
		j.defi();
		user.defi();
		if (temps1 > temps2)
			j.win();
		else
			user.win();
		user.addExp(exp1);
		j.addExp(exp2);
		menu.experience=user.getExp();
		menu.editor.putInt("defis", user.getDefis());
		menu.editor.putInt("win", user.getWin());
		saveAdv();
		menu.saveData();
	}
	
	public void cancel(View v) {
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.INVISIBLE);
	}
	
	public void nouveauDefi(View v) {
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.VISIBLE);
	}
	
	public void supprDefi(View v) {
		adversaires.remove(v.getId());
		saveAdv();
	}
	
	public int getAdversaire(String name) {
		int i = 0;
		while (i < adversaires.size()  && !adversaires.get(i).getPseudo().equals(name))
				i++;
		if (i == adversaires.size())
			return -1;
		else return i;
	}
	
	private void loadAdv() {
		adversaires = new ArrayList<Joueur>();
		String adv = menu.pref.getString("adversaires", "");
		if(adv!="") {
			for(String ad : adv.split(", ")) {
				adversaires.add(new Joueur(ad));
			}
		}
	}
	
	private void saveAdv() {
		String adv = adversaires.toString();
		menu.editor.putString("adversaires", adv.substring(1, adv.length()-1));
		menu.editor.commit();
		adapt.notifyDataSetChanged();
		dispUser();
	}
	
	private void dispUser() {
		TextView name = (TextView) findViewById(R.id.user_name);
		name.setText(user.getPseudo());
		TextView exp = (TextView) findViewById(R.id.user_exp);
		exp.setText("Expérience :\n"+user.getExp());
		TextView defis = (TextView) findViewById(R.id.user_defis);
		defis.setText("Défis joués : "+user.getDefis());
		TextView win = (TextView) findViewById(R.id.user_wins);
		win.setText("Défis gagnés : "+user.getWin());
	}
}
