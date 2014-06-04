package com.game.colibri;

import android.content.SharedPreferences;

public class Joueur {
	
	private String pseudo;
	private int exp, defis, win;
	
	/**
	 * Création d'un nouveau joueur.
	 * @param nom
	 */
	public Joueur(String nom) {
		pseudo=nom;
		exp=0;
		defis=0;
		win=0;
	}
	
	/**
	 * Récupération des paramètres du joueur propriétaire du smartphone.
	 * @param sav
	 */
	public Joueur(SharedPreferences sav) {
		pseudo=sav.getString("pseudo", null);
		exp=sav.getInt("exp", 0);
		defis=sav.getInt("defis", 0);
		win=sav.getInt("win", 0);
	}
	
	public String getPseudo() {
		return pseudo;
	}
	
	public int getExp() {
		return exp;
	}
	
	public int getDefis() {
		return defis;
	}
	
	public int getWin() {
		return win;
	}
	
	/**
	 * Incrémente le compteur de défis du joueur.
	 */
	public void defi() {
		defis++;
	}
	
	/**
	 * Incrémente le compteur de victoires du joueur.
	 */
	public void win() {
		win++;
	}
	
	/**
	 * Ajoute de l'expérience au joueur.
	 * @param e
	 */
	public void addExp(int e) {
		exp+=e;
	}
}
