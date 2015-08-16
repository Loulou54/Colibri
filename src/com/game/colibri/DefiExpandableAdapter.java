package com.game.colibri;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DefiExpandableAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private String user;
	private ArrayList<Defi> adversaires;
	
	public DefiExpandableAdapter(Context c, String user, ArrayList<Defi> defis) {
		context = c;
		this.user = user;
		adversaires = defis;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return adversaires.get(groupPosition).participants.values().toArray(new Participation[0])[childPosition];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Participation p = (Participation) getChild(groupPosition, childPosition);
		if(convertView==null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_participation, parent, false);
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(p.joueur.getAvatar());
			((TextView) convertView.findViewById(R.id.nomJoueur)).setText(p.joueur.getPseudo());
		}
		((TextView) convertView.findViewById(R.id.scoreJoueur)).setText(""+p.win);
		((ImageView) convertView.findViewById(R.id.etatJoueur)).setImageResource((p.t_cours==0) ? R.drawable.horloge : R.drawable.check);
		((TextView) convertView.findViewById(R.id.expJoueur)).setText(""+p.joueur.getExp());
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return adversaires.get(groupPosition).participants.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return adversaires.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return adversaires.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Defi d = (Defi) getGroup(groupPosition);
		if(convertView==null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_defi, parent, false);
			((TextView) convertView.findViewById(R.id.nomDefi)).setText(d.nom);
			((Button) convertView.findViewById(R.id.etat)).setContentDescription(""+groupPosition);
			((Button) convertView.findViewById(R.id.fermer)).setContentDescription(""+groupPosition);
		}
		((TextView) convertView.findViewById(R.id.nMatch)).setText(""+d.nMatch);
		String etat="";
		int color=0;
		switch(d.getEtat(user)) {
		case Defi.ATTENTE:
			etat = context.getString(R.string.etat_attente);
			color = R.color.bleu_moyen;
			break;
		case Defi.RESULTATS:
			etat = context.getString(R.string.etat_resultats);
			color = R.color.violet;
			break;
		case Defi.RELEVER:
			etat = context.getString(R.string.etat_relever);
			color = R.color.bleu_moyen;
			break;
		case Defi.LANCER:
			etat = context.getString(R.string.etat_lancer);
			color = R.color.vert_fonce;
			break;
		}
		((Button) convertView.findViewById(R.id.etat)).setText(etat);
		((Button) convertView.findViewById(R.id.etat)).setTextColor(color);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
