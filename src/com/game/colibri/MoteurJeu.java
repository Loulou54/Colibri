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
	
	/**
	 * Laura et Mariam :
	 *   Voici la classe dans laquelle vous devez gérer les déplacements et leurs commandes (cf : onTouch, lisez la doc du MotionEvent si vous voulez gérer les "glisser" plus tard.)
	 * Idéalement, pensez déjà au système de buffer pour les commandes ! Laura tu vois de quoi je parle. :)
	 * Il s'agit d'enregistrer chaque commande (monter/descendre/droite/gauche) dans une file d'attente
	 * dans laquelle vous puisez dès que vous pouvez effectuer une nouvelle action.
	 * 
	 * La "carte" contient tout le niveau et gère son affichage.
	 * - carte.colibri est l'objet graphique "Animal" repr�sentant le colibri.
	 * Son animation est gèrée dans la classe Animal. S'y référer pour les commandes de position.
	 * - carte.vaches sera la liste des vaches de type "Animal". On ne s'y int�resse pas tout de suite ! ;)
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
	 * Handler de rafra�chissement
	 * Il appelle la m�thode void move() qui elle m�me appelle moveHandler.sleep(PERIODE) pour boucler.
	 * Utiliser moveHandler.sleep(PERIODE) pour appeler le handler apr�s PERIODE ms.
	 * Utiliser moveHandler.removeMessages(0) pour arr�ter le cycle.
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
	 *  Initialise les variables du moteur de jeu (buffer, niv, ...) : appel� apr�s chaque appel de carte.loadNiveau
	 */
	public void init() {
		niv=carte.niv; // pour avoir une r�f�rence locale vers le niveau en cours et un nom moins long
		buf.clear();
	}
	
	/**
	 *  Commence le jeu 
	 */
	public void start() {
		// TODO : d�marrer le handler de rafra�chissement
		moveHandler.sleep(PERIODE);
		carte.colibri.start();
		// TODO : d�marrer l'animation des vaches
	}
	
	/**
	 * Met le jeu sur pause
	 * @throws InterruptedException 
	 */

	public void pause() {
		carte.colibri.stop();
		moveHandler.removeMessages(0);
	}

	/*public void pause() throws InterruptedException {
		// arr�ter toutes les animations et le handler
	}*/
	
	/**
	 * M�thode appel�e p�riodiquement par le handler moveHandler lorsque le jeu est en marche.
	 * C'est ici que s'effectue les d�placements des animaux.
	 */
	private void move() {
		if (carte.colibri.mx==0 & carte.colibri.my==0) {
			if (buf.size()>0) carte.colibri.setDirection(buf.poll());
		}
		else {
			carte.colibri.deplacer();
			int [] co=carte.colibri.getPos();
			if (co[0]<=0) {
				carte.colibri.setPos(0, co[1]);
				carte.colibri.mx=0;
			}
			else if (co[0]>=carte.ww-carte.cw) {
				carte.colibri.setPos(carte.ww-carte.cw, co[1]);
				carte.colibri.mx=0;
			}
			else if (co[1]<=0) {
				carte.colibri.setPos(co[0], 0);
				carte.colibri.my=0;
			}
			else if (co[1]>=carte.wh-carte.ch) {
				carte.colibri.setPos(co[0], carte.wh-carte.ch);
				carte.colibri.my=0;
			}
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
