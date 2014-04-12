package com.game.colibri;

import android.view.MotionEvent;

public class MoteurJeu {
	
	/**
	 * Classe g�rant les d�placements des �l�ments (colibri, vaches, ...) selon les r�gles
	 * et le rafra�chissement de l'�cran.
	 */
	
	private Carte carte;
	private Niveau niv;
	private Jeu jeu;
	
	/**
	 * Laura et Mariam :
	 *   Voici la classe dans laquelle vous devez g�rer les d�placements et leurs commandes (cf : onTouch, lisez la doc du MotionEvent si vous voulez g�rer les "glisser" plus tard.)
	 * Id�alement, pensez d�j� au syst�me de buffer pour les commandes ! Laura tu vois de quoi je parle. :)
	 * Il s'agit d'enregistrer chaque commande (monter/descendre/droite/gauche) dans une file d'attente
	 * dans laquelle vous puisez d�s que vous pouvez effectuer une nouvelle action.
	 * 
	 * La "carte" contient tout le niveau et g�re son affichage.
	 * - carte.colibri est l'objet graphique "Animal" repr�sentant le colibri.
	 * Son animation est g�r�e dans la classe Animal. S'y r�f�rer pour les commandes de position.
	 * - carte.vaches sera la liste des vaches de type "Animal". On ne s'y int�resse pas tout de suite ! ;)
	 * - carte.loadNiveau permet de charger un nouveau niveau : vous ne devez pas l'utiliser ici.
	 * - carte.invalidate() : permet de redessiner la carte. Il ne faut l'appeler que lorsqu'un objet statique
	 * a boug� ou a disparu. Typiquement lorsque l'on mange une fleur ! (miam) Le d�placement des animaux ne
	 * n�cessite pas de redessiner la carte. (car ce sont des View � eux seuls)
	 * 
	 * Le Jeu "jeu" permet de faire r�f�rence � l'activit� appelante. C'est � elle notamment qu'il
	 * faudra signaler lorsque le joueur a fini le niveau par un appel : jeu.gagne() pour pouvoir passer au suivant !
	 * 
	 * Bon courage ! Vous pouvez bien s�r modifier des trucs dans les autres classes mais presque tout
	 * votre travail devrait se faire l� normalement ! ;)
	 */
	
	public MoteurJeu(Jeu activ, Carte c) {
		carte = c;
		jeu=activ;
	}
	
	// Initialise les variables du moteur de jeu (buffer, niv, ...) : appel� apr�s chaque appel de carte.loadNiveau
	public void init() {
		niv=carte.niv; // pour avoir une r�f�rence locale vers le niveau en cours et un nom moins long
	}
	
	public void start() {
		// TODO : d�marrer le handler de rafra�chissement
		carte.colibri.start();
		// TODO : d�marrer l'animation des vaches
	}
	
	public void pause() {
		// arr�ter toutes les animations et le handler
	}
	
	public void onTouch(MotionEvent ev) {
		carte.colibri.setPos((int) ev.getX(), (int) ev.getY()); // Petit exemple
	}
}
