package com.game.colibri;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DefiAdapter extends ArrayAdapter<Joueur> {
	
	public DefiAdapter(Context context, ArrayList<Joueur> adversaires) {
		super(context, R.layout.choix_niveaux, adversaires);
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.bouton_defi, parent, false);
		TextView name = (TextView) rowView.findViewById(R.id.name);
		TextView exp = (TextView) rowView.findViewById(R.id.exp);
		TextView score = (TextView) rowView.findViewById(R.id.score);
		Joueur j = getItem(position);
		name.setText(j.getPseudo());
		exp.setText("Expérience :" + j.getExp());
		int scoreOwner = j.getDefis() - j.getWin();
		score.setText("Score : " + scoreOwner + " - " + j.getWin());
		return rowView;
	}
}
