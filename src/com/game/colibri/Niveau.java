package com.game.colibri;
/**
 * Classe contenant toutes les donn�es d'UN niveau : carte, position initiale du colibri, solution
 * TODO : coordonnées des vaches et leur parcours
 */

public class Niveau {
	
	
	
	public int[][] carte;
	public int db_l,db_c;
	public int[][] solution;
	
	/**
	 * Consruicteur de niveau 
	 * 		@param map
	 * 		@param l
	 * 		@param c
	 *		 @param sol
	 */
	public Niveau(int[][] map,int l,int c,int[][] sol) {
		carte=new int[12][20];
		System.arraycopy(map, 0, carte, 0, 12); // Copie de l'originale car les modifications durant le cours du jeu (fleurs ramassées, etc) ne doivent pas affecter l'originale
		db_l=l;
		db_c=c;
		solution=sol;
	}
}
