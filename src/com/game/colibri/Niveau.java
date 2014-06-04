package com.game.colibri;


import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;

import android.util.Log;

/**
 * Classe contenant toutes les données d'UN niveau : carte, position initiale du colibri, 
 * déplacement des animaux mouvants (chats et vaches) et solution (optionnelle)
 */
public class Niveau {

	/*
	 * Définit s'il s'agit d'un niveau aléatoire ou non.
	 */
	public boolean isRandom;
	
	/*
	 * Carte contenant les informations statiques du niveau en
	 * question c'est-à-dire les menhirs, les fleurs ainsi
	 * que les fleurs magiques et les dynamites.
	 * Elle est modifiée pendant le jeu.
	 */
	public int[][] carte;
	
	/*
	 * Carte du niveau qui ne sera pas modifiée durant le jeu. Permet de rétablir la carte si on recommence le niveau.
	 */
	private int[][] carteOrigin;
	
	/*
	 * Position initiale du Colibri (la ligne de départ) au 
	 * lancement du niveau
	 */
	
	public int db_l;
	/*
	 * Position initiale du Colibri (la colonne de départ) au 
	 * lancement du niveau
	 */
	public int db_c;
	
	/*
	 * Optionnel pour chaque niveau, la solution afin de résoudre
	 * celui-ci si l'utilisateur est trop bloqué
	 */
	public int[][] solution=null;
	
	/*
	 * Liste des déplacements des différentes vaches. Pour chaque
	 * vache on a une liste de déplacements sous la forme d'une 
	 * matrice nombre_déplacements*2 c'est-à-dire les coordonnées
	 * dans la matrice des points par lesquels elle passe.
	 */
	public LinkedList<int[][]> vaches;
	
	/*
	 * Liste des déplacements des différents chats. Pour chaque
	 * chat on a une liste de déplacements sous la forme d'une 
	 * matrice nombre_déplacements*2 c'est-à-dire les coordonnées
	 * dans la matrice des points par lesquels il passe.
	 */
	public LinkedList<int[][]> chats;
	
	/* 
	* Pour la génération de niveaux aléatoires, cette matrice
	* contient le nombre de passages du Colibri sur chaque case
	*/
	private int[][] chemin;
	
	/**
	 * Consructeur de niveau de campagne
	 * 		@param niveau 
	 * 			nombre du niveau à charger
	 */
	public Niveau(InputStream file) {
		isRandom=false;
		carteOrigin= new int[12][20];
		carte=new int[12][];
		vaches = new LinkedList<int[][]>();
		chats = new LinkedList<int[][]>();
		lireNiveau(file);
		replay();
	}
	
	/**
	 * Constructeur d'un niveau aléatoire de paramètres donnés.
	 * @param lon la longueur minimale du niveau
	 * @param var la variation aléatoire supplémentaire de longueur
	 */
	public Niveau(int lon, int var) {
		isRandom=true;
		carteOrigin= new int[12][20];
		carte=new int[12][];
		chemin= new int[12][20];
		vaches = new LinkedList<int[][]>();
		chats = new LinkedList<int[][]>();
		this.geneNivRand(lon, var);
		replay();
	}
	
	/**
	 * Permet de recommencer le niveau : on rétablit la carte d'origine en copiant "carteOrigin"
	 */
	public void replay() {
		for(int i=0; i<carteOrigin.length; i++) {
			carte[i]=carteOrigin[i].clone();
		}
	}
	
	/**
	 * Permet de modifier les différents éléments du niveau gràce au
	 * fichier texte dans lequel il y a les différentes informations
	 * (position de départ du colibri, carte, déplacements des vaches
	 * et des chats, solution du niveau)
	 * 
	 * @param niveau
	 * 			numéro du niveau qui doit être chargé selon le ficher .txt
	 */
	public void lireNiveau(InputStream ips){
		String [][]matrice =null;
		try {
			//InputStream ips=new FileInputStream("niveaux/niveau"+niveau+".txt");
							// ouverture du fichier texte voulu
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			String text="";
			int nbLigne=0;
			// boucle permettant de compter le nombre de lignes dans le fichier texte
			// et remplissant une variable "text" de type String avec les données dans
			// le fichier texte
			while ((ligne=br.readLine())!=null){
				text=text+ ligne+ "\n";
				nbLigne++;
			}
			br.close();
			// permet de créer une liste avec les différentes lignes du fichier texte
			String[] list=text.split("\n");
			
			/* 
			* Matrice qui contiendra les différents éléments de la matrice du fichier texte 
			* mais ceux-ci seront des String */
			matrice=new String[12][];
			for(int i=0;i<12;i++){
				matrice[i]=list[i].split(",");
			}
			Log.i("matrice :",matrice.toString());
			/*
			 *  Changement des éléments String en éléments int pour obtenir la 
			 *  matrice désirée
			 */
			for(int i=0;i<12;i++){
				for(int j=0;j<20;j++){
					carteOrigin[i][j]=Integer.valueOf(matrice[i][j]);
				}
			}
			// La ligne 13 possède les informations de départ du colibri
			String[] depart = list[12].split(",");
			db_l = Integer.valueOf(depart[0]);
			db_c = Integer.valueOf(depart[1]);
			/* 
			 * Les lignes suivantes posèdent des informations quant aux 
			 * animaux mouvants ainsi que la solution. On ne connaît pas
			 * leur nombre au préalable et la solution est optionnelle, d'où
			 * l'importance d'avoir compté le nombre de lignes auparavant
			 */
			for (int i = 13; i < nbLigne ;i++) {
				/*
				 *  Si la ligne décrit les déplacements d'une vache et est
				 *  donc sous la forme "vache=" suivi d'une succession de 
				 *  nombres et de virgules
				 */
				if (list[i].startsWith("vache", 0)) {
					String line = list[i].substring(6, list[i].length());
					String[] elements = line.split(",");
					//nombre de points de déplacements que possède la vache
					int nombre_deplacements = (elements.length)/2;
					int[][] list_vache = new int[nombre_deplacements][2];
					for (int j = 0; j < nombre_deplacements; j ++) {
						list_vache[j][0] = Integer.valueOf(elements[2*j]);
						list_vache[j][1] = Integer.valueOf(elements[2*j+1]);
					}
					vaches.add(list_vache);
				}
				/*
				 *  Si la ligne décrit les déplacements d'un chat et est
				 *  donc sous la forme "chat=" suivi d'une succession de 
				 *  nombres et de virgules
				 */
				if (list[i].startsWith("chat", 0)) {
					String line = list[i].substring(5, list[i].length());
					String[] elements = line.split(",");
					//nombre de points de déplacements que possède le chat
					int nombre_deplacements = (elements.length)/2;
					int[][] list_chat = new int[nombre_deplacements][2];
					for (int j = 0; j < nombre_deplacements; j ++) {
						list_chat[j][0] = Integer.valueOf(elements[2*j]);
						list_chat[j][1] = Integer.valueOf(elements[2*j+1]);
					}
					chats.add(list_chat);
				}
				/*
				 *  Si la ligne décrit la solution et est donc sous la forme 
				 *  "solution=" suivi d'une succession de nombres et de virgules
				 *  ATTENTION : la solution dans les fochiers est décrite selon (lig,col) alors que dans notre jeu c'est (x,y) !!
				 */
				if (list[i].startsWith("solution",0)) {
					String line = list[i].substring(9, list[i].length());
					String[] elements = line.split(",");
					int nombre_deplacements = (elements.length)/2;
					solution = new int[nombre_deplacements][2];
					for (int j = 0; j < nombre_deplacements; j ++) {
						solution[j][1] = Integer.valueOf(elements[2*j]);
						solution[j][0] = Integer.valueOf(elements[2*j+1]);
					}
				}
			}
		}
		//si une exception est levée au cours de l'exécution
		catch (Exception e) {
			System.out.println("Mauvaise syntaxe du niveau : " + e.toString());
		} 
	}

	/**
	 * Écrit la matrice du niveau dans un fichier 
	 * 		@param filename le nom du fichier dans lequel on va ecrire la matrice 
	 * 		@param matrice  la matrice à écrir 
	 * 		@throws IOException
	 */
	public void ecrirNiveau(String filename) throws IOException {
		try {
			PrintWriter ecri = new PrintWriter(new FileWriter(filename));
			for(int i=0;i<12;i++){
				for(int j=0;j<20;j++){
					ecri.print(carteOrigin[i][j]);
					if(j<19){
						ecri.print(",");
					}
				}
				ecri.print("\n");
			}
			ecri.print(db_l + "," + db_c);
			ecri.print("\n");
			for (int i=0; i< vaches.size(); i++) {
				ecri.print("vache=");
				int[][] vachette = vaches.get(i);
				for (int k = 0; k < vachette.length-1; k++) {
					ecri.print(vachette[k][0]+","+vachette[k][1]+",");
				}
				ecri.print(vachette[(vachette.length)-1][0]+","+vachette[(vachette.length)-1][1]);
			}
			for (int i=0; i< chats.size(); i++) {
				ecri.print("chat=");
				int[][] chat = chats.get(i);
				for (int k = 0; k < chat.length-1; k++) {
					ecri.print(chat[k][0]+","+chat[k][1]+",");
				}
				ecri.print(chat[(chat.length)-1][0]+","+chat[(chat.length)-1][1]);
			}
			if (solution != null) {
				ecri.print("solution=");
				for (int k = 0; k < solution.length-1; k++) {
					ecri.print(solution[k][0]+","+solution[k][1]+",");
				}
				ecri.print(solution[(solution.length)-1][0]+","+solution[(solution.length)-1][1]);
			}
			ecri.flush();
			ecri.close();
			System.out.println("Fichier créé");
		} catch (IOException ioe) {
			System.err.println("Erreur levée de type IOException au niveau de la méthode ecrirNiveau() : ");
			ioe.printStackTrace();
		}
	}
	
	
	public int[][] getCarte() {
		return carteOrigin;
	}


	public int getDb_l() {
		return db_l;
	}


	public int getDb_c() {
		return db_c;
	}


	public int[][] getSolution() {
		return solution;
	}


	public LinkedList<int[][]> getVaches() {
		return vaches;
	}


	public LinkedList<int[][]> getChats() {
		return chats;
	}


	public int[][] getChemin() {
		return chemin;
	}

	// NIVEAU ALEATOIRE 

	/**
	 * @param carte la carte 
	 * @param rd  la ligne de depart 
	 * @param cd la colonne de depart 
	 * @param rf ligne de fin 
	 * @param cf colonne de fin 
	 * @param s sens deplacement selon la ligne 
	 * @return un  chemin
	 */
	public int valideCheminR(int rd, int cd, int rf,int cf, int s){
		Random r = new Random();
		int i =cd+s; 
		while(i!=cf+s && carteOrigin[rd][i]%2==0){
			chemin[rd][i]+=1;
			if( r.nextInt(4)==0){
				carteOrigin[rd][i]=2;
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
	public  int valideCheminC(int rd, int cd, int rf,int cf, int s){
		Random r = new Random();
		int i =rd+s; 
		while(i!=rf+s && carteOrigin[i][cd]%2==0){
			chemin[i][cd]+=1;
			if( r.nextInt(5)==0){
				carteOrigin[i][cd]=2;
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
	public void geneChemin(int n,int r,int c){
		int rd=r;
		int cd=c;
		Random random = new Random();
		int direc;
		int bord;
		int ran;
		int s;
		int rf;
		int cf;
		for (int k=0; k<n;k++) {  
			direc=random.nextInt(2); // Choisit si le prochain deplacement se fera selon une ligne ou une colonne.
			if (direc==0) { // SELON LA COLONNE
				bord=random.nextInt(10); // Il y a une probabilité de 1/5 d'aller jusqu'à la cloture du niveau. Cela permet de ne pas trop confiner le chemin au milieu de la carte, et de donner davantage de possibilités de résolution au joueur...
				if (bord<2) {
					ran=bord*11;
					s=2*bord-1;
				} else {
						ran=1+random.nextInt(10); // on tire une position sur la ligne (hors bords)
						s=Integer.signum(ran-r); // sens du déplacement selon la ligne s=1 ou -1
					while (ran==r ||chemin[ran+s][c]>1 || (ran+s==rd && c==cd)) {// il faut en particulier vérifier que l'emplacement du menhir d'arrêt (aux coord (ran+s,c)) ne bloque pas le chemin précédemment généré ! Pour cela : si chemin[ran+s][c]==0 c'est bon; si ==1 on met une fleur magique, car elle deviendra menhir après le passage; si >=2 (i.e. le colibri est déjà passé plus de 2 fois sur cette case) ce n'est pas possible, il faut choisir un autre emplacement !
						ran=1+random.nextInt(10);
						s=Integer.signum(ran-r);
					}
				}
				rf=valideCheminC(r,c,ran,c,s); // On obtient la destination prévue "ran" sauf si un menhir se trouvait sur le chemin entre (r,c) et (ran,c) : dans ce cas, on obtient la ligne avant le menhir qui nous bloque.
				if (rf==r) { // si finalement on a pas bougé, on refait un tour de boucle en plus.
					k--;
				} else { // sinon, on a donc un mouvement valide de prévu, la nouvelle position du colibri est donc (rf,c)
					solution[k][0]=0;
					solution[k][1]=s;
					r=rf;
				}
				if (r==ran && ran!=0 && ran!=11) {
					carteOrigin[ran+s][c]=1+2*chemin[ran+s][c]; // On ajoute le menhir d'arrêt, ou bien la fleur magique d'arrêt. (NB : menhirs codés par "1" dans la carte, fleurs par "2", fleurs magiques par "3")		
				}
			} else {// SELON LA LIGNE  (il s'agit de la même chose mais le déplacement se fait sur la ligne)
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
				cf=valideCheminR(r,c,r,ran,s);
				if (cf==c) {
						k--;
				} else {
					solution[k][0]=s;
					solution[k][1]=0;
					c=cf;
				}
				if (c==ran && ran!=0 && ran!=19){
					carteOrigin[r][ran+s]=1+2*chemin[r][ran+s];
				}
			}
		}
	}
	
	/**
	 * Génère un niveau aléatoire ! La longueur de la solution du niveau sera : lon+rand[0:var].
	 *  @param lon longueur minimale de la solution générée
	 *  @param var intervalle de variation aléaoire de la longueur du chemin
	 */
	public void geneNivRand(int lon, int var){
		Random r = new Random();
		//initMatrice(carte); //Carte vide qui sera remplie par "geneChemin"
		//initMatrice(this.chemin);// Contient le nombre de passage du Colibri sur chaque case
		int longueur = lon+r.nextInt(var);
		solution = new int[longueur][2]; //Liste des mouvements à effectuer pour résoudre le niveau
		db_l = r.nextInt(12);
		db_c = r.nextInt(20); // on tire un emplacement départ
		chemin[db_l][db_c]=1;
		geneChemin(longueur,db_l,db_c); // on génère la carte pour une solution en 10 à 40 coups !
		//carte_cour=[carte,rd,cd,mvt] // "carte_cour" est la carte qui sera chargée et jouée
	}

}


