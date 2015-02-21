package com.game.colibri;


import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;

import android.util.SparseArray;

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
	public int[][] chemin;
	
	/**
	 * Consructeur de niveau de campagne
	 * 		@param niveau 
	 * 			nombre du niveau à charger
	 */
	public Niveau(InputStream file) {
		isRandom=false;
		init();
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
		init();
		this.geneNivRand(lon, var);
		replay();
	}
	
	/**
	 * Initialisation des structures de données.
	 */
	private void init() {
		carteOrigin= new int[12][20];
		carte=new int[12][];
		if(isRandom) chemin= new int[12][20];
		vaches = new LinkedList<int[][]>();
		chats = new LinkedList<int[][]>();
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
					solution = new int[nombre_deplacements][3];
					for (int j = 0; j < nombre_deplacements; j ++) {
						solution[j][2] = 0;
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
					ecri.print(solution[k][0]+","+solution[k][1]+","+solution[k][2]);
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
	 * Permet de définir un passage possiblement récurrent.
	 * 
	 * @author Louis
	 *
	 */
	private class Occurence {
		int r,mod,delta;
		
		/**
		 * Constructeur
		 * @param r time%mod lorsque le passage a lieu.
		 * @param mod la période
		 * @param delta
		 */
		public Occurence(int r, int mod, int delta) {
			if(mod==0)
				mod=Integer.MAX_VALUE;
			this.r=r%mod;
			this.mod=mod;
			this.delta=delta;
		}
		
		private int gcd(int a, int b) { return b==0 ? a : gcd(b, a%b); }
		
		private int distance(int r1, int r2, int m) {
			return Math.min(((r1-r2)%m+m)%m , ((r2-r1)%m+m)%m);
		}
		
		public int difference(int time) {
			return distance(time,r,mod);
		}
		
		public boolean isColidingWith(Occurence v) {
			if(mod==Integer.MAX_VALUE && v.mod==Integer.MAX_VALUE)
				return Math.abs(r-v.r)<=delta+v.delta;
			else if(mod==Integer.MAX_VALUE)
				return distance(v.r,r,v.mod)<=delta+v.delta;
			else if(v.mod==Integer.MAX_VALUE)
				return distance(v.r,r,mod)<=delta+v.delta;
			else {
				int pgcd = gcd(mod,v.mod);
				return distance(v.r,r,pgcd) <= delta+v.delta;
			}
		}
	}
	
	/**
	 * Structure contenant les passages d'objets mouvants sur les cases de la carte.
	 * 
	 * @author Louis
	 *
	 */
	public class Passages {
		public int col;
		private SparseArray<LinkedList<Occurence>> passages = new SparseArray<LinkedList<Occurence>>();
		
		public Passages(int colonnes) {
			col = colonnes;
			passages = new SparseArray<LinkedList<Occurence>>();
		}
		
		/**
		 * Ajoute un passage sur la case (r,c) passant à l'instant time et de période period,
		 * delta est le nombre de frames pris par l'objet pour sortir de la case.
		 * La fonction retourne également l'occurence ajoutée.
		 * @param r ligne
		 * @param c colonne
		 * @param time temps (nb de frames depuis le début)
		 * @param period période du mouvement en nb de frames (==Integer.MAX_VALUE si passage unique)
		 * @param delta nombre de frames pris par l'objet pour sortir de la case
		 * @return oc l'occurence ajoutée.
		 */
		public Occurence addOccurence(int r, int c, int time, int period, int delta) {
			LinkedList <Occurence> occurences = passages.get(r*col+c);
			if(occurences==null) {
				occurences = new LinkedList<Occurence>();
				passages.put(r*col+c, occurences);
			}
			Occurence oc = new Occurence(time,period,delta);
			occurences.addLast(oc);
			return oc;
		}
		
		/**
		 * Retourne la liste des passages (occurences) sur la case (r,c)
		 * @param r
		 * @param c
		 * @return la liste d'occurences
		 */
		public LinkedList <Occurence> getOccurences(int r, int c) {
			return passages.get(r*col+c);
		}
		
		public boolean isColidingWith(int r, int c, Occurence v) {
			LinkedList <Occurence> occurences = getOccurences(r,c);
			if(occurences==null)
				return false;
			boolean ici=false;
			for(Occurence oc : occurences) {
				ici=oc.isColidingWith(v);
				if(ici)
					break;
			}
			return ici;
		}
		
		public Occurence returnColisionWith(int r, int c, Occurence v) {
			LinkedList <Occurence> occurences = getOccurences(r,c);
			if(occurences==null)
				return null;
			Occurence ici=null;
			for(Occurence oc : occurences) {
				if(oc.isColidingWith(v)) {
					ici = oc;
					break;
				}
			}
			return ici;
		}
		
		@Override
		public String toString() {
			String s="";
			for(int i=0; i<passages.size(); i++) {
				LinkedList<Occurence> occs = passages.valueAt(i);
				s+="("+(passages.keyAt(i)/20)+","+(passages.keyAt(i)%20)+") : ";
				for(Occurence o : occs) {
					s+=o.r+" "+o.mod+" "+o.delta+" | ";
				}
				s+="\n";
			}
			return s;
		}
	}
	
	
	public Passages passColibri;
	public Passages passVaches;
	//private Passages passChats;
	private int frame, frameMem;
	private long seed;
	private double step,v_max,acc;
	public int nVaches=5, nDyna=5, nChats=5, nArcs=5; // Paramètres de quantité
	public boolean pVaches=true, pDyna=true, pChats=true, pArcs=true; // Paramètres de présence
	
	private void fillPosAleat(Random ra, int dist, LinkedList<Integer> pos, int sig) { // Génération des possibilités.
		int tot = 0;
		int n;
		while(tot<=dist-3) {
			n=ra.nextInt(3)+1;
			tot+=n;
			pos.add(sig*n);
		}
		if(tot!=dist)
			pos.add(sig*(dist-tot));
	}
	
	private boolean valideCheminRVache(int r, int c, int rf, int cf, int frameMod, int period, Occurence colibri, int rc, int cc) {
		int s = Integer.signum(cf-c);
		Occurence vache = new Occurence(frameMod, period, 18); // Delta = 20-2 car la vitesse d'une vache est de 1/20 case/frame et que l'on peut passer même quand la vache est un peu sur la case.
		while(c>=0 && c<20 && c!=cf+s && carteOrigin[r][c]%2==0 && !passVaches.isColidingWith(r, c, vache) && !passColibri.isColidingWith(r, c, vache) && !(r==rc && c==cc && colibri.isColidingWith(vache))) {
			frameMod=(frameMod+20)%period;
			vache = new Occurence(frameMod, period, 18);
			c+=s;
		}
		return c==cf+s;
	}
	
	private boolean valideCheminCVache(int r, int c, int rf, int cf, int frameMod, int period, Occurence colibri, int rc, int cc) {
		int s = Integer.signum(rf-r);
		Occurence vache = new Occurence(frameMod, period, 18); // Delta = 20-2 car la vitesse d'une vache est de 1/20 case/frame et que l'on peut passer même quand la vache est un peu sur la case.
		while(r>=0 && r<12 && r!=rf+s && carteOrigin[r][c]%2==0 && !passVaches.isColidingWith(r, c, vache) && !passColibri.isColidingWith(r, c, vache) && !(r==rc && c==cc && colibri.isColidingWith(vache))) {
			frameMod=(frameMod+20)%period;
			vache = new Occurence(frameMod, period, 18);
			r+=s;
		}
		return r==rf+s;
	}
	
	private int[][] geneVache(int r, int c, int frameDeb, int[] lon, int rColi, int cColi) {
		Random ra = new Random();
		ra.setSeed(seed);
		int distLig = ra.nextInt(6)+1;
		int distCol = ra.nextInt(6)+1;
		LinkedList<Integer> posLig = new LinkedList<Integer>(); // Possibilités de déplacement sur les lignes, dont la somme fait 0. Ex : [3,1,2,-1,-1,-3,-1]
		LinkedList<Integer> posCol = new LinkedList<Integer>(); // Idem sur les colonnes.
		fillPosAleat(ra,distLig,posLig,1);
		fillPosAleat(ra,distLig,posLig,-1);
		fillPosAleat(ra,distCol,posCol,1);
		fillPosAleat(ra,distCol,posCol,-1);
		int pof;
		int cs=posCol.size(),ls=posLig.size();
		int dep;
		int n;
		lon[0] = (distLig+distCol)*40;
		int frameMod = frameDeb%lon[0];
		boolean valide;
		int[][] deplacements = new int[cs+ls][2];
		int[] depart = {-1,-1}; // Si le départ ne tombe pas sur un checkpoint, on spécifiera ses coordonnées.
		int shift=0;
		int cpt=0, boucles=0;
		Occurence colibri = new Occurence(frameDeb+10, 0, 20); // Création d'une occurence représentant la position du colibri une fois arrêté contre la vache, pour éviter qu'il soit poussé par la vache.
		while((cs!=0 || ls!=0) && boucles<=Math.max(cs+ls , 4)) {
			if(boucles==0) {
				deplacements[cpt][0]=r; deplacements[cpt][1]=c;}
			pof=ra.nextInt(2);
			if(pof==0 && cs!=0 || ls==0) { // Déplacement selon une colonne
				n=ra.nextInt(cs);
				dep=posCol.get(n);
				valide=valideCheminCVache(r, c, r+dep, c, frameMod, lon[0], colibri, rColi, cColi);
				if(valide) {
					r+=dep;
					frameMod+=20*Math.abs(dep);
					cpt++;
					if(frameMod>lon[0]) {
						frameMod=frameMod%lon[0];
						depart[0]=r-Integer.signum(dep)*frameMod/20;
						depart[1]=c;
						shift=cpt;
					} else if(frameMod==lon[0]) {
						frameMod=0;
						shift=cpt;
					}
					posCol.remove(n);
					cs--;
					boucles=0;
				} else
					boucles++;
			} else { // Déplacement selon une ligne
				n=ra.nextInt(ls);
				dep=posLig.get(n);
				valide=valideCheminRVache(r, c, r, c+dep, frameMod, lon[0], colibri, rColi, cColi);
				if(valide) {
					c+=dep;
					frameMod+=20*Math.abs(dep);
					cpt++;
					if(frameMod>lon[0]) {
						frameMod=frameMod%lon[0];
						depart[0]=r;
						depart[1]=c-Integer.signum(dep)*frameMod/20;
						shift=cpt;
					} else if(frameMod==lon[0]) {
						frameMod=0;
						shift=cpt;
					}
					posLig.remove(n);
					ls--;
					boucles=0;
				} else
					boucles++;
			}
		}
		if(boucles==0) { // On a trouvé un chemin de vache !
			int[][] deplacShift;
			int decalage=0;
			if(depart[0]!=-1)
				decalage=1;
			deplacShift = new int[deplacements.length+decalage][];
			if(decalage==1)
				deplacShift[0]=depart;
			for(int i=0; i<deplacements.length; i++) {
				deplacShift[i+decalage]=deplacements[(i+shift)%deplacements.length];
			}
			return deplacShift;
		} else { // Il n'y a pas assez de place pour le chemin prévu.
			return null;
		}
	}

	/**
	 * @param carte la carte 
	 * @param rd la ligne de depart 
	 * @param cd la colonne de depart 
	 * @param rf ligne de fin 
	 * @param cf colonne de fin 
	 * @param s sens deplacement selon la ligne 
	 * @return un chemin
	 */
	public int valideCheminR(int rd, int cd, int rf,int cf, int s, int wait){
		Random r = new Random();
		r.setSeed(seed);
		double av = cd+0.5;
		int i = cd;
		double step2 = (wait==0) ? step : 0;
		frame+=wait;
		int frColib=frameMem;
		Occurence colibri = new Occurence(frColib, 0, frame-frameMem);
		while(i!=cf && carteOrigin[rd][i+s]%2==0 && !passVaches.isColidingWith(rd, i+s, colibri)){
			while((int) av==i) {
				step2=Math.min(step2+acc, v_max);
				av+=s*step2;
				frame++;
			}
			i = (int) av;
			chemin[rd][i]+=1;
			frColib=(frame+frameMem)/2;
			colibri=passColibri.addOccurence(rd,i-s,frColib,0,frame-frameMem);
			frameMem = frame;
			if(r.nextInt(4)==0){
				carteOrigin[rd][i]=2;
			}
		}
		if(i!=cd) {
			frame += (i!=cf && carteOrigin[rd][i+s]%2==0) ? 1 : 2;
			step=step2;
		} else
			frame-=wait;
		return i;
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
	public int valideCheminC(int rd, int cd, int rf, int cf, int s, int wait){
		Random r = new Random();
		r.setSeed(seed);
		double av = rd+0.5;
		int i = rd;
		double step2 = (wait==0) ? step : 0;
		frame+=wait;
		int frColib=frameMem;
		Occurence colibri = new Occurence(frColib, 0, frame-frameMem);
		while(i!=rf && carteOrigin[i+s][cd]%2==0 && !passVaches.isColidingWith(i+s, cd, colibri)){
			while((int) av==i) {
				step2=Math.min(step2+acc, v_max);
				av+=s*step2;
				frame++;
			}
			i = (int) av;
			chemin[i][cd]+=1;
			frColib=(frame+frameMem)/2;
			colibri=passColibri.addOccurence(i-s,cd,frColib,0,frame-frameMem);
			frameMem = frame;
			if(r.nextInt(4)==0){
				carteOrigin[i][cd]=2;
			}
		}
		if(i!=rd) {
			frame += (i!=rf && carteOrigin[i+s][cd]%2==0) ? 1 : 2;
			step=step2;
		} else
			frame-=wait;
		return i;
	}
	
	private boolean valideCheminCBool(int rd, int cd, int rf, int cf, int s, int[] wait){
		double av = rd+0.5;
		int i = rd;
		int frColib=frameMem;
		int frame2=frame+wait[0], frameMem2=frameMem;
		double step2 = (wait[0]==0) ? step : 0;
		Occurence colibri = new Occurence(frColib, 0, frame2-frameMem2);
		while(i!=rf && carteOrigin[i+s][cd]%2==0 && !passVaches.isColidingWith(i+s, cd, colibri)){
			while((int) av==i) {
				step2=Math.min(step2+acc, v_max);
				av+=s*step2;
				frame2++;
			}
			i = (int) av;
			frColib=(frame2+frameMem2)/2;
			colibri = new Occurence(frColib, 0, frame2-frameMem2);
			frameMem2 = frame2;
		}
		// S'assurer que l'on rentre suffisamment dans le milieu de la vache pour être sûr de ne pas la louper, sinon, l'éviter.
		Occurence occ = passVaches.returnColisionWith(i+s, cd, colibri);
		if(occ!=null && wait[0]<100) {
			if(occ.difference(frColib)>6) {
				if(wait[0]==0)
					wait[0]-=2;
				wait[0]=Math.max(1 , wait[0]+20-(frColib%20)); // Attention : une valeur interdite : 0 (si frColib%20=18) !
				return valideCheminCBool(rd,cd,rf,cf,s,wait);
			}
		}
		if(passVaches.isColidingWith(i, cd, new Occurence(frame2+6,0,4))) // Permet de détecter si la vache va pousser le colibri à l'arrêt.
			wait[0]=-1;
		return i==rf;
	}
	
	private boolean valideCheminRBool(int rd, int cd, int rf,int cf, int s, int[] wait){
		double av = cd+0.5;
		int i = cd;
		int frColib=frameMem;
		int frame2=frame+wait[0], frameMem2=frameMem;
		double step2 = (wait[0]==0) ? step : 0;
		Occurence colibri = new Occurence(frColib, 0, frame2-frameMem2);
		while(i!=cf && carteOrigin[rd][i+s]%2==0 && !passVaches.isColidingWith(rd, i+s, colibri)){
			while((int) av==i) {
				step2=Math.min(step2+acc, v_max);
				av+=s*step2;
				frame2++;
			}
			i = (int) av;
			frColib=(frame2+frameMem2)/2;
			colibri = new Occurence(frColib, 0, frame2-frameMem2);
			frameMem2 = frame2;
		}
		Occurence occ = passVaches.returnColisionWith(rd, i+s, colibri);
		if(occ!=null && wait[0]<100) {
			if(occ.difference(frColib)>6) {
				if(wait[0]==0)
					wait[0]-=2;
				wait[0]=Math.max(1 , wait[0]+20-(frColib%20));
				return valideCheminRBool(rd,cd,rf,cf,s,wait);
			}
		}
		if(passVaches.isColidingWith(rd, i, new Occurence(frame2+6,0,4))) // Permet de détecter si la vache va pousser le colibri à l'arrêt.
			wait[0]=-1;
		return i==cf;
	}
	
	private void addVache(int[][] itin, int lon) {
		int ca=0;
		int r=itin[0][0],c=itin[0][1];
		for(int[] m : itin) {
			while(r!=m[0] || c!=m[1]) {
				passVaches.addOccurence(r, c, ca*20, lon, 18);
				ca++;
				r+=Math.signum(m[0]-r);
				c+=Math.signum(m[1]-c);
			}
		}
		while(r!=itin[0][0] || c!=itin[0][1]) {
			passVaches.addOccurence(r, c, ca*20, lon, 18);
			ca++;
			r+=Math.signum(itin[0][0]-r);
			c+=Math.signum(itin[0][1]-c);
		}
		vaches.add(itin);
	}
	
	/**
	 * 
	 * @param n
	 * @param r
	 * @param c
	 * @param n_fleur
	 * @param mvt
	 */
	public boolean geneChemin(int n,int r,int c){
		int rd=r, cd=c;
		Random random = new Random();
		random.setSeed(seed);
		int direc, bord, ran, s;
		int rf, cf;
		int loop=0;
		int[] w = {0};
		int frameDepart;
		int[][] list_vache;
		for (int k=0; k<n && loop<17;k++) { // Pour garantir une proba voulue (pv) de ne pas interrompre un cas encore possible de proba p, il faut une limite de n>=ln(1-pv)/ln(1-p). On a 17 pour p=0.133 et pv=0.9.
			System.out.println("Fr="+frame+" Pos : "+r+","+c);
			direc=random.nextInt(2); // Choisit si le prochain deplacement se fera selon une ligne ou une colonne.
			w[0]=0; frameDepart=frame;
			if (direc==0) { // SELON LA COLONNE
				bord=random.nextInt(10); // Il y a une probabilité de 1/5 d'aller jusqu'à la cloture du niveau. Cela permet de ne pas trop confiner le chemin au milieu de la carte, et de donner davantage de possibilités de résolution au joueur...
				if (bord<2) {
					ran=bord*11;
					s=2*bord-1;
					valideCheminCBool(r,c,ran,c,s,w);
					if(w[0]!=-1) rf=valideCheminC(r,c,ran,c,s,w[0]);
					else rf=r;
				} else {
					ran=1+random.nextInt(10); // on tire une position sur la ligne (hors bords)
					s=Integer.signum(ran-r); // sens du déplacement selon la ligne s=1 ou -1
					if(ran==r || chemin[ran+s][c]>6 || chemin[ran+s][c]>1 && !pVaches || (ran+s==rd && c==cd)) // il faut en particulier vérifier que l'emplacement du menhir d'arrêt (aux coord (ran+s,c)) ne bloque pas le chemin précédemment généré ! Pour cela : si chemin[ran+s][c]==0 c'est bon; si ==1 on met une fleur magique, car elle deviendra menhir après le passage; si >=2 (i.e. le colibri est déjà passé plus de 2 fois sur cette case) ce n'est pas possible, il faut choisir un autre emplacement !
						rf=r; // Destination (ran,c) non valide
					else if(chemin[ran+s][c]>1 || passVaches.getOccurences(ran+s, c)!=null || random.nextInt(40-nVaches*3)==0) { // Ajout d'une vache !
						int tvoy=Math.abs(ran-r)*4/3+2;
						int frameDeb = ((frame+tvoy)/20+1)*20;
						w[0] = frameDeb-frame-tvoy;
						int wait=w[0];
						if(valideCheminCBool(r,c,ran,c,s,w)) { // On peut accéder à la destination, on va mettre un vache.
							int boucles=0;
							int[] lon={0};
							list_vache=null;
							while(list_vache==null && boucles<10) {
								list_vache = geneVache(ran+s, c, frameDeb+w[0]-wait, lon, ran, c);
								boucles++;
							}
							if(list_vache==null)
								rf=r;
							else {
								rf=valideCheminC(r,c,ran,c,s,w[0]);
								frame--; // Rectification, la détection d'une vache se fait en une frame ou deux de moins qu'un menhir..
								addVache(list_vache,lon[0]);
							}
						}
						else { // On s'arrête avant sur un obstacle.
							w[0]=0;
							if(valideCheminCBool(r,c,ran,c,s,w) || w[0]==-1)
								rf=r;
							else
								rf=valideCheminC(r,c,ran,c,s,w[0]);
						}
					}
					else {
						valideCheminCBool(r,c,ran,c,s,w);
						if(w[0]!=-1) rf=valideCheminC(r,c,ran,c,s,w[0]); // On obtient la destination prévue "ran" sauf si un menhir se trouvait sur le chemin entre (r,c) et (ran,c) : dans ce cas, on obtient la ligne avant le menhir qui nous bloque.
						else rf=r;
					}
				}
				if (rf==r) { // si finalement on a pas bougé, on refait un tour de boucle en plus (ou si une vache nous pousse).
					k--;
					loop++;
				} else { // sinon, on a donc un mouvement valide de prévu, la nouvelle position du colibri est donc (rf,c)
					solution[k][0]=0;
					solution[k][1]=s;
					solution[k][2]=(w[0]==0) ? 0 : frameDepart+w[0];
					r=rf;
					loop=0;
					if (r==ran && ran!=0 && ran!=11 && passVaches.getOccurences(ran+s, c)==null) {
						carteOrigin[ran+s][c]=1+2*chemin[ran+s][c]; // On ajoute le menhir d'arrêt, ou bien la fleur magique d'arrêt. (NB : menhirs codés par "1" dans la carte, fleurs par "2", fleurs magiques par "3")
					}
				}
			} else {// SELON LA LIGNE  (il s'agit de la même chose mais le déplacement se fait sur la ligne)				
				bord=random.nextInt(10);
				if (bord<2) {
					ran=bord*19;
					s=2*bord-1;
					valideCheminRBool(r,c,r,ran,s,w);
					if(w[0]!=-1) cf=valideCheminR(r,c,r,ran,s,w[0]);
					else cf=c;
				} else {
					ran=random.nextInt(18)+1;
					s=Integer.signum(ran-c);
					if(ran==c || chemin[r][ran+s]>6 || chemin[r][ran+s]>1 && !pVaches || (ran+s==cd && r==rd))
						cf=c;
					else if(chemin[r][ran+s]>1 || passVaches.getOccurences(r, ran+s)!=null || random.nextInt(40-nVaches*3)==0) {
						int tvoy=Math.abs(ran-c)*4/3+2;
						int frameDeb = ((frame+tvoy)/20+1)*20;
						w[0] = frameDeb-frame-tvoy;
						int wait=w[0];
						if(valideCheminRBool(r,c,r,ran,s,w)) {
							int boucles=0;
							int[] lon={0};
							list_vache=null;
							while(list_vache==null && boucles<10) {
								list_vache = geneVache(r, ran+s, frameDeb+w[0]-wait, lon, r, ran);
								boucles++;
							}
							if(list_vache==null)
								cf=c;
							else {
								cf=valideCheminR(r,c,r,ran,s,w[0]);
								frame--;
								addVache(list_vache,lon[0]);
							}
						}
						else {
							w[0]=0;
							if(valideCheminRBool(r,c,r,ran,s,w) || w[0]==-1)
								cf=c;
							else
								cf=valideCheminR(r,c,r,ran,s,w[0]);
						}
					}
					else {
						valideCheminRBool(r,c,r,ran,s,w);
						if(w[0]!=-1) cf=valideCheminR(r,c,r,ran,s,w[0]);
						else cf=c;
					}
				}
				if (cf==c) {
					k--;
					loop++;
				} else {
					solution[k][0]=s;
					solution[k][1]=0;
					solution[k][2]=(w[0]==0) ? 0 : frameDepart+w[0];
					c=cf;
					loop=0;
					if (c==ran && ran!=0 && ran!=19 && passVaches.getOccurences(r, ran+s)==null) {
						carteOrigin[r][ran+s]=1+2*chemin[r][ran+s];
					}
				}
			}
		}
		return loop==0;
	}
	
	/**
	 * Génère un niveau aléatoire ! La longueur de la solution du niveau sera : lon+rand[0:var].
	 *  @param lon longueur minimale de la solution générée
	 *  @param var intervalle de variation aléaoire de la longueur du chemin
	 */
	public void geneNivRand(int lon, int var){
		Random r = new Random();
		seed=r.nextLong();
		//Rseed=4983205564285282166L;
		//Rseed=9082031921687295366L;
		//Rseed=-223378056952982477L;
		//Rseed=-8533473861727707694L;
		//Rseed=-5460858976118910708L; // Vache sur vache et menhir
		//Rseed=-4700723420494060597L; // Vache en dehors
		//Rseed=-8083600409997685496L;
		//Rseed=6319504429268332568L;
		//Rseed=792677002865193748L;
		//Rseed=-3865935805870908818L; // Menhir rouge !
		//Rseed=6097550526607919138L; // Plusieurs arrêts sur vache
		//Rseed=-4404222512431318952L; // Poussette !
		//Rseed=144287376290610878L; // Menhir rouge !
		//Rseed=640222947886042564L;
		//Rseed=-2758439859207146033L;
		//Rseed=-1528515903479504062L;
		//Rseed=-3137553431549039113L;
		System.out.println("SEED : "+seed);
		r.setSeed(seed);
		int longueur = lon+r.nextInt(var);
		frame = 0; frameMem = 0;
		step = 0.;
		v_max = 0.75;
		acc = 0.1;
		solution = new int[longueur][3]; //Liste des mouvements à effectuer pour résoudre le niveau
		db_l = r.nextInt(12);
		db_c = r.nextInt(20); // on tire un emplacement départ
		chemin[db_l][db_c]=1;
		passColibri = new Passages(20);
		passVaches = new Passages(20);
		//passChats = new Passages(20);
		if(!geneChemin(longueur,db_l,db_c)) { // on génère la carte pour une solution en "longueur" coups !
			init();
			geneNivRand(lon,var);
		}
	}

}