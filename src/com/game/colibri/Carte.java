package com.game.colibri;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Carte extends View {
	
	/**
	 * View contenant un canvas sur lequel les graphismes statiques sont dessinés (menhirs, fleurs, dynamite)
	 * On spécifie le niveau à afficher par la méthode publique loadNiveau.
	 * La méthode invalidate() permet de lancer onDraw.
	 * Nécessité de rafraîchir à chaque élément ramassé.
	 */
	
	public int ww,wh,cw,ch; // windowWidth/Height, caseWidth/Height en pixels
	private Niveau map=null; // Le niveau à afficher
	private Bitmap menhir,fleur,fleurm,menhir0,fleur0,fleurm0; // Les images : -0 sont les originales avant redimensionnement
	
	// Constructeurs
    public Carte(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadImg(context);
    }

    public Carte(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadImg(context);
    }
    
    private void loadImg(Context context) {
    	menhir0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.menhir)).getBitmap();
    	fleur0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.fleur)).getBitmap();
    	fleurm0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.fleurm)).getBitmap();
    }
    
    // Méthode publique pour spécifier le niveau à afficher
    public void loadNiveau(Niveau niv) {
    	map=niv;
    	this.invalidate();
    }
    
    // Dessin du canvas : événement déclenché par this.invalidate()
    @Override
    protected void onDraw(Canvas can) {
    	if (map!=null) {
	    	for (int l=0; l<12; l++) {
	    		for (int c=0; c<20; c++) {
	    			if (map.carte[l][c]==1)
	    				can.drawBitmap(menhir, c*cw-cw/8, l*ch-cw/8, null);
	    			else if (map.carte[l][c]==2)
	    				can.drawBitmap(fleur, c*cw, l*ch, null);
	    			else if (map.carte[l][c]==3)
	    				can.drawBitmap(fleurm, c*cw, l*ch, null);
	    		}
	    	}
	    Log.i("onDraw","Rafraichissement !");
    	}
    }
    
    // Evénement utilisé pour récupérer les dimensions de la View.
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		ww=super.getWidth();
		wh=super.getHeight();
		Log.i("Dimensions écran :",ww+"*"+wh);
		cw=ww/20;
		ch=wh/12;
		menhir = Bitmap.createScaledBitmap(menhir0, 5*cw/4, 5*ch/4, true);
		fleur = Bitmap.createScaledBitmap(fleur0, cw, ch, true);
		fleurm = Bitmap.createScaledBitmap(fleurm0, cw, ch, true);
		this.invalidate();
	}
}
