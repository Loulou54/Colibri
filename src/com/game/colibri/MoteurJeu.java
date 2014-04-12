package com.game.colibri;

import android.view.MotionEvent;

public class MoteurJeu {
	
	/**
	 * Classe gérant les déplacements des éléments (colibri, vaches, ...) selon les règles
	 * et le rafraîchissement de l'écran.
	 */
	
	private Carte carte;
	private Niveau niv;
	private Jeu jeu;
	
	/**
	 * Laura et Mariam :
	 *   Voici la classe dans laquelle vous devez gérer les déplacements et leurs commandes (cf : onTouch, lisez la doc du MotionEvent si vous voulez gérer les "glisser" plus tard.)
	 * Idéalement, pensez déjà au système de buffer pour les commandes ! Laura tu vois de quoi je parle. :)
	 * Il s'agit d'enregistrer chaque commande (monter/descendre/droite/gauche) dans une file d'attente
	 * dans laquelle vous puisez dès que vous pouvez effectuer une nouvelle action.
	 * 
	 * La "carte" contient tout le niveau et gère son affichage.
	 * - carte.colibri est l'objet graphique "Animal" représentant le colibri.
	 * Son animation est gérée dans la classe Animal. S'y référer pour les commandes de position.
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
	
	public MoteurJeu(Jeu activ, Carte c) {
		carte = c;
		jeu=activ;
	}
	
	// Initialise les variables du moteur de jeu (buffer, niv, ...) : appelé après chaque appel de carte.loadNiveau
	public void init() {
		niv=carte.niv; // pour avoir une référence locale vers le niveau en cours et un nom moins long
	}
	
	public void start() {
		// TODO : démarrer le handler de rafraîchissement
		carte.colibri.start();
		// TODO : démarrer l'animation des vaches
	}
	
	public void pause() {
		// arrêter toutes les animations et le handler
	}
	
	public void onTouch(MotionEvent ev) {
		carte.colibri.setPos((int) ev.getX(), (int) ev.getY()); // Petit exemple
	}
}
