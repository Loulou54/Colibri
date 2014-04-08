package com.game.colibri;

public class Niveau {
	
	/**
	 * Classe contenant toutes les données d'UN niveau : carte, position initiale du colibri, solution
	 * TODO : coordonnées des vaches et leur parcours
	 */
	
	public int[][] carte;
	public int dbx,dby;
	public int[][] solution;
	
	public Niveau(int[][] map,int x,int y,int[][] sol) {
		carte=new int[12][20];
		System.arraycopy(map, 0, carte, 0, 12); // Copie de l'originale car les modifications durant le cours du jeu (fleurs ramassées, etc) ne doivent pas affecter l'originale
		dbx=x;
		dby=y;
		solution=sol;
	}
}
