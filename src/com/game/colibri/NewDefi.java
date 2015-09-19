package com.game.colibri;

import static com.network.colibri.CommonUtilities.SERVER_URL;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class NewDefi {
	
	private Context context;
	private callBackInterface callback;
	private String user, nomDefi;
	private DropDownAdapter dropDownAdapter;
	private JoueursAdapter jAdapter;
	private AsyncHttpClient client;
	
	@SuppressLint("InlinedApi")
	public NewDefi(Context context, AsyncHttpClient client, String user, callBackInterface callback) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.context = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
		} else {
			this.context = context;
		}
		this.client = client;
		this.callback= callback;
		this.user = user;
		ArrayList<Joueur> joueurs = new ArrayList<Joueur>();
		jAdapter = new JoueursAdapter(context, R.layout.element_joueur, joueurs);
		dropDownAdapter = new DropDownAdapter(context, R.layout.simple_list_element, new ArrayList<String>(), user, joueurs);
	}
	
	/**
	 * Affiche la boîte de dialogue de création de nouveau défi.
	 */
	@SuppressLint("InflateParams")
	public void show() {
		final EditText et = new EditText(context);
		et.setHint(R.string.nom_defi);
		et.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		AlertDialog.Builder boxParticipants = new AlertDialog.Builder(context);
		boxParticipants.setTitle(R.string.nouveau_defi);
		DialogInterface.OnClickListener check = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==DialogInterface.BUTTON_POSITIVE) {
					nomDefi = et.getText().toString().trim();
					showParticipants();
				}
			}
		};
		boxParticipants.setPositiveButton(R.string.accept, check);
		boxParticipants.setNegativeButton(R.string.annuler, check);
		boxParticipants.setView(et);
		boxParticipants.show();
	}
	
	/**
	 * Affiche la boîte de dialogue de recherche de participants.
	 */
	@SuppressLint("InflateParams")
	public void showParticipants() {
		final LinearLayout lay = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.newdefi_layout, null);
		final SuggestionsEditText actv = (SuggestionsEditText) lay.findViewById(R.id.searchAdv);
		actv.setLoadingIndicator((ProgressBar) lay.findViewById(R.id.loading_indicator)); 
		actv.setAdapter(dropDownAdapter);
		actv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				addJoueur(dropDownAdapter.getItem(arg2));
				actv.setText("");
			}
		});
		ImageButton advAuto = (ImageButton) lay.findViewById(R.id.advAuto);
		advAuto.setOnClickListener(new ImageButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				addJoueur(null);
				hide_keyboard_from(context, actv);
			}
		});
		ListView jlv = (ListView) lay.findViewById(R.id.listAdv);
		jAdapter.setTextView((TextView) lay.findViewById(R.id.advTextView));
		jlv.setAdapter(jAdapter);
		jAdapter.updateTextView();
		jlv.setEmptyView((TextView) lay.findViewById(R.id.defaultView));
		
		AlertDialog.Builder boxParticipants = new AlertDialog.Builder(context);
		boxParticipants.setTitle(R.string.nouveau_defi);
		boxParticipants.setCancelable(false);
		DialogInterface.OnClickListener check = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==DialogInterface.BUTTON_POSITIVE) {
					if(jAdapter.getCount()==0) {
						Toast.makeText(context, R.string.nojoueur, Toast.LENGTH_LONG).show();
						showParticipants();
					} else
						createDefi();
				}
			}
		};
		boxParticipants.setPositiveButton(R.string.creer, check);
		boxParticipants.setNegativeButton(R.string.annuler, check);
		boxParticipants.setView(lay);
		boxParticipants.show();
	}
	
	public static void hide_keyboard_from(Context context, View view) {
	    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private String getStringListOfJoueurs() {
		StringBuilder joueurs = new StringBuilder("\""+user+"\",");
		int fin = jAdapter.getCount();
		for(int i=0; i<fin; i++) {
			joueurs.append("\""+jAdapter.getItem(i).getPseudo()+"\",");
		}
		joueurs.deleteCharAt(joueurs.length()-1);
		return joueurs.toString();
	}
	
	private void addJoueur(String name) {
		RequestParams params = new RequestParams();
		if(name==null) // Mode auto. On spécifie la liste des joueurs déjà pris.
			params.put("auto", getStringListOfJoueurs());
		else
			params.put("joueur", name);
		client.post(SERVER_URL+"/get_joueur.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Gson g = new Gson();
				try {
					Joueur j = g.fromJson(response, Joueur.class);
					jAdapter.add(j);
					jAdapter.updateTextView();
					jAdapter.notifyDataSetChanged();
				} catch (JsonSyntaxException e) {
					Toast.makeText(context, R.string.nojoueurfound, Toast.LENGTH_LONG).show();
					return;
				}
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				Toast.makeText(context, R.string.err, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void createDefi() {
		RequestParams params = new RequestParams();
		params.put("pseudo", user);
		params.put("nom", nomDefi);
		params.put("participants", "["+getStringListOfJoueurs()+"]");
		params.put("t_max", ""+3*60);
		client.post(SERVER_URL+"/newdefi.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				callback.create(response);
			}

			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				Toast.makeText(context, R.string.err, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public interface callBackInterface {
		void create(String jsonData);
	}
	
}
