package com.game.colibri;

public class Participation {
	
	public Joueur joueur;
	public int win;
	public int t_cours, penalite_cours;
	public int t_fini, penalite_fini, exp;
	public boolean gagne, resultatsVus;
	
	public Participation(Joueur j, int w, int tc, int pc, int tf, int pf, int exp, boolean gagne, boolean resultatsVus) {
		joueur = j;
		win = w;
		t_cours = tc;
		penalite_cours = pc;
		t_fini = tf;
		penalite_fini = pf;
		this.exp = exp;
		this.gagne = gagne;
		this.resultatsVus = resultatsVus;
	}
	
	public void solved(int t, int p) {
		t_cours = t;
		penalite_cours = p;
	}
	
	public void fini(boolean gagne, int exp) {
		t_fini = t_cours;
		penalite_fini = penalite_cours;
		t_cours = 0;
		t_fini = 0;
		this.gagne = gagne;
		resultatsVus = false;
		if(gagne) {
			win++;
			joueur.win();
			joueur.addExp(exp*2);
			this.exp = exp*2;
		} else {
			joueur.loose();
			joueur.addExp(exp);
			this.exp = exp;
		}
		joueur.defi();
	}
}
