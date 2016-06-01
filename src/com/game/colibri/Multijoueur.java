package com.game.colibri;

import static com.network.colibri.CommonUtilities.BROADCAST_MESSAGE_ACTION;
import static com.network.colibri.CommonUtilities.EXTRA_MESSAGE;
import static com.network.colibri.CommonUtilities.SENDER_ID;
import static com.network.colibri.CommonUtilities.SERVER_URL;

import java.util.ArrayList;
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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class Multijoueur extends Activity {
	
	public static MenuPrinc menu;
	public static boolean active = false;
	
	private ExpandableListView lv;
	private DefiExpandableAdapter adapt;
	private SparseArray<Joueur> joueurs;
	private ArrayList<Defi> adversaires;
	private AlertDialog boxNiv;
	private ViewSwitcher loader;
	public Defi defi;
	public Joueur user;
	public ConnectionDetector connect;
	public AsyncHttpClient client;
	public DBController base;
	private boolean gcmActive = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multijoueur);
		active = true;
		connect = new ConnectionDetector(this);
		client = new AsyncHttpClient();
		base = new DBController(this);
		joueurs = new SparseArray<Joueur>();
		adversaires = new ArrayList<Defi>();
		((TextView) findViewById(R.id.titreMulti)).setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Adventure.otf"));
		((TextView) findViewById(R.id.nvDefi)).setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Sketch_Block.ttf"));
		((TextView) findViewById(R.id.nvPRapide)).setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Sketch_Block.ttf"));
		((TextView) findViewById(R.id.user_name)).setTypeface(Typeface.createFromAsset(getAssets(),"fonts/YummyCupcakes.ttf"), Typeface.BOLD);
		lv = (ExpandableListView) findViewById(R.id.listView1);
		lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Participation p = (Participation) adapt.getChild(groupPosition, childPosition);
				(new DispJoueur(Multijoueur.this, p.joueur)).show();
				return true;
			}
		});
		lv.setEmptyView((TextView) findViewById(R.id.defaultViewDefis));
		loader = (ViewSwitcher) findViewById(R.id.loader);
		try {
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			registerReceiver(mHandleMessageReceiver, new IntentFilter(BROADCAST_MESSAGE_ACTION));
		} catch (Exception e) {
			gcmActive = false;
			System.out.println("No GCM : Notification system disabled.");
		}
		loadData();
	}
	
	@Override
    protected void onDestroy() {
		active = false;
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
		int userId = menu.pref.getInt("id",0);
		if(userId==0) { // Utilisateur non inscrit !
			if(!connect.isConnectedToInternet()) {
				Toast.makeText(this, R.string.connexion_register, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			String regId = null;
			if(gcmActive) {
				regId = GCMRegistrar.getRegistrationId(this);
			}
			if(regId!=null && regId.equals("")) {
				Toast.makeText(this, R.string.gcm_register, Toast.LENGTH_SHORT).show();
				GCMRegistrar.register(this, SENDER_ID);
			} else { // Si jamais l'enregistrement GCM a fonctionné mais pas l'enregistrement à notre serveur
				registerUser();
			}
		} else {
			base.getDefis(userId,joueurs,adversaires);
			adapt = new DefiExpandableAdapter(this, userId, adversaires);
			lv.setAdapter(adapt);
			dispUser();
			if(!connect.isConnectedToInternet()) {
				Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_SHORT).show();
			} else {
				syncData();
			}
		}
	}
	
	private void loadJoueurs() {
		int userId = menu.pref.getInt("id",0);
		base.getJoueurs(joueurs);
		System.out.println("Nombre de Joueurs : "+joueurs.size());
		for(int i=0, length=joueurs.size(); i<length; i++) {
			Joueur j = joueurs.valueAt(i);
			System.out.println(j.getId()+" : "+j.getPseudo());
		}
		user = joueurs.get(userId);
		if(user==null) {
			if(!connect.isConnectedToInternet()) {
				Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_SHORT).show();
				finish();
			} else {
				user = new Joueur(userId, menu.pref.getString("pseudo",null), "", 0, 0, 0, 0, 0, 0, 0);
				base.taskSyncTotale(userId);
			}
		}
	}
	
	@SuppressLint("InflateParams")
	public void choixNiveau() {
		boxNiv = new AlertDialog.Builder(this).create();
		boxNiv.setTitle("Défi : "+defi.nom);
		LinearLayout lay = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.choix_niveau_multi, null);
		Typeface font = Typeface.createFromAsset(getAssets(),"fonts/Passing Notes.ttf");
		((TextView) lay.getChildAt(0)).setTypeface(font);
		LinearLayout opt_aleat = (LinearLayout) lay.getChildAt(1);
		for(int i=0; i<opt_aleat.getChildCount(); i++) {
			((TextView) opt_aleat.getChildAt(i)).setTypeface(font);
		}
		boxNiv.setView(lay);
		boxNiv.show();
	}
	
	public void paramAleat(View v) {
		ParamAleat pa = new ParamAleat(new ParamAleat.callBackInterface() {
			@Override
			public void launchFunction(int mode) {
				newMatch(mode);
			}
		}, this, defi.getProgressMin());
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
		Jeu.opt.putInt("progressMin", defi.getProgressMin());
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
		Jeu.opt.putInt("progressMin", defi.match.progressMin);
		startActivity(new Intent(this, Jeu.class));
	}
	
	public void nouveauDefi(View v) {
		if(!connect.isConnectedToInternet() || user==null) {
			Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_SHORT).show();
			return;
		}
		if(menu.expToSync!=0)
			syncData();
		(new NewDefi(this, client, user.getId(), new NewDefi.callBackInterface() {
			@Override
			public void create(String jsonData) {
				insertJSONData(jsonData);
			}
		})).show();
	}
	
	public void partieRapide(View v) {
		if(!connect.isConnectedToInternet() || user==null) {
			Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_SHORT).show();
			return;
		}
		final ProgressDialog prgDialog = new ProgressDialog(this);
		prgDialog.setMessage(getString(R.string.progress));
		prgDialog.setCancelable(false);
		prgDialog.show();
		RequestParams params = new RequestParams();
		params.put("joueur", ""+menu.pref.getInt("id", 0));
		client.post(SERVER_URL+"/partie_rapide.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				prgDialog.dismiss();
				insertJSONData(response);
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				prgDialog.dismiss();
				if (statusCode == 404) {
					Toast.makeText(Multijoueur.this, R.string.err404, Toast.LENGTH_LONG).show();
				} else if (statusCode == 500 || statusCode == 503) {
					Toast.makeText(Multijoueur.this, R.string.err500, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(Multijoueur.this, R.string.err, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	public void supprDefi(View v) {
		final int groupPosition = (Integer) v.getTag();
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.supprDefi)
			.setMessage(this.getString(R.string.supprDefiConf, adversaires.get(groupPosition).nom))
			.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					base.removeDefi(adversaires.remove(groupPosition), user.getId());
					syncData();
				}
			})
			.setNegativeButton(R.string.annuler, null)
			.show();
	}
	
	public void actionDefi(View v) {
		final int groupPosition = (Integer) v.getTag();
		defi = adversaires.get(groupPosition);
		switch(defi.getEtat(user.getId())) {
		case Defi.ATTENTE: // Send POKE
			// TODO: Poke.
			break;
		case Defi.RESULTATS: // Afficher les résultats
			Resultats.callback = new Resultats.callBackInterface() {
				@Override
				public void suite() {
					if(defi.type>0) { // Partie rapide
						base.removeDefi(adversaires.remove(groupPosition), user.getId());
						syncData();
					} else {
						base.setResultatsVus(defi.id,defi.nMatch);
						adapt.notifyDataSetChanged();
					}
				}
			};
			Resultats.multi = this;
			Resultats.DISPLAY_RES = true;
			startActivity(new Intent(this, Resultats.class));
			break;
		case Defi.RELEVER: // Lancer le jeu avec Seed donnée
			releverMatch();
			break;
		case Defi.LANCER: // Lancer nouveau défi
			choixNiveau();
			break;
		case Defi.OBSOLETE: // Deadline dépassée -> il faudrait synchroniser.
			Toast.makeText(this, R.string.obsolete_txt, Toast.LENGTH_LONG).show();
		}
	}
	
	public void syncTotale(View v) {
		if(!connect.isConnectedToInternet()) {
			Toast.makeText(this, R.string.hors_connexion, Toast.LENGTH_SHORT).show();
			return;
		}
		base.taskSyncTotale(user.getId());
		syncData();
	}
	
	private void dispUser() {
		((ImageView) findViewById(R.id.user_avatar)).setImageResource(user.getAvatar());
		((TextView) findViewById(R.id.user_name)).setText(user.getPseudo());
		((TextView) findViewById(R.id.user_exp)).setText(getString(R.string.exp)+" :\n"+String.format("%,d", user.getExp()));
		((TextView) findViewById(R.id.user_defis)).setText(getString(R.string.defis_joues)+" : "+user.getDefis());
		((TextView) findViewById(R.id.user_wins)).setText(getString(R.string.defis_gagnés)+" : "+user.getWin());
	}
	
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			System.out.println("onReceiveNotification : "+newMessage);
			if(newMessage.equals("GCMRegistartion")) { // GCM Registration successful => Server registration
				Toast.makeText(Multijoueur.this, R.string.gcm_success, Toast.LENGTH_SHORT).show();
				registerUser();
				return;
			} else {
				String title = context.getString(R.string.app_name), msg="";
				try {
					JSONObject o = new JSONObject(newMessage);
					String typ = o.getString("type");
					if(typ.equals("newMatch")) {
						title = o.getString("nomDefi");
						msg = context.getString(R.string.notif_newdefi, o.getString("initPlayer"));
					} else if(typ.equals("results")) {
						title = o.getString("nomDefi");
						if(o.has("initPlayer"))
							msg = context.getString(R.string.notif_results, o.getString("initPlayer"));
						else
							msg = context.getString(R.string.notif_results_exp);
					} else if(typ.equals("message")) {
						msg = o.getString("message");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Toast.makeText(Multijoueur.this, context.getString(R.string.notification)+" !\n\n"+title+"\n"+msg, Toast.LENGTH_LONG).show();
				syncData();
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
			public int getProgress() {
				return menu.avancement;
			}
			@Override
			public boolean registered(String JSONresponse, String name) {
				try {
					JSONArray j = new JSONArray(JSONresponse);
					base.insertJSONJoueurs(j);
					menu.editor.putString("pseudo", name);
					menu.editor.putInt("id", j.getJSONObject(0).getInt("id"));
					menu.editor.commit();
					loadJoueurs();
					base.getDefis(user.getId(),joueurs,adversaires);
					adapt = new DefiExpandableAdapter(Multijoueur.this, user.getId(), adversaires);
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
		adapt.setLaunchEnabled(false);
		adapt.notifyDataSetChanged();
		loader.showNext();
		loader.setEnabled(false);
		RequestParams params = new RequestParams();
		params.put("joueur", ""+menu.pref.getInt("id", 0));
		params.put("tasks", base.getTasks());
		System.out.println(base.getTasks());
		params.put("expToSync", ""+menu.expToSync);
		params.put("progress", ""+menu.avancement);
		client.post(SERVER_URL+"/sync_data.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				loader.showNext();
				loader.setEnabled(true);
				adapt.setLaunchEnabled(true);
				if(insertJSONData(response)) {
					base.clearTasks();
					// TODO : acquitement pour que le serveur supprime les *ToSync.
				}
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				loader.showNext();
				loader.setEnabled(true);
				adapt.setLaunchEnabled(true);
				adapt.notifyDataSetChanged();
				Toast.makeText(Multijoueur.this, R.string.sync_fail, Toast.LENGTH_SHORT).show();
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
	private boolean insertJSONData(String def) {
		boolean res;
		System.out.println(def);
		if(def.length()>=4) { // Dans le cas où def vaut []
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
				if(o.has("tasks"))
					base.execJSONTasks(this, (JSONArray) o.get("tasks"), user.getId());
				res = true;
			} catch (JSONException e) {
				e.printStackTrace();
				res = false;
			}
		} else
			res = true;
		int pRapide = base.getDefis(user.getId(),joueurs,adversaires);
		adapt.notifyDataSetChanged();
		if(pRapide!=-1) {
			View v = new View(this);
			v.setTag(pRapide);
			actionDefi(v);
		}
		return res;
	}
	
}
