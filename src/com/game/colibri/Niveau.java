package com.game.colibri;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
	 * 		@param map la carte des éléments fixes : matrice 12*20
	 * 		@param db_l ligne départ colibri
	 * 		@param db_c colonne départ colibri
	 * 		@param itineraire itinéraire pour vache ou chat (null pour colibri)
	 *		@param sol la solution (optionnel)
	 */
	public Niveau(int[][] map, int db_l, int db_c, int[][] sol) {
		carte=new int[12][20];
		System.arraycopy(map, 0, carte, 0, 12); // Copie de l'originale car les modifications durant le cours du jeu (fleurs ramassées, etc) ne doivent pas affecter l'originale
		this.db_l=db_l;
		this.db_c=db_c;
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
	
	/**
	 * Écrit la matrice de niveau dans un fichier 
	 * 		@param filename le nom du fichier dans lequel on va ecrire la matrice 
	 * 		@param matrice  la matrice à écrir 
	 * 		@throws IOException
	 */
	public static void ecrirNiveau(String filename , int[][] matrice ) throws IOException{
		try {
			PrintWriter ecri = new PrintWriter(new FileWriter(filename));
			 for(int i=0;i<12;i++){
				   for(int j=0;j<20;j++){
					    ecri.print(matrice[i][j]);
					    if(j<19){
					    ecri.print(",");
					    }
				   }
				    ecri.print("\n");
					ecri.flush();
			   }
			 ecri.close();
			 
			 System.out.println("Fichier créé");
		} catch (IOException ioe) {
			System.err
					.println("Erreur levée de type IOException au niveau de la méthode "
							+ "ecrirNiveau(...) : ");
			ioe.printStackTrace();
		}
	}
	
}
