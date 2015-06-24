package com.game.colibri;



import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;


/**
 * Classe gérant les déplacements des éléments (colibri, vaches, ...) selon les régles
 * et le rafraîchissement de l'écran.
 */
public class MoteurJeu {
	
	public int frame, total_frames;
	private Carte carte;
	public Niveau niv;
	private Jeu jeu;
	public boolean isRunning=false;
	private int dejaPasse=0;
	private int wait = 0;
	private int directionDyna = 0;
	private LinkedList <int[]> buf; // la file d'attente des touches
	private int[] lastMove=new int[] {0,0};
	private int[][] trace_diff; // Contient le différentiel de position lors des ACTION_MOVE.
	private static int SEUIL=15; // seuil de vitesse de glissement du doigt sur l'écran.
	public static int PERIODE=1000/25; // pour 25 frames par secondes
	public static final int MENHIR=1;
	public static final int FLEUR=2;
	public static final int FLEURM=3;
	public static final int DYNA=4;
	public static final int MENHIR_ROUGE=5; // Menhir sur lequel on déposerait une dynamite.
	public static final char VIDE=0;
	public static final int UP=1,RIGHT=2,LEFT=3,DOWN=4;
	
	private static final int LIG=12, COL=20; // Dimensions de la grille
	
	/**
	 * Handler de rafraîchissement
	 * Il appelle la méthode void move() qui elle même appelle moveHandler.sleep(PERIODE) pour boucler.
	 * Utiliser moveHandler.sleep(PERIODE) pour appeler le handler après PERIODE ms.
	 * Utiliser moveHandler.removeMessages(0) pour arrêter le cycle.
	 */
	
	public RefreshHandler moveHandler = new RefreshHandler();
	
	@SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==jeu.n_niv) { // Fin de l'animation de l'explosion. (on utilise le handler pour contourner l'abscence de listener pour AnimationDrawable)
				MoteurJeu.this.finExplosion(msg.arg1,msg.arg2);
			} else MoteurJeu.this.move();
		}
		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};
	
	/**
	 * Constructeur 
	 *		@param activ le jeu actif
	 * 		@param c   la carte 
	 */
	public MoteurJeu(Jeu activ, Carte c) {
		carte = c;
		jeu=activ;
		buf = new LinkedList<int[]>();
		trace_diff=new int[3][2];
		SEUIL = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, activ.getResources().getDisplayMetrics());
	}
	
	/**
	 *  Initialise les variables du moteur de jeu (buffer, niv, ...) : appelé après chaque appel de carte.loadNiveau
	 */
	public void init(boolean replay) {
		niv=carte.niv; // pour avoir une référence locale vers le niveau en cours et un nom moins long
		buf.clear();
		total_frames = replay ? total_frames+frame : 0;
		frame=0;
		wait=0;
	}
	
	/**
	 *  Commence le jeu 
	 */
	public void start() {
		carte.colibri.start();
		for(Vache v : carte.vaches) {
			v.start();
		}
		for(Chat c : carte.chats) {
			c.start();
		}
		isRunning=true;
		moveHandler.sleep(PERIODE);
	}
	
	/**
	 * Met le jeu sur pause
	 */
	public void pause() {
		isRunning=false;
		moveHandler.removeMessages(0);
		carte.colibri.stop();
		for(Vache v : carte.vaches) {
			v.stop();
		}
		for(Chat c : carte.chats) {
			c.stop();
		}
	}
	
	private void mort() {
		pause();
		carte.animMort();
		Animation a = carte.mort.getAnimation();
    	a.setAnimationListener(new AnimationListener() {
    		public void onAnimationStart(Animation an) {
    			
    		}
    		public void onAnimationRepeat(Animation an) {
    			
    		}
    		public void onAnimationEnd(Animation an) {
    			carte.mort.setVisibility(View.INVISIBLE);
    			jeu.mort();
    		}
    	});
	}
	
	/**
	 * Place dans le buffer les déplacements de solution du niveau. 
	 */
	public void solution() {
		for(int[] m : niv.solution) {
    		buf.addLast(m.clone());
    	}
	}
	
	/**
	 * Méthode appelée périodiquement par le handler moveHandler lorsque le jeu est en marche.
	 * C'est ici que s'effectue les déplacements des animaux.
	 */
	private void move() {
		frame++;
		jeu.updateMenuLateral();
		if (carte.colibri.mx==0 & carte.colibri.my==0) { // Le colibri est à l'arrêt
			if (buf.size()>0) {
				int[] mov=buf.getFirst();
				if(mov[2]!=0)
					carte.colibri.step=0;
				if(mov[2]<=frame) {
					buf.removeFirst();
					carte.colibri.setDirection(mov); // On effectue le prochain mouvement de la file.
					carte.colibri.setSpriteDirection(); // On choisit la direction de l'image.
					if(carte.n_dyna>0) removeMenhirRouge(mov); // On enlève si nécessaire le menhir rouge de sélection.
					if(mov[0]==mov[1]) { // <=> mov=={0,0} : pose une dynamite.
						System.out.println("DYNAMITE : "+frame);
						exploseMenhir();
					}
					lastMove=mov;
				}
			}
			else carte.colibri.step=0; // La vitesse est mise à 0. Dans le premier cas, la vitesse est conservée.
		} else { // Le colibri est en mouvement
			ramasser(); // On ramasse l'item potentiel
			int[] dir= carte.colibri.getDirection();
			int ml=dir[1] , mc=dir[0];
			int l=carte.colibri.getRow(); // ligne du colibri
			int c=carte.colibri.getCol(); //  colone du colibri
			// On détecte si l'on arrive contre un obstacle
			boolean outOfMap=l+ml<0 || l+ml>=LIG || c+mc<0 || c+mc>=COL;
			if(outOfMap || niv.carte[l+ml][c+mc]==MENHIR){
				carte.colibri.mx=0;
				carte.colibri.my=0;
				carte.colibri.setPos(c, l);
				System.out.println("Frame : "+frame+" Pos : "+l+","+c);
				if(!outOfMap && carte.n_dyna>0) {
					niv.carte[l+ml][c+mc]=MENHIR_ROUGE;
					carte.fond.invalidate();
				}
			}
			else
				carte.colibri.deplacer();
		}
		for(Vache v : carte.vaches) {
			v.deplacer();
			collisionVache(v);
		}
		for(Chat c : carte.chats) {
			c.deplacer();
			collisionChat(c);
		}
		if(isRunning) moveHandler.sleep(PERIODE);
	}
	
	/**
	 * Détecte s'il y a colision entre le colibri et une vache, et le cas échéant effectue les opérations nécessaires.
	 * @param va la vache dont il faut tester la position par rapport au colibri
	 */
	private void collisionVache(Vache va) {
		double[] c_co = carte.colibri.getPos();
		double cx=c_co[0],cy=c_co[1];
		double[] c_va = va.getPos();
		double vx0=c_va[0],vy0=c_va[1];
		if(Math.abs(vx0-cx)<1 && Math.abs(vy0-cy)<1) { // teste si colision
			double vx = vx0+va.mx*va.step, vy = vy0+va.my*va.step; // On calcule en fonction de la prochaine position de la vache.
			if(Math.min(Math.abs(vx-cx) , Math.abs(vy-cy)) > 0.94)
				return;
			// Choisit de quel côté de la vache il faut replacer le colibri
			int l=carte.colibri.getRow(), c=carte.colibri.getCol();
			if(niv.carte[l][c]>=10 && dejaPasse!=niv.carte[l][c] && !(l==va.getRow() && c==va.getCol())) // Pour éviter de se faire bloquer AVANT de passer dans un arc.
				return;
			if(carte.n_dyna>0) removeMenhirRouge(lastMove); // On enlève le menhir rouge mais on ne rafraîchit pas.
			boolean plutotHoriz=1-Math.abs(vx-cx) < 1-Math.abs(vy-cy);
			boolean clairementSurHoriz=plutotHoriz && 0.75 < Math.abs(vx-cx);
			boolean clairementSurVert=!plutotHoriz && 0.75 < Math.abs(vy-cy);
			boolean vaVite=carte.colibri.step>2*carte.colibri.v_max/3;
			if(!vaVite && plutotHoriz || vaVite && (clairementSurHoriz || carte.colibri.mx!=0) && !clairementSurVert) { // sur l'horizontale
				if(cx<vx) {
					if(c-1<0 || niv.carte[l][c-1]==MENHIR && carte.colibri.mx<1) cx=Math.max(vx0-1,c); // Détecte si le colibri est bloqué par un menhir ou un bord.
					else cx=vx0-1;
					carte.colibri.mx=Math.min(carte.colibri.mx, 0); // arrête le mouvement du colibri s'il est vers la vache
				}
				else {
					if(c+1>=COL || niv.carte[l][c+1]==MENHIR && carte.colibri.mx>-1) cx=Math.min(vx0+1,c);
					else cx=vx0+1;
					carte.colibri.mx=Math.max(carte.colibri.mx, 0);
				}
				carte.colibri.setPos(cx , cy);
				if(Math.abs(vx0-cx)<0.5) mort();
			} else { // sur la verticale
				if(cy<vy) {
					if(l-1<0 || niv.carte[l-1][c]==MENHIR && carte.colibri.my<1) cy=Math.max(vy0-1,l);
					else cy=vy0-1;
					carte.colibri.my=Math.min(carte.colibri.my, 0);
				}
				else {
					if(l+1>=LIG || niv.carte[l+1][c]==MENHIR && carte.colibri.my>-1) cy=Math.min(vy0+1,l);
					else cy=vy0+1;
					carte.colibri.my=Math.max(carte.colibri.my, 0);
				}
				carte.colibri.setPos(cx , cy);
				if(Math.abs(vy0-cy)<0.5) mort();
			}
			ramasser(); // On ramasse l'item potentiel
			int nl=carte.colibri.getRow(), nc=carte.colibri.getCol();
			// Si le colibri a été poussé sur une autre case, il faut changer le menhir rouge de sélection !
			if(carte.n_dyna>0 && carte.colibri.mx+carte.colibri.my==0) {
				int ml=lastMove[1], mc=lastMove[0];
				if(nl+ml>=0 && nl+ml<LIG && nc+mc>=0 && nc+mc<COL && niv.carte[nl+ml][nc+mc]==MENHIR) {
					niv.carte[nl+ml][nc+mc]=MENHIR_ROUGE;
				}
				if(nl!=l || nc!=c)
					carte.fond.invalidate();
			}
			System.out.println("Frame : "+frame+" Pos : "+carte.colibri.getRow()+","+carte.colibri.getCol()+" VACHE");
		}
	}
	
	/**
	 * Détecte s'il y a colision entre le colibri et un chat, et le cas échéant effectue les opérations nécessaires.
	 * @param va le chat dont il faut tester la position par rapport au colibri
	 */
	private void collisionChat(Chat va) {
		double[] c_co = carte.colibri.getPos();
		double cx=c_co[0],cy=c_co[1];
		double[] c_va = va.getPos();
		double vx=c_va[0],vy=c_va[1]+0.25; // Décalage de la queue de 0.25.
		if(Math.abs(vx-cx)<0.75 && Math.abs(vy-cy)<0.75) { // teste si colision
			mort();
		}
	}
	
	/**
	 * Vérifie si un item peut être ramassé sur la case (l,c) de la carte, et le ramasse le cas échéant. (fleur, dynamite)
	 * @param l ligne
	 * @param c colonne
	 */
	private void ramasser() {
		int l=carte.colibri.getRow(), c=carte.colibri.getCol();
		if(niv.carte[l][c]==FLEUR) {
			niv.carte[l][c]=VIDE;
			carte.n_fleur--;
			carte.fond.invalidate();
		} else if(niv.carte[l][c]==FLEURM) {
			niv.carte[l][c]=MENHIR;
			carte.n_fleur--;
			carte.fond.invalidate();
		} else if(niv.carte[l][c]==DYNA) {
			niv.carte[l][c]=VIDE;
			carte.n_dyna++;
			jeu.bout_dyna.setText(Integer.toString(carte.n_dyna));
			if(carte.n_dyna==1) jeu.showDyna();
			carte.fond.invalidate();
		} else if(niv.carte[l][c]>=10) { // Passage dans un arc-en-ciel.
			if(dejaPasse!=niv.carte[l][c]) { // Pour éviter de se téléporter dans l'autre sens.
				int[] dest = carte.rainbows.get(niv.carte[l][c]);
				double step = carte.colibri.step;
				if(dest[0]==l && dest[1]==c)
					carte.colibri.setPos(dest[3]-carte.colibri.mx*step, dest[2]-carte.colibri.my*step);
				else
					carte.colibri.setPos(dest[1]-carte.colibri.mx*step, dest[0]-carte.colibri.my*step);
			}
			dejaPasse=niv.carte[l][c];
		} else
			dejaPasse=0;
		if(carte.n_fleur==0) jeu.gagne((total_frames+frame)*PERIODE);
	}
	
	/**
	 * Enlève le menhir rouge de sélection si besoin.
	 */
	private void removeMenhirRouge(int[] mov) {
		int l=carte.colibri.getRow();
		int c=carte.colibri.getCol();
		int ml=lastMove[1], mc=lastMove[0];
		if(l+ml>=0 && l+ml<LIG && c+mc>=0 && c+mc<COL && niv.carte[l+ml][c+mc]==MENHIR_ROUGE) {
			niv.carte[l+ml][c+mc]=MENHIR;
			if(ml!=mov[1] || mc!=mov[0]) carte.fond.invalidate();
		}
	}
	
	/**
	 * S'il existe, explose le menhir en face du colibri.
	 */
	private void exploseMenhir() {
		int l=carte.colibri.getRow();
		int c=carte.colibri.getCol();
		int ml=lastMove[1], mc=lastMove[0];
		if(l+ml>=0 && l+ml<LIG && c+mc>=0 && c+mc<COL && niv.carte[l+ml][c+mc]==MENHIR && ml+mc!=0) {
			if(buf.size()!=0) {
				int[] next = buf.getFirst();
				if(next[0]==mc && next[1]==ml)
					next[2] = frame+25;
			} else {
				directionDyna = getDirection(mc,ml);
				wait = frame+25;
			}
			carte.n_dyna--;
			carte.animBoom(l+ml,c+mc); // Gère l'animation de l'explosion.
			jeu.bout_dyna.setText(Integer.toString(carte.n_dyna));
			if(carte.n_dyna==0) jeu.hideDyna();
			Message msg = moveHandler.obtainMessage(jeu.n_niv);
			msg.arg1=l+ml;
			msg.arg2=c+mc;
			moveHandler.sendMessageDelayed(msg,800);
		}
	}
	
	/**
	 * Appelé à la fin de l'explosion d'un menhir. Enlève le menhir de la carte et la View explo.
	 */
	private void finExplosion(int l, int c) {
		niv.carte[l][c]=VIDE;
		carte.fond.invalidate();
	}
	
	/**
	 * Appelée par l'activité Jeu. Prends en charge le toucher de l'écran tactile.
	 * @param ev le MotionEvent
	 * @return 0:cache le menu latéral ; 1:montre le menu latéral ; 2:ne fais rien
	 */
	public int onTouch(MotionEvent ev) {
		//carte.colibri.setPos((int) ev.getX(), (int) ev.getY()); // Petit exemple
		int x=(int) ev.getX();
		int y=(int) ev.getY();

		// Version commande au clic sur zones de l'écran
		/*if (ev.getActionMasked()==MotionEvent.ACTION_DOWN) {
			if (y*carte.ww<x*carte.wh) {
				if (y*carte.ww<(carte.ww-x)*carte.wh) direction(UP);
				else direction(RIGHT);
			}
			else {
				if (y*carte.ww<(carte.ww-x)*carte.wh) direction(LEFT);
				else direction(DOWN);
			}
		}*/
		
		// Version glissement à seuil de vitesse
		if (ev.getAction()==MotionEvent.ACTION_DOWN) {
			trace_diff[1][0]=x;
			trace_diff[1][1]=y;
			trace_diff[2][0]=x; // début du toucher
			trace_diff[2][1]=y;
			return 2;
		}
		else if (ev.getAction()==MotionEvent.ACTION_MOVE) {
			trace_diff[0][0]=trace_diff[1][0];
			trace_diff[0][1]=trace_diff[1][1];
			trace_diff[1][0]=x;
			trace_diff[1][1]=y;
			return swipe_dir();
		}
		else if (ev.getAction()==MotionEvent.ACTION_UP) {
			int mx=x-trace_diff[2][0] , my=y-trace_diff[2][1];
			if(mx*mx+my*my<SEUIL*SEUIL)
				return 1;
		}
		return 2;
	}
	
	/**
	 * Gère la commande du colibri par swipe sur l'écran tactile.
	 */
	private int swipe_dir() {
		int mx=trace_diff[1][0]-trace_diff[0][0] , my=trace_diff[1][1]-trace_diff[0][1];
		if(mx*mx+my*my>SEUIL*SEUIL) {
			if(Math.abs(mx)>Math.abs(my)) {
				if(mx>0) direction(RIGHT);
				else direction(LEFT);
			} else {
				if(my>0) direction(DOWN);
				else direction(UP);
			}
			return 0;
		}
		return 2;
	}
	
	/**
	 * Ajoute à la file la prochaine direction du colibri.
	 * @param dir
	 */
	public void direction(int dir) {
		int[] mvt;
		if(buf.size()==0) mvt=new int[3];
		else mvt=buf.getLast();
		System.out.println("direc : "+wait+" "+directionDyna+" "+dir);
		if(wait!=0 && dir!=directionDyna)
			wait=0;
		switch (dir) {
		case UP:
			if(mvt[1]!=-1) buf.add(new int[]{0,-1,wait});
			break;
		case RIGHT:
			if(mvt[0]!=1) buf.add(new int[]{1,0,wait});
			break;
		case LEFT:
			if(mvt[0]!=-1) buf.add(new int[]{-1,0,wait});
			break;
		case DOWN:
			if(mvt[1]!=1) buf.add(new int[]{0,1,wait});
			break;
		}
		wait=0;
	}
	
	/**
	 * Donne l'indice de direction d'un déplacement {mc,ml}. (Réciproque de direction(dir))
	 * @param ml -1,0,1 selon les lignes
	 * @param mc -1,0,1 selon les colonnes
	 * @return int : UP, RIGHT, LEFT, DOWN
	 */
	private int getDirection(int mc, int ml) {
		if(mc==0) {
			if(ml==1)
				return DOWN;
			else
				return UP;
		} else {
			if(mc==1)
				return RIGHT;
			else
				return LEFT;
		}
	}
	
	/**
	 * Ajoute à la file l'action de poser une dynamite.
	 */
	public void dynamite() {
		buf.add(new int[]{0,0,0});
	}
	
}
