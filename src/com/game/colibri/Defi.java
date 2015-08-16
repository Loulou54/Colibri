package com.game.colibri;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.network.colibri.DBController;

public class Defi {
	
	public static final byte ATTENTE=0, RESULTATS=1, RELEVER=2, LANCER=3; // États possible du défi contre "adversaire".
	public static DBController base;
	
	public int id;
	public String nom;
	public HashMap<String,Participation> participants;
	public int nMatch;
	public Match match; // Le match en cours
	public Match matchFini; // Le dernier match terminé
	
	public Defi(int id, String nom, HashMap<String,Participation> p, int nMatch, String nivCours, String nivFini) {
		this.id = id;
		this.nom = nom;
		participants = p;
		this.nMatch = nMatch;
		Gson g = new Gson();
		try {
			match = g.fromJson(nivCours, Match.class);
		} catch (JsonSyntaxException e) {
			match = null;
			e.printStackTrace();
		}
		try {
			matchFini = g.fromJson(nivFini, Match.class);
		} catch (JsonSyntaxException e) {
			matchFini = null;
			e.printStackTrace();
		}
	}
	
	/**
	 * Appelé en fin de match pour incrémenter les différents scores, etc
	 * @param user
	 */
	public boolean finMatch(String user, int temps, int penalite) {
		participants.get(user).solved(temps,penalite);
		int t_min=Integer.MAX_VALUE;
		for(Participation p : participants.values()) {
			if(p.t_cours+p.penalite_cours<=t_min) {
				t_min = p.t_cours+p.penalite_cours;
			}
		}
		if(t_min!=0) { // Tous les participants ont fini.
			for(Participation p : participants.values()) {
				p.fini(p.t_cours+p.penalite_cours==t_min && t_min!=Integer.MAX_VALUE, match.exp);
			}
			nMatch++;
			matchFini = match;
			match = null;
			base.updateDefiTout(this, user);
		} else {
			base.updateParticipation(participants.get(user), id);
		}
		return t_min!=0;
	}
	
	public int getEtat(String user) {
		Participation p = participants.get(user);
		if(!p.resultatsVus && matchFini!=null)
			return RESULTATS;
		else if(p.t_cours!=0)
			return ATTENTE;
		else if(match==null)
			return LANCER;
		else
			return RELEVER;
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
		public int exp;
		
		public Match(int m, long s, int[] p, int e) {
			mode=m;
			seed=s;
			param=p;
			exp=e;
		}
		
	}
}
