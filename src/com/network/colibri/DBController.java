package com.network.colibri;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.game.colibri.Defi;
import com.game.colibri.Joueur;
import com.game.colibri.Participation;
import com.game.colibri.R;
import com.google.gson.Gson;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBController  extends SQLiteOpenHelper {
		
	public DBController(Context applicationcontext) {
        super(applicationcontext, "Colibri.db", null, 5);
        Defi.base = this;
    }
	
	//Creates Tables
	@Override
	public void onCreate(SQLiteDatabase database) {
		String query;
		query = "CREATE TABLE IF NOT EXISTS defis ("
				+ " id int NOT NULL,"
				+ " nom varchar(25) DEFAULT NULL,"
				+ " nMatch int NOT NULL,"
				+ " nivCours text NOT NULL,"
				+ " nivFini text NOT NULL,"
				+ " t_max int NOT NULL,"
				+ " limite int NOT NULL,"
				+ " PRIMARY KEY (`id`)"
				+ ")";
        database.execSQL(query);
        query = "CREATE TABLE IF NOT EXISTS joueurs ("
        		+ " pseudo varchar(15) NOT NULL,"
        		+ " pays varchar(10) DEFAULT NULL,"
				+ " exp int NOT NULL DEFAULT 0,"
				+ " defis int NOT NULL DEFAULT 0,"
				+ " win int NOT NULL DEFAULT 0,"
				+ " loose int NOT NULL DEFAULT 0,"
				+ " avatar int NOT NULL DEFAULT 0,"
				+ " PRIMARY KEY (pseudo)"
				+ ")";
        database.execSQL(query);
        query = "CREATE TABLE IF NOT EXISTS participations ("
        		+ " defi int NOT NULL,"
        		+ " joueur varchar(15) NOT NULL,"
				+ " win int NOT NULL DEFAULT 0,"
				+ " t_cours int NOT NULL DEFAULT 0,"
				+ " penalite_cours int NOT NULL DEFAULT 0,"
				+ " t_fini int NOT NULL DEFAULT 0,"
				+ " penalite_fini int NOT NULL DEFAULT 0,"
				+ " exp int NOT NULL DEFAULT 0,"
				+ " gagne int NOT NULL DEFAULT 0,"
				+ " PRIMARY KEY (defi, joueur),"
				+ "FOREIGN KEY(defi) REFERENCES defis(id) ON DELETE CASCADE ON UPDATE CASCADE,"
				+ "FOREIGN KEY(joueur) REFERENCES joueurs(pseudo) ON DELETE CASCADE ON UPDATE CASCADE"
				+ ")";
        database.execSQL(query);
        query = "CREATE TABLE IF NOT EXISTS resultatsVus ("
        		+ " defi int NOT NULL,"
				+ " nMatch int NOT NULL DEFAULT 0,"
				+ " PRIMARY KEY (defi),"
				+ "FOREIGN KEY(defi) REFERENCES defis(id) ON DELETE CASCADE ON UPDATE CASCADE"
				+ ")";
        database.execSQL(query);
        query = "CREATE TABLE IF NOT EXISTS tasks ("
				+ " id INTEGER PRIMARY KEY,"
				+ " task text NOT NULL"
				+ ")";
        database.execSQL(query);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		String query;
		query = "DROP TABLE IF EXISTS defis";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS joueurs";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS participations";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS resultatsVus";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS tasks";
		database.execSQL(query);
        onCreate(database);
	}
	
	/**
	 * 
	 * @param jsonArray [{id:2,nMatch:4, ...},{},...]
	 * @return
	 */
	public void insertJSONDefis(JSONArray jsonArray) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		for(int i=0; i<jsonArray.length(); i++) {
			try {
				JSONObject d = jsonArray.getJSONObject(i);
				values.put("id", d.getInt("id"));
				values.put("nom",d.getString("nom"));
				values.put("nMatch",d.getInt("nMatch"));
				values.put("nivCours",d.getString("nivCours"));
				values.put("nivFini",d.getString("nivFini"));
				values.put("t_max", d.getInt("t_max"));
				values.put("limite", d.getInt("t_restant")+System.currentTimeMillis()/1000);
				database.insertWithOnConflict("defis", null, values, SQLiteDatabase.CONFLICT_REPLACE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		database.close();
	}
	
	public void insertJSONParticipations(JSONArray jsonArray) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		for(int i=0; i<jsonArray.length(); i++) {
			try {
				JSONObject d = jsonArray.getJSONObject(i);
				values.put("defi",d.getInt("defi"));
				values.put("joueur",d.getString("joueur"));
				values.put("win",d.getInt("win"));
				values.put("t_cours",d.getInt("t_cours"));
				values.put("penalite_cours",d.getInt("penalite_cours"));
				values.put("t_fini",d.getInt("t_fini"));
				values.put("penalite_fini",d.getInt("penalite_fini"));
				values.put("exp",d.getInt("exp"));
				values.put("gagne",d.getInt("gagne"));
				database.insertWithOnConflict("participations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		database.close();
	}
	
	public void insertJSONJoueurs(JSONArray jsonArray) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		for(int i=0; i<jsonArray.length(); i++) {
			try {
				JSONObject d = jsonArray.getJSONObject(i);
				values.put("pseudo",d.getString("pseudo"));
				values.put("pays",d.getString("pays"));
				values.put("exp",""+d.getInt("exp"));
				values.put("defis",""+d.getInt("defis"));
				values.put("win",""+d.getInt("win"));
				values.put("loose",""+d.getInt("loose"));
				values.put("avatar",""+d.getInt("avatar"));
				database.insertWithOnConflict("joueurs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		database.close();
	}
	
	/**
	 * Retourne les tâches en attente à envoyer au serveur sous cette forme :
	 * [{"task":"finMatch", "id_defi":_, "pseudo":_, "temps":_, "vainqueur":_, "exp_v":_, "exp_p":_},
	 *  {"task":"solvedMatch", "id_defi":_, "pseudo":_, "temps":_}, {"task":"nouveauMatch", ...}]
	 * @return
	 */
	public String getTasks() {
		ArrayList<String> tasks = new ArrayList<String>();
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery("SELECT task FROM `tasks`", null);
	    if (cursor.moveToFirst()) {
	        do {
				tasks.add(cursor.getString(0));
	        } while(cursor.moveToNext());
	    }
	    database.close();
		return tasks.toString();
	}
	
	/**
	 * Une fois que les tâches ont été envoyées au serveur, supprime les tâches.
	 */
	public void clearTasks() {
		SQLiteDatabase database = this.getWritableDatabase();
		database.delete("tasks", null, null);
		//database.delete("sqlite_sequence", "name='tasks'", null);
		database.close();
	}
	
	/**
	 * Retourne la HashMap des joueurs impliqués dans les défis.
	 * @return
	 */
	public HashMap<String, Joueur> getJoueurs(HashMap<String,Joueur> joueurs) {
		joueurs.clear();
		String selectQuery = "SELECT pseudo,pays,exp,defis,win,loose,avatar FROM `joueurs`";
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    if (cursor.moveToFirst()) {
	        do {
	        	joueurs.put(cursor.getString(0), new Joueur(cursor.getString(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6)));
	        } while(cursor.moveToNext());
	    }
	    database.close();
		return joueurs;
	}
	
	/**
	 * Retourne la liste des défis en ordonnant en premier les défis qui ne sont pas en Attente.
	 * @param user le nom de l'utilisateur
	 * @param joueurs les joueurs
	 * @return la liste des défis à afficher
	 */
	public ArrayList<Defi> getDefis(String user, HashMap<String,Joueur> joueurs, ArrayList<Defi> l) {
		l.clear();
		String selectQuery = "SELECT id,nom,nMatch,nivCours,nivFini,t_max,limite FROM `defis` JOIN `participations` ON id=defi AND t_cours=0 WHERE joueur='"+user+"' UNION SELECT id,nom,nMatch,nivCours,nivFini,t_max,limite FROM `defis` JOIN `participations` ON id=defi AND t_cours>0 WHERE joueur='"+user+"'";
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    if (cursor.moveToFirst()) {
	        do {
	        	String selectQuery2 = "SELECT joueur,win,t_cours,penalite_cours,t_fini,penalite_fini,exp,gagne FROM `participations` WHERE defi="+cursor.getInt(0);
	        	Cursor cursor2 = database.rawQuery(selectQuery2, null);
	        	HashMap<String,Participation> part = new HashMap<String, Participation>();
	        	if(cursor2.moveToFirst()) {
		        	do {
		        		Participation p = new Participation(joueurs.get(cursor2.getString(0)),cursor2.getInt(1),cursor2.getInt(2),cursor2.getInt(3),cursor2.getInt(4),cursor2.getInt(5),cursor2.getInt(6),cursor2.getInt(7));
		        		part.put(cursor2.getString(0), p);
		        	} while(cursor2.moveToNext());
	        	}
	        	l.add(new Defi(cursor.getInt(0), cursor.getString(1), part, cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6)));
	        } while (cursor.moveToNext());
	    }
	    database.close();
		return l;
	}
	
	/**
	 * Supprime les Joueurs qui n'ont aucune participation dans les défis de l'utilisateur.
	 * @param database
	 */
	private void cleanJoueurs(SQLiteDatabase database) {
		database.rawQuery("DELETE FROM `joueurs` WHERE pseudo NOT IN (SELECT joueur FROM Participations)", null);
	}
	
	/**
	 * Supprimer un défi et retirer sa participation.
	 * @param defi
	 */
	public void removeDefi(Defi defi) {
		SQLiteDatabase database = this.getWritableDatabase();
		database.delete("defis", "id="+defi.id, null);
		cleanJoueurs(database);
		// Requête serveur
		ContentValues values = new ContentValues();
		JSONObject o = new JSONObject();
		try {
			o.put("task","removeParticip");
			o.put("defi",defi.id);
			values.put("task", o.toString());
			database.insert("tasks", null, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		database.close();
	}

	/**
	 * Stocker le nouveau défi et envoyer maj pour seulement le défi.
	 * @param defi
	 */
	public void updateDefi(Defi defi) {
		Gson g = new Gson();
		SQLiteDatabase database = this.getWritableDatabase();
		String m = g.toJson(defi.match);
		// Stocker le nouveau match
		ContentValues values = new ContentValues();
		values.put("nivCours", m);
		values.put("limite", defi.limite);
		database.update("defis", values, "id="+defi.id, null);
		// Mettre en queue une requête de nouveau match
		values = new ContentValues();
		JSONObject o = new JSONObject();
		try {
			o.put("task","nouveauMatch");
			o.put("defi",defi.id);
			o.put("nivCours",m);
			values.put("task", o.toString());
			database.insert("tasks", null, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		database.close();
	}
	
	/**
	 * Stocker les modifications dans la BDD + envoyer une maj pour le defi, les participations
	 * ET les joueurs.
	 * @param defi
	 */
	public void updateDefiTout(Defi defi, String user, int nMatch) {
		Gson g = new Gson();
		SQLiteDatabase database = this.getWritableDatabase();
		// MAJ Defi
		ContentValues values = new ContentValues();
		values.put("nMatch", defi.nMatch);
		values.put("nivCours", g.toJson(defi.match));
		values.put("nivFini", g.toJson(defi.matchFini));
		database.update("defis", values, "id="+defi.id, null);
		for(Participation p : defi.participants.values()) {
			// MAJ Participations
			values = new ContentValues();
			values.put("win", p.win);
			values.put("t_cours", p.t_cours);
			values.put("penalite_cours", p.penalite_cours);
			values.put("t_fini", p.t_fini);
			values.put("penalite_fini", p.penalite_fini);
			values.put("exp", p.exp);
			values.put("gagne", p.gagne);
			database.update("participations", values, "defi="+defi.id+" AND joueur='"+p.joueur.getPseudo()+"'", null);
			// MAJ Joueurs
			values = new ContentValues();
			values.put("exp", p.joueur.getExp());
			values.put("defis", p.joueur.getDefis());
			values.put("win", p.joueur.getWin());
			values.put("loose", p.joueur.getLost());
			database.update("joueurs", values, "pseudo='"+p.joueur.getPseudo()+"'", null);
		}
		// Requête serveur
		values = new ContentValues();
		JSONObject o = new JSONObject();
		try {
			Participation p = defi.participants.get(user);
			o.put("task","finMatch");
			o.put("defi",defi.id);
			o.put("temps",(p.t_cours!=0) ? p.t_cours : p.t_fini);
			o.put("penalite",(p.t_cours!=0) ? p.penalite_cours : p.penalite_fini);
			o.put("nMatch", nMatch);
			values.put("task", o.toString());
			database.insert("tasks", null, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		database.close();
	}
	
	/**
	 * Stocker les modifications dans la BDD + envoyer une maj pour la participation SEULEMENT.
	 * @param participation
	 */
	public void updateParticipation(Participation p, int defi, int nMatch) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("win", p.win);
		values.put("t_cours", p.t_cours);
		values.put("penalite_cours", p.penalite_cours);
		values.put("t_fini", p.t_fini);
		values.put("penalite_fini", p.penalite_fini);
		values.put("exp", p.exp);
		values.put("gagne", p.gagne);
		database.update("participations", values, "defi="+defi+" AND joueur='"+p.joueur.getPseudo()+"'", null);
		// Requète Serveur
		values = new ContentValues();
		JSONObject o = new JSONObject();
		try {
			o.put("task","finMatch");
			o.put("defi",defi);
			o.put("temps",(p.t_cours!=0) ? p.t_cours : p.t_fini);
			o.put("penalite",(p.t_cours!=0) ? p.penalite_cours : p.penalite_fini);
			o.put("nMatch", nMatch);
			values.put("task", o.toString());
			database.insert("tasks", null, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		database.close();
	}
	
	/**
	 * Détermine si les résultats du défi id ont été vus pour le match nMatch. Seulement local.
	 * @param id le défi
	 * @param nMatch la valeur
	 */
	public void setResultatsVus(int id, int nMatch) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("defi", id);
		values.put("nMatch", nMatch);
		database.insertWithOnConflict("resultatsVus", null, values, SQLiteDatabase.CONFLICT_REPLACE);
		database.close();
	}
	
	/**
	 * Retourne le nMatch des derniers résultats vus.
	 */
	public int getResultatsVus(int id) {
		String selectQuery = "SELECT nMatch FROM resultatsVus WHERE defi="+id;
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    if(cursor.moveToFirst())
	    	return cursor.getInt(0);
	    else
	    	return 0;
	}
	
	/**
	 * Supprime tout et demande le renvoi de toutes les données.
	 */
	public void taskSyncTotale() {
		SQLiteDatabase database = this.getWritableDatabase();
		database.delete("participations", null, null);
		database.delete("defis", null, null);
		database.delete("joueurs", null, null);
		ContentValues values = new ContentValues();
		values = new ContentValues();
		JSONObject o = new JSONObject();
		try {
			o.put("task","syncTotale");
			values.put("task", o.toString());
			database.insert("tasks", null, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		database.close();
	}

	public void deletePart(Context context, JSONArray jsonArray) {
		SQLiteDatabase database = this.getWritableDatabase();
		String liste = "";
		for(int i=0; i<jsonArray.length(); i++) {
			try {
				JSONObject d = jsonArray.getJSONObject(i);
				Cursor cursor = database.rawQuery("SELECT nom FROM `defis` WHERE id="+d.getInt("part_defi"), null);
				String nomDefi="?";
			    if (cursor.moveToFirst()) {
			    	nomDefi = cursor.getString(0);
			    }
				database.delete("participations", "defi="+d.getInt("part_defi")+" AND joueur='"+d.getString("part_joueur")+"'", null);
				liste+=context.getResources().getString(R.string.deletedPart, d.getString("part_joueur"), nomDefi)+"\n";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		AlertDialog.Builder box = new AlertDialog.Builder(context);
		box.setTitle(R.string.notification);
		box.setMessage(liste);
		box.show();
		database.close();
	}

	public void deleteDef(Context context, JSONArray jsonArray) {
		SQLiteDatabase database = this.getWritableDatabase();
		String liste = "";
		for(int i=0; i<jsonArray.length(); i++) {
			try {
				JSONObject d = jsonArray.getJSONObject(i);
				Cursor cursor = database.rawQuery("SELECT nom FROM `defis` WHERE id="+d.getInt("defi"), null);
				String nomDefi="?";
			    if (cursor.moveToFirst()) {
			    	nomDefi = cursor.getString(0);
			    }
				database.delete("defis", "id="+d.getInt("defi"), null);
				liste+=context.getResources().getString(R.string.deletedDef, nomDefi)+"\n";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		AlertDialog.Builder box = new AlertDialog.Builder(context);
		box.setTitle(R.string.notification);
		box.setMessage(liste);
		box.show();
		database.close();
	}

}
