package com.game.colibri;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Classe gérant la boîte de dialogue de définition des paramètres de génération des niveaux aléatoires.
 * @author Louis
 *
 */
public class ParamAleat {
	
	/**
	 *  Paramètres : {lon, nVaches, nDyna, nChats, nArcs, base} avec nX dans [-6,6] (négatif==uncheck), lon la longueur de la solution dans [5,30], base dans [0,1] définissant l'usage ou non de BaseNiveau.
	 */
	public static int[] param = new int[6];
	public final static int[] paramDefaut = new int[] {12, 3, 3, 2, 2, 1};
	
	private AlertDialog.Builder boxAleat = null;
	private Context context;
	private callBackInterface callback;
	private int avancement;
	private TempParam tp;
	
	public static void loadParams(SharedPreferences pref) {
		for(int i=0; i<param.length; i++) {
			param[i] = pref.getInt("paramAleat"+i, paramDefaut[i]);
		}
	}
	
	public static void saveParams(SharedPreferences.Editor editor) {
		for(int i=0; i<param.length; i++) {
			editor.putInt("paramAleat"+i, param[i]);
		}
		editor.commit();
	}
	
	@SuppressLint("InlinedApi")
	public ParamAleat(callBackInterface callback, Context context, int avancement) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
		} else {
			this.context = context;
		}
		this.callback = callback;
		this.avancement = avancement;
	}
	
	public interface callBackInterface {
		void launchFunction(int mode);
	}
	
	private void prepareParam(final LinearLayout parent) {
		final TextView tv = (TextView) parent.findViewById(R.id.long_text);
		final SeekBar sb = (SeekBar) parent.findViewById(R.id.long_seekbar);
		final CheckBox cb = (CheckBox) parent.findViewById(R.id.base_checkbox);
		sb.setProgress(param[0]-5);
		cb.setChecked(param[5]==1);
		tv.setText(context.getString(R.string.longueur)+" "+(param[0]+param[0]/4)+" ± "+(param[0]/4));
		tp.updateExp(parent);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tv.setText(context.getString(R.string.longueur)+" "+(5*(progress+5)/4)+" ± "+((progress+5)/4));
				tp.p[0] = progress+5;
				tp.updateExp(parent);
			}
		});
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				tp.p[5] = isChecked ? 1 : 0;
			}
		});
	}
	
	public void show(final SharedPreferences.Editor editor) {
		tp = new TempParam();
		final ScrollView content = (ScrollView) LayoutInflater.from(context).inflate(R.layout.param_aleat, null);
		final LinearLayout lay = (LinearLayout) content.findViewById(R.id.param_lay);
		prepareParam(lay);
		int index = 1;
		new ParamElement(index++,lay,9,R.drawable.vache_0,R.string.vaches);
		new ParamElement(index++,lay,16,R.drawable.dynamite,R.string.dynamites);
		new ParamElement(index++,lay,21,R.drawable.chat_0,R.string.chats);
		new ParamElement(index++,lay,23,R.drawable.rainbow,R.string.arcs);
		boxAleat = new AlertDialog.Builder(context);
		boxAleat.setTitle(R.string.paramAleat);
		DialogInterface.OnClickListener check = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface d,int i) {
				if(i==DialogInterface.BUTTON_POSITIVE) {
					tp.commit();
					saveParams(editor);
					callback.launchFunction(Niveau.PERSO);
				} else if(i==DialogInterface.BUTTON_NEUTRAL) {
					param = paramDefaut.clone();
					saveParams(editor);
					ParamAleat.this.show(editor);
				}
			}
		};
		boxAleat.setPositiveButton(R.string.accept, check);
		boxAleat.setNeutralButton(R.string.defaut, check);
		boxAleat.setNegativeButton(R.string.annuler, check);
		boxAleat.setView(content);
		boxAleat.show();
	}
	
	/**
	 * Contient la vue et les informations d'une ligne de paramètres.
	 * @author Louis
	 *
	 */
	private class ParamElement {
		
		private LinearLayout elem;
		
		/**
		 * Constructeur d'un élément de réglage pour les paramètres des niveaux aléatoires.
		 * @param parent Le Layout parent.
		 * @param seuil Le seuil de progression dans Campagne nécessaire pour débloquer l'option.
		 * @param check L'état de la CheckBox.
		 * @param resDrawable La ressource de l'image à dessiner.
		 * @param resString La ressource du texte à afficher.
		 * @param value La valeur actuelle de l'option sur la SeekBar, entre 1 et 6.
		 */
		public ParamElement(int index, final LinearLayout parent, int seuil, int resDrawable, int resString) {
			final int i = index;
			elem =(LinearLayout) LayoutInflater.from(context).inflate(R.layout.param_element, null);
			((ImageView) elem.findViewById(R.id.imageParam)).setImageResource(resDrawable);
			((TextView) elem.findViewById(R.id.textParam)).setText(resString);
			if(avancement<seuil) { // L'option n'est pas encore débloquée
				tp.p[i] = -Math.abs(tp.p[i]);
				setEnabled(false);
				((SeekBar) elem.findViewById(R.id.seekBarParam)).setVisibility(View.GONE);
				TextView tb = (TextView) elem.findViewById(R.id.blockParam);
				tb.setVisibility(View.VISIBLE);
				tb.setText(tb.getText()+" "+avancement+"/"+seuil);
			} else {
				CheckBox cb = (CheckBox) elem.findViewById(R.id.checkBoxParam);
				SeekBar sb = (SeekBar) elem.findViewById(R.id.seekBarParam);
				cb.setChecked(param[i]>0);
				sb.setEnabled(param[i]>0);
				sb.setProgress(Math.abs(param[i])-1);
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						tp.p[i] = (isChecked ? 1 : -1)*Math.abs(tp.p[i]);
						((SeekBar) elem.findViewById(R.id.seekBarParam)).setEnabled(isChecked);
						tp.updateExp(parent);
					}
				});
				sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						tp.p[i] = progress+1;
						tp.updateExp(parent);
					}
				});
			}
			parent.addView(elem, i);
		}
		
		public void setEnabled(boolean e) {
			for(int i = 0; i < elem.getChildCount(); i++) {
				elem.getChildAt(i).setEnabled(e);
			}
		}
	}
	
	private class TempParam {
		
		public int[] p;
		
		public TempParam() {
			p = param.clone();
		}
		
		public void updateExp(LinearLayout parent) {
			int min = p[0]*(10+p[0]/4);
			int lmax = 3*p[0]/2;
			int max = lmax*(10+lmax/4)+Math.max(p[1],0)*20+Math.max(p[2],0)*15+Math.max(p[3],0)*40+Math.max(p[4],0)*30;
			((TextView) parent.findViewById(R.id.param_exp)).setText(context.getString(R.string.exp)+" : "+min+" - "+max);
		}
		
		public void commit() {
			param = p.clone();
		}
		
	}
}
