package com.game.colibri;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;

/**
 * Classe contenant toutes les données d'UN niveau : carte, position initiale du colibri, solution
 * TODO : coordonnées des vaches et leur parcours
 */
public class Niveau {



	public int[][] carte;
	public int db_l,db_c;
	public int[][] solution;
	private int[][] chemin;
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
		chemin=new int[12][20];
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
	public void initMatrice(int [][] matrice){
		for(int i=0;i<12;i++){
			for(int j=0;j<20;j++){
				matrice[i][j]=0;

			}
		}
	}


	/**
	 * @param carte la carte 
	 * @param rd  la ligne de depart 
	 * @param cd la colonne de depart 
	 * @param rf ligne de fin 
	 * @param cf colonne de fin 
	 * @param s sens deplacement selon la ligne 
	 * @return un  chemin
	 */
	public int valideCheminR(int rd, int cd, int rf,int cf, int s,int n_fleur ){
		Random r = new Random();
		int i =cd+s; 
		while(i!=cf+s && this.carte[rd][i]%2==0){
			chemin[rd][i]+=1;
			if( r.nextInt(4)==0){
				carte[rd][i]=2;
				n_fleur+=1;
			}
			i+=s;	
		}
		return i-s;
	}

	/**
	 * @param carte la carte 
	 * @param rd  la ligne de depart 
	 * @param cd la colonne de depart 
	 * @param rf ligne de fin 
	 * @param cf colonne de fin 
	 * @param s sens deplacement selon la ligne 
	 * @return un  chemin
	 */
	public  int valideCheminC(int rd, int cd, int rf,int cf, int s,int n_fleur ){
		Random r = new Random();
		int i =rd+s; 
		while(i!=rf+s && this.carte[i][cd]%2==0){
			chemin[i][cd]+=1;
			if( r.nextInt(5)==0){
				carte[i][cd]=2;
				n_fleur+=1;
			}
			i+=s;	
		}
		return i-s;
	}

	/**
	 * 
	 * @param n
	 * @param r
	 * @param c
	 * @param n_fleur
	 * @param mvt
	 */
	public void geneChemin(int n,int r,int c, int n_fleur, LinkedList<int[]> mvt){
		int rd=r;
		int cd=c;
		Random random = new Random();
		int direc;
		int bord;
		int ran;
		int s;
		int rf;
		int cf;
		for (int k =1; k<n+1;k++) {  
			direc=random.nextInt(2); // Choisit si le prochain deplacement se fera selon une ligne ou une colonne.
			if (direc==0) { // SELON LA COLONNE
				bord=random.nextInt(10); // Il y a une probabilit� de 1/5 d'aller jusqu'� la cloture du niveau. Cela permet de ne pas trop confiner le chemin au milieu de la carte, et de donner davantage de possibilit�s de r�solution au joueur...
				if (bord<2) {
					ran=bord*11;
					s=2*bord-1;
				} else {
						ran=1+random.nextInt(10); // on tire une position sur la ligne (hors bords)
						s=Integer.signum(ran-r); // sens du d�placement selon la ligne s=1 ou -1
					while (ran==r ||chemin[ran+s][c]>1 || (ran+s==rd && c==cd)) {// il faut en particulier v�rifier que l'emplacement du menhir d'arr�t (aux coord (ran+s,c)) ne bloque pas le chemin pr�c�demment g�n�r� ! Pour cela : si chemin[ran+s][c]==0 c'est bon; si ==1 on met une fleur magique, car elle deviendra menhir apr�s le passage; si >=2 (i.e. le colibri est d�j� pass� plus de 2 fois sur cette case) ce n'est pas possible, il faut choisir un autre emplacement !
						ran=1+random.nextInt(10);
						s=Integer.signum(ran-r);
					}
				}
				rf=valideCheminC(r,c,ran,c,s, n_fleur); // On obtient la destination pr�vue "ran" sauf si un menhir se trouvait sur le chemin entre (r,c) et (ran,c) : dans ce cas, on obtient la ligne avant le menhir qui nous bloque.
				if (rf==r) { // si finalement on a pas boug�, on refait un tour de boucle en plus.
					n+=1;
				} else { // sinon, on a donc un mouvement valide de pr�vu, la nouvelle position du colibri est donc (rf,c)
					mvt.add(new int[]{s,0}); // on ajoute le mouvement dans la liste solution du niveau g�n�r�.
					r=rf;
				}
				if (r==ran && ran!=0 && ran!=11) {
					carte[ran+s][c]=1+2*chemin[ran+s][c]; // On ajoute le menhir d'arr�t, ou bien la fleur magique d'arr�t. (NB : menhirs cod�s par "1" dans la carte, fleurs par "2", fleurs magiques par "3")
					n_fleur+=chemin[ran+s][c]; // On ajoute 1 au compteur de fleur si on a pos� une fleur magique d'arr�t.
			
				}
			} else {// SELON LA LIGNE  (il s'agit de la m�me chose mais le d�placement se fait sur la ligne)
				bord=random.nextInt(10);
				if (bord<2) {
					ran=bord*19;
					s=2*bord-1;
				} else {
					ran=random.nextInt(18)+1;
					s=Integer.signum(ran-c);
					while (ran==c || chemin[r][ran+s]>1 || (ran+s==cd && r==rd)) {
						ran=random.nextInt(18)+1;
						s=Integer.signum(ran-c);
					}
				}
				cf=valideCheminR(r,c,r,ran,s,n_fleur);
				if (cf==c) {
						n+=1;
				} else {
					mvt.add(new int[]{0,s});
					c=cf;
				}
				if (c==ran && ran!=0 && ran!=19){
					carte[r][ran+s]=1+2*chemin[r][ran+s];
					n_fleur+=chemin[r][ran+s];
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public void geneNivRand(){
		Random r = new Random();
		initMatrice(this.carte); //Carte vide qui sera remplie par "geneChemin"
		initMatrice(this.chemin);// Contient le nombre de passage du Colibri sur chaque case
		int n_fleur=0 ;// Compteur de fleurs pos�es
		LinkedList<int[]> mvt= new LinkedList<int[]>(); //Liste des mouvements � effectuer pour r�soudre le niveau
		int rd = r.nextInt(12);
		int cd = r.nextInt(20); // on tire un emplacement d�part
		chemin[rd][cd]=1;
		geneChemin(10+r.nextInt(30),rd,cd, n_fleur, mvt); // on g�n�re la carte pour une solution en 10 � 40 coups !
		//carte_cour=[carte,rd,cd,mvt] // "carte_cour" est la carte qui sera charg�e et jou�e
	}

	/**
	 * @param mode
	 */
	public void niveaux_random(int mode) {
			geneNivRand(); //on g�n�re une carte et sa solution
			mode=1; //d�finit le mode al�atoire
			//MoteurJeu.start();  //on lance le jeu !

	}
}


