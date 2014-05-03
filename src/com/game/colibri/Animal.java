package com.game.colibri;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public abstract class Animal extends ImageView {
	
	protected RelativeLayout.LayoutParams params;
	public static int cw,ch; // largeur et hauteur d'une case
	public int step; // pas des mouvements en px/frame
	protected int acc; // accélération en px/frame/frame
	public int mx,my; // sens du mouvement de l'animal (1,-1,0)
	
	/**
	 * Constructeur java d'un animal 
	 * 
	 * @param context le contexte de création 
	 * @param id_anim la ressource "drawable" de l'animation
	 * @param dbx abscisse du coin supérieur gauche
	 * @param dby ordonnée du coin supérieur gauche
	 * @param w largeur
	 * @param h hauteur
	 * @param cw largeur d'une case de la carte
	 * @param ch hauteur d'une case de la carte
	 */
	public Animal(Context context, int dbx, int dby, int w, int h) {
		super(context);
		params=new RelativeLayout.LayoutParams(w,h);
		params.leftMargin = dbx;
	    params.topMargin = dby;
	    mx=0;
	    my=0;
	    this.setLayoutParams(params);
	}
	
	
	/**
	 * Constructeurs XML d'un animal
	 * 		@param context
	 * 		@param attrs
	 */
	public Animal(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Animal(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	/**
	 * Met a jour la  postion 
	 * 		@param x la nouvelle  abcisse 
	 * 		@param y le nouveau ordonné 
	 */
	public void setPos(int x, int y) {
		params.leftMargin = x;
	    params.topMargin = y;
		this.setLayoutParams(params);
	}
	
	/**
	 * Donne la direction de déplacement de l'animal, en attribuant les valeurs mx et my.
	 * 		@param dir un couple donnant la direction x/y de déplacement de l'animal. (ex : {0,-1} = vers la gauche)
	 * Donne la direction de déplacement de l'animal, en attribuant les valeurs mx et my.
	 * 		@param dir un couple donnant la direction x/y de déplacement de l'animal. (ex : {0,-1} = vers la gauche)
	 */
	public void setDirection(int[] dir) {
		mx=dir[0];
		my=dir[1];
	}
	
	/**
	 * Renvoi la direction de déplacement de l'animal { mx, my}.
	 *
	 */
	public int[] getDirection() {
		int[] dir = new int[2];
		dir[0]=mx;
		dir[1]=my;
		return dir;
	}
	
	/**
	 * Déplace l'animal rapport a sa position d'origne d'une certaine valeur
	 * 		@param dx le delta dont il faut déplacer l'abcisse 
	 * 		@param dy le delta dont il faut délacer l'ordonné
	 */
	public void deplacer(int dx, int dy) {
		params.leftMargin += dx;
	    params.topMargin += dy;
		this.setLayoutParams(params);
	}
	
	/**
	 * Déplace l'animal selon l'implémentation voulue par la classe fille. (méthode abstraite)
	 */
	public abstract void deplacer();
	
	/**
	 * Retourne la positon  de l'animal
	 * 
	 * 		@return la position {x,y} de l'animal dans un tableau 
	 */
	public int[] getPos() {
	    return new int[] {params.leftMargin , params.topMargin};
	}
	
	/**
	 * Retourne la ligne sur laquelle se trouve le centre de l'animal
	 */
	public int getRow() {
		return (params.topMargin+params.height/2)/ch;
	}
	
	/**
	 * Retourne la colonne sur laquelle se trouve le centre de l'animal
	 */
	public int getCol() {
		return (params.leftMargin+params.width/2)/cw;
	}
	
	/**
	 * Commencer l'animation
	 */
	public void start() {
		((AnimationDrawable) this.getBackground()).start();
	}
	
	
	/**
	 * Stopper l'animation
	 */
	public void stop() {
		((AnimationDrawable) this.getBackground()).stop();
	}
}
