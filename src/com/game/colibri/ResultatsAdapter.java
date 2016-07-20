package com.game.colibri;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultatsAdapter extends ArrayAdapter<Participation> {
	
	public static double prog;
	public static int etape;
	private Typeface font;
	
	public ResultatsAdapter(Context context, Participation[] joueurs) {
		super(context, R.layout.element_resultat, joueurs);
		font = Typeface.createFromAsset(context.getAssets(),"fonts/Passing Notes.ttf");
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Participation p = getItem(position);
		ViewHolder h;
		if(convertView==null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.element_resultat, parent, false);
			h = new ViewHolder();
			h.rang = (TextView) convertView.findViewById(R.id.rangJoueurRes);
			h.avatar = (ImageView) convertView.findViewById(R.id.avatarRes);
			h.nom = (TextView) convertView.findViewById(R.id.nomJoueurRes);
			h.nom.setTypeface(font);
			h.temps = (TextView) convertView.findViewById(R.id.tempsJoueurRes);
			h.penalite = (TextView) convertView.findViewById(R.id.penaliteJoueurRes);
			h.exp = (TextView) convertView.findViewById(R.id.expJoueurRes);
			h.score = (TextView) convertView.findViewById(R.id.scoreJoueurRes);
			convertView.setTag(h);
		} else {
			h = (ViewHolder) convertView.getTag();
		}
		h.avatar.setImageResource(p.joueur.getAvatar());
		h.nom.setText(p.joueur.getPseudo());
		h.score.setText(""+(p.gagne==1 && etape<3 ? p.win-1 : p.win));
		if(p.gagne!=0) {
			h.temps.setText(Jeu.getFormattedTime(etape<2 ? (int) (p.t_fini*prog) : p.t_fini+p.penalite_fini));
			if(etape>=1)
				h.penalite.setText(Jeu.getFormattedTime(p.penalite_fini));
			if(etape>=3)
				h.exp.setText(""+p.exp);
			if(etape==3 && p.gagne==1)
				h.score.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.aleat_opt_anim));
		} else {
			int strRes = (p.t_fini==Participation.FORFAIT) ? R.string.forfait : R.string.not_played;
			h.temps.setText(getContext().getString(strRes)+" !");
			h.penalite.setText("");
			h.exp.setText("");
		}
		if(etape>=4) {
			if(p.gagne==1)
				h.nom.setTextColor(getContext().getResources().getColor(R.color.vert_fonce));
			else if(p.t_fini==Participation.NOT_PLAYED)
				h.nom.setTextColor(getContext().getResources().getColor(R.color.theme_gris));
			else
				h.nom.setTextColor(getContext().getResources().getColor(R.color.red));
			if(p.gagne!=0) {
				h.rang.setText(""+p.gagne);
				h.rang.setVisibility(View.VISIBLE);
			} else
				h.rang.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}
	
	static class ViewHolder {
		ImageView avatar;
		TextView rang, nom, temps, penalite, exp, score;
	}
}
