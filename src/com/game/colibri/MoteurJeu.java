package com.game.colibri;



import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;


/**
 * Classe gérant les déplacements des éléments (colibri, vaches, ...) selon les régles
 * et le rafraîchissement de l'écran.
 */
public class MoteurJeu {
	
	
	
	private Carte carte;
	private Niveau niv;
	private Jeu jeu;
	public boolean isRunning=false;
	private LinkedList <int[]> buf; // la file d'attente des touches
	private static final int PERIODE=1000/25; // pour 25 frames par secondes
	public static int menhir=1;
	public static int fleur=2;
	public static int fleurm=3;
	public static char vide=0;
	public static final int UP=1,RIGHT=2,LEFT=3,DOWN=4;
	
	private static final int LIG=12, COL=20; // Dimensions de la grille
	
	/**
	 * Laura et Mariam :
	 *   Voici la classe dans laquelle vous devez gérer les déplacements et leurs commandes (cf : onTouch, lisez la doc du MotionEvent si vous voulez gérer les "glisser" plus tard.)
	 * Idéalement, pensez déjà au système de buffer pour les commandes ! Laura tu vois de quoi je parle. :)
	 * Il s'agit d'enregistrer chaque commande (monter/descendre/droite/gauche) dans une file d'attente
	 * dans laquelle vous puisez dès que vous pouvez effectuer une nouvelle action.
	 * 
	 * La "carte" contient tout le niveau et gère son affichage.
	 * - carte.colibri est l'objet graphique "Animal" représentant le colibri.
	 * Son animation est gèrée dans la classe Animal. S'y référer pour les commandes de position.
	 * - carte.vaches sera la liste des vaches de type "Animal". On ne s'y intéresse pas tout de suite ! ;)
	 * - carte.loadNiveau permet de charger un nouveau niveau : vous ne devez pas l'utiliser ici.
	 * - carte.invalidate() : permet de redessiner la carte. Il ne faut l'appeler que lorsqu'un objet statique
	 * a bougé ou a disparu. Typiquement lorsque l'on mange une fleur ! (miam) Le déplacement des animaux ne
	 * nécessite pas de redessiner la carte. (car ce sont des View à eux seuls)
	 * 
	 * Le Jeu "jeu" permet de faire référence à l'activité appelante. C'est à elle notamment qu'il
	 * faudra signaler lorsque le joueur a fini le niveau par un appel : jeu.gagne() pour pouvoir passer au suivant !
	 * 
	 * Bon courage ! Vous pouvez bien sûr modifier des trucs dans les autres classes mais presque tout
	 * votre travail devrait se faire là normalement ! ;)
	 */
	
	/**
	 * Handler de rafraîchissement
	 * Il appelle la méthode void move() qui elle même appelle moveHandler.sleep(PERIODE) pour boucler.
	 * Utiliser moveHandler.sleep(PERIODE) pour appeler le handler après PERIODE ms.
	 * Utiliser moveHandler.removeMessages(0) pour arrêter le cycle.
	 */
	
	public RefreshHandler moveHandler = new RefreshHandler();
	
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			MoteurJeu.this.move();
		}
		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};
	
	/**
	 * Constructeur 
	 *		@param activ le jeu actif
	 * 		@param c   la carte 
	 */
	public MoteurJeu(Jeu activ, Carte c) {
		carte = c;
		jeu=activ;
		buf = new LinkedList<int[]>();
	}
	
	/**
	 *  Initialise les variables du moteur de jeu (buffer, niv, ...) : appelé après chaque appel de carte.loadNiveau
	 */
	public void init() {
		niv=carte.niv; // pour avoir une référence locale vers le niveau en cours et un nom moins long
		buf.clear();
	}
	
	/**
	 *  Commence le jeu 
	 */
	public void start() {
		carte.colibri.start();
		int len=carte.vaches.size();
		for(int i=0; i<len; i++) {
			carte.vaches.get(i).start();
		}
		isRunning=true;
		moveHandler.sleep(PERIODE);
	}
	
	/**
	 * Met le jeu sur pause
	 */
	public void pause() {
		isRunning=false;
		moveHandler.removeMessages(0);
		carte.colibri.stop();
		int len=carte.vaches.size();
		for(int i=0; i<len; i++) {
			carte.vaches.get(i).stop();
		}
	}

	/**
	 * Méthode appelée périodiquement par le handler moveHandler lorsque le jeu est en marche.
	 * C'est ici que s'effectue les déplacements des animaux.
	 */
	private void move() {
		if (carte.colibri.mx==0 & carte.colibri.my==0) {
			if (buf.size()>0)carte.colibri.setDirection(buf.poll());
			else carte.colibri.step=0; // La vitesse est mise à 0. Dans le premier cas, la vitesse est conservée.
		}else {
				int []dir= carte.colibri.getDirection();
				int ml=dir[1] , mc=dir[0];
				int l=carte.colibri.getRow(); // ligne du colibri
				int c=carte.colibri.getCol(); //  colone du colibri
				ramasser(l,c); // On ramasse l'item potentiel
				// On détecte si l'on arrive contre un obstacle
				if(l+ml<0 || l+ml>=LIG || c+mc<0 || c+mc>=COL || niv.carte[l+ml][c+mc]==menhir){
					carte.colibri.mx=0;
					carte.colibri.my=0;
					carte.colibri.setPos(c*carte.cw, l*carte.ch);
				}
			carte.colibri.deplacer();
		}
		int len=carte.vaches.size();
		for(int i=0; i<len; i++) {
			carte.vaches.get(i).deplacer();
			collisionVache(carte.vaches.get(i));
		}
		if(isRunning) moveHandler.sleep(PERIODE);
	}
	
	/**
	 * Détecte s'il y a colision entre le colibri et une vache, et le cas échéant effectue les opérations nécessaires.
	 * @param va la vache dont il faut tester la position par rapport au colibri
	 */
	private void collisionVache(Animal va) {
		int[] c_co = carte.colibri.getPos();
		int cx=c_co[0],cy=c_co[1];
		int[] c_va = va.getPos();
		int vx=c_va[0],vy=c_va[1];
		if(Math.abs(vx-cx)<carte.cw && Math.abs(vy-cy)<carte.ch) { // teste si colision
			// Choisit de quel côté de la vache il faut replacer le colibri
			int l=carte.colibri.getRow(), c=carte.colibri.getCol();
			ramasser(l,c); // On ramasse l'item potentiel
			if(carte.cw-Math.abs(vx-cx) < carte.ch-Math.abs(vy-cy)) { // sur l'horizontale
				if(cx<vx) {
					if(c-1<0 || niv.carte[l][c-1]==menhir) cx=Math.max(vx-carte.cw,c*carte.cw); // Détecte si le colibri est bloqué par un menhir ou un bord.
					else cx=vx-carte.cw;
					carte.colibri.mx=Math.min(carte.colibri.mx, 0); // arrête le mouvement du colibri s'il est vers la vache
				}
				else {
					if(c+1>=COL || niv.carte[l][c+1]==menhir) cx=Math.min(vx+carte.cw,c*carte.cw);
					else cx=vx+carte.cw;
					carte.colibri.mx=Math.max(carte.colibri.mx, 0);
				}
				carte.colibri.setPos(cx , cy);
				if(Math.abs(vx-cx)<carte.cw/2) jeu.mort();
			} else { // sur la verticale
				if(cy<vy) {
					if(l-1<0 || niv.carte[l-1][c]==menhir) cy=Math.max(vy-carte.ch,l*carte.ch);
					else cy=vy-carte.ch;
					carte.colibri.my=Math.min(carte.colibri.my, 0);
				}
				else {
					if(l+1>=LIG || niv.carte[l+1][c]==menhir) cy=Math.min(vy+carte.ch,l*carte.ch);
					else cy=vy+carte.ch;
					carte.colibri.my=Math.max(carte.colibri.my, 0);
				}
				carte.colibri.setPos(cx , cy);
				if(Math.abs(vy-cy)<carte.ch/2) jeu.mort();
			}
		}
	}
	
	/**
	 * Vérifie si un item peut être ramassé sur la case (l,c) de la carte, et le ramasse le cas échéant. (fleur, dynamite)
	 * @param l ligne
	 * @param c colonne
	 */
	private void ramasser(int l, int c) {
		if(niv.carte[l][c]==fleur) {
			niv.carte[l][c]=vide;
			carte.n_fleur--;
			carte.invalidate();
		} else if(niv.carte[l][c]==fleurm) {
			niv.carte[l][c]=menhir;
			carte.n_fleur--;
			carte.invalidate();
		}
		if(carte.n_fleur==0) jeu.gagne();
	}
	
	/**
	 * Deplace le colibri vers la position demandée 
	 * 		@param ev  evenement declencheur du mouvement  
	 */
	public void onTouch(MotionEvent ev) {
		//carte.colibri.setPos((int) ev.getX(), (int) ev.getY()); // Petit exemple
		int x=(int) ev.getX();
		int y=(int) ev.getY();
		if (ev.getActionMasked()==MotionEvent.ACTION_DOWN) {
			if (y*carte.ww<x*carte.wh) {
				if (y*carte.ww<(carte.ww-x)*carte.wh) direction(UP);
				else direction(RIGHT);
			}
			else {
				if (y*carte.ww<(carte.ww-x)*carte.wh) direction(LEFT);
				else direction(DOWN);
			}
		}
	}
	
	public void direction(int dir) {
		switch (dir) {
		case UP:
			buf.add(new int[]{0,-1});
			break;
		case RIGHT:
			buf.add(new int[]{1,0});
			break;
		case LEFT:
			buf.add(new int[]{-1,0});
			break;
		case DOWN:
			buf.add(new int[]{0,1});
			break;
		}
	}

}
