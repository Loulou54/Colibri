package com.game.colibri;

import android.content.Context;

public class Joueur {
	
	public static int[] img = new int[] {R.drawable.colibri_d1, R.drawable.fleur, R.drawable.fleurm, R.drawable.menhir, R.drawable.menhir_rouge, R.drawable.dynamite_allumee, R.drawable.explo2, R.drawable.rainbow, R.drawable.skull, R.drawable.chat_0, R.drawable.vache_0, R.drawable.doge, R.drawable.doge_lunettes, R.drawable.megusta, R.drawable.ampoule, R.drawable.coupe, android.R.drawable.star_big_on, android.R.drawable.sym_def_app_icon};
	
	private String pseudo, pays;
	private int exp, progress, defis, win, loose, avatar, rang;
	private long lastVisit;
	
	/**
	 * Récupération des paramètres du joueur propriétaire du smartphone.
	 * @param sav
	 */
	/*public Joueur(SharedPreferences sav) {
		pseudo=sav.getString("pseudo", null);
		pays=Resources.getSystem().getConfiguration().locale.getCountry();
		exp=sav.getInt("exp", 0);
		defis=sav.getInt("defis", 0);
		win=sav.getInt("win", 0);
		loose=sav.getInt("loose", 0);
		avatar=img[0];
	}*/
	
	/**
	 * Créer un nouveau joueur.
	 * @param sav
	 */
	public Joueur(String nom, String loc, int e, int prog, int d, int w, int l, int av, long lv) {
		pseudo=nom;
		pays=loc;
		exp=e;
		progress=prog;
		defis=d;
		win=w;
		loose=l;
		avatar=av;
		lastVisit=lv;
		rang=0;
	}
	
	public String getPseudo() {
		return pseudo;
	}
	
	public String getPays() {
		return pays;
	}
	
	public int getExp() {
		return exp;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public int getDefis() {
		return defis;
	}
	
	public int getWin() {
		return win;
	}
	
	public int getLost() {
		return loose;
	}
	
	public int getAvatar() {
		return img[avatar];
	}
	
	public int getRang() {
		return rang;
	}
	
	public String getLastVisit(Context c) {
		/*DateFormat df = SimpleDateFormat.getDateTimeInstance();
		Date d = new Date(lastVisit*1000);
		return df.format(d);*/
		long t = System.currentTimeMillis()/1000-lastVisit;
		if(t<3600)
			return c.getResources().getString(R.string.minutesAgo, t/60, t/60>1 ? "s" : "");
		else if(t<3600*24)
			return c.getResources().getString(R.string.hoursAgo, t/3600, t/3600>1 ? "s" : "");
		else
			return c.getResources().getString(R.string.daysAgo, t/(3600*24), t/(3600*24)>1 ? "s" : "");
	}
	
	/**
	 * Pour transformer le temps relatif en absolu de lastVisit. A appeler après désérialisation JSON.
	 */
	public void computeLastVisit() {
		lastVisit=System.currentTimeMillis()/1000-lastVisit;
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
	public void win(int n) {
		win+=n;
	}
	
	/**
	 * Incrémente le compteur de défaites du joueur.
	 */
	public void loose() {
		loose++;
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
		return pseudo+";"+pays+";"+exp+";"+progress+";"+defis+";"+win+";"+loose+";"+avatar+";"+lastVisit;
	}
}
