package com.game.colibri;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class Colibri extends Animal {
	
	public int v_max;
	private int xf=10,yf=10;
	private boolean versDroite=false;
	
	public Colibri(Context context, int dbx, int dby, int w, int h) {
		super(context,dbx,dby,w,h);
		this.setBackgroundResource(R.drawable.colibri_d);
		acc=cw/10;
		v_max=3*cw/4;
	    step=0;
	}
	
	/**
	 * Constructeurs XML d'un colibri
	 * 		@param context
	 * 		@param attrs
	 */
	public Colibri(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setBackgroundResource(R.drawable.colibri_d);
	}
	
	public Colibri(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setBackgroundResource(R.drawable.colibri_d);
	}
	
	@Override
	public void deplacer() {
		step=Math.min(step+acc, v_max); // Vitesse plafonnée à v_max case/frame.
		params.leftMargin += mx*step;
	    params.topMargin += my*step;
	    this.setLayoutParams(params);
	}
	
	public void setSpriteDirection() {
		if(mx==1)
			this.setBackgroundResource(R.drawable.colibri_d);
		else if(mx==-1)
			this.setBackgroundResource(R.drawable.colibri_g);
		this.start();
	}
	
	/**
	 * Pour le menu de sélection, permet d'enregistrer la position du colibri à la fin d'un déplacement.
	 */
	public void savePosFinAnim(int x, int y, boolean regardeVersDroite) {
		xf=x;
		yf=y;
		versDroite=regardeVersDroite;
	}
	
	/**
	 * Pour le menu de sélection, permet de définir la position du colibri selon xf et yf.
	 */
	public void setPosFinAnim() {
		params=(RelativeLayout.LayoutParams) this.getLayoutParams();
	    params.leftMargin = xf;
	    params.topMargin = yf;
	    this.setLayoutParams(params);
	    if(versDroite) {
	    	mx=1;
	    	setSpriteDirection();
	    }
	}
	
	/**
	 * Permet de définir l'emplacement du colibri dans le menu de sélection à la fin de l'animation.
	 * Contournement du bug du listener d'animation. 
	 */
	@Override
	protected void onAnimationEnd() {
	    super.onAnimationEnd();
	    setPosFinAnim();
	}
}
