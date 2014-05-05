package com.game.colibri;

import java.io.IOException;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
	public Colibri colibri;
	public LinkedList<Vache> vaches = new LinkedList<Vache>(); // La liste des vaches du niveau
	public LinkedList<Chat> chats = new LinkedList<Chat>(); // La liste des chats du niveau
	public ImageView mort,sang;
	private Context context;
	
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
    	this.context=context;
    	mort = new ImageView(context);
    	mort.setBackgroundResource(R.drawable.skull);
    	sang = new ImageView(context);
    	sang.setBackgroundResource(R.drawable.sang);
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
    public void loadNiveau(int index_niv, RelativeLayout lay) {
    	if (niv!=null) { // Supprimer les "Animaux" du niveau précédent.
    		lay.removeView(mort);
    		lay.removeView(sang);
    		lay.removeView(colibri);
    		for(Vache v : vaches) {
    			lay.removeView(v);
    		}
    		vaches.clear();
    		for(Chat c : chats) {
    			lay.removeView(c);
    		}
    		chats.clear();
    	}
    	try { // On ouvre le Niveau index_niv.
			niv=new Niveau(context.getAssets().open("niveaux/niveau"+index_niv+".txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	n_fleur=0;
    	for(int l=0; l<LIG; l++) {
    		for(int c=0; c<COL; c++) {
    			if(niv.carte[l][c]==2 || niv.carte[l][c]==3) n_fleur++;
    		}
    	}
    	colibri = new Colibri(this.getContext(), niv.db_c*cw, niv.db_l*ch, cw, ch);
    	lay.addView(colibri);
    	lay.addView(sang);
    	sang.setVisibility(INVISIBLE);
    	// On crée les vaches et les chats
    	for (int[][] itin : niv.vaches) {
    		vaches.addLast(new Vache(this.getContext(), cw, ch, itin));
    		lay.addView(vaches.getLast());
    	}
    	for (int[][] itin : niv.chats) {
    		chats.addLast(new Chat(this.getContext(), cw, 5*ch/4, itin));
    		lay.addView(chats.getLast());
    	}
    	lay.addView(mort);
    	mort.setVisibility(INVISIBLE);
    	this.invalidate();
    }
    
    public void animMort() {
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(3*cw/2,3*ch/2);
    	int[] pos = colibri.getPos();
		params.leftMargin = pos[0]-cw/4;
	    params.topMargin = pos[1]-ch/4;
	    mort.setLayoutParams(params);
	    mort.setVisibility(VISIBLE);
	    sang.setLayoutParams(params);
	    sang.setVisibility(VISIBLE);
    	mort.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dead_anim));
    	sang.startAnimation(AnimationUtils.loadAnimation(context, R.anim.blood_anim));
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
		super.onSizeChanged(w, h, oldw, oldh);
		ww=super.getWidth();
		wh=super.getHeight();
		Log.i("Dimensions écran :",ww+"*"+wh);
		cw=ww/COL;
		ch=wh/LIG;
		Animal.cw=cw;
		Animal.ch=ch;
		menhir = Bitmap.createScaledBitmap(menhir0, 5*cw/4, 5*ch/4, true);
		fleur = Bitmap.createScaledBitmap(fleur0, cw, ch, true);
		fleurm = Bitmap.createScaledBitmap(fleurm0, cw, ch, true);
		this.invalidate();
	}
}
