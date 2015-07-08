package com.game.colibri;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
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
	private ArrayList<Joueur> adversaires;
	private AlertDialog boxNiv;
	public int temps1, exp1, penalite1;
	public int temps2, exp2, penalite2;
	public int gagne;
	public Joueur user,j;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DefiAdapter.expMot=getString(R.string.exp);
		DefiAdapter.scoreMot=getString(R.string.score);
		setContentView(R.layout.activity_multijoueur);
		lv = (ListView) findViewById(R.id.listView1);
		loadPlayers();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    Multijoueur.this.j = adversaires.get(position);
			    Multijoueur.this.choixNiveau();
		}});
	}
	
	/**
	 * Crée toutes les instances de Joueur pour l'utilisateur du jeu et pour tous ses adversaires en liste.
	 */
	private void loadPlayers() {
		user=new Joueur(menu.pref);
		if(user.getPseudo()==null) {
			menu.editor.putString("pseudo", getString(R.string.vous));
			menu.editor.commit();
			user=new Joueur(menu.pref);
		}
		dispUser();
		// TODO : tester si user non inscrit et afficher procédure d'inscription.
		loadAdv();
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
	
	public void choixNiveau() {
		boxNiv = new AlertDialog.Builder(this).create();
		boxNiv.setTitle("vs "+j.getPseudo());
		boxNiv.setView(LayoutInflater.from(this).inflate(R.layout.choix_niveau_multi, null));
		boxNiv.show();
		//newDefi((new Random()).nextInt(3)+1);
	}
	
	public void paramAleat(View v) {
		ParamAleat pa = new ParamAleat(new ParamAleat.callBackInterface() {
			@Override
			public void launchFunction(int mode) {
				newDefi(mode);
			}
		}, this, menu.avancement);
		pa.show(menu.editor); // Si appui sur "OK", lance un niveau aléatoire en mode PERSO.
	}
	
	public void facile(View v) {
		newDefi(Niveau.FACILE);
	}
	
	public void moyen(View v) {
		newDefi(Niveau.MOYEN);
	}

	public void difficile(View v) {
		newDefi(Niveau.DIFFICILE);
	}
	
	public void newDefi(int type) {
		boxNiv.dismiss();
		Jeu.multi=this;
		temps1=0;
		temps2=0;
		exp1=0;
		exp2=0;
		Jeu.startMsg = getString(R.string.c_est_parti)+" "+getString(R.string.joueur)+" 1 ! ("+user.getPseudo()+")";
		menu.launchAleat(type);
	}
	
	public void finDefi(int exp1, int exp2) {
		this.exp1 = exp1;
		this.exp2 = exp2;
		j.defi();
		user.defi();
		if (gagne==1) {
			user.win();
			j.loose();
		} else if(gagne==2) {
			j.win();
			user.loose();
		}
		user.addExp(exp1);
		j.addExp(exp2);
		menu.experience=user.getExp();
		menu.editor.putInt("defis", user.getDefis());
		menu.editor.putInt("win", user.getWin());
		menu.editor.putInt("loose", user.getLost());
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
		String adv = menu.pref.getString("adversaires", "[]");
		if(adv.length()>2) {
			adv=adv.substring(1, adv.length()-1);
			for(String ad : adv.split(", ")) {
				adversaires.add(new Joueur(ad));
			}
		}
	}
	
	private void saveAdv() {
		String adv = adversaires.toString();
		menu.editor.putString("adversaires", adv);
		menu.editor.commit();
		adapt.notifyDataSetChanged();
		dispUser();
	}
	
	private void dispUser() {
		TextView name = (TextView) findViewById(R.id.user_name);
		name.setText(user.getPseudo());
		TextView exp = (TextView) findViewById(R.id.user_exp);
		exp.setText(getString(R.string.exp)+" :\n"+user.getExp());
		TextView defis = (TextView) findViewById(R.id.user_defis);
		defis.setText(getString(R.string.defis_joues)+" : "+user.getDefis());
		TextView win = (TextView) findViewById(R.id.user_wins);
		win.setText(getString(R.string.defis_gagnés)+" : "+user.getWin());
	}
}
