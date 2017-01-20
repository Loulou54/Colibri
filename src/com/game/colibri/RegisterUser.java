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
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import static com.network.colibri.CommonUtilities.APP_TOKEN;
import static com.network.colibri.CommonUtilities.SERVER_URL;

public class RegisterUser {
	
	private Context context;
	private ProgressDialog prgDialog;
	private callBackInterface callback;
	private AsyncHttpClient client;
	private int avatar=0;
	private boolean register;
	
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
		register = true;
	}
	
	/**
	 * Affiche la boîte de dialogue d'inscription / connexion.
	 * @param content le contenu de la ligne de saisie. Doit être null à la première saisie.
	 * 			Permet d'afficher l'essai précédent en cas de pseudo déjà pris.
	 * @param editor les SharedPreferences dans lesquels stocker l'inscription de l'utilisateur.
	 */
	@SuppressLint("InflateParams")
	public void show(String content) {
		ScrollView root = (ScrollView) LayoutInflater.from(context).inflate(R.layout.register_layout, null);
		final LinearLayout lay = (LinearLayout) root.getChildAt(0);
		((TextView) lay.getChildAt(0)).setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/Passing Notes.ttf"));
		if(content!=null)
			((EditText) lay.findViewById(R.id.pseudo)).setText(content);
		if(callback.getExp()!=0) {
			TextView expTV = (TextView) lay.findViewById(R.id.expToSyncMsg);
			expTV.setText(context.getString(R.string.expToSyncMsg, String.format("%,d", callback.getExp())));
			expTV.setVisibility(View.VISIBLE);
		}
		final View reg = lay.findViewById(R.id.sw_reg), con = lay.findViewById(R.id.sw_con);
		reg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				register = true;
				reg.setClickable(false);
				reg.setBackgroundColor(context.getResources().getColor(R.color.theme_gris_alpha));
				con.setClickable(true);
				con.setBackgroundColor(context.getResources().getColor(R.color.theme_vert));
				lay.findViewById(R.id.lostPassword).setVisibility(View.GONE);
				View layAv = lay.findViewById(R.id.pickAvatar);
				layAv.setVisibility(View.VISIBLE);
				layAv.startAnimation(AnimationUtils.loadAnimation(context, R.anim.aleat_opt_anim));
			}
		});
		con.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				register = false;
				con.setClickable(false);
				con.setBackgroundColor(context.getResources().getColor(R.color.theme_gris_alpha));
				reg.setClickable(true);
				reg.setBackgroundColor(context.getResources().getColor(R.color.theme_vert));
				lay.findViewById(R.id.pickAvatar).setVisibility(View.GONE);
				lay.findViewById(R.id.lostPassword).setVisibility(View.VISIBLE);
			}
		});
		lay.findViewById(R.id.lost_chkbox).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lay.findViewById(R.id.lost_exp).setVisibility(((CheckBox) v).isChecked() ? View.VISIBLE : View.GONE);
			}
		});
		if(!register) {
			con.setClickable(false);
			con.setBackgroundColor(context.getResources().getColor(R.color.theme_gris_alpha));
			reg.setClickable(true);
			reg.setBackgroundColor(context.getResources().getColor(R.color.theme_vert));
			lay.findViewById(R.id.pickAvatar).setVisibility(View.GONE);
			lay.findViewById(R.id.lostPassword).setVisibility(View.VISIBLE);
		} else
			reg.setClickable(false);
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
		boxRegister.setCancelable(false);
		DialogInterface.OnClickListener check = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==DialogInterface.BUTTON_POSITIVE) {
					String name = ((EditText) lay.findViewById(R.id.pseudo)).getText().toString().trim();
					String mdp = ((EditText) lay.findViewById(R.id.mdp)).getText().toString();
					String mail = ((EditText) lay.findViewById(R.id.mail)).getText().toString();
					boolean lostMdp = ((CheckBox) lay.findViewById(R.id.lost_chkbox)).isChecked();
					if(name.length()<3 || name.length()>20) {
						Toast.makeText(context, R.string.nom_invalide, Toast.LENGTH_LONG).show();
						show(name);
					} else if(!register && lostMdp) {
						lostMdp(name);
					} else if(mdp.length()<6) {
						Toast.makeText(context, R.string.mdp_invalide, Toast.LENGTH_LONG).show();
						show(name);
					} else if(register && !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
						Toast.makeText(context, R.string.mail_invalide, Toast.LENGTH_LONG).show();
						show(name);
					} else if(register) {
						registerUser(name, mdp, mail);
					} else {
						connectUser(name, mdp);
					}
				} else {
					callback.cancelled();
				}
			}
		};
		boxRegister.setPositiveButton(R.string.accept, check);
		boxRegister.setNegativeButton(R.string.annuler, check);
		boxRegister.setView(root);
		boxRegister.show();
	}
	
	public interface callBackInterface {
		int getExp();
		int getProgress();
		boolean registered(String JSONresponse, String name, boolean sync);
		void cancelled();
	}
	
	private void registerUser(final String name, String mdp, String mail) {
		RequestParams params = new RequestParams();
		params.setHttpEntityIsRepeatable(true);
		params.put("token", APP_TOKEN);
		params.put("pseudo", name);
		params.put("password", mdp);
		params.put("mail", mail);
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
					Toast.makeText(context, R.string.errServ, Toast.LENGTH_LONG).show();
					show(name);
				} else { // Succès
					if(callback.registered(response, name, false))
						Toast.makeText(context, R.string.enregistre, Toast.LENGTH_LONG).show();
					else {
						Toast.makeText(context, R.string.errServ, Toast.LENGTH_LONG).show();
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
	
	private void connectUser(final String name, String mdp) {
		RequestParams params = new RequestParams();
		params.setHttpEntityIsRepeatable(true);
		params.put("token", APP_TOKEN);
		params.put("pseudo", name);
		params.put("password", mdp);
		params.put("regId", GCMRegistrar.getRegistrationId(context));
		prgDialog.show();
		client.post(SERVER_URL+"/connect.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				prgDialog.dismiss();
				if(response.equalsIgnoreCase("not registered")) { // Le pseudo n'existe pas
					Toast.makeText(context, R.string.not_registered, Toast.LENGTH_LONG).show();
					show(name);
				} else if(response.equalsIgnoreCase("wrong password")) { // Mauvais mot de passe
					Toast.makeText(context, R.string.wrong_password, Toast.LENGTH_LONG).show();
					show(name);
				} else { // Succès
					if(callback.registered(response, name, true))
						Toast.makeText(context, R.string.connected, Toast.LENGTH_LONG).show();
					else {
						Toast.makeText(context, R.string.errServ, Toast.LENGTH_LONG).show();
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
	
	private void lostMdp(final String name) {
		RequestParams params = new RequestParams();
		params.setHttpEntityIsRepeatable(true);
		params.put("pseudo", name);
		prgDialog.show();
		client.post(SERVER_URL+"/mail_mdp.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				prgDialog.dismiss();
				if(response.equalsIgnoreCase("not found")) { // Le pseudo n'existe pas
					Toast.makeText(context, R.string.not_registered, Toast.LENGTH_LONG).show();
				} else if(response.equalsIgnoreCase("already sent")) { // Un token de moins d'un jour est déjà attribué à cet utilisateur
					Toast.makeText(context, R.string.lost_mdp_already_sent, Toast.LENGTH_LONG).show();
				} else if(response.equalsIgnoreCase("OK")) { // Un token de moins d'un jour est déjà attribué à cet utilisateur
					Toast.makeText(context, R.string.lost_mdp_ok, Toast.LENGTH_LONG).show();
				} else { // Problème d'envoi
					Toast.makeText(context, R.string.lost_mdp_pb, Toast.LENGTH_LONG).show();
				}
				show(name);
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
	
}
