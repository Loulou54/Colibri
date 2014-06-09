package com.game.colibri;

import android.content.SharedPreferences;

public class Joueur {
	
	private static int[] img = new int[] {R.drawable.colibri_d1, R.drawable.chat_0, R.drawable.fleur, R.drawable.fleurm, R.drawable.menhir, R.drawable.menhir_rouge, R.drawable.rainbow, R.drawable.skull, R.drawable.vache_0};
	
	private String pseudo;
	private int exp, defis, win, avatar;
	
	/**
	 * Création d'un nouveau joueur.
	 * @param nom
	 */
	public Joueur(String nom) {
		String[] data = nom.split(";");
		if(data.length==1) {
			pseudo=nom;
			exp=0;
			defis=0;
			win=0;
			avatar=img[(int) (Math.random()*img.length)];
		} else {
			pseudo=data[0];
			exp=Integer.parseInt(data[1]);
			defis=Integer.parseInt(data[2]);
			win=Integer.parseInt(data[3]);
			avatar=Integer.parseInt(data[4]);
		}
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
		avatar=img[0];
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
	
	public int getAvatar() {
		return avatar;
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
	
	@Override
	public String toString() {
		return pseudo+";"+exp+";"+defis+";"+win+";"+avatar;
	}
}
