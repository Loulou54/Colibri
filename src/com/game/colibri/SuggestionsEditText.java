package com.game.colibri;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

public class SuggestionsEditText extends AutoCompleteTextView {

    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int AUTOCOMPLETE_DELAY = 750;
    private ProgressBar mLoadingIndicator;


    @SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SuggestionsEditText.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public SuggestionsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), AUTOCOMPLETE_DELAY);
    }
    
    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.GONE);
        }
    	if(getAdapter().getCount()==0)
    		setTextColor(getContext().getResources().getColor(R.color.red));
    	else
    		setTextColor(getContext().getResources().getColor(R.color.noir));
        super.onFilterComplete(count);
    }

}
