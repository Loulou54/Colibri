package com.game.colibri;

import static com.network.colibri.CommonUtilities.BROADCAST_MESSAGE_ACTION;
import static com.network.colibri.CommonUtilities.EXTRA_MESSAGE;
import static com.network.colibri.CommonUtilities.SENDER_ID;
import static com.network.colibri.CommonUtilities.SERVER_URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.network.colibri.ConnectionDetector;
import com.network.colibri.DBController;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class Multijoueur extends Activity {
	
	public static MenuPrinc menu;
	
	private ExpandableListView lv;
	private DefiExpandableAdapter adapt;
	private HashMap<String,Joueur> joueurs;
	private ArrayList<Defi> adversaires;
	private AlertDialog boxNiv;
	public Defi defi;
	public Joueur user;
	public ConnectionDetector connect;
	public DBController base;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multijoueur);
		connect = new ConnectionDetector(this);
		base = new DBController(this);
		adversaires = new ArrayList<Defi>();
		lv = (ExpandableListView) findViewById(R.id.listView1);
		//GCMRegistrar.checkDevice(this); TODO /!\
		//GCMRegistrar.checkManifest(this);
		registerReceiver(mHandleMessageReceiver, new IntentFilter(BROADCAST_MESSAGE_ACTION));
		loadData();
	}
	
	@Override
    protected void onDestroy() {
		try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
		super.onDestroy();
	}
	
	/**
	 * Crée toutes les instances de Joueur pour l'utilisateur du jeu et pour tous ses adversaires en liste.
	 */
	private void loadData() {
		loadJoueurs();
		if(user==null) { // Utilisateur non inscrit !
			//final String regId = GCMRegistrar.getRegistrationId(this); TODO /!\
			//if(regId.equals("")) {
			if(false) {
				Toast.makeText(this, R.string.gcm_register, Toast.LENGTH_SHORT).show();
				GCMRegistrar.register(this, SENDER_ID);
			} else { // Si jamais l'enregistrement GCM a fonctionné mais pas l'enregistrement à notre serveur
				registerUser();
			}
		} else {
			base.getDefis(user.getPseudo(),joueurs,adversaires);
			adapt = new DefiExpandableAdapter(this, user.getPseudo(), adversaires);
			lv.setAdapter(adapt);
			System.out.println("Adversaires : "+adversaires.size());
			System.out.println("Joueurs : "+joueurs.size());
			dispUser();
			if(!connect.isConnectedToInternet())
				Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_LONG).show();
			else {
				syncData();
			}
		}
	}
	
	private void loadJoueurs() {
		joueurs = base.getJoueurs();
		user = joueurs.get(menu.pref.getString("pseudo",null));
	}
	
	@SuppressLint("InflateParams")
	public void choixNiveau() {
		boxNiv = new AlertDialog.Builder(this).create();
		boxNiv.setTitle("Défi : "+defi.nom);
		boxNiv.setView(LayoutInflater.from(this).inflate(R.layout.choix_niveau_multi, null));
		boxNiv.show();
	}
	
	public void paramAleat(View v) {
		ParamAleat pa = new ParamAleat(new ParamAleat.callBackInterface() {
			@Override
			public void launchFunction(int mode) {
				newMatch(mode);
			}
		}, this, menu.avancement);
		pa.show(menu.editor); // Si appui sur "OK", lance un niveau aléatoire en mode PERSO.
	}
	
	public void facile(View v) {
		newMatch(Niveau.FACILE);
	}
	
	public void moyen(View v) {
		newMatch(Niveau.MOYEN);
	}

	public void difficile(View v) {
		newMatch(Niveau.DIFFICILE);
	}
	
	/**
	 * Lance un NOUVEAU niveau.
	 * @param mode
	 */
	public void newMatch(int mode) {
		boxNiv.dismiss();
		Jeu.multi=this;
		long seed = (new Random()).nextLong();
		Jeu.startMsg = getString(R.string.c_est_parti)+" "+user.getPseudo()+" !";
		Jeu.opt.putInt("mode", mode);
		Jeu.opt.putLong("seed", seed);
		Jeu.opt.putIntArray("param", ParamAleat.param);
		startActivity(new Intent(this, Jeu.class));
	}
	
	/**
	 * Lance un niveau défini par le contenu de defi.match.
	 * 
	 */
	public void releverMatch() {
		Jeu.multi=this;
		Jeu.startMsg = getString(R.string.c_est_parti)+" "+user.getPseudo()+" !";
		Jeu.opt.putInt("mode", defi.match.mode);
		Jeu.opt.putLong("seed", defi.match.seed);
		Jeu.opt.putIntArray("param", defi.match.param);
		startActivity(new Intent(this, Jeu.class));
	}
	
	public void finMatch() {
		menu.experience=user.getExp();
		menu.saveData();
		syncData();
		dispUser();
	}
	
	/*public void cancel(View v) {
		LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		nouveauJoueur.setVisibility(View.INVISIBLE);
	}*/
	
	public void nouveauDefi(View v) {
		//LinearLayout nouveauJoueur = (LinearLayout) findViewById(R.id.ajoutJoueur);
		//nouveauJoueur.setVisibility(View.VISIBLE);
		// TODO: Sélection d'un adversaire soit automatique, soit par recherche du pseudo.
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("pseudo", menu.pref.getString("pseudo", ""));
		params.put("nAdv", "1");
		client.post(SERVER_URL+"/newdefi_auto.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				insertJSONData(response);
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				Toast.makeText(Multijoueur.this, R.string.err, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public void supprDefi(View v) {
		// TODO : supprimer défi de la bdd
		int groupPosition = Integer.parseInt((String) v.getContentDescription());
		adversaires.remove(groupPosition);
		adapt.notifyDataSetChanged();
	}
	
	public void actionDefi(View v) {
		int groupPosition = Integer.parseInt((String) v.getContentDescription());
		Multijoueur.this.defi = adversaires.get(groupPosition);
		switch(defi.getEtat(user.getPseudo())) {
		case Defi.ATTENTE: // Send POKE
			// TODO: Poke.
			break;
		case Defi.RESULTATS: // Afficher les résultats
			Resultats.callback = new Resultats.callBackInterface() {
				@Override
				public void suite() {
					defi.participants.get(user.getPseudo()).resultatsVus=true;
					base.setresultatsVus(defi.id,user.getPseudo(),true);
					adapt.notifyDataSetChanged();
					//releverMatch();
				}
			};
			Resultats.multi = Multijoueur.this;
			Resultats.DISPLAY_RES = true;
			startActivity(new Intent(Multijoueur.this, Resultats.class));
			break;
		case Defi.RELEVER: // Lancer le jeu avec Seed donnée
			releverMatch();
			break;
		default: // Lancer nouveau défi
			Multijoueur.this.choixNiveau();
		}
	}
	
	public void syncTotale(View v) {
		base.taskSyncTotale();
		syncData();
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
	
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			if(newMessage.equals("GCMRegistartion")) { // GCM Registration successful => Server registration
				Toast.makeText(Multijoueur.this, R.string.gcm_success, Toast.LENGTH_SHORT).show();
				registerUser();
				return;
			}
		}
	};
	
	private void registerUser() {
		(new RegisterUser(this, new RegisterUser.callBackInterface() {
			@Override
			public int getExp() {
				return menu.experience;
			}
			@Override
			public boolean registered(String JSONresponse, String name) {
				try {
					base.insertJSONJoueurs(new JSONArray(JSONresponse));
					menu.editor.putString("pseudo", name);
					menu.editor.commit();
					loadJoueurs();
					base.getDefis(user.getPseudo(),joueurs,adversaires);
					adapt = new DefiExpandableAdapter(Multijoueur.this, user.getPseudo(), adversaires);
					lv.setAdapter(adapt);
					menu.expToSync = 0;
					menu.saveData();
					dispUser();
					return true;
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
			@Override
			public void cancelled() {
				finish();
			}
		})).show(null);
	}
	
	public void syncData() {
		adapt.notifyDataSetChanged();
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("pseudo", menu.pref.getString("pseudo", ""));
		params.put("tasks", base.getTasks());
		params.put("expToSync", ""+menu.expToSync);
		client.post(SERVER_URL+"/sync_data.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				insertJSONData(response);
				base.clearTasks();
				if(menu.expToSync!=0) {
					menu.expToSync = 0;
					menu.saveData();
				}
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				Toast.makeText(Multijoueur.this, R.string.sync_fail, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	/*private String getJSONDefis() {
		Gson g = new Gson();
		return g.toJson(adversaires);
	}*/
	
	/*private void setJSONDefis(String data) {
		Gson g = new Gson();
		adversaires = new ArrayList<Defi>(Arrays.asList(g.fromJson(data, Defi[].class)));
	}*/
	
	/**
	 * Insert ou met à jour les défis contenus dans def sous le format :
	 * {defis:[{Defi},{Defi},...],participations:[{Participation},{Participation},...],joueurs:[{Joueur},{Joueur},...]}
	 * @param def
	 */
	private void insertJSONData(String def) {
		System.out.println(def);
		try {
			JSONObject o = new JSONObject(def);
			int up=base.insertJSONJoueurs((JSONArray) o.get("joueurs"));
			if(up>0)
				loadJoueurs();
			int up2=base.insertJSONDefis((JSONArray) o.get("defis"));
			up2+=base.insertJSONParticipations((JSONArray) o.get("participations"));
			if(up2>0)
				base.getDefis(user.getPseudo(),joueurs,adversaires);
			if(up+up2>0)
				adapt.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
