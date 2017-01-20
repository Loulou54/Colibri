package com.game.colibri;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;


public class Carte extends RelativeLayout {
	
	/**
	 * RelativeLayout gérant l'affichage complet d'un niveau.
	 * Le layout contient un fond sur lequel est dessiné la carte (statique) que l'on rafraîchit
	 * seulement lorsque nécessaire avec fond.invalidate() (commandé par le moteur de jeu avec "fond.invalidate()").
	 * Il contient aussi les éléments mobiles sous forme de View. (Animal)
	 * On spécifie le niveau à afficher par la méthode publique loadNiveau.
	 * Nécessité de rafraîchir fond à chaque élément ramassé.
	 */
	
	public int ww,wh; // windowWidth/Height
	public double cw,ch; // caseWidth/Height en pixels
	private static final int LIG=12, COL=20;
	public Niveau niv=null; // Le niveau à afficher
	public int n_fleur,n_dyna; // Le nombre de fleurs sur la carte et le nombre de dynamites ramassées.
	private int index_dyna; // L'index de l'animation courante d'explosion.
	private Bitmap menhir,fleur,fleurm,dyna,menhir_rouge,rainbow,menhir0,fleur0,fleurm0,dyna0,menhir_rouge0,rainbow0; // Les images : -0 sont les originales avant redimensionnement
	public Colibri colibri;
	public LinkedList<Vache> vaches = new LinkedList<Vache>(); // La liste des vaches du niveau
	public LinkedList<Chat> chats = new LinkedList<Chat>(); // La liste des chats du niveau
	public LinkedList<View> explo = new LinkedList<View>(); // La liste des explosions
	public SparseArray<int[]> rainbows = new SparseArray<int[]>();
	private int[] colors = new int[] {0x50FAE96C, 0x50552DA2, 0x502FE0D6, 0x50FA9B44, 0x50FA95E5, 0x50F92722, 0x5000FF54};
	public View mort,sang,fond;
	
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
     * Chargement des images de la carte.
     * 		@param context
     */
    private void loadImg(Context context) {
    	mort = new View(context);
    	mort.setBackgroundResource(R.drawable.skull);
    	sang = new View(context);
    	sang.setBackgroundResource(R.drawable.sang);
    	menhir0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.menhir)).getBitmap();
    	fleur0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.fleur)).getBitmap();
    	fleurm0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.fleurm)).getBitmap();
    	dyna0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.dynamite)).getBitmap();
    	menhir_rouge0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.menhir_rouge)).getBitmap();
    	rainbow0 = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.rainbow)).getBitmap();
    	// Définition du fond qui comporte la carte statique.
    	final Paint mPaint = new Paint();
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeJoin(Paint.Join.ROUND);
    	mPaint.setStrokeCap(Paint.Cap.ROUND);
    	mPaint.setStrokeWidth(5);
    			fond=new View(this.getContext()) {
    	    		// Dessin du canvas : événement déclenché par fond.invalidate()
    	    	    @Override
    	    	    protected void onDraw(Canvas can) {
    	    	    	if (niv!=null) {
    	    	    		mPaint.setColor(0x708B4500);
    	    	    		for(Vache v : vaches) {
    	    	    			for(int i=1; i<=3; i++) {
    	    	    				mPaint.setStrokeWidth((float) ((0.4-i*0.1)*cw));
    	    	    				can.drawPath(v.path, mPaint);
    	    	    			}
    	    	    		}
    	    	    		for(Chat c : chats) {
    	    	    			for(int i=1; i<=3; i++) {
    	    	    				mPaint.setStrokeWidth((float) ((0.4-i*0.1)*cw));
    	    	    				can.drawPath(c.path, mPaint);
    	    	    			}
    	    	    		}
    	    		    	for (int l=0; l<LIG; l++) {
    	    		    		for (int c=0; c<COL; c++) {
    	    		    			if (niv.carte[l][c]==1)
    	    		    				can.drawBitmap(menhir, (int)(c*cw-cw/8), (int)(l*ch), null);
    	    		    			else if (niv.carte[l][c]==2)
    	    		    				can.drawBitmap(fleur, (int)(c*cw), (int)(l*ch), null);
    	    		    			else if (niv.carte[l][c]==3)
    	    		    				can.drawBitmap(fleurm, (int)(c*cw), (int)(l*ch), null);
    	    		    			else if (niv.carte[l][c]==4)
    	    		    				can.drawBitmap(dyna, (int)(c*cw), (int)(l*ch), null);
    	    		    			else if (niv.carte[l][c]==5)
    	    		    				can.drawBitmap(menhir_rouge, (int)(c*cw-cw/8), (int)(l*ch), null);
    	    		    			else if (niv.carte[l][c]>=10) {
    	    		    				mPaint.setColor(colors[(niv.carte[l][c]-10)%colors.length]);
    	    		    				for(int i=1; i<=4; i++) {
    	    	    	    				mPaint.setStrokeWidth((float) ((0.8-i*0.15)*cw));
    	    	    	    				can.drawPoint((float) (cw*(c+0.5)), (float) (ch*(l+0.7)), mPaint);
    	    		    				}
    	    		    				can.drawBitmap(rainbow, (int)(c*cw-cw/4), (int)(l*ch), null);
    	    		    			}
    	    		    		}
    	    		    	}
    	    	    	}
    	    	    }
    	    	};
    }
    
    // Méthode publique pour spécifier le niveau à afficher
    /**
     * Charge un niveau sur la carte
     * @param niveau le niveau à charger
     */
    public void loadNiveau(Niveau niveau) {
    	// Supprimer les "Animaux" du niveau précédent.
		removeAllViews();
		vaches.clear();
		chats.clear();
		explo.clear();
		rainbows.clear();
    	
		Animal.cw=cw;
		Animal.ch=ch;
		niv=niveau;
		addView(fond);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ww,wh);
	    fond.setLayoutParams(params);
	    
	    /*
	    Random ran = new Random();
	    for(int i=0; i<colors.length; i++) {
	    	colors[i] = Color.argb(0x40, 56+ran.nextInt(200), 56+ran.nextInt(200), 56+ran.nextInt(200));
	    }
	    */
    	n_dyna=0;
    	n_fleur=0;
    	index_dyna=0;
    	for(int l=0; l<LIG; l++) {
    		for(int c=0; c<COL; c++) {
    			if(niv.carte[l][c]==2 || niv.carte[l][c]==3)
    				n_fleur++;
    			else if(niv.carte[l][c]==4) {
    				index_dyna++;
    				View e = new View(this.getContext());
    		    	explo.addLast(e);
    		    	e.setBackgroundResource(R.drawable.explosion);
    		    	addView(e);
    		    	e.setVisibility(INVISIBLE);
    			} else if(niv.carte[l][c]>=10) {
    				int[] autre = rainbows.get(niv.carte[l][c]);
    				if(autre!=null) { // On ajoute la case correspondante à celle déjà enregistrée.
    					autre[2]=l;
    					autre[3]=c;
    				} else {
    					rainbows.put(niv.carte[l][c] , new int[] {l,c,0,0});
    				}
    			}
    		}
    	}
    	colibri = new Colibri(this.getContext(), niv.db_c, niv.db_l, 1, 1);
    	addView(colibri);
    	addView(sang);
    	sang.setVisibility(INVISIBLE);
    	// On crée les vaches et les chats
    	for (int[][] itin : niv.vaches) {
    		vaches.addLast(new Vache(this.getContext(), 1, 1, itin));
    		addView(vaches.getLast());
    	}
    	for (int[][] itin : niv.chats) {
    		chats.addLast(new Chat(this.getContext(), 1, 1.25, itin));
    		addView(chats.getLast());
    	}
    	addView(mort);
    	mort.setVisibility(INVISIBLE);
    	this.invalidate();
    }
    
    /**
     * Effectue l'animation de mort du colibri. :( (flaque de sang qui se répend + tête de mort qui s'envole)
     */
    public void animMort() {
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(3*cw/2), (int)(3*ch/2));
    	double[] pos = colibri.getPos();
		params.leftMargin = (int)(cw*pos[0]-cw/4);
	    params.topMargin = (int)(ch*pos[1]-ch/4);
	    mort.setLayoutParams(params);
	    mort.setVisibility(VISIBLE);
	    sang.setLayoutParams(params);
	    sang.setVisibility(VISIBLE);
    	mort.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.dead_anim));
    	sang.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.blood_anim));
    }
    
    /**
     * Effectue l'animation d'explosion d'un menhir par une dynamite.
     */
    public void animBoom(int l, int c) {
    	index_dyna--;
    	View e = explo.get(index_dyna);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(3*cw/2), (int)(3*ch/2));
		params.leftMargin = (int)(c*cw-cw/4);
	    params.topMargin = (int)(l*ch);
	    params.bottomMargin = (int)((LIG-l)*ch+3*ch/2);
	    e.setLayoutParams(params);
    	e.setVisibility(VISIBLE);
    	((AnimationDrawable) e.getBackground()).start();
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
		cw=((double)ww)/COL;
		ch=((double)wh)/LIG;
		menhir = Bitmap.createScaledBitmap(menhir0, (int)(5*cw/4), (int)(5*ch/4), true);
		fleur = Bitmap.createScaledBitmap(fleur0, (int)cw, (int)ch, true);
		fleurm = Bitmap.createScaledBitmap(fleurm0, (int)cw, (int)ch, true);
		dyna = Bitmap.createScaledBitmap(dyna0, (int)cw, (int)ch, true);
		menhir_rouge = Bitmap.createScaledBitmap(menhir_rouge0, (int)(5*cw/4), (int)(5*ch/4), true);
		rainbow = Bitmap.createScaledBitmap(rainbow0, (int)(3*cw/2), (int)ch, true);
    	this.invalidate();
	}
}
