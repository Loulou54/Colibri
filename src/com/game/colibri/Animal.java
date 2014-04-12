package com.game.colibri;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Animal extends ImageView {
	
	private RelativeLayout.LayoutParams params;
	
	
	/**
	 * Constructeur java d'un animal 
	 * 
	 * @param context le contexte de création 
	 * @param id_anim
	 * @param dbx
	 * @param dby
	 * @param w
	 * @param h
	 */
	public Animal(Context context, int id_anim, int dbx, int dby, int w, int h) {
		super(context);
		params=new RelativeLayout.LayoutParams(w,h);
		params.leftMargin = dbx;
	    params.topMargin = dby;
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
	 * 		@param y le nouveau ordonné 
	 */
	public void setPos(int x, int y) {
		params.leftMargin = x;
	    params.topMargin = y;
		this.setLayoutParams(params);
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
	 * Retourne la positon  de l'animal
	 * 
	 * 		@return la position {x,y} de l'animal dans un tableau 
	 */
	public int[] getPos() {
	    return new int[] {params.leftMargin , params.topMargin};
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
