package com.game.colibri;

public class Participation {
	
	public static final int FORFAIT = Integer.MAX_VALUE-1, NOT_PLAYED = Integer.MAX_VALUE;
	
	public Joueur joueur;
	public int win;
	public int t_cours, penalite_cours;
	public int t_fini, penalite_fini, exp;
	public int gagne;
	
	public Participation(Joueur j, int w, int tc, int pc, int tf, int pf, int exp, int gagne) {
		joueur = j;
		win = w;
		t_cours = tc;
		penalite_cours = pc;
		t_fini = tf;
		penalite_fini = pf;
		this.exp = exp;
		this.gagne = gagne;
	}
	
	public void solved(int t, int p) {
		t_cours = t;
		penalite_cours = p;
	}
	
	public void fini(int classement, int exp, int multiplicateur) {
		t_fini = t_cours;
		penalite_fini = penalite_cours;
		t_cours = 0;
		penalite_cours = 0;
		this.gagne = classement;
		if(t_fini>=FORFAIT)
			gagne=0;
		if(t_fini==NOT_PLAYED)
			return;
		if(gagne==1) {
			if(multiplicateur>1) {
				win++;
				joueur.win(multiplicateur-1);
			}
			joueur.addExp(exp*multiplicateur);
			this.exp = exp*multiplicateur;
		} else {
			joueur.loose();
			this.exp = t_fini==FORFAIT ? 0: exp;
			joueur.addExp(this.exp);
		}
		if(multiplicateur>1)
			joueur.defi();
	}
}
