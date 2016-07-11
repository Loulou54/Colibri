package com.game.colibri;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DispJoueur {
	
	private Context context;
	private Joueur j;
	
	@SuppressLint("InlinedApi")
	public DispJoueur(Context context, Joueur joueur) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
		} else {
			this.context = context;
		}
		j = joueur;
	}
	
	@SuppressLint("InflateParams")
	public void show() {
		LinearLayout lay = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.details_joueur_layout, null);
		((TextView) lay.findViewById(R.id.nomDetails)).setText(j.getPseudo());
		((TextView) lay.findViewById(R.id.nomDetails)).setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Passing Notes.ttf"));
		((ImageView) lay.findViewById(R.id.avatarDetails)).setImageResource(j.getAvatar());
		((TextView) lay.findViewById(R.id.paysDetails)).setText((new Locale("", j.getPays())).getDisplayCountry(Resources.getSystem().getConfiguration().locale));
		ImageView pays = (ImageView) lay.findViewById(R.id.flagDetails);
		try {
			InputStream file = context.getAssets().open("drapeaux/"+j.getPays().toLowerCase(Locale.FRANCE)+".png");
			pays.setImageBitmap(BitmapFactory.decodeStream(file));
			pays.setVisibility(View.VISIBLE);
		} catch (IOException e) {
			pays.setVisibility(View.INVISIBLE);
		}
		((TextView) lay.findViewById(R.id.lastConnectDetails)).setText(""+j.getLastVisit(context));
		((TextView) lay.findViewById(R.id.expDetails)).setText(String.format("%,d", j.getExp()));
		((TextView) lay.findViewById(R.id.progressDetails)).setText(""+(j.getProgress()-1)+"/"+Jeu.NIV_MAX);
		((TextView) lay.findViewById(R.id.defisDetails)).setText(""+j.getDefis());
		((TextView) lay.findViewById(R.id.winsDetails)).setText(""+j.getWin());
		((TextView) lay.findViewById(R.id.looseDetails)).setText(""+j.getLost());
		AlertDialog.Builder box = new AlertDialog.Builder(context);
		box.setView(lay);
		box.show();
	}
	
}
