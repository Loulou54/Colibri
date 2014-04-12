package com.game.colibri;

public class Niveau {
	
	/**
	 * Classe contenant toutes les donn�es d'UN niveau : carte, position initiale du colibri, solution
	 * TODO : coordonn�es des vaches et leur parcours
	 */
	
	public int[][] carte;
	public int db_l,db_c;
	public int[][] solution;
	
	public Niveau(int[][] map,int l,int c,int[][] sol) {
		carte=new int[12][20];
		System.arraycopy(map, 0, carte, 0, 12); // Copie de l'originale car les modifications durant le cours du jeu (fleurs ramass�es, etc) ne doivent pas affecter l'originale
		db_l=l;
		db_c=c;
		solution=sol;
	}
}
