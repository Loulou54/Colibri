package com.game.colibri;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;


public class Carte extends View {
	
	/**
	 * View contenant un Canvas sur lequel les graphismes statiques sont dessin�s (menhirs, fleurs, dynamite)
	 * On sp�cifie le niveau � afficher par la m�thode publique loadNiveau.
	 * La m�thode invalidate() permet de lancer onDraw.
	 * N�cessit� de rafra�chir � chaque �l�ment ramass�.
	 */
	
	public int ww,wh,cw,ch; // windowWidth/Height, caseWidth/Height en pixels
	public Niveau niv=null; // Le niveau � afficher
	private Bitmap menhir,fleur,fleurm,menhir0,fleur0,fleurm0; // Les images : -0 sont les originales avant redimensionnement
	public Animal colibri;
	//public Animal[] vaches; TODO : impl�mentation des vaches.
	
    /**
     * Constructeur une carte 
     * 		@param context
     * 		@param attrs
     */
    public Carte(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadImg(context);
    }

    /**
     * Construiteur une  carte
     * 		@param context
     * 		@param attrs
     * 		@param defStyle
     */
    public Carte(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadImg(context);
    }
    
    /**
     * Charge des images sur la carte 
     * 		@param context
     */
    private void loadImg(Context context) {
    	menhir0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.menhir)).getBitmap();
    	fleur0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.fleur)).getBitmap();
    	fleurm0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.fleurm)).getBitmap();
    }
    
    // M�thode publique pour sp�cifier le niveau � afficher
    /**
     * Charge un  niveau sur la carte 
     		* @param niveau le niveau a chargé 
     		* @param lay
     */
    public void loadNiveau(Niveau niveau, RelativeLayout lay) {
    	if (niv!=null) { // Supprimer les "Animaux" du niveau pr�c�dent.
    		lay.removeView(colibri);
    	}
    	niv=niveau;
    	colibri = new Animal(this.getContext(), R.drawable.colibri_d, niv.db_c*cw, niv.db_l*ch, 5*cw/4, 5*ch/4);
    	// TODO : cr�er les vaches
    	lay.addView(colibri);
    	this.invalidate();
    }
    
    // Dessin du canvas : �v�nement d�clench� par this.invalidate()
    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas can) {
    	if (niv!=null) {
	    	for (int l=0; l<12; l++) {
	    		for (int c=0; c<20; c++) {
	    			if (niv.carte[l][c]==1)
	    				can.drawBitmap(menhir, c*cw-cw/8, l*ch-cw/8, null);
	    			else if (niv.carte[l][c]==2)
	    				can.drawBitmap(fleur, c*cw, l*ch, null);
	    			else if (niv.carte[l][c]==3)
	    				can.drawBitmap(fleurm, c*cw, l*ch, null);
	    		}
	    	}
	    Log.i("onDraw","Rafraichissement !");
    	}
    }
    
    // Ev�nement utilis� pour r�cup�rer les dimensions de la View.
    /* (non-Javadoc)
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		ww=super.getWidth();
		wh=super.getHeight();
		Log.i("Dimensions �cran :",ww+"*"+wh);
		cw=ww/20;
		ch=wh/12;
		menhir = Bitmap.createScaledBitmap(menhir0, 5*cw/4, 5*ch/4, true);
		fleur = Bitmap.createScaledBitmap(fleur0, cw, ch, true);
		fleurm = Bitmap.createScaledBitmap(fleurm0, cw, ch, true);
		this.invalidate();
	}
}
