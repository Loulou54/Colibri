package com.game.colibri;

import android.content.Context;
import android.util.AttributeSet;

public class Colibri extends Animal {
	
	public int v_max;
	
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
	}
	
	public Colibri(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
}
