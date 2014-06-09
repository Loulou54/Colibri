package com.game.colibri;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DefiAdapter extends ArrayAdapter<Joueur> {
	
	public DefiAdapter(Context context, ArrayList<Joueur> adversaires) {
		super(context, R.layout.choix_niveaux, adversaires);
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.bouton_defi, parent, false);
		ImageView avatar = (ImageView) rowView.findViewById(R.id.avatar);
		TextView name = (TextView) rowView.findViewById(R.id.name);
		TextView exp = (TextView) rowView.findViewById(R.id.exp);
		TextView score = (TextView) rowView.findViewById(R.id.score);
		Button fer = (Button) rowView.findViewById(R.id.fermer);
		Joueur j = getItem(position);
		avatar.setImageResource(j.getAvatar());
		name.setText(j.getPseudo());
		exp.setText("Expérience :" + j.getExp());
		int scoreOwner = j.getDefis() - j.getWin();
		score.setText("Score : " + scoreOwner + " - " + j.getWin());
		fer.setId(position);
		return rowView;
	}
}
