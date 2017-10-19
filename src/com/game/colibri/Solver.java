package com.game.colibri;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import com.game.colibri.Niveau.Occurrence;

/**
 * Le solveur de niveau basé sur A* aussi utilisé pour les indications de jeu.
 * 
 * @author Louis
 *
 */
public class Solver {
	
	private static final boolean DEBUG = true;
	public final static int STEPS_VACHES = 20; // 20 ticks par case
	public final static int STEPS_CHATS = 4; // 4 ticks par case
	public final static int STEPS_COLIBRI_ACC = 4; // lorsque à l'arrêt, 2 ticks de switch delay + 4 ticks pour sortir d'une case
	public final static int STEPS_COLIBRI_FULL = 2; // lancé full speed
	public final static int DYNA_EXPL_TIME = 25; // Ticks avant fin explosion dynamite
	
	public boolean OPT_TIME = true; // Optimisation du temps (true) ou du nombre de coups (false)
	private Niveau niv; // Le niveau
	private int sol_r=-1, sol_c=-1; // La position pour laquelle a été calculée "solution".
	private Heuristic heuristic; // La structure de données pour un calcul rapide de l'heuristique.
	private Path solution; // La solution à partir de (sol_r,sol_c). Si aucune solution: null et sol_r et sol_c != -1
	private HashSet<String> closedSet;
	private PriorityQueue<State> openSet;
	
	/**
	 * Constructeur du Solveur pour le niveau niv.
	 * @param niv
	 */
	public Solver(Niveau niv) {
		this.niv = niv;
		heuristic = new Heuristic(niv);
	}
	
	/**
	 * Retourne la solution à partir de la position (r,c), ayant nFleurs à ramasser et
	 * nDynas en stock. Si elle est invalide, on la calcule.
	 * @param frame l'instant actuel en frames
	 * @param r
	 * @param c
	 * @param nFleurs le nombre de fleurs à ramasser
	 * @param nDynas le nombre de dynamites en stock
	 * @return solution null si impossible
	 */
	public Path getSolution(int frame, int r, int c, int nFleurs, int nDynas, boolean opt_time) {
		if(r==sol_r && c==sol_c && opt_time==OPT_TIME) {
			return solution;
		} else {
			solution = null;
			sol_r=r;
			sol_c=c;
			OPT_TIME = opt_time;
			heuristic.prepareRootState();
			findSolution(frame, r,c,nFleurs,nDynas);
			if(solution==null) {
				System.out.println("Pas de solution !");
				solution = new Path(0);
			}
			if(DEBUG) {
				System.out.println("direction, wait, travel, rf, cf");
				System.out.println("Temps : "+(solution.t_cumul - frame)+" ; Moves : "+solution.length);
			}
			return solution;
		}
	}
	
	/**
	 * Algorithme de recherche de solution basé sur A*.
	 * @param frame l'instant actuel en frames
	 * @param rd ligne de départ
	 * @param cd colonne de départ
	 * @param nFleurs
	 * @param nDynas
	 */
	private void findSolution(int frame, int rd, int cd, int nFleurs, int nDynas) {
		closedSet = new HashSet<String>(128); // Contient les hash des états déjà visités.
		openSet = new PriorityQueue<State>(); // Contient les états à la frontière.
		
		openSet.add(new State(frame, rd, cd, nFleurs, nDynas));
		int n = 0;
		while(!openSet.isEmpty()) {
			//System.out.println(openSet.size()+"  |  "+closedSet.size());
			//System.out.println(openSet);
			State current = openSet.poll();
			//System.out.println(current);
			/*
			if(n==100) {
				try {
					System.in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
			if(n == 800) { // Paramétrization de l'heuristique: approxime davantage si la recherche devient trop longue.
				heuristic.h_param += 2;
				if(heuristic.h_param < 14)
					n -= 400;
			}
			if(current.goal_reached) { // <=> h=0 : FIN !
				solution = current.path;
				System.out.println("Itérations : "+(n + heuristic.h_param/2*400)+"  |  h_param : "+heuristic.h_param+"  |  openSet : "+openSet.size()+"  |  closedSet : "+closedSet.size());
				return;
			}
			if(closedSet.add(current.hash)) { // Ajoute l'état dans le closedSet, si déjà présent, false.
				current.computeNeighbors();
				for(State s : current.voisins) {
					if(!closedSet.contains(s.hash)) {
						s.computeScores();
						openSet.add(s);
					}
				}
			}
			n++;
		}
	}
	
	
	// Structures
	
	private class State implements Comparable<State> {
		private State parent; // L'état parent, null si root.
		private Position pos; // Position du colibri dans cet état.
		private String hash; // Identifie un état : r,c+grid.hash
		private DynaGrid grid; // Grille du niveau avec les changements.
		private Path path; // Les déplacements jusqu'ici.
		private LinkedList<State> voisins; // Les prochaines destinations possibles
		private IntervalsModulo possibPresence; // Les intervalles possibles de présence sur cette case.
		private int f_score_t, f_score_m; // f_score: time & moves (f coût total départ->arrivée, g coût parcouru, h estimation heuristique : f=g+h)
		private boolean goal_reached = false; // true si cet état est final (h==0)
		
		/**
		 * Constructeur pour l'état racine, partant de la position (r,c).
		 * @param frame l'instant actuel en frames
		 * @param r
		 * @param c
		 * @param nFleurs
		 * @param nDynas
		 */
		public State(int frame, int r, int c, int nFleurs, int nDynas) {
			parent = null;
			path = new Path(frame);
			grid = new DynaGrid(nFleurs, nDynas);
			pos = new Position(r, c, 0, path.t_cumul, grid);
			voisins = new LinkedList<State>();
			possibPresence = grid.getFreeIntervals(pos, 0, STEPS_COLIBRI_ACC);
			computeHash();
			computeScores();
		}
		
		/**
		 * Constructeur pour un état voisin obtenu à partir de s et du mouvement m.
		 * @param s
		 * @param m
		 */
		public State(State s, Move m) {
			parent = s;
			path = new Path(s.path, m);
			grid = new DynaGrid(s.grid, s.pos, m, s.path.t_cumul);
			pos = new Position(m.posFinale.r, m.posFinale.c, m.step, path.t_cumul, grid);
			voisins = new LinkedList<State>();
			possibPresence = grid.getFreeIntervals(pos, 0, STEPS_COLIBRI_ACC);
			computeHash();
		}
		
		@Override
		public String toString() {
			return pos.r+","+pos.c+","+path.length+": f_t="+f_score_t+", f_m="+f_score_m;
		}
		
		/**
		 * Calcule le hash représentant cet état : position du colibri et état de la grille.
		 */
		private void computeHash() {
			hash = pos.r+","+pos.c+" "+grid.getHash(path.t_cumul);
		}
		
		/**
		 * Calcule l'heuristique et le score total.
		 */
		public void computeScores() {
			heuristic.compute(pos, grid);
			if(heuristic.h_m==0)
				goal_reached = true;
			f_score_t = path.t_cumul + heuristic.h_t;
			f_score_m = path.length + heuristic.h_m;
		}
		
		@Override
		public int compareTo(State s2) {
			if(OPT_TIME) {
				int df = f_score_t - s2.f_score_t;
				return df!=0 ? df : f_score_m - s2.f_score_m;
			} else {
				int df = f_score_m - s2.f_score_m;
				return df!=0 ? df : f_score_t - s2.f_score_t;
			}
		}
		
		/**
		 * Calcule et ajoute les voisins à la liste.
		 */
		public void computeNeighbors() {
			// Les quatre directions
			for(int i=1; i<=4; i++) {
				LinkedList<Move> pMoves = possibleMovesInDirection(i);
				for(Move m : pMoves) {
					voisins.add(new State(this, m));
				}
			}
		}
		
		private LinkedList<Move> possibleMovesInDirection(int dir) {
			LinkedList<Move> pMoves = new LinkedList<Move>();
			Position tempPos = new Position(pos);
			LinkedList<Niveau.Occurrence> cell = grid.obstacle(tempPos.step(dir)); // null=obstacle fixe, sinon, occurrences des vaches.
			// Dynamite
			if(cell==null) { // On est bloqué : si menhir, dynamite ?
				if(grid.getCell(tempPos)==1 && grid.nDynas>0)
					addMoveDynamite(pMoves, dir);
				return pMoves;
			}
			// Poussée de vache
			LinkedList<Niveau.Occurrence> occs = grid.getVachesPushingTo(pos,dir);
			if(!occs.isEmpty()) {
				addMovePushedByVache(pMoves, dir, occs, new IntervalsModulo(true));
			}
			// Déplacements
			grid.addCatsObstacles(tempPos, cell); // Pour ajouter les chats comme obstacles au déplacement
			tempPos.simul(cell, niv);
			cell = grid.obstacle(tempPos.step(dir));
			int steps = 0; // Pour détecter une boucle infinie entre deux arcs-en-ciel.
			while(cell!=null && steps < 2*Position.COL) {
				if(!cell.isEmpty() && cell.getFirst().mod!=Integer.MAX_VALUE) // Arrêt contre une vache (et non une dynamite qui va exploser)
					addMoveToVache(pMoves, tempPos.prev(dir), dir, cell, new IntervalsModulo(true));
				grid.addCatsObstacles(tempPos, cell);
				tempPos.simul(cell, niv);
				cell = grid.obstacle(tempPos.step(dir));
				steps++;
			}
			if(cell==null) // Arrêt contre menhir
				addMoveToMenhir(pMoves, tempPos.prev(dir), dir, new IntervalsModulo(true));
			return pMoves;
		}
		
		private void addMoveDynamite(LinkedList<Move> pMoves, int dir) {
			pMoves.add(new Move(10+dir, 0, (path.move.direction==dir ? 1 : 3), path.move.step, pos));
		}
		
		private void addMovePushedByVache(LinkedList<Move> pMoves, int dir, LinkedList<Niveau.Occurrence> occs, IntervalsModulo arrivVoulue) {
			Position dest = pos.next(dir);
			System.out.println("Poussée : "+pos.r+","+pos.c+","+dir);
			dest.t_travel[0] = STEPS_VACHES;
			dest.t_travel[1] = STEPS_VACHES;
			dest.step[0] = 0;
			IntervalsModulo possibIntervals = grid.getFreeIntervals(dest, STEPS_VACHES, STEPS_VACHES-2);
			possibIntervals.intersectArrivee(arrivVoulue, STEPS_VACHES, STEPS_VACHES);
			possibIntervals.intersectOccArrivee(occs, path.t_cumul, STEPS_VACHES, STEPS_VACHES);
			addMoveWithin(pMoves, dest, -dir, possibIntervals);
		}
		
		private void addMoveToVache(LinkedList<Move> pMoves, Position dest, int dir, LinkedList<Niveau.Occurrence> occs, IntervalsModulo arrivVoulue) {
			IntervalsModulo possibIntervals = dest.getDepartsIntervals(); // Instants où le départ est possible
			possibIntervals.intersectArrivee(arrivVoulue, dest.t_travel[0], dest.t_travel[1]); // Instant où le départ est possible ET l'arrivée voulue
			possibIntervals.intersectOccArrivee(occs, path.t_cumul, dest.t_travel[0], dest.t_travel[1]);
			addMoveWithin(pMoves, dest, dir, possibIntervals);
			//int k = Math.ceil((path.t_cumul+t_travel-v.r)/v.mod);
			//int wait = v.r + k*v.mod - path.t_cumul - t_travel;
		}
		
		private void addMoveToMenhir(LinkedList<Move> pMoves, Position dest, int dir, IntervalsModulo arrivVoulue) {
			//System.out.println("To Menhir : "+dest.r+","+dest.c);
			IntervalsModulo possibIntervals = dest.getDepartsIntervals(); // Instants où le départ est possible
			//System.out.println(possibIntervals.intervals);
			possibIntervals.intersectArrivee(arrivVoulue, dest.t_travel[0], dest.t_travel[1]); // Instant où le départ est possible ET l'arrivée voulue
			//System.out.println(possibIntervals.intervals);
			addMoveWithin(pMoves, dest, dir, possibIntervals);
		}
		
		private void addMoveWithin(LinkedList<Move> pMoves, Position dest, int dir, IntervalsModulo possibIntervals) {
			if(possibIntervals.isEmpty()) { // Voyage impossible...
				System.out.println("IMPOSSIBLE");
				return;
			}
			int depart = possibIntervals.getFirstPossib();
			if(depart > 0 && possibPresence.firstClose() - 2 < depart) { // attente impossible (+2 pour prendre en compte deux frames supplémentaires d'accélération après un wait)
				System.out.println("PARENT");
				if(parent==null)
					return;
				// Appel au parent pour venir plus tard
				parent.createDelayedMoveTo(path.move, possibPresence.getIntervalsForDepartureIn(possibIntervals));
			} else { // OK !
				int stoppedIndex = depart==0 ? 0 : 1;
				pMoves.add(new Move(dir, depart, dest.t_travel[stoppedIndex], dest.step[stoppedIndex], dest));
			}
		}
		
		/**
		 * Crée un état avec un départ compris dans les intervals spécifiés et l'ajoute
		 * aux voisins ET à l'open set. (Appelé par un fils.)
		 * @param dest destination (= pos du fils)
		 * @param dir la direction du mouvement
		 * @param arrivVoulue les intervals d'arrivée voulus
		 */
		protected void createDelayedMoveTo(Move m, IntervalsModulo arrivVoulue) {
			arrivVoulue.translate(m.wait+m.travel); // Pour replacer l'origine de cet IntervalsModulo au même que cet état
			LinkedList<Move> pMoves = new LinkedList<Move>();
			if(m.direction>10) { // Pose de dynamite
				parent.createDelayedMoveTo(path.move, arrivVoulue);
			} else if(m.direction<0) { // Poussée de vache
				LinkedList<Niveau.Occurrence> occs = grid.getVachesPushingTo(m.posFinale,-m.direction);
				addMovePushedByVache(pMoves, -m.direction, occs, arrivVoulue);
			} else {
				LinkedList<Niveau.Occurrence> cell = grid.obstacle(m.posFinale.next(m.direction));
				if(cell==null)
					addMoveToMenhir(pMoves, m.posFinale, m.direction, arrivVoulue);
				else
					addMoveToVache(pMoves, m.posFinale, m.direction, cell, arrivVoulue);
			}
			if(pMoves.isEmpty())
				return;
			State s = new State(this, pMoves.getFirst());
			s.hash += "d"+s.path.t_cumul; // Pour rendre cet état unique
			voisins.add(s);
			s.computeScores();
			openSet.add(s);
		}
	}
	
	private class DynaGrid {
		public TreeMap<Integer, Integer> changes; // Les cases changées à r*col+c
		public char[] flowersBySegment; // Dénote le nombre de fleurs non ramassées par segment de heuristic.minSegments
		public int nFleurs, nDynas; // Fleurs restantes et dynamites en stock
		
		/**
		 * Constructeur de la grille d'origine correspondant au point de départ.
		 * @param nF
		 * @param nD
		 */
		public DynaGrid(int nF, int nD) {
			nFleurs = nF;
			nDynas = nD;
			changes = new TreeMap<Integer, Integer>();
			flowersBySegment = new char[heuristic.minSegments.size()];
			int i = 0;
			for(Heuristic.Segment s : heuristic.minSegments) {
				flowersBySegment[i++] = s.nFleurs;
			}
		}

		/**
		 * Constructeur d'une grille copie de dg suivie d'un mouvement m.
		 * @param dg
		 * @param m
		 */
		public DynaGrid(DynaGrid dg, Position p_init, Move m, int t_cumul) {
			nFleurs = dg.nFleurs;
			nDynas = dg.nDynas;
			changes = new TreeMap<Integer, Integer>(dg.changes);
			flowersBySegment = dg.flowersBySegment.clone();
			performMove(p_init, m, t_cumul);
		}
		
		/**
		 * Effectue le mouvement m sur la carte courante et répercute les changements
		 * dans change, nFleurs et nDynas
		 * @param m
		 */
		private void performMove(Position p_init, Move m, int t_cumul) {
			Position p = new Position(p_init);
			int cell;
			while(p.r!=m.posFinale.r || p.c!=m.posFinale.c) {
				p.step(m.direction);
				cell = getCell(p);
				if(cell==2 || cell==3) { // fleur ou fleur magique
					int index = p.r*Position.COL + p.c;
					changes.put(index, cell - 2);
					nFleurs--;
					flowersBySegment[heuristic.fleurToSegment.get(index)]--;
				} else if(cell==4) { // Dynamite
					changes.put(p.r*Position.COL + p.c, 0);
					nDynas++;
				} else if(cell>=10) { // Arc-en-ciel
					int[] dest = niv.rainbows.get(cell);
					if(dest[0]==p.r && dest[1]==p.c) {
						p.r=dest[2]; p.c=dest[3];
					} else {
						p.r=dest[0]; p.c=dest[1];
					}
				}
			}
			if(m.direction > 10) { // Pose de dynamite
				p.step(m.direction - 10);
				changes.put(p.r*Position.COL + p.c, -t_cumul);
				nDynas--;
			}
		}
		
		/**
		 * Computes and return the string representation of the changes.
		 */
		public String getHash(int t_cumul) {
			String hash = "";
			for(Entry<Integer,Integer> pair : changes.entrySet()) {
				int v = pair.getValue();
				if(v<0) {
					if(-v+DYNA_EXPL_TIME < t_cumul) { // Menhir déjà explosé
						changes.put(pair.getKey(), 0);
						continue;
					}
				}
				hash += pair.getKey()+"="+v+";";
			}
			return hash;
		}
		
		/**
		 * Retourne le contenu de la cellule à la position pos, prenant en compte
		 * les transformations effectuées à la carte.
		 * @param pos la position
		 * @return contenu de la case
		 */
		public int getCell(Position pos) {
			if(pos.isOut())
				return -1;
			Integer cell = changes.get(pos.r*Position.COL + pos.c);
			return cell==null ? niv.carte[pos.r][pos.c] : cell;
		}
		
		/**
		 * Retourne les intervalles où la case est non occupée, en rétractant les intervalles de Delta.
		 * @param pos la position sur la grille
		 * @param delta le temps de présence de l'animal en question (colibri) à prendre en compte
		 * @return IntervalsModulo correspondant
		 */
		public IntervalsModulo getFreeIntervals(Position pos, int shift, int delta) {
			LinkedList<Niveau.Occurrence> occs = new LinkedList<Niveau.Occurrence>();
			LinkedList<Niveau.Occurrence> vaches = niv.passVaches.getOccurrences(pos.r, pos.c);
			if(vaches!=null)
				occs.addAll(vaches);
			LinkedList<Niveau.Occurrence> chats = niv.passChats.getOccurrences(pos.r, pos.c);
			if(chats!=null)
				occs.addAll(chats);
			IntervalsModulo im = new IntervalsModulo(true);
			im.addOccurrences(occs, pos.t_cumul+shift, delta, false);
			return im;
		}
		
		/**
		 * Retourne l'obstacle présent à la position pos : null pour un obstacle
		 * fixe (menhir ou bord) ; la liste des occurrences des vaches sinon.
		 * @param pos la position dans la grille
		 * @return liste d'occurrences ou null
		 */
		public LinkedList<Niveau.Occurrence> obstacle(Position pos) {
			int cell = getCell(pos);
			if(pos.isOut() || cell==1) { // Obstacle fixe : menhir ou bord
				return null;
			} else if(cell < 0) { // Dynamite posée à t = -cell
				LinkedList<Niveau.Occurrence> dyna_ex = new LinkedList<Niveau.Occurrence>();
				dyna_ex.add(new Niveau.Occurrence(-cell, 0, DYNA_EXPL_TIME));
				return dyna_ex;
			} else { // Occurrences de vaches (possiblement vides)
				LinkedList<Niveau.Occurrence> occs = niv.passVaches.getOccurrences(pos.r,  pos.c);
				return occs==null ? new LinkedList<Niveau.Occurrence>() : occs;
			}
		}
		
		/**
		 * Ajoute les occurrences des chats aux occurrences de cell.
		 * @param pos la position sur la carte
		 * @param cell les occurrences sur cette case auquels ajouter les chats
		 */
		public void addCatsObstacles(Position pos, LinkedList<Occurrence> cell) {
			LinkedList<Niveau.Occurrence> chats = niv.passChats.getOccurrences(pos.r, pos.c);
			if(chats!=null)
				cell.addAll(chats);
		}
		
		/**
		 * Retourne l'ensemble des occurrences de vaches à pos poussant
		 * (en entrant sur la case) dans la direction dir.
		 * @param pos la position
		 * @param dir la direction de poussée
		 * @return les occurrences
		 */
		public LinkedList<Niveau.Occurrence> getVachesPushingTo(Position pos, int dir) {
			LinkedList<Niveau.Occurrence> vaches = niv.passVaches.getOccurrences(pos.r, pos.c);
			if(vaches==null)
				return new LinkedList<Niveau.Occurrence>();
			LinkedList<Niveau.Occurrence> res = new LinkedList<Niveau.Occurrence>();
			for(Niveau.Occurrence v : vaches) {
				if(v.dir_in==dir)
					res.add(v);
			}
			return res;
		}
	}
	
	public static class Path {
		public Move move;
		public Path prevPath;
		public int length;
		public int t_cumul;
		
		/**
		 * Constructeur de chemin initial, vide.
		 * @param frame l'instant actuel en frames
		 */
		public Path(int frames) {
			move = null;
			prevPath = null;
			length = 0;
			t_cumul = frames;
		}
		
		/**
		 * Constructeur d'un chemin copie de p, suivi du mouvement m.
		 * @param p
		 * @param m
		 */
		public Path(Path p, Move m) {
			move = m;
			prevPath = p;
			length = p.length + 1;
			t_cumul = p.t_cumul + m.wait + m.travel;
		}
		
		/**
		 * Retourne une liste de mouvements interprétables par le jeu.
		 * (dx, dy, wait)
		 * @param frame l'instant actuel en frames
		 * @return
		 */
		public LinkedList<int[]> getGamesMoves() {
			if(move==null)
				return new LinkedList<int[]>();
			else {
				LinkedList<int[]> gMoves = prevPath.getGamesMoves();
				int prevDir = prevPath.length==0 ? -1 : prevPath.move.direction;
				if(move.direction>=10 && prevDir!=move.direction-10) { // move supplémentaire en cas de dyna pour se diriger vers le bon menhir.
					move.direction-=10;
					gMoves.add(move.getGameMove(prevPath.t_cumul));
					move.direction+=10;
				}
				gMoves.add(move.getGameMove(prevPath.t_cumul));
				if(DEBUG)
					System.out.println(move.toString()+" : end_frame="+t_cumul);
				return gMoves;
			}
		}
		
		@Override
		public String toString() {
			return length > 0 ? prevPath.toString()+move.toString() : "";
		}
	}
	
	public static class Move {
		public static final int UP=1,RIGHT=2,LEFT=3,DOWN=4;
		
		public int direction;
		public int wait;
		public int travel;
		public double step;
		public Position posFinale; // La position d'arrivée, contenant notamment les departsPossibles
		
		public Move(int d, int w, int t, double s, Position pf) {
			direction = d;
			wait = w;
			travel = t;
			step = s;
			posFinale = new Position(pf);
		}
		
		/**
		 * Retourne un triplet correspondant à un move dans le jeu.
		 * @param t_cumul le temps cumulé jusqu'à ce move
		 * @return le triplet correspondant (dx, dy, wait_absolute)
		 */
		public int[] getGameMove(int t_cumul) {
			return new int[] {
					direction==LEFT ? -1 : (direction==RIGHT ? 1 : 0),
					direction==UP ? -1 : (direction==DOWN ? 1 : 0),
					wait==0 ? 0 : t_cumul + wait
			};
		}
		
		@Override
		public String toString() {
			return direction+" "+wait+" "+travel+" "+posFinale.r+" "+posFinale.c+":\n"+posFinale.departsPossibles.intervals+"\n";
		}
	}
	
	public static class Position {
		public static final int ROW=12, COL=20;
		private static final double v_max = 0.7501;
		private static final double acc = 0.1;
		
		public int r;
		public int c;
		
		/* Variables de simulation pour déterminer le temps de déplacement dans le cas
		   vitesse conservée (0) ou vitesse réinitialisée (1) */
		private double[] dep; // Le déplacement effectué dans le moteur physique
		private double[] step; // La vitesse (le pas par itéreation)
		private int[] t_travel; // Le nombre de frames écoulées pour effectuer le déplacement
		private int t_cumul;
		private IntervalsModulo departsPossibles; // Les intervals de départ possibles pour faire ce déplacement
		
		private DynaGrid grid; // La grille sur laquelle évolue la position
		
		public Position(int r, int c, double currentStep, int t_cumul, DynaGrid grid) {
			this.r = r;
			this.c = c;
			dep = new double[] {0.5, 0.5};
			step = new double[] {currentStep, 0};
			t_travel = new int[] {2, 1}; // Init : temps de commutation entre deux coups.
			this.t_cumul = t_cumul;
			departsPossibles = new IntervalsModulo(true);
			this.grid = grid;
		}
		
		public Position(Position p) {
			r = p.r;
			c = p.c;
			dep = p.dep.clone();
			step = p.step.clone();
			t_travel = p.t_travel.clone();
			t_cumul = p.t_cumul;
			departsPossibles = new IntervalsModulo(p.departsPossibles);
			grid = p.grid;
		}
		
		public boolean isOut() {
			return r<0 || r>ROW-1 || c<0 || c>COL-1;
		}
		
		public IntervalsModulo getDepartsIntervals() {
			return new IntervalsModulo(departsPossibles);
		}
		
		/**
		 * Simule un pas dans le moteur physique pour déterminer le temps de voyage.
		 * @param occs les occurrences de la prochaine case à ajouter dans departsPossibles
		 * @param niv référence vers le niveau en cours pour avoir les correspondances d'arc-en-ciel
		 */
		public void simul(LinkedList<Niveau.Occurrence> occs, Niveau niv) {
			//System.out.println("Step on "+r+","+c);
			// Effectue le pas
			int [] t_travel = {0, 0};
			for(int i=0; i<2; i++) {
				int n = (int) dep[i];
				do {
					step[i] = Math.min(step[i]+acc, v_max);
					dep[i] += step[i];
					t_travel[i]++;
				} while(n == (int) dep[i]);
			}
			int cell = grid.getCell(this);
			if(cell>=10) { // Arc-en-ciel
				//System.out.println(r+","+c+" : "+cell);
				//System.out.println(grid.changes);
				int[] dest = niv.rainbows.get(cell);
				if(dest[0]==r && dest[1]==c) {
					r=dest[2]; c=dest[3];
				} else {
					r=dest[0]; c=dest[1];
				}
				t_travel[0]++;
				t_travel[1]++;
				dep[0] = 0.5;
				dep[1] = 0.5;
			}
			// Ajoute les occurrences de la prochaine case aux departsPossibles
			departsPossibles.addOccurrences(occs, t_cumul+this.t_travel[1], (int)Math.ceil(1/step[1]/2), false);
			departsPossibles.evaluateT0(occs, t_cumul+this.t_travel[0], (int)Math.ceil(1/step[0]/2), false);
			this.t_travel[0] += t_travel[0];
			this.t_travel[1] += t_travel[1];
		}
		
		/**
		 * Fait progresser la position d'un pas vers la direction dir
		 * et retourne l'objet modifié.
		 * @param dir la direction
		 * @return this après modification
		 */
		private Position step(int dir) {
			switch(dir) {
			case Move.UP:
				r--;
				break;
			case Move.RIGHT:
				c++;
				break;
			case Move.LEFT:
				c--;
				break;
			case Move.DOWN:
				r++;
			}
			return this;
		}
		
		/**
		 * Nouvel objet Position après un pas dans la direction dir.
		 * @param dir la direction
		 * @return nouvelle position
		 */
		public Position next(int dir) {
			return (new Position(this)).step(dir);
		}
		
		/**
		 * Nouvel objet Position après un pas dans la direction opposée à dir.
		 * @param dir la direction
		 * @return nouvelle position
		 */
		public Position prev(int dir) {
			return (new Position(this)).step(5-dir);
		}
	}
	
	public static class Bound implements Comparable<Bound> {
		public int value; // The value of the bound
		public boolean open; // true if [value, ...[ ; false if [..., value[
		
		public Bound(int v, boolean o) {
			value = v;
			open = o;
		}
		
		@Override
		public int compareTo(Bound b) {
			return value - b.value;
		}
		
		public boolean equals(Bound b) {
			return value == b.value;
		}
		
		@Override
		public String toString() {
			return value+" "+open;
		}
	}
	
	public static class IntervalsModulo {
		public int mod;
		public TreeSet<Bound> intervals;
		public boolean state_t0; // Cas particulier pour t=0 puisque la vitesse est conservée
		public boolean empty_state; // L'état lorsque vide.
		
		/**
		 * Crée un nouvel interval plein ou vide selon la valeur de full.
		 * @param full true pour plein, false pour vide
		 */
		public IntervalsModulo(boolean full) {
			mod = 1;
			intervals = new TreeSet<Bound>();
			state_t0 = full;
			empty_state = full;
		}
		
		/**
		 * Crée une copie de im.
		 * @param im
		 */
		public IntervalsModulo(IntervalsModulo im) {
			mod = im.mod;
			intervals = new TreeSet<Bound>(im.intervals);
			state_t0 = im.state_t0;
			empty_state = im.empty_state;
		}

		private int lcm(int a, int b) {
			return a * (b / gcd(a, b));
		}
		
		private int gcd(int a, int b) {
			return b==0 ? a : gcd(b, a%b);
		}
		
		private int modulo(int v) {
			return mod==Integer.MAX_VALUE ? v : (v%mod + mod)%mod;
		}
		
		/**
		 * Retourne l'instant de la première borne fermante.
		 * @return
		 */
		public int firstClose() {
			try {
				Bound b = intervals.first();
				if(b.open)
					return 0;
				else
					return b.value;
			} catch(NoSuchElementException e) {
				return !empty_state ? 0 : Integer.MAX_VALUE;
			}
		}

		/**
		 * Retourne le premier instant possible.
		 * @return
		 */
		public int getFirstPossib() {
			if(state_t0)
				return 0;
			try {
				Bound b = intervals.first();
				if(b.open)
					return b.value;
				else if(b.value > 1)
					return 1;
				else
					return intervals.higher(b).value;
			} catch(NoSuchElementException e) {
				return empty_state ? 1 : Integer.MAX_VALUE;
			}
		}
		
		/**
		 * Retourne l'état de l'instant t selon l'IntervalsModulo.
		 * @param t un instant par rapport à la même origine (le 0).
		 * @return
		 */
		public boolean isInstantValid(int t) {
			if(intervals.isEmpty())
				return empty_state;
			Bound b = new Bound(modulo(t), true);
			Bound b_prec = intervals.floor(b);
			return b_prec==null ? !intervals.ceiling(b).open : b_prec.open;
		}
		
		public boolean isFull() {
			return intervals.isEmpty() && empty_state;
		}
		
		public boolean isEmpty() {
			return intervals.isEmpty() && !empty_state;
		}
		
		public void setFullWithState(boolean state) {
			intervals.clear();
			empty_state = state;
			mod = 1;
		}
		
		/**
		 * Étend le mod de cet IntervalsModulo et duplique les entrées pour recouvrir
		 * le nouveau domaine.
		 * @param newMod nouvelle valeur de mod. Doit être un multiple de mod.
		 */
		public void extendTo(int newMod) {
			if(newMod==mod)
				return;
			Bound[] bounds = intervals.toArray(new Bound[0]);
			for(Bound b : bounds) {
				if(b.value>=mod)
					break;
				for(int p=mod; p<newMod; p+=mod) {
					intervals.add(new Bound(b.value+p, b.open));
				}
			}
			mod = newMod;
		}
		
		/**
		 * Translate l'IntervalsModulo de delta_t.
		 * @param delta_t le temps à translater (positif ou négatif)
		 */
		public void translate(int delta_t) {
			TreeSet<Bound> newIntervals = new TreeSet<Bound>();
			for(Bound b : intervals) {
				newIntervals.add(new Bound(modulo(b.value + delta_t), b.open));
			}
			intervals = newIntervals;
		}
		
		/**
		 * Évalue l'état du départ à t=0 (sans perte de vitesse). S'il y a collision
		 * avec l'une des Occurrences dans occs à t_instant avec un temps de présence
		 * delta, state_t0 est mis à possib.
		 * @param occs
		 * @param t_instant
		 * @param delta
		 * @param possib
		 */
		public void evaluateT0(LinkedList<Niveau.Occurrence> occs, int t_instant, int delta, boolean possib) {
			if(state_t0==possib)
				return;
			Niveau.Occurrence coli = new Niveau.Occurrence(t_instant, 0, delta);
			for(Niveau.Occurrence o : occs) {
				if(coli.isColidingWith(o)) {
					state_t0 = possib;
					break;
				}
			}
		}
		
		/**
		 * Insère l'état possib pour le temps des occurrences o, rapportées à l'instant
		 * t_instant et élargies de delta.
		 * @param occs
		 * @param t_instant
		 * @param delta
		 * @param possib
		 */
		public void addOccurrences(LinkedList<Niveau.Occurrence> occs, int t_instant, int delta, boolean possib) {
			for(Niveau.Occurrence o : occs) {
				//System.out.println(t_instant+" "+o.r+", "+o.delta+"("+delta+"), "+o.mod+", "+possib+" : "+this.intervals);
				addOccurrence(o, t_instant, delta, possib);
				//System.out.println(this.intervals);
			}
		}
		
		/**
		 * Insère l'état possib pour le temps de l'occurrence o, rapportée à l'instant
		 * t_instant et élargie de delta.
		 * @param o
		 * @param t_instant
		 * @param delta
		 * @param possib
		 */
		public void addOccurrence(Niveau.Occurrence o, int t_instant, int delta, boolean possib) {
			//if(o.delta+delta==0) // Ensemble vide
			//	return;
			if(o.mod==Integer.MAX_VALUE) { // Occurrence sporadique et non périodique => on multiplie la période par 2
				int end = o.r + o.delta - t_instant;
				if(end > 0) { // Pas dépassée
					if(mod==1) // Intervalle vide
						mod = o.mod;
					else if(mod!=Integer.MAX_VALUE)
						extendTo(2*mod);
					addInterval(0, end, possib);
				}
				return;
			}
			if(mod==Integer.MAX_VALUE) { // Si l'IntervalsModulo ne contenait qu'une intervalle sporadique.
				mod = 2*o.mod;
			} else if(mod%o.mod!=0) { // On étend la période de l'IntervalsModulo au PPCM
				extendTo(lcm(mod, o.mod));
			}
			if(2*(o.delta+delta)>=mod) { // Ensemble complet
				setFullWithState(possib);
				return;
			}
			for(int t=o.r; t<mod; t+=o.mod) {
				addInterval(t-t_instant - o.delta-delta, t-t_instant + o.delta+delta, possib);
			}
		}
		
		public void addInterval(int open, int close, boolean state) {
			Bound b0 = new Bound(modulo(open), state);
			Bound b1 = new Bound(modulo(close), !state);
			//System.out.println("b0:"+b0.value+" "+state+" b1:"+b1.value+" ; mod:"+mod);
			//System.out.println(intervals);
			// Borne ouvrante
			addOpenBound(b0);
			//System.out.println(intervals);
			// Borne fermante
			addCloseBound(b1);
			//System.out.println(intervals);
			// Supprime les bornes entre les deux
			cleanInBetween(b0,b1,state);
			//System.out.println(intervals);
		}
		
		private void cleanInBetween(Bound b0, Bound b1, boolean stateIfFull) {
			if(b0.compareTo(b1)<0) { // Dans le bon ordre
				Bound b = intervals.higher(b0);
				while(b!=null && b.compareTo(b1)<0) {
					intervals.remove(b);
					b = intervals.higher(b0);
				}
			} else { // Aux extrémités
				Bound b;
				try {
					b = intervals.first();
					while(b.compareTo(b1)<0) {
						intervals.remove(b);
						b = intervals.first();
					}
					b = intervals.last();
					while(b0.compareTo(b)<0) {
						intervals.remove(b);
						b = intervals.last();
					}
				} catch(NoSuchElementException e) {
					setFullWithState(stateIfFull);
				}
			}
		}

		private void addOpenBound(Bound b) {
			if(intervals.isEmpty()) {
				intervals.add(b);
				return;
			}
			Bound prev = intervals.floor(b);
			if(prev==null) prev = intervals.last();
			// prev est la borne précédente ou égale modulo mod
			if(prev.equals(b)) { // Borne égale
				if(prev.open!=b.open)
					intervals.remove(prev);
			} else if(prev.open!=b.open) { // Borne strictement inférieure
				intervals.add(b);
			}
		}
		
		private void addCloseBound(Bound b) {
			if(intervals.isEmpty()) {
				intervals.add(b);
				return;
			}
			Bound next = intervals.ceiling(b);
			if(next==null) next = intervals.first();
			// next est la borne supérieure ou égale modulo mod
			if(next.equals(b)) { // Borne égale
				if(next.open!=b.open)
					intervals.remove(next);
			} else if(next.open!=b.open) { // Borne strictement supérieure
				intervals.add(b);
			}
		}
		/*
		private void addOpenBound(Bound b) {
			Bound prev = intervals.floor(b);
			if(prev==null) { // Pas de borne inférieure ou égale
				prev = intervals.ceiling(b); // next
				if(prev==null || prev.open==b.open) // Ensemble vide ou ouverture avant le prochain
					intervals.add(b);
			} else if(prev.equals(b)) { // La borne existe déjà
				if(prev.open!=b.open)
					intervals.remove(prev);
			} else { // Il y a une borne strictement inférieure
				if(prev.open!=b.open)
					intervals.add(b);
			}
		}
		
		private void addCloseBound(Bound b) {
			Bound next = intervals.ceiling(b);
			if(next==null) { // Pas de borne supérieure ou égale
				next = intervals.floor(b); // prev
				if(next==null) { // Intervals vide
					setFullWithState(!b.open);
				} else if(next.open!=b.open) // Fermeture après avant le précédent
					intervals.add(b);
			} else if(next.equals(b)) { // La borne existe déjà
				if(next.open!=b.open)
					intervals.remove(next);
			} else { // Il y a une borne strictement supérieure
				if(next.open!=b.open) {
					intervals.add(b);
				}
			}
		}
		*/
		/**
		 * Intersection de this.intervals et im.intervals en étendant le modulo au
		 * ppcm des deux.
		 * @param im l'IntervalsModulo à intersecter. ATTENTION: il est aussi affecté.
		 */
		public void intersect(IntervalsModulo im) {
			if(im.intervals.isEmpty()) {
				if(!im.empty_state) {
					intervals.clear();
					empty_state = false;
				}
				return;
			} else if(intervals.isEmpty()) {
				if(empty_state) {
					intervals.addAll(im.intervals);
					mod = im.mod;
				}
				return;
			}
			if(mod==Integer.MAX_VALUE) {
				if(im.mod!=Integer.MAX_VALUE) {
					im.extendTo(2*im.mod);
				}
			} else if(im.mod==Integer.MAX_VALUE) {
				if(mod!=Integer.MAX_VALUE) {
					extendTo(2*mod);
				}
			} else { // Les deux intervalles ne sont pas sporadiques.
				int ppcm = lcm(im.mod, mod);
				extendTo(ppcm);
				im.extendTo(ppcm);
			}
			int openCount = 0; // +1 quand open, -1 quand close.
			if(!intervals.first().open) openCount++; // Si première Bound close
			if(!im.intervals.first().open) openCount++; // Idem
			intervals.addAll(im.intervals); // On ajoute toutes les bornes à un même interval
			TreeSet<Bound> inter = new TreeSet<Bound>();
			for(Bound b : intervals) {
				if(b.open) {
					openCount++;
					if(openCount>=2)
						inter.add(b);
				} else {
					if(openCount>=2)
						inter.add(b);
					openCount--;
				}
			}
			intervals = inter;
			if(intervals.isEmpty())
				empty_state = false;
		}
		
		/**
		 * Intersectionne arrivVoulue avec this.intervals en translatant arrivVoulue
		 * de t_travel dans le cas t==0 et t!=0.
		 * @param arrivVoulue intervals voulus après t_travel ATTENTION: affecté par la fonction
		 * @param t_travelVitesseCons t_travel dans le cas départ à t==0
		 * @param t_travelVitesse0 t_travel dans le cas départ à t!=0
		 */
		public void intersectArrivee(IntervalsModulo arrivVoulue, int t_travelVitesseCons, int t_travelVitesse0) {
			if(arrivVoulue.isFull())
				return;
			if(!arrivVoulue.isInstantValid(t_travelVitesseCons))
				state_t0 = false;
			arrivVoulue.translate(-t_travelVitesse0);
			intersect(arrivVoulue);
		}
		
		/**
		 * Intersecte l'IntervalsModulo avec les instants de présence des occs.
		 * @param occs
		 * @param t_instant instant de référence de l'IntervalModulo
		 * @param t_travelVitesseCons t_travel dans le cas départ à t==0
		 * @param t_travelVitesse0 t_travel dans le cas départ à t!=0
		 */
		public void intersectOccArrivee(LinkedList<Niveau.Occurrence> occs, int t_instant, int t_travelVitesseCons, int t_travelVitesse0) {
			IntervalsModulo occsIntervals = new IntervalsModulo(false);
			occsIntervals.addOccurrences(occs, t_instant + t_travelVitesse0, -3*STEPS_VACHES/4, true);
			// Cas t==0 (conservation vitesse)
			if(state_t0 && !occsIntervals.isInstantValid(t_travelVitesseCons - t_travelVitesse0))
				state_t0 = false;
			intersect(occsIntervals);
		}
		
		/**
		 * Crée un IntervalsModulo décrivant les instants d'arrivée voulus sur cette case,
		 * permettant de partir dans possibDeparture.
		 * Dans ce contexte, this décrit les intervalles de présence possibles sur la case,
		 * et possibDeparture décrit les intervalles de départ possibles.
		 * Le résultat est inclu dans this, mais chaque intervalle doit être temporellement
		 * connexe à un intervalle de possibDeparture.
		 * @param possibDeparture les instants de départ possibles
		 * @return les instants d'arrivée voulus sur la case courante
		 */
		public IntervalsModulo getIntervalsForDepartureIn(IntervalsModulo possibDeparture) {
			if(possibDeparture.intervals.isEmpty() || this.intervals.isEmpty()) {
				if(!possibDeparture.empty_state)
					return new IntervalsModulo(false); // Si pas de départ possible, vide.
				else
					return new IntervalsModulo(this); // Si l'un ou l'autre plein, possibPresence
			}
			IntervalsModulo arrivVoulues = new IntervalsModulo(false);
			IntervalsModulo possibPresence = new IntervalsModulo(this);
			IntervalsModulo possibDep = new IntervalsModulo(possibDeparture);
			int ppcm = Integer.MAX_VALUE; // Définition de la nouvelle période commune
			if(possibDep.mod==Integer.MAX_VALUE) {
				if(possibPresence.mod!=Integer.MAX_VALUE) {
					ppcm = 2*possibPresence.mod;
					possibPresence.extendTo(ppcm);
				}
			} else if(possibPresence.mod==Integer.MAX_VALUE) {
				if(possibDep.mod!=Integer.MAX_VALUE) {
					ppcm = 2*possibDep.mod;
					possibDep.extendTo(ppcm);
				}
			} else { // Les deux intervalles ne sont pas sporadiques.
				ppcm = lcm(possibPresence.mod, possibDep.mod);
				possibPresence.extendTo(ppcm);
				possibDep.extendTo(ppcm);
			}
			arrivVoulues.mod = ppcm;
			for(Bound b : possibPresence.intervals) {
				if(!b.open) { // Fin présence
					Bound openingPres = possibPresence.intervals.lower(b);
					Bound precBoundDep = possibDep.intervals.lower(b);
					if(openingPres==null)
						openingPres = possibPresence.intervals.last();
					if(precBoundDep==null)
						precBoundDep = possibDep.intervals.last();
					if(precBoundDep.open) { // On ajoute l'intervalle de présence entier
						arrivVoulues.intervals.add(openingPres);
						arrivVoulues.intervals.add(b);
					} else {
						Bound precBoundPres = possibPresence.intervals.lower(precBoundDep);
						if(precBoundPres==null)
							precBoundPres = possibPresence.intervals.last();
						if(precBoundPres==openingPres) { // On ajoute l'intervalle de présence limité à la borne de fin de départ possible.
							arrivVoulues.intervals.add(openingPres);
							arrivVoulues.intervals.add(precBoundDep);
						}
					}
				}
			}
			return arrivVoulues;
		}
	}
	
	public static class Heuristic {
		private Niveau niv;
		public int h_param = 0; // nb de frames en plus par segment : utilisé pour paramétrer la résolution de l'heuristique
		public int h_t, h_m; // Heuristique temporelle et nombre de coups
		public TreeMap<Integer, Integer> fleurToSegment; // Segment parent de la fleur d'indice r*COL+c.
		public LinkedList<Segment> minSegments;
		
		public Heuristic(Niveau niveau) {
			niv = niveau;
		}
		
		/**
		 * Prépare les segments recouvrant la grille qui seront ensuite utilisés
		 * par "compute" pour le calcul de l'heuristique de chaque état.
		 */
		public void prepareRootState() {
			minSegments = new LinkedList<Segment>();
			fleurToSegment = new TreeMap<Integer, Integer>();
			PriorityQueue<Segment> segments = new PriorityQueue<Segment>();
			Segment colSegment = new Segment();
			Segment[] rowSegments = new Segment[Position.ROW];
			for(int r=0; r < Position.ROW; r++)
				rowSegments[r] = new Segment();
			// Préparation de tous les segments
			for(int c=0; c < Position.COL; c++) {
				for(int r=0; r < Position.ROW; r++) {
					int cell = niv.carte[r][c];
					if(cell==2 || cell==3) { // Fleur
						Flower f = new Flower(r, c);
						colSegment.addFlower(f);
						rowSegments[r].addFlower(f);
					} else if(cell==1 || cell==5 || cell>=10) { // Menhir (ou menhir rouge car en cours de jeu !) ou arc-en-ciel
						if(colSegment.nFleurs!=0) {
							segments.add(colSegment);
							colSegment = new Segment();
						}
						if(rowSegments[r].nFleurs!=0) {
							segments.add(rowSegments[r]);
							rowSegments[r] = new Segment();
						}
					}
				}
				if(colSegment.nFleurs!=0) {
					segments.add(colSegment);
					colSegment = new Segment();
				}
			}
			for(int r=0; r < Position.ROW; r++) {
				if(rowSegments[r].nFleurs!=0) {
					segments.add(rowSegments[r]);
				}
			}
			// Sélection d'un set non recouvrant par ordre des plus grands segments
			Segment s;
			while(!segments.isEmpty()) {
				s = segments.poll();
				if(s.nFleurs==0)
					break;
				int indexSeg = minSegments.size();
				minSegments.add(s);
				s.invalidFlowers(segments, fleurToSegment, indexSeg);
				s.computeLength();
			}
		}
		
		/**
		 * Calcule l'heuristique de la grille par rapport à la position du colibri.
		 * Utilise l'ensemble minimal de segments pour calculer une estimation
		 * d'itinéraire entre ces derniers.
		 * @param coli la position du colibri dans l'état considéré
		 * @param grid DynaGrid de l'état actuel de niv
		 * @return
		 */
		public void compute(Position coli, DynaGrid grid) {
			int i = 0;
			h_t = 0;
			h_m = 0;
			for(Segment s : minSegments) {
				if(grid.flowersBySegment[i++] > 0) { // Le segment n'a pas été complètement ramassé.
					h_t += s.length * 4 / 3 + h_param;
					h_m += 1 + h_param/2;
				}
			}
		}
		
		
		public static class Flower {
			public int r, c;
			public LinkedList<Segment> segments;
			
			public Flower(int r, int c) {
				this.r = r;
				this.c = c;
				segments = new LinkedList<Segment>();
			}
			
			public void addSegment(Segment s) {
				segments.add(s);
			}
		}
		
		public static class Segment implements Comparable<Segment>  {
			public char nFleurs = 0;
			public int length = 0;
			public LinkedList<Flower> flowers;
			
			public Segment() {
				flowers = new LinkedList<Flower>();
			}
			
			@Override
			public int compareTo(Segment s) {
				return s.nFleurs - nFleurs;
			}
			
			public void addFlower(Flower f) {
				flowers.add(f);
				f.addSegment(this);
				nFleurs++;
			}
			
			/**
			 * Calcule la longueur du segment en nombre de cases puis efface les fleurs.
			 */
			public void computeLength() {
				Flower f1 = flowers.getFirst();
				Flower f2 = flowers.getLast();
				length = f2.r - f1.r + f2.c - f1.c + 1;
				flowers = null; // Pour ne pas garder les objets Fleur en mémoire.
			}
			
			/**
			 * Invalide ses fleurs dans les autres segments.
			 * @param segs queue de tous les segments
			 * @param fleurToSegment map de fleur à segment parent
			 * @param indexSeg indice du segment dans minSegments
			 */
			public void invalidFlowers(PriorityQueue<Segment> segs, TreeMap<Integer, Integer> fleurToSegment, int indexSeg) {
				for(Flower f : flowers) {
					fleurToSegment.put(f.r*Position.COL + f.c, indexSeg);
					for(Segment s : f.segments) {
						if(s!=this) {
							segs.remove(s);
							s.flowers.remove(f);
							s.nFleurs--;
							segs.add(s); // Pour replacer le segment dans la queue
						}
					}
				}
			}
		}
	}
}
