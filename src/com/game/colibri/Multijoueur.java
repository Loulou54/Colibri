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
import android.widget.ImageView;
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
	public AsyncHttpClient client;
	public DBController base;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multijoueur);
		connect = new ConnectionDetector(this);
		client = new AsyncHttpClient();
		base = new DBController(this);
		joueurs = new HashMap<String, Joueur>();
		adversaires = new ArrayList<Defi>();
		lv = (ExpandableListView) findViewById(R.id.listView1);
		lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Participation p = (Participation) adapt.getChild(groupPosition, childPosition);
				(new DispJoueur(Multijoueur.this, p.joueur)).show();
				return true;
			}
		});
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
			dispUser();
			if(!connect.isConnectedToInternet())
				Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_LONG).show();
			else {
				syncData();
			}
		}
	}
	
	private void loadJoueurs() {
		base.getJoueurs(joueurs);
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
	
	public void nouveauDefi(View v) {
		if(!connect.isConnectedToInternet()) {
			Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_LONG).show();
			return;
		}
		if(menu.expToSync!=0)
			syncData();
		(new NewDefi(this, client, user.getPseudo(), new NewDefi.callBackInterface() {
			@Override
			public void create(String jsonData) {
				insertJSONData(jsonData);
			}
		})).show();
	}
	
	public void supprDefi(View v) {
		// TODO : supprimer défi de la bdd
		int groupPosition = (Integer) v.getTag();
		base.removeDefi(adversaires.remove(groupPosition));
		syncData();
	}
	
	public void actionDefi(View v) {
		int groupPosition = (Integer) v.getTag();
		Multijoueur.this.defi = adversaires.get(groupPosition);
		switch(defi.getEtat(user.getPseudo())) {
		case Defi.ATTENTE: // Send POKE
			// TODO: Poke.
			break;
		case Defi.RESULTATS: // Afficher les résultats
			Resultats.callback = new Resultats.callBackInterface() {
				@Override
				public void suite() {
					base.setResultatsVus(defi.id,defi.nMatch);
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
		case Defi.LANCER: // Lancer nouveau défi
			Multijoueur.this.choixNiveau();
			break;
		case Defi.OBSOLETE: // Deadline dépassée -> il faudrait synchroniser.
			Toast.makeText(Multijoueur.this, R.string.obsolete_txt, Toast.LENGTH_LONG).show();
		}
	}
	
	public void syncTotale(View v) {
		base.taskSyncTotale();
		syncData();
	}
	
	private void dispUser() {
		((ImageView) findViewById(R.id.user_avatar)).setImageResource(user.getAvatar());
		((TextView) findViewById(R.id.user_name)).setText(user.getPseudo());
		((TextView) findViewById(R.id.user_exp)).setText(getString(R.string.exp)+" :\n"+user.getExp());
		((TextView) findViewById(R.id.user_defis)).setText(getString(R.string.defis_joues)+" : "+user.getDefis());
		((TextView) findViewById(R.id.user_wins)).setText(getString(R.string.defis_gagnés)+" : "+user.getWin());
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
		(new RegisterUser(this, client, new RegisterUser.callBackInterface() {
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
		RequestParams params = new RequestParams();
		params.put("pseudo", menu.pref.getString("pseudo", ""));
		params.put("tasks", base.getTasks());
		System.out.println(base.getTasks());
		params.put("expToSync", ""+menu.expToSync);
		client.post(SERVER_URL+"/sync_data.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				insertJSONData(response);
				base.clearTasks();
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
	 * {defis:[{Defi},{Defi},...],participations:[{Participation},{Participation},...],joueurs:[{Joueur},{Joueur},...],tasks:[{task:"delete", defi:3},{task:"message", msg:"Message!"},...]}
	 * @param def
	 */
	private void insertJSONData(String def) {
		System.out.println(def);
		if(def.length()<4) // Dans le cas où def vaut []
			return;
		try {
			JSONObject o = new JSONObject(def);
			if(o.has("joueurs")) {
				base.insertJSONJoueurs((JSONArray) o.get("joueurs"));
				loadJoueurs();
				menu.expToSync = 0;
				menu.experience = user.getExp();
				menu.saveData();
				dispUser();
			}
			if(o.has("defis"))
				base.insertJSONDefis((JSONArray) o.get("defis"));
			if(o.has("participations"))
				base.insertJSONParticipations((JSONArray) o.get("participations"));
			if(o.has("deletePart"))
				base.deletePart(this, (JSONArray) o.get("deletePart"));
			if(o.has("deleteDef"))
				base.deleteDef(this, (JSONArray) o.get("deleteDef"));
			if(o.length()!=0) { // On rafraîchit l'affichage.
				base.getDefis(user.getPseudo(),joueurs,adversaires);
				adapt.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
