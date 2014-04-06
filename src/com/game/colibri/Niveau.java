package com.game.colibri;

public class Niveau {
	
	public int[][] carte;
	public int dbx,dby;
	public int[][] solution;
	
	public Niveau(int[][] map,int x,int y,int[][] sol) {
		carte=new int[12][20];
		System.arraycopy(map, 0, carte, 0, 12);
		dbx=x;
		dby=y;
		solution=sol;
	}
}
