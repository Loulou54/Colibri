package com.game.colibri;

import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import static com.network.colibri.CommonUtilities.SERVER_URL;

public class RegisterUser {
	
	private Context context;
	private ProgressDialog prgDialog;
	private callBackInterface callback;
	private AsyncHttpClient client;
	private int avatar=0;
	
	@SuppressLint("InlinedApi")
	public RegisterUser(Context context, AsyncHttpClient client, callBackInterface callback) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
		} else {
			this.context = context;
		}
		this.client = client;
		this.callback= callback;
		prgDialog = new ProgressDialog(this.context);
		prgDialog.setMessage(context.getString(R.string.progress));
		prgDialog.setCancelable(false);
	}
	
	/**
	 * Affiche la boîte de dialogue d'inscription.
	 * @param content le contenu de la ligne de saisie. Doit être null à la première saisie.
	 * 			Permet d'afficher l'essai précédent en cas de pseudo déjà pris.
	 * @param editor les SharedPreferences dans lesquels stocker l'inscription de l'utilisateur.
	 */
	@SuppressLint("InflateParams")
	public void show(String content) {
		final LinearLayout lay = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.register_layout, null);
		if(content!=null)
			((EditText) lay.findViewById(R.id.pseudo)).setText(content);
		final LinearLayout imagePicker = (LinearLayout) lay.findViewById(R.id.imagePicker);
		final ImageView avatarReg = (ImageView) lay.findViewById(R.id.avatarReg);
		avatarReg.setImageResource(Joueur.img[avatar]);
		avatarReg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				avatarReg.setVisibility(View.GONE);
				imagePicker.setVisibility(View.VISIBLE);
			}
		});
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics()), LinearLayout.LayoutParams.MATCH_PARENT);
		OnClickListener click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				imagePicker.setVisibility(View.GONE);
				avatar = v.getId();
				avatarReg.setImageResource(Joueur.img[avatar]);
				avatarReg.setVisibility(View.VISIBLE);
				avatarReg.startAnimation(AnimationUtils.loadAnimation(context, R.anim.aleat_opt_anim));
			}
		};
		for(int i=0; i<Joueur.img.length; i++) {
			ImageView iv= new ImageView(context);
			iv.setLayoutParams(params);
			iv.setId(i);
			iv.setImageResource(Joueur.img[i]);
			iv.setOnClickListener(click);
			imagePicker.addView(iv);
		}
		AlertDialog.Builder boxRegister = new AlertDialog.Builder(context);
		boxRegister.setTitle(R.string.register);
		boxRegister.setCancelable(false);
		DialogInterface.OnClickListener check = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==DialogInterface.BUTTON_POSITIVE) {
					String name = ((EditText) lay.findViewById(R.id.pseudo)).getText().toString().trim();
					if (!name.contains(",") && !name.contains(";") && name.length()>2 && name.length()<15)
						registerUser(name);
					else {
						Toast.makeText(context, R.string.nom_invalide, Toast.LENGTH_LONG).show();
						show(name);
					}
				} else {
					callback.cancelled();
				}
			}
		};
		boxRegister.setPositiveButton(R.string.accept, check);
		boxRegister.setNegativeButton(R.string.annuler, check);
		boxRegister.setView(lay);
		boxRegister.show();
	}
	
	public interface callBackInterface {
		int getExp();
		int getProgress();
		boolean registered(String JSONresponse, String name);
		void cancelled();
	}
	
	private void registerUser(final String name) {
		RequestParams params = new RequestParams();
		params.put("pseudo", name);
		params.put("avatar", ""+avatar);
		params.put("exp", ""+callback.getExp());
		params.put("progress", ""+callback.getProgress());
		params.put("regId", GCMRegistrar.getRegistrationId(context));
		params.put("pays", Resources.getSystem().getConfiguration().locale.getCountry());
		prgDialog.show();
		client.post(SERVER_URL+"/register.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				prgDialog.dismiss();
				if(response.equalsIgnoreCase("pris")) { // Nom déjà pris
					Toast.makeText(context, R.string.deja_pris, Toast.LENGTH_LONG).show();
					show(name);
				} else if(response.equalsIgnoreCase("error")) { // Erreur
					Toast.makeText(context, R.string.err, Toast.LENGTH_LONG).show();
				} else { // Succès
					if(callback.registered(response, name))
						Toast.makeText(context, R.string.enregistre, Toast.LENGTH_LONG).show();
					else {
						Toast.makeText(context, R.string.err, Toast.LENGTH_LONG).show();
						show(name);
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				prgDialog.dismiss();
				if (statusCode == 404) {
					Toast.makeText(context, R.string.err404, Toast.LENGTH_LONG).show();
				} else if (statusCode == 500 || statusCode == 503) {
					Toast.makeText(context, R.string.err500, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, R.string.err, Toast.LENGTH_LONG).show();
				}
				show(name);
			}
		});
	}
	
	/*private boolean isNicknameTaken(String response, String name) {
		try {
			JSONArray arr = new JSONArray(response);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject user = (JSONObject) arr.get(i);
				if(user.get("userName")==name)
					return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}*/
	
}
