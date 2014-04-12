package com.game.colibri;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Animal extends ImageView {
	
	private RelativeLayout.LayoutParams params;
	
	// Constructeur java
	public Animal(Context context, int id_anim, int dbx, int dby, int w, int h) {
		super(context);
		params=new RelativeLayout.LayoutParams(w,h);
		params.leftMargin = dbx;
	    params.topMargin = dby;
	    this.setLayoutParams(params);
	    this.setBackgroundResource(id_anim);
	}
	
	// Constructeurs XML (qu'on utilisera pas normalement)
	public Animal(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Animal(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	// Méthodes
	// Positionner à (x,y)
	public void setPos(int x, int y) {
		params.leftMargin = x;
	    params.topMargin = y;
		this.setLayoutParams(params);
	}
	
	// Déplacer de (dx,dy)
	public void deplacer(int dx, int dy) {
		params.leftMargin += dx;
	    params.topMargin += dy;
		this.setLayoutParams(params);
	}
	
	// Retourne la position {x,y} (on peut aussi utiliser les héritages float getX et getY)
	public int[] getPos() {
	    return new int[] {params.leftMargin , params.topMargin};
	}
	
	// Commencer l'animation
	public void start() {
		((AnimationDrawable) this.getBackground()).start();
	}
	
	// Stopper l'animation
	public void stop() {
		((AnimationDrawable) this.getBackground()).stop();
	}
}
