package com.game.colibri;

import java.util.Arrays;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.network.colibri.DBController;

public class Defi {
	
	public static final byte ATTENTE=0, RESULTATS=1, RELEVER=2, LANCER=3, OBSOLETE=4; // États possibles du défi.
	
	public int id;
	public String nom;
	public SparseArray<Participation> participants;
	public int nMatch;
	public Match match; // Le match en cours
	public Match matchFini; // Le dernier match terminé
	public int t_max;
	public long limite;
	public int type;
	public int resVus;
	
	public static Defi DefiFromJSON(String jsonDefi) throws JSONException {
		if(jsonDefi==null)
			return null;
		Gson g = new Gson();
		Defi d = g.fromJson(jsonDefi, Defi.class);
		JSONObject jso = (new JSONObject(jsonDefi)).getJSONObject("participants"); // Gson ne parvient pas à reconstruire les participations de SparseArray donc on le fait nous même.
		JSONArray pArray = jso.getJSONArray("mValues"); // Participations
		JSONArray kArray = jso.getJSONArray("mKeys"); // Clés (id joueurs)
		int nP = jso.getInt("mSize"); // Taille de la SparseArray
		for(int i=0; i<nP; i++) {
			d.participants.put(kArray.getInt(i), g.fromJson(pArray.getString(i), Participation.class));
		}
		return d;
	}
	
	public Defi(int id, String nom, SparseArray<Participation> p, int nMatch, String nivCours, String nivFini, int t_m, int lim, int type, int resVus) {
		this.id = id;
		this.nom = nom;
		participants = p;
		this.nMatch = nMatch;
		Gson g = new Gson();
		try {
			match = g.fromJson(nivCours, Match.class);
		} catch (JsonSyntaxException e) {
			match = null;
		}
		try {
			matchFini = g.fromJson(nivFini, Match.class);
		} catch (JsonSyntaxException e) {
			matchFini = null;
		}
		t_max = t_m;
		limite = lim;
		this.type = type;
		this.resVus = resVus;
	}
	
	public String toJSON() {
		return (new Gson()).toJson(this, Defi.class);
	}
	
	public int computeScores(Participation[] classement, double[] scores) {
		int partEffectives = 0;
		double scoreTotal = 0;
		double cotisations = 0;
		// Comptage des participants (non joués exclus) et la somme de leurs scores
		for(Participation p : classement) {
			if(p.t_cours!=Participation.NOT_PLAYED) {
				partEffectives++;
				scoreTotal += Math.sqrt(p.joueur.getScore())+8;
			}
		}
		if(scoreTotal==0) scoreTotal = 1;
		// Cotisations
		for(Participation p : classement) {
			if(p.t_cours!=Participation.NOT_PLAYED) {
				double cotis = 10 + 6*Math.sqrt(partEffectives)*(Math.sqrt(p.joueur.getScore())+8)/scoreTotal;
				p.setCotisation(cotis);
				cotisations += cotis;
			}
		}
		// Redistribution
		double C = 5.;
		double A = cotisations/(Math.log(1+partEffectives/C)-partEffectives*Math.log(1+1./(partEffectives+C-1)));
		double B = A*Math.log(1+1./(partEffectives+C-1));
		int nEgal=1, t_pos=0; // permet de traiter les égalités dans le classement
		for(int ligne=0; ligne < classement.length; ligne++) {
			if(classement[ligne].t_cours == Participation.NOT_PLAYED) {
				scores[ligne] = 0;
				continue;
			}
			scores[ligne] = A*Math.log(1+1./(ligne+C))-B;
			nEgal = classement[ligne].t_cours!=t_pos ? 1 : nEgal+1;
			t_pos = classement[ligne].t_cours;
			if(nEgal > 1) {
				double scoreEgal = (scores[ligne-1]*(nEgal-1) + scores[ligne])/nEgal;
				for(int i=0; i < nEgal; i++) {
					scores[ligne-i] = scoreEgal;
				}
			}
		}
		return partEffectives;
	}
	
	/**
	 * Appelé en fin de match pour incrémenter les différents scores, etc
	 * @param user
	 */
	public boolean finMatch(DBController base, int user, int temps) {
		participants.get(user).solved(temps);
		Participation[] classement = new Participation[participants.size()];
		for(int i=0; i<classement.length; i++) {
			classement[i] = participants.valueAt(i);
		}
		Arrays.sort(classement, new Comparator<Participation>() {
			@Override
			public int compare(Participation lhs, Participation rhs) {
				return lhs.t_cours - rhs.t_cours;
			}
		});
		boolean result = (classement[0].t_cours!=0 && (type==0 || classement.length==type));
		if(result) { // Tous les participants ont fini.
			double scores[] = new double[classement.length];
			int partEffectives = computeScores(classement, scores);
			int pos=0, t_pos=0; // permet de traiter les égalités dans le classement
			for(int ligne=0; ligne < classement.length; ligne++) {
				pos = classement[ligne].t_cours!=t_pos ? ligne+1 : pos;
				t_pos = classement[ligne].t_cours;
				classement[ligne].fini(pos, partEffectives, scores[ligne]);
			}
			nMatch++;
			matchFini = match;
			match = null;
			if(base!=null)
				base.updateDefiTout(this, user, nMatch-1);
		} else if(base!=null) {
			base.updateParticipation(participants.get(user), id, nMatch);
		}
		return result;
	}
	
	public int getEtat(int user) {
		Participation p = participants.get(user);
		if(matchFini!=null && resVus!=nMatch)
			return RESULTATS;
		else if(match!=null && t_max!=0 && limite-System.currentTimeMillis()/1000<0)
			return OBSOLETE;
		else if(p.t_cours!=0)
			return ATTENTE;
		else if(match==null)
			return LANCER;
		else
			return RELEVER;
	}
	
	/**
	 * Retourne l'avancement minimal des participants dans la campagne.
	 * @return
	 */
	public int getProgressMin() {
		int m = Integer.MAX_VALUE;
		for(int i=0, length=participants.size(); i<length; i++) {
			if(participants.valueAt(i).joueur.getProgress()<m) {
				m = participants.valueAt(i).joueur.getProgress();
			}
		}
		return m;
	}
	
	/**
	 * Destiné à contenir les infos du match en cours.
	 * @author Louis
	 *
	 */
	public static class Match {
		
		public int mode;
		public long seed;
		public int[] param;
		public int avancement;
		public int progressMin;
		public int exp;
		
		public Match(int m, long s, int[] p, int a, int pm, int e) {
			mode=m;
			seed=s;
			param=p;
			avancement=a;
			progressMin=pm;
			exp=e;
		}
		
	}
}
