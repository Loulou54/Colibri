package com.game.colibri;

import java.util.LinkedList;

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
	 * View contenant un Canvas sur lequel les graphismes statiques sont dessinés (menhirs, fleurs, dynamite)
	 * On spécifie le niveau à afficher par la méthode publique loadNiveau.
	 * La méthode invalidate() permet de lancer onDraw.
	 * Nécessité de rafraîchir à chaque élément ramassé.
	 */
	
	public int ww,wh,cw,ch; // windowWidth/Height, caseWidth/Height en pixels
	private static final int LIG=12, COL=20;
	public Niveau niv=null; // Le niveau à afficher
	public int n_fleur; // Le nombre de fleurs sur la carte
	private Bitmap menhir,fleur,fleurm,menhir0,fleur0,fleurm0; // Les images : -0 sont les originales avant redimensionnement
	public Animal colibri;
	public LinkedList<Animal> vaches = new LinkedList<Animal>(); // TODO : implémentation des vaches.
	
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
     * Constructeur une  carte
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
    
    // Méthode publique pour spécifier le niveau à afficher
    /**
     * Charge un  niveau sur la carte 
     		* @param niveau le niveau a chargé 
     		* @param lay
     */
    public void loadNiveau(Niveau niveau, RelativeLayout lay) {
    	if (niv!=null) { // Supprimer les "Animaux" du niveau précédent.
    		lay.removeView(colibri);
    		int len=vaches.size();
    		for(int i=0; i<len; i++) {
    			lay.removeView(vaches.get(i));
    		}
    		vaches.clear();
    	}
    	niv=niveau;
    	n_fleur=0;
    	for(int l=0; l<LIG; l++) {
    		for(int c=0; c<COL; c++) {
    			if(niv.carte[l][c]==2 || niv.carte[l][c]==3) n_fleur++;
    		}
    	}
    	colibri = new Animal(this.getContext(), R.drawable.colibri_d, niv.db_c*cw, niv.db_l*ch, 5*cw/4, 5*ch/4, cw, ch, null);
    	lay.addView(colibri);
    	// TODO : créer les vaches
    	vaches.addLast(new Animal(this.getContext(), R.drawable.vache, cw, ch, cw, ch, cw, ch, new int[][] {{1,1},{1,4},{3,4},{3,1}}));
    	vaches.addLast(new Animal(this.getContext(), R.drawable.vache, 16*cw, 11*ch, cw, ch, cw, ch, new int[][] {{11,16},{11,19},{11,13},{11,16},{5,16},{5,19},{11,19}}));
    	lay.addView(vaches.getFirst());
    	lay.addView(vaches.getLast());
    	this.invalidate();
    }
    
    // Dessin du canvas : événement déclenché par this.invalidate()
    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas can) {
    	if (niv!=null) {
	    	for (int l=0; l<LIG; l++) {
	    		for (int c=0; c<COL; c++) {
	    			if (niv.carte[l][c]==1)
	    				can.drawBitmap(menhir, c*cw-cw/8, l*ch, null);
	    			else if (niv.carte[l][c]==2)
	    				can.drawBitmap(fleur, c*cw, l*ch, null);
	    			else if (niv.carte[l][c]==3)
	    				can.drawBitmap(fleurm, c*cw, l*ch, null);
	    		}
	    	}
	    Log.i("onDraw","Rafraichissement !");
    	}
    }
    
    // Événement utilisé pour récupérer les dimensions de la View.
    /* (non-Javadoc)
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		ww=super.getWidth();
		wh=super.getHeight();
		Log.i("Dimensions écran :",ww+"*"+wh);
		cw=ww/COL;
		ch=wh/LIG;
		menhir = Bitmap.createScaledBitmap(menhir0, 5*cw/4, 5*ch/4, true);
		fleur = Bitmap.createScaledBitmap(fleur0, cw, ch, true);
		fleurm = Bitmap.createScaledBitmap(fleurm0, cw, ch, true);
		this.invalidate();
	}
}
