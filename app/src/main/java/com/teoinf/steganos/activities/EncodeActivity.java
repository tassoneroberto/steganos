package com.teoinf.steganos.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.teoinf.steganos.R;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.controller.EncodeParametersController;
import com.teoinf.steganos.directorydialog.ChoosenDirectoryListener;
import com.teoinf.steganos.directorydialog.DirectoryDialog;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.parameters.EncodeParameters;
import com.teoinf.steganos.process.EncodeProcess;
import com.teoinf.steganos.tools.Utils;
import com.teoinf.steganos.wifi.WifiActivity;

import java.io.File;


public class EncodeActivity extends Activity{


	private final int CHOOSE_FILE_CONTENT = 0;
	private final int CHOOSE_VIDEO_CONTAINER = 1;
	private final int RECORD_VIDEO = 2;
	private final int SETTINGS_ACCESS = 3;
	private static final int ERROR_IN_PROCESS = 0;
	private static final int PROCESS_OK = 1;

	// Graphical components
	private ImageButton _btnBack;
	private ImageButton _btnCamera;
	private ImageButton _btnSettings;
	private Button _btnSelectSourceVideo;
	private Button _btnSelectVideoDestination;
	private Button _btnSelectFileToHide;
	private Button _btnEncode;
	private RadioButton _checkBoxFileToHide;
	private RadioButton _checkBoxTextToHide;
	private RadioButton _noCompressio;
	private RadioButton _radioCompressTextLZW;
	private RadioButton _radioCompressTextDeflate;
	private EditText _editTextContentToHide;
	private EditText _editTextCryptographyKeyEncode;
	private TextView _sourcePath;
	private TextView _destinationPath;
	private TextView _filePath;
	private LinearLayout _linearLayoutEncode;
	private CardView _cardLayoutCryptographyKeyEncode;
	private CardView _cardLayoutCompressionText;


	private ProgressDialog _progressDialog;
	private EncodeParameters _encodeParameters;

	private Handler _processHandler = new Handler()
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
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

		_sourcePath=(TextView)findViewById(R.id.sourcePath);
		_destinationPath=(TextView)findViewById(R.id.destinationPath);
		_filePath=(TextView)findViewById(R.id.filePath);
		_btnBack = (ImageButton) findViewById(R.id.btn_back_encode);
		_btnCamera = (ImageButton) findViewById(R.id.btn_camera);
		_btnSettings = (ImageButton) findViewById(R.id.btn_settings_encode);
		_btnSelectSourceVideo = (Button) findViewById(R.id.btn_select_video_source_encode);
		_btnSelectVideoDestination = (Button) findViewById(R.id.btn_select_video_destination_encode);
		_btnSelectFileToHide = (Button) findViewById(R.id.btn_select_file_to_hide);
		_btnEncode = (Button) findViewById(R.id.btn_encode);
		_checkBoxFileToHide = (RadioButton) findViewById(R.id.chk_box_file_to_hide);
		_checkBoxTextToHide = (RadioButton) findViewById(R.id.chk_box_text_to_hide);
		_radioCompressTextLZW = (RadioButton) findViewById(R.id.lzwCompressText);
		_radioCompressTextDeflate = (RadioButton) findViewById(R.id.deflateCompressText);
		_noCompressio=(RadioButton)findViewById(R.id.radio_no_compression);
		_editTextContentToHide = (EditText) findViewById(R.id.edit_text_content_to_hide);
		_editTextCryptographyKeyEncode = (EditText) findViewById(R.id.edit_text_cryptography_key_encode);
		_linearLayoutEncode = (LinearLayout) findViewById(R.id.linearLayoutEncode);
		_cardLayoutCryptographyKeyEncode=(CardView) findViewById(R.id.card_layout_cryptography_key_encode);
		_cardLayoutCompressionText=(CardView)findViewById(R.id.card_layout_compression);
		_btnBack.setOnClickListener(onClickListener);
		_btnCamera.setOnClickListener(onClickListener);
		_btnSettings.setOnClickListener(onClickListener);
		_btnSelectSourceVideo.setOnClickListener(onClickListener);
		_btnSelectVideoDestination.setOnClickListener(onClickListener);
		_btnSelectFileToHide.setOnClickListener(onClickListener);
		_btnEncode.setOnClickListener(onClickListener);
		_checkBoxFileToHide.setChecked(false);
        _checkBoxTextToHide.setChecked(true);
		_radioCompressTextLZW.setChecked(true);
		_radioCompressTextDeflate.setChecked(false);
		_checkBoxFileToHide.setOnCheckedChangeListener(onCheckedChangeListener);
        _checkBoxTextToHide.setOnCheckedChangeListener(onCheckedChangeListener);
		_editTextCryptographyKeyEncode.addTextChangedListener(onTextChangedListenerCryptographyKey);
		_editTextContentToHide.addTextChangedListener(onTextChangedListenerTextToHide);
		_encodeParameters = new EncodeParameters();
		
		updateImageViews();
		updateLinearLayoutCryptographyVisibility();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.encode, menu);
		return true;
	}
		
	private void displayProcessErrors() {
		ErrorManager.getInstance().displayErrorMessages(this);
	}
	
	private void displayProcessSuccess() {
		final Uri contentUri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			contentUri = Uri.fromFile(new File("file://" + Environment.getExternalStorageDirectory()));
			scanIntent.setData(contentUri);
			sendBroadcast(scanIntent);
		} else {
			final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
			sendBroadcast(intent);
		}

		Snackbar snackbar = Snackbar
				.make(_linearLayoutEncode, "A new file in the selected destination was created.", Snackbar.LENGTH_LONG)
				.setAction("SEND", new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//TODO verificare se il wifi è accieso, se è spento parte choice
						Intent intent = new Intent(EncodeActivity.this, ChoiceActivity.class);
						intent.putExtra("file_url", _encodeParameters.getDestinationVideoDirectory()+_encodeParameters.get_fileName());
						startActivity(intent);

					}
				});

		snackbar.setActionTextColor(Color.parseColor("#008f30"));

		snackbar.show();
	}
	
	private void updateLinearLayoutCryptographyVisibility() {
		if (Preferences.getInstance() != null) {
			if (Preferences.getInstance().getUseCryptography()) {
				_cardLayoutCryptographyKeyEncode.setVisibility(View.VISIBLE);
			} else {
				_cardLayoutCryptographyKeyEncode.setVisibility(View.GONE);
			}
		}
	}
	
	private void updateImageViews() {
		EncodeParametersController controller = new EncodeParametersController(true);
		
		if (controller.controlSrcVideoPath(_encodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_video_source_encode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
			((ImageView) findViewById(R.id.img_view_valid_video_source_encode)).setImageResource(R.drawable.ic_error_white_48dp);
		}

		if (controller.controlDestVideoPath(_encodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_video_destination_encode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
			((ImageView) findViewById(R.id.img_view_valid_video_destination_encode)).setImageResource(R.drawable.ic_error_white_48dp);
		}

		if (controller.controlContentToHide(_encodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_content_to_hide_encode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
		((ImageView) findViewById(R.id.img_view_valid_content_to_hide_encode)).setImageResource(R.drawable.ic_error_white_48dp);
		}
		
		if (controller.controlCryptographyKey(_encodeParameters)) {
			((ImageView) findViewById(R.id.img_view_valid_key_length_encode)).setImageResource(R.drawable.ic_check_circle_white_48dp);
		} else {
			((ImageView) findViewById(R.id.img_view_valid_key_length_encode)).setImageResource(R.drawable.ic_error_white_48dp);
		}
	}
	
	private void recordVideo() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		
	    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(takeVideoIntent, RECORD_VIDEO); 
	    }
	}
	
	private void showDirectoryChooser() {
		DirectoryDialog dialog = new DirectoryDialog(this);
		dialog.setChoosenDirectoryListener(onChoosenDirectoryListener);
		dialog.show();
	}
	
	private void showFileChooser(int code) {
	    Intent intent = new Intent(Intent.ACTION_PICK);
	    //intent.addCategory(Intent.CATEGORY_OPENABLE);

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
			case CHOOSE_FILE_CONTENT:
				callbackFileChooserFileToHide(requestCode, resultCode, data);
				break;
			case SETTINGS_ACCESS:
				updateLinearLayoutCryptographyVisibility();
				updateImageViews();
				break;
			case RECORD_VIDEO:
				if (resultCode != 0)
					Toast.makeText(this, "Video saved", Toast.LENGTH_LONG).show();
				break;
			default:
				ErrorManager.getInstance().addErrorMessage("[Encode Activity] Activity result not known");
				ErrorManager.getInstance().displayErrorMessages(this);
				break;
		}
	}

	private void callbackFileChooserVideoContainer(int requestCode, int resultCode, Intent data) {
		Uri selectedVideoLocation;
        if (resultCode == Activity.RESULT_OK) {
            selectedVideoLocation = data.getData();
            if (selectedVideoLocation != null) {
                _encodeParameters.setSourceVideoPath(Utils.getRealPathFromUri(this, selectedVideoLocation));
                _sourcePath.setText(_encodeParameters.getSourceVideoPath());
                if (_encodeParameters.getDestinationVideoDirectory() == null || _encodeParameters.getDestinationVideoDirectory().isEmpty()) {
                    _encodeParameters.setDestinationVideoDirectory(Utils.getBasenameFromPath(_encodeParameters.getSourceVideoPath()));
					_destinationPath.setText(_encodeParameters.getDestinationVideoDirectory());
				}
            } else {
                _encodeParameters.setSourceVideoPath("");
            }
            updateImageViews();
        }
	}
	
	private void callbackFileChooserFileToHide(int requestCode, int resultCode, Intent data) {
		Uri selectedFileLocation;
		
		if (resultCode == Activity.RESULT_OK) {
			selectedFileLocation = data.getData();
			if (selectedFileLocation != null) {
				_encodeParameters.setFileToHidePath(Utils.getRealPathFromUri(this, selectedFileLocation));
			} else {
				_encodeParameters.setFileToHidePath("");
			}
			_filePath.setText(_encodeParameters.getFileToHidePath());
			updateImageViews();
		}
	}
	
	private void process() {
		_progressDialog = ProgressDialog.show(this, "Loading", "Hiding data...");
		
		new Thread(new Runnable() {
			public void run() {


				Message res = _processHandler.obtainMessage(PROCESS_OK);

				EncodeParametersController controller;
				String textToHide=_editTextContentToHide.getText().toString();

				_encodeParameters.setCompressLZW(_radioCompressTextLZW.isChecked());
				_encodeParameters.setCompressDeflate(_radioCompressTextDeflate.isChecked());
				_encodeParameters.setTextToHide(textToHide);
				controller = new EncodeParametersController(false);
				if (controller.controlAllData(_encodeParameters)){
					EncodeProcess process = new EncodeProcess();
					if (!process.encode(_encodeParameters)) {
						res = _processHandler.obtainMessage(ERROR_IN_PROCESS);
					}
				} else {
					res = _processHandler.obtainMessage(ERROR_IN_PROCESS);
				}
				
				_processHandler.sendMessage(res);

			}
		}).start();
	}
		
	private ChoosenDirectoryListener onChoosenDirectoryListener = new ChoosenDirectoryListener() {

		@Override
		public void onChoosenDir(String directory) {
			_encodeParameters.setDestinationVideoDirectory(directory);
			_destinationPath.setText(_encodeParameters.getDestinationVideoDirectory());
			updateImageViews();
		}
	};

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if (arg0.getId() == R.id.chk_box_file_to_hide && arg1) {
				_editTextContentToHide.setVisibility(View.GONE);
				_cardLayoutCompressionText.setVisibility(View.GONE);
				_btnSelectFileToHide.setVisibility(View.VISIBLE);
				_filePath.setVisibility(View.VISIBLE);
				_checkBoxTextToHide.setChecked(false);
				_encodeParameters.setHidingText(false);
			} else if (arg0.getId() == R.id.chk_box_file_to_hide) {
				_editTextContentToHide.setVisibility(View.VISIBLE);
				_cardLayoutCompressionText.setVisibility(View.VISIBLE);
				_btnSelectFileToHide.setVisibility(View.GONE);
				_filePath.setVisibility(View.GONE);
				_checkBoxTextToHide.setChecked(true);
				_encodeParameters.setHidingText(true);
			} else if (arg0.getId() == R.id.chk_box_text_to_hide && arg1) {
				_btnSelectFileToHide.setVisibility(View.GONE);
				_filePath.setVisibility(View.GONE);
				_editTextContentToHide.setVisibility(View.VISIBLE);
				_cardLayoutCompressionText.setVisibility(View.VISIBLE);
				_checkBoxFileToHide.setChecked(false);
				_encodeParameters.setHidingText(true);
			} else {
				_filePath.setVisibility(View.VISIBLE);
				_btnSelectFileToHide.setVisibility(View.VISIBLE);
				_editTextContentToHide.setVisibility(View.GONE);
				_cardLayoutCompressionText.setVisibility(View.GONE);
				_checkBoxFileToHide.setChecked(true);
				_encodeParameters.setHidingText(false);
			}
			updateImageViews();
		}
	};
	
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
				case R.id.btn_back_encode:
					finish();
					break;
				case R.id.btn_camera:
					recordVideo();
					break;
				case R.id.btn_settings_encode:
					Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
					startActivityForResult(intent, SETTINGS_ACCESS);
					break;					
				case R.id.btn_select_video_source_encode:
					showFileChooser(CHOOSE_VIDEO_CONTAINER);
					break;
				case R.id.btn_select_video_destination_encode:
					showDirectoryChooser();
					break;
				case R.id.btn_select_file_to_hide:
					showFileChooser(CHOOSE_FILE_CONTENT);
					break;
				case R.id.btn_encode:	
					process();
					break;
				default:
					ErrorManager.getInstance().addErrorMessage("[Encode Activity] Click event not known");
					ErrorManager.getInstance().displayErrorMessages(arg0.getContext());
					break;
			}
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
			if (s != null) {
				_encodeParameters.setCryptographyKey(s.toString());
			} 
			updateImageViews();
		}
	};

	private TextWatcher onTextChangedListenerTextToHide = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if (s != null) {
				_encodeParameters.setTextToHide(s.toString());
			}
			updateImageViews();
		}
	};
	
}
