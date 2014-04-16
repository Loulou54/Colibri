package com.game.colibri;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Classe contenant toutes les données d'UN niveau : carte, position initiale du colibri, solution
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
	/**
	 * Renvoie la matrice d'un niveau lu dans un fichier 
	 * 		@param filename le  nom du ficher .txt contenant le niveau 
	 * 		@return la matrice du niveau 
	 */
	public static int[][] lireNiveau(String filename){
		String [][]matrice =null;
		int [][] result=null;
		try {
			   InputStream ips=new FileInputStream(filename);
			   InputStreamReader ipsr=new InputStreamReader(ips);
			   BufferedReader br=new BufferedReader(ipsr);
			   String ligne;
			   String text="";
			   int nbLigne=0;
			   while ((ligne=br.readLine())!=null){
				   	text=text+ ligne+ "\n";
			   		nbLigne++;
			   }
			   br.close();
			   String []list=text.split("\n");
			   int nbCol=list[0].split(",").length;
			   matrice=new String[nbLigne][nbCol];
			   result=new int[nbLigne][nbCol];
			   for(int i=0;i<nbLigne;i++){
				   matrice[i]=list[i].split(",");
			   }
			   for(int i=0;i<nbLigne;i++){
				   for(int j=0;j<nbCol;j++){
					  result[i][j]=new Integer(matrice[i][j]);
				   }
			   }
			  }
			  catch (Exception e) {
			   System.out.println(e.toString());
			  } 
		return result;
	}
	
}
