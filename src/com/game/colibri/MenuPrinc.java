package com.game.colibri;

import android.os.Bundle;
import android.view.Display;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.app.Activity;

public class MenuPrinc extends Activity {
	
	public int ww,wh;
	// Salut GitHub !
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_princ);
		Display display = getWindowManager().getDefaultDisplay();
		ww = display.getWidth();
		wh = display.getHeight();
		Button btn_lay = (Button)findViewById(R.id.bout1);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) btn_lay.getLayoutParams();
		layoutParams.leftMargin = ww*4/9;
	    layoutParams.topMargin = wh/3;
	    btn_lay.setLayoutParams(layoutParams);
	}

}
