package com.game.colibri;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Animal extends ImageView {
	
	private RelativeLayout.LayoutParams params;
	private int cw,ch; // largeur et hauteur d'une case
	public int step; // pas des mouvements en px/frame
	private int acc; // accélération en px/frame/frame
	public int mx,my; // mouvement en pixels par frame de l'animal
	
	/**
	 * Constructeur java d'un animal 
	 * 
	 * @param context le contexte de crÃ©ation 
	 * @param id_anim la ressource "drawable" de l'animation
	 * @param dbx abscisse du coin supérieur gauche
	 * @param dby ordonnée du coin supérieur gauche
	 * @param w largeur
	 * @param h hauteur
	 * @param cw largeur d'une case de la carte
	 * @param ch hauteur d'une case de la carte
	 */
	public Animal(Context context, int id_anim, int dbx, int dby, int w, int h, int cw, int ch) {
		super(context);
		params=new RelativeLayout.LayoutParams(w,h);
		params.leftMargin = dbx;
	    params.topMargin = dby;
	    this.cw=cw;
	    this.ch=ch;
	    mx=0;
	    my=0;
	    acc=cw/10;
	    step=0;
	    this.setLayoutParams(params);
	    this.setBackgroundResource(id_anim);
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
	 * 		@param y le nouveau ordonnÃ© 
	 */
	public void setPos(int x, int y) {
		params.leftMargin = x;
	    params.topMargin = y;
		this.setLayoutParams(params);
	}
	
	/**
	 * Donne la direction de déplacement du colibri, en attribuant les valeurs mx et my.
	 * 		@param dir un couple donnant la direction x/y de déplacement du colibri. (ex : {0,-1} = vers la gauche)
	 */
	public void setDirection(int[] dir) {
		mx=dir[0];
		my=dir[1];
	}
	
	/**
	 * DÃ©place l'animal rapport a sa position d'origne d'une certaine valeur
	 * 		@param dx le delta dont il faut dÃ©placer l'abcisse 
	 * 		@param dy le delta dont il faut dÃ©lacer l'ordonnÃ©
	 */
	public void deplacer(int dx, int dy) {
		params.leftMargin += dx;
	    params.topMargin += dy;
		this.setLayoutParams(params);
	}
	
	public void deplacer() {
		step=Math.min(step+acc, cw); // Vitesse plafonnée à 1 case/s.
		params.leftMargin += mx*step;
	    params.topMargin += my*step;
	    if (params.leftMargin<0) { // Arrêt sontre les bords de la map.
	    	params.leftMargin=0;
			mx=0;
		}
		else if (params.leftMargin>19*cw) {
			params.leftMargin=19*cw;
			mx=0;
		}
		else if (params.topMargin<0) {
			params.topMargin=0;
			my=0;
		}
		else if (params.topMargin>11*ch) {
			params.topMargin=11*ch;
			my=0;
		}
		this.setLayoutParams(params);
	}
	
	/**
	 * Retourne la positon  de l'animal
	 * 
	 * 		@return la position {x,y} de l'animal dans un tableau 
	 */
	public int[] getPos() {
	    return new int[] {params.leftMargin , params.topMargin};
	}
	
	/**
	 * Retourne la ligne sur laquelle se trouve le colibri
	 */
	public int getRow() {
		return params.topMargin/ch;
	}
	
	/**
	 * Retourne la colonne sur laquelle se trouve le colibri
	 */
	public int getCol() {
		return params.leftMargin/cw;
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
