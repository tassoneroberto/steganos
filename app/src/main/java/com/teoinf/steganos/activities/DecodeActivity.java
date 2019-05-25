package com.teoinf.steganos.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.teoinf.steganos.R;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.controller.DecodeParametersController;
import com.teoinf.steganos.directorydialog.ChoosenDirectoryListener;
import com.teoinf.steganos.directorydialog.DirectoryDialog;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.parameters.DecodeParameters;
import com.teoinf.steganos.process.DecodeProcess;
import com.teoinf.steganos.tools.Utils;

public class DecodeActivity extends Activity {

	private final int CHOOSE_VIDEO_CONTAINER = 0;
	private final int SETTINGS_ACCESS = 1;
	private static final int ERROR_IN_PROCESS = 0;
	private static final int PROCESS_OK = 1;
	
	// Graphical components
	private ImageButton _btnBack;
	private ImageButton _btnSettings;
	private Button _btnSelectSourceVideo;
	private Button _btnDecode;
	private Button _btnSelectFileDestination;
	private RadioButton _checkBoxDisplayContent;
	private RadioButton _checkBoxSaveIntoFile;
	private RadioButton _radioCompressedTextLZW;
	private RadioButton _radioCompressedTextDeflate;
	private RadioButton _radioNoCompression;
	private TextView _sourcePath;
	private EditText _editTextCryptographyKeyDecode;
	private EditText _editTextFileExtension;
	private CardView _cardViewCryptographyKeyDecode;
	private CardView _cardViewSaveIntoFileDest;
	private CardView _cardViewSaveInoFileExt;
	private CardView _card_layout_decompression;


	private ProgressDialog _progressDialog;
	private DecodeParameters _decodeParameters;
	
	private Handler processHandler = new Handler()
	{
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	_progressDialog.cancel();
	        if (msg.what == ERROR_IN_PROCESS)
	        {
	        	displayProcessErrors();
	        } else {
	        	displayProcessSuccess();
	        }
	    }
	};
	
	private void displayProcessErrors() {
		ErrorManager.getInstance().displayErrorMessages(this);
	}
	
	private void displayProcessSuccess() {
		String toastMsg = "Your data was unhidden.";

		if (_decodeParameters.getDisplay())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Your data");
			builder.setMessage(_decodeParameters.getDisplayText());
			builder.setPositiveButton("OK", null);
			builder.show();
			
		}
		Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        _sourcePath=(TextView)findViewById(R.id.sourcePath);
        _btnBack = (ImageButton) findViewById(R.id.btn_back_decode);
		_btnDecode = (Button) findViewById(R.id.btn_decode);
		_btnSelectSourceVideo = (Button) findViewById(R.id.btn_select_video_source_decode);
		_btnSelectFileDestination = (Button) findViewById(R.id.btn_select_file_destination_decode);
		_btnSettings = (ImageButton) findViewById(R.id.btn_settings_decode);
		_checkBoxDisplayContent = (RadioButton) findViewById(R.id.chk_box_display_content);
		_checkBoxSaveIntoFile = (RadioButton) findViewById(R.id.chk_box_save_into_file);
		_radioCompressedTextLZW = (RadioButton) findViewById(R.id.lzwDecompressedText);
		_radioCompressedTextDeflate = (RadioButton) findViewById(R.id.deflateDecompressedText);

		_editTextCryptographyKeyDecode = (EditText) findViewById(R.id.edit_text_cryptography_key_decode);
		_editTextFileExtension = (EditText) findViewById(R.id.edit_text_file_extension);
		_cardViewCryptographyKeyDecode=(CardView)findViewById(R.id.card_layout_cryptography_key_decode) ;
		_cardViewSaveIntoFileDest = (CardView) findViewById(R.id.card_view_save_into_file_destination);
		_cardViewSaveInoFileExt = (CardView) findViewById(R.id.card_view_save_into_file_extension);
		_card_layout_decompression=(CardView)findViewById(R.id.card_layout_decompression);

		_btnBack.setOnClickListener(onClickListener);
		_btnSettings.setOnClickListener(onClickListener);
		_btnSelectSourceVideo.setOnClickListener(onClickListener);
		_btnDecode.setOnClickListener(onClickListener);
		_btnSelectFileDestination.setOnClickListener(onClickListener);
		_checkBoxDisplayContent.setChecked(true);
		_checkBoxSaveIntoFile.setChecked(false);
		_radioCompressedTextLZW.setChecked(true);
		_radioCompressedTextDeflate.setChecked(false);
		_checkBoxDisplayContent.setOnCheckedChangeListener(onCheckedChangeListener);
		_checkBoxSaveIntoFile.setOnCheckedChangeListener(onCheckedChangeListener);
		_editTextCryptographyKeyDecode.addTextChangedListener(onTextChangedListenerCryptographyKey);
		_editTextFileExtension.addTextChangedListener(onTextChangedListenerFileExtension);
		_cardViewSaveInoFileExt.setVisibility(View.GONE);
		_cardViewSaveIntoFileDest.setVisibility(View.GONE);
		_decodeParameters = new DecodeParameters();
		
		updateImageViews();
		updateLinearLayoutCryptographyVisibility();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.decode, menu);
		return true;
	}
	
	private void updateLinearLayoutCryptographyVisibility() {
		if (Preferences.getInstance().getUseCryptography()) {
			_cardViewCryptographyKeyDecode.setVisibility(View.VISIBLE);
		} else {
			_cardViewCryptographyKeyDecode.setVisibility(View.GONE);
		}
	}
	
	private void updateImageViews() {
		DecodeParametersController controller = new DecodeParametersController(true);
		
		if (controller.controlSrcVideoPath(_decodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_video_source_decode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
			((ImageView) findViewById(R.id.img_view_valid_video_source_decode)).setImageResource(R.drawable.ic_error_white_48dp);
		}
		
		if (controller.controlDestFilePath(_decodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_video_destination_decode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
			((ImageView) findViewById(R.id.img_view_valid_video_destination_decode)).setImageResource(R.drawable.ic_error_white_48dp);
		}

		
		if (controller.controlCryptographyKey(_decodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_key_length_decode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
			((ImageView) findViewById(R.id.img_view_valid_key_length_decode)).setImageResource(R.drawable.ic_error_white_48dp);
		}
	}
	
	private void showFileChooser(int code) {
	    Intent intent = new Intent(Intent.ACTION_PICK);

	    if (code == CHOOSE_VIDEO_CONTAINER) {
	    	intent.setType("video/*");
	    } else {
	    	intent.setType("*/*"); 
	    }

	    try {
	        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.destination_string)), code);
	    } catch (android.content.ActivityNotFoundException ex) {
	        Toast.makeText(this, getResources().getString(R.string.error_file_manager_string), Toast.LENGTH_SHORT).show();
	    }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case CHOOSE_VIDEO_CONTAINER:
				callbackFileChooserVideoContainer(requestCode, resultCode, data);
				break;
			case SETTINGS_ACCESS:
				updateLinearLayoutCryptographyVisibility();
				updateImageViews();
				break;
			default:
				ErrorManager.getInstance().addErrorMessage("[Decode Activity] Activity result not known");
				ErrorManager.getInstance().displayErrorMessages(this);
				break;
		}
	}
	
	private void callbackFileChooserVideoContainer(int requestCode, int resultCode, Intent data) {
		Uri selectedVideoLocation;
		
		if (resultCode == Activity.RESULT_OK) {
			selectedVideoLocation = data.getData();
            _decodeParameters.setVideoPath(Utils.getRealPathFromUri(this, selectedVideoLocation));
			if (_decodeParameters.getDestinationVideoDirectory() == null || _decodeParameters.getDestinationVideoDirectory().isEmpty()) {
				_decodeParameters.setDestinationVideoDirectory(Utils.getBasenameFromPath(_decodeParameters.getVideoPath()));
                _sourcePath.setText(_decodeParameters.getDestinationVideoDirectory());
            }
			updateImageViews();
		}
	}
	
	private void showDirectoryChooser() {
		DirectoryDialog dialog = new DirectoryDialog(this);
		dialog.setChoosenDirectoryListener(onChoosenDirectoryListener);
		dialog.show();
	}
	
	private void process() {
		_progressDialog = ProgressDialog.show(this, "Loading", "Extracting data...");
		
		new Thread(new Runnable() {
		    public void run() {
		    	Message res = processHandler.obtainMessage(PROCESS_OK);
		    	
		    	DecodeParametersController controller;
				_decodeParameters.setCompressLZW(_radioCompressedTextLZW.isChecked());
				_decodeParameters.setCompressDeflate(_radioCompressedTextDeflate.isChecked());
				_decodeParameters.setUseAudioChannel(Preferences.getInstance().getUseAudioChannel());
				_decodeParameters.setUseVideoChannel(Preferences.getInstance().getUseVideoChannel());
				controller = new DecodeParametersController(false);
				if (controller.controlAllData(_decodeParameters)){
					DecodeProcess process = new DecodeProcess();
					if (!process.decode(_decodeParameters)) {
						ErrorManager.getInstance().addErrorMessage("[Decode Activity] Impossible to find something in this file");
						res = processHandler.obtainMessage(ERROR_IN_PROCESS);
					}
				} else {
					res = processHandler.obtainMessage(ERROR_IN_PROCESS);
				}
				
				processHandler.sendMessage(res);
		    }
		  }).start();
		
	}
	
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
				case R.id.btn_back_decode:
					finish();
					break;
				case R.id.btn_settings_decode:
					Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
					startActivityForResult(intent, SETTINGS_ACCESS);
					break;
				case R.id.btn_select_video_source_decode:
					showFileChooser(CHOOSE_VIDEO_CONTAINER);
					break;
				case R.id.btn_select_file_destination_decode:
					showDirectoryChooser();
					break;
				case R.id.btn_decode:
					process();
					break;
				default:
					ErrorManager.getInstance().addErrorMessage("[Decode Activity] Click event not known");
					ErrorManager.getInstance().displayErrorMessages(arg0.getContext());
					break;
			}
		}
	};
	
	private ChoosenDirectoryListener onChoosenDirectoryListener = new ChoosenDirectoryListener() {

		@Override
		public void onChoosenDir(String directory) {
			_decodeParameters.setDestinationVideoDirectory(directory);
			updateImageViews();
		}
	};

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if (arg0.getId() == R.id.chk_box_display_content && arg1) {
				_cardViewSaveInoFileExt.setVisibility(View.GONE);
				_cardViewSaveIntoFileDest.setVisibility(View.GONE);
				_checkBoxSaveIntoFile.setChecked(false);
				_card_layout_decompression.setVisibility(View.VISIBLE);
				_decodeParameters.setDisplay(true);
			} else if (arg0.getId() == R.id.chk_box_display_content) {
				_cardViewSaveInoFileExt.setVisibility(View.VISIBLE);
				_cardViewSaveIntoFileDest.setVisibility(View.VISIBLE);
				_card_layout_decompression.setVisibility(View.GONE);
				_checkBoxSaveIntoFile.setChecked(true);
				_decodeParameters.setDisplay(false);
			} else if (arg0.getId() == R.id.chk_box_save_into_file && arg1) {
				_cardViewSaveInoFileExt.setVisibility(View.VISIBLE);
				_cardViewSaveIntoFileDest.setVisibility(View.VISIBLE);
				_card_layout_decompression.setVisibility(View.GONE);
				_checkBoxDisplayContent.setChecked(false);
				_decodeParameters.setDisplay(false);
			} else {
				_cardViewSaveInoFileExt.setVisibility(View.GONE);
				_cardViewSaveIntoFileDest.setVisibility(View.GONE);
				_card_layout_decompression.setVisibility(View.VISIBLE);
				_checkBoxDisplayContent.setChecked(true);
				_decodeParameters.setDisplay(true);
			}
			updateImageViews();
		}
	};
	
	private TextWatcher onTextChangedListenerCryptographyKey = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			_decodeParameters.setCryptographyKey(s.toString());
			updateImageViews();
		}
	};
	
	private TextWatcher onTextChangedListenerFileExtension = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			updateImageViews();
		}
	};
}
