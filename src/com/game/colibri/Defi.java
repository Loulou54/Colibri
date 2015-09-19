package com.game.colibri;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.network.colibri.DBController;

public class Defi {
	
	public static final byte ATTENTE=0, RESULTATS=1, RELEVER=2, LANCER=3, OBSOLETE=4; // États possible du défi contre "adversaire".
	public static DBController base;
	
	public int id;
	public String nom;
	public HashMap<String,Participation> participants;
	public int nMatch;
	public Match match; // Le match en cours
	public Match matchFini; // Le dernier match terminé
	public int t_max;
	public long limite;
	
	public Defi(int id, String nom, HashMap<String,Participation> p, int nMatch, String nivCours, String nivFini, int t_m, int lim) {
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
	}
	
	/**
	 * Appelé en fin de match pour incrémenter les différents scores, etc
	 * @param user
	 */
	public boolean finMatch(String user, int temps, int penalite) {
		participants.get(user).solved(temps,penalite);
		Participation[] classement = participants.values().toArray(new Participation[0]);
		Arrays.sort(classement, new Comparator<Participation>() {
			@Override
			public int compare(Participation lhs, Participation rhs) {
				return lhs.t_cours+lhs.penalite_cours - (rhs.t_cours+rhs.penalite_cours);
			}
		});
		int partEffectives = 0;
		for(Participation p : classement) {
			if(p.t_cours!=Participation.NOT_PLAYED)
				partEffectives++;
		}
		if(classement[0].t_cours!=0) { // Tous les participants ont fini.
			int pos=1, t_pos=classement[0].t_cours;
			for(Participation p : classement) {
				p.fini(p.t_cours!=t_pos ? ++pos : pos, match.exp, partEffectives);
			}
			nMatch++;
			matchFini = match;
			match = null;
			base.updateDefiTout(this, user, nMatch-1);
		} else {
			base.updateParticipation(participants.get(user), id, nMatch);
		}
		return classement[0].t_cours!=0;
	}
	
	public int getEtat(String user) {
		Participation p = participants.get(user);
		if(matchFini!=null && base.getResultatsVus(id)!=nMatch)
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
