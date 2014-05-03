package com.game.colibri;

import android.content.Context;
import android.util.AttributeSet;

public class Vache extends Animal {
	
	private int[][] itineraire=null; // itinéraire des vaches/chats. Coordonnées de chaque point de passage. (au moins 2) Ex : {{1,1},{1,4},{3,4}}
	private int chkpt=0; // le prochain checkpoint de l'animal dans itineraire.
	
	public Vache(Context context, int w, int h, int[][] itin) {
		super(context,cw*itin[0][1],ch*itin[0][0],w,h);
		this.setBackgroundResource(R.drawable.vache);
		itineraire=itin;
		acc=0;
    	step=cw/20;
	}
	
	/**
	 * Constructeurs XML d'un animal
	 * 		@param context
	 * 		@param attrs
	 */
	public Vache(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Vache(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void deplacer() {
    	// on teste si l'on est arrivé au checkpoint :
    	int c=itineraire[chkpt][1] , l=itineraire[chkpt][0];
    	if (Math.abs(c*cw-params.leftMargin)<=step) {
    		mx=0;
    		params.leftMargin=c*cw;
    	} else params.leftMargin += mx*step;
    	if (Math.abs(l*ch-params.topMargin)<=step) {
    		my=0;
    		params.topMargin=l*ch;
    	} else params.topMargin += my*step;
		if (mx==0 && my==0) {
			chkpt=(chkpt+1)%itineraire.length;
			mx=(int) Math.signum(itineraire[chkpt][1]-c);
			my=(int) Math.signum(itineraire[chkpt][0]-l);
		}
		this.setLayoutParams(params);
	}

}
