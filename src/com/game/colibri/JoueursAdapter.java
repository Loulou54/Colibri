package com.game.colibri;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JoueursAdapter extends ArrayAdapter<Joueur> {
	
	private TextView adv;
	
	public JoueursAdapter(Context context, int resource, List<Joueur> objects) {
		super(context, resource, objects);
	}
	
	public void setTextView(TextView tv) {
		adv=tv;
	}
	
	public void updateTextView() {
		adv.setText(getContext().getText(R.string.adversaires)+" "+getCount());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Joueur j = getItem(position);
		final ViewHolder h;
		if(convertView==null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.element_joueur, parent, false);
			h = new ViewHolder();
			h.avatar = (ImageView) convertView.findViewById(R.id.avatarAdv);
			h.nom = (TextView) convertView.findViewById(R.id.nomAdv);
			h.pays = (ImageView) convertView.findViewById(R.id.paysAdv);
			h.exp = (TextView) convertView.findViewById(R.id.expAdv);
			h.defis = (TextView) convertView.findViewById(R.id.winDefisAdv);
			h.poubelle = (ImageView) convertView.findViewById(R.id.poubelle);
			convertView.setTag(h);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(h.poubelle.getVisibility()==View.VISIBLE) {
						h.poubelle.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.menu_right));
						h.poubelle.setVisibility(View.GONE);
						h.poubelle.setClickable(false);
					} else {
						h.poubelle.setVisibility(View.VISIBLE);
						h.poubelle.setClickable(true);
						h.poubelle.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.aleat_opt_anim));
					}
				}
			});
			h.poubelle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JoueursAdapter.this.remove((Joueur) h.poubelle.getTag());
					updateTextView();
				}
			});
		} else {
			h = (ViewHolder) convertView.getTag();
		}
		h.avatar.setImageResource(j.getAvatar());
		h.nom.setText(j.getPseudo());
		try {
			InputStream file = getContext().getAssets().open("drapeaux/"+j.getPays().toLowerCase(Locale.FRANCE)+".png");
			h.pays.setImageBitmap(BitmapFactory.decodeStream(file));
			h.pays.setVisibility(View.VISIBLE);
		} catch (IOException e) {
			h.pays.setVisibility(View.INVISIBLE);
		}
		h.exp.setText(String.format("%,d", j.getExp()));
		h.defis.setText(j.getWin()+"/"+j.getDefis());
		h.poubelle.setVisibility(View.GONE);
		h.poubelle.setClickable(false);
		h.poubelle.setTag(j);
		return convertView;
	}
	
	static class ViewHolder {
		ImageView avatar, pays, poubelle;
		TextView nom, exp, defis;
	}
}
