package com.game.colibri;

import android.content.Context;
import android.util.AttributeSet;

public class Colibri extends Animal {
	
	public Colibri(Context context, int dbx, int dby, int w, int h) {
		super(context,dbx,dby,w,h);
		this.setBackgroundResource(R.drawable.colibri_d);
		acc=cw/10;
	    step=0;
	}
	
	/**
	 * Constructeurs XML d'un colibri
	 * 		@param context
	 * 		@param attrs
	 */
	public Colibri(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Colibri(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void deplacer() {
		step=Math.min(step+acc, 3*cw/4); // Vitesse plafonnée à 3/4 case/frame.
		params.leftMargin += mx*step;
	    params.topMargin += my*step;
	    this.setLayoutParams(params);
	}
	
}
