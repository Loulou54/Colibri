package com.game.colibri;

import java.util.Arrays;
import java.util.Comparator;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.network.colibri.DBController;

public class Defi {
	
	public static final byte ATTENTE=0, RESULTATS=1, RELEVER=2, LANCER=3, OBSOLETE=4; // États possible du défi contre "adversaire".
	public static DBController base;
	
	public int id;
	public String nom;
	public SparseArray<Participation> participants;
	public int nMatch;
	public Match match; // Le match en cours
	public Match matchFini; // Le dernier match terminé
	public int t_max;
	public long limite;
	public int type;
	
	public Defi(int id, String nom, SparseArray<Participation> p, int nMatch, String nivCours, String nivFini, int t_m, int lim, int type) {
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
	}
	
	/**
	 * Appelé en fin de match pour incrémenter les différents scores, etc
	 * @param user
	 */
	public boolean finMatch(int user, int temps, int penalite) {
		participants.get(user).solved(temps,penalite);
		Participation[] classement = new Participation[participants.size()];
		for(int i=0; i<classement.length; i++) {
			classement[i] = participants.valueAt(i);
		}
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
		boolean result = (classement[0].t_cours!=0 && classement.length>=type);
		if(result) { // Tous les participants ont fini.
			int ligne=0, pos=0, t_pos=0;
			for(Participation p : classement) {
				ligne++;
				pos = p.t_cours!=t_pos ? ligne : pos;
				t_pos=p.t_cours;
				p.fini(pos, match.exp, partEffectives);
			}
			nMatch++;
			matchFini = match;
			match = null;
			base.updateDefiTout(this, user, nMatch-1);
		} else {
			base.updateParticipation(participants.get(user), id, nMatch);
		}
		return result;
	}
	
	public int getEtat(int user) {
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
		public int progressMin;
		public int exp;
		
		public Match(int m, long s, int[] p, int pm, int e) {
			mode=m;
			seed=s;
			param=p;
			progressMin=pm;
			exp=e;
		}
		
	}
}
