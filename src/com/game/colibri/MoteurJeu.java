package com.game.colibri;



import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;


/**
 * Classe gérant les déplacements des éléments (colibri, vaches, ...) selon les régles
 * et le rafraîchissement de l'écran.
 */
public class MoteurJeu {
	
	
	
	private Carte carte;
	private Niveau niv;
	private Jeu jeu;
	private LinkedList <int[]> buf; // la file d'attente des touches
	private final static int PERIODE=1000/25; // pour 25 frames par secondes
	public static int menhir=1;
	public static int fleur=2;
	public static int fleurm=3;
	public static char vide=0;
	
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
		moveHandler.sleep(PERIODE);
	}
	
	/**
	 * Met le jeu sur pause
	 */
	public void pause() {
		carte.colibri.stop();
		int len=carte.vaches.size();
		for(int i=0; i<len; i++) {
			carte.vaches.get(i).stop();
		}
		moveHandler.removeMessages(0);
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
				int l= carte.colibri.getRow(); // ligne du colibri 
				int c=carte.colibri.getCol(); //  colone du colibri
				int ml=dir[1];
				int mc=dir[0];
				int [][] mat= niv.carte;
				if(mat[l][c]==fleur){
					niv.carte[l][c]=vide;
					carte.invalidate();
				}else if(mat[l][c]==fleurm){
					niv.carte[l][c]=menhir;
					carte.invalidate();
				}
				if(l+ml<0 || l+ml>=12 || c+mc<0 || c+mc>=20 || mat[l+ml][c+mc]==menhir){
					carte.colibri.mx=0;
					carte.colibri.my=0;
					carte.colibri.setPos(c*carte.cw, l*carte.ch);
				}
			carte.colibri.deplacer();
		}
		int len=carte.vaches.size();
		for(int i=0; i<len; i++) {
			carte.vaches.get(i).deplacer();
		}
		moveHandler.sleep(PERIODE);
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
				if (y*carte.ww<(carte.ww-x)*carte.wh) { // appui sur HAUT
					Log.i("Appui :","HAUT");
					buf.add(new int[]{0,-1});
				}
				else { // Appui sur DROITE
					Log.i("Appui :","DROITE");
					buf.add(new int[]{1,0});
				}
			}
			else {
				if (y*carte.ww<(carte.ww-x)*carte.wh) { // appui sur GAUCHE
					Log.i("Appui :","GAUCHE");
					buf.add(new int[]{-1,0});
				}
				else { // appui sur BAS
					Log.i("Appui :","BAS");
					buf.add(new int[]{0,1});
				}
			}
		}
	}

	
}
