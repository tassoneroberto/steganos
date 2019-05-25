package com.teoinf.steganos.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.teoinf.steganos.R;
import com.teoinf.steganos.error.ErrorManager;

public class AboutActivity extends Activity {

	private ImageButton _btnBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
		_btnBack = (ImageButton) findViewById(R.id.btn_back_about);
		_btnBack.setOnClickListener(onClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
				case R.id.btn_back_about:
					finish();
					break;
				default:
					ErrorManager.getInstance().addErrorMessage("[About Activity] Click event not known");
					ErrorManager.getInstance().displayErrorMessages(arg0.getContext());
					break;
			}
		}
	};
	
}
