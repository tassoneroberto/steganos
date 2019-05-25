package com.teoinf.steganos.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class Preferences {

	// Default values
	private final String DEFAULT_STEGANOGRAPHY_AUDIO_ALGORITHM = "com.teoinf.steganos.algorithms.steganography.audio.AACSteganographyContainerLsb1Bit";
	private final String DEFAULT_STEGANOGRAPHY_VIDEO_ALGORITHM = "com.teoinf.steganos.algorithms.steganography.video.H264SteganographyContainerLsb1Bit";
	private final String DEFAULT_CRYPTOGRAPHY_ALGORITHM = "com.teoinf.steganos.algorithms.cryptography.AdvancedEncryptionStandard128";
	
	// Keys
	private final String KEY_USE_AUDIO_CHANNEL = "com.teoinf.steganos.USE_AUDIO_CHANNEL_KEY";
	private final String KEY_AUDIO_ALGORITHM = "com.teoinf.steganos.AUDIO_ALGORITHM_KEY";

	private final String KEY_USE_VIDEO_CHANNEL = "com.teoinf.steganos.USE_VIDEO_CHANNEL_KEY";
	private final String KEY_VIDEO_ALGORITHM = "com.teoinf.steganos.VIDEO_ALGORITHM_KEY";
	
	private final String KEY_USE_METADATA_CHANNEL = "com.teoinf.steganos.USE_METADATA_CHANNEL_KEY";
	private final String KEY_METADATA_ALGORITHM = "com.teoinf.steganos.METADATA_ALGORITHM_KEY";
	
	private final String KEY_USE_CRYPTOGRAPHY = "com.teoinf.steganos.USE_CRYPTOGRAPHY_KEY";
	private final String KEY_CRYPTOGRAPHY_ALGORITHM = "com.teoinf.steganos.CRYPTOGRAPHY_ALGORITHM_KEY";
	
	private final String FILE_PREFERENCE = "com.teoinf.steganos.preferences";
	
	private Activity _mainActivity;
	private boolean _useAudioChannel;
	private boolean _useVideoChannel;
	private boolean _useMetadataChannel;
	private boolean _useCryptography;
	private String _audioAlgorithm;
	private String _videoAlgorithm;
	private String _metadataAlgorithm;
	private String _cryptographyAlgorithm;
	// TODO: Complete the list of attributes

	private static Preferences s_configuration;
	
	public static Preferences	getInstance() {
		if (s_configuration == null) {
			s_configuration = new Preferences();
			s_configuration.init(null);
		}
		return (s_configuration);
	}
	
	public static Preferences	getInstance(Activity mainActivity) {
		if (s_configuration == null) {
			s_configuration = new Preferences();
			s_configuration.init(mainActivity);
		}
		return (s_configuration);
	}
	
	private Preferences() {
		
	}
	
	private void init(Activity activity) {
		this._useAudioChannel = false;
		this._useVideoChannel = false;
		this._useMetadataChannel = false;
		this._useCryptography = false;
		this._audioAlgorithm = "";
		this._videoAlgorithm = "";
		this._metadataAlgorithm = "";
		this._cryptographyAlgorithm = "";
		this._mainActivity = activity;
	}
	
	public boolean getUseAudioChannel() {
		return _useAudioChannel;
	}

	public void setUseAudioChannel(boolean useAudioChannel) {
		this._useAudioChannel = useAudioChannel;
		saveData();
	}

	public boolean getUseVideoChannel() {
		return _useVideoChannel;
	}

	public void setUseVideoChannel(boolean useVideoChannel) {
		this._useVideoChannel = useVideoChannel;
		saveData();
	}

	public boolean getUseMetadataChannel() {
		return _useMetadataChannel;
	}

	public void setUseMetadataChannel(boolean useMetadataChannel) {
		this._useMetadataChannel = useMetadataChannel;
		saveData();
	}
	
	public boolean getUseCryptography() {
		return _useCryptography;
	}

	public void setUseCryptography(boolean useCryptography) {
		this._useCryptography = useCryptography;
		saveData();
	}

	public String getAudioAlgorithm() {
		return _audioAlgorithm;
	}

	public void setAudioAlgorithm(String audioAlgorithm) {
		this._audioAlgorithm = audioAlgorithm;
		saveData();
	}

	public String getVideoAlgorithm() {
		return _videoAlgorithm;
	}

	public void setVideoAlgorithm(String videoAlgorithm) {
		this._videoAlgorithm = videoAlgorithm;
		saveData();
	}

	public String getMetadataAlgorithm() {
		return _metadataAlgorithm;
	}

	public void setMetadataAlgorithm(String metadataAlgorithm) {
		this._metadataAlgorithm = metadataAlgorithm;
		saveData();
	}

	public String getCryptographyAlgorithm() {
		return _cryptographyAlgorithm;
	}

	public void setCryptographyAlgorithm(String cryptographyAlgorithm) {
		this._cryptographyAlgorithm = cryptographyAlgorithm;
		saveData();
	}
	
	public void saveData() {
		SharedPreferences sharedPref;
		SharedPreferences.Editor editor;

		if (_mainActivity == null) {
			Log.d("DEBUG", "Configuration: Unable to save data...");
			return;
		}
		
		sharedPref = _mainActivity.getSharedPreferences(FILE_PREFERENCE, Context.MODE_PRIVATE);
		editor = sharedPref.edit();

		editor.putBoolean(KEY_USE_AUDIO_CHANNEL, _useAudioChannel);
		editor.putBoolean(KEY_USE_VIDEO_CHANNEL, _useVideoChannel);
		editor.putBoolean(KEY_USE_METADATA_CHANNEL, _useMetadataChannel);
		editor.putBoolean(KEY_USE_CRYPTOGRAPHY, _useCryptography);
		
		editor.putString(KEY_AUDIO_ALGORITHM, _audioAlgorithm);
		editor.putString(KEY_VIDEO_ALGORITHM, _videoAlgorithm);
		editor.putString(KEY_METADATA_ALGORITHM, _metadataAlgorithm);
		editor.putString(KEY_CRYPTOGRAPHY_ALGORITHM, _cryptographyAlgorithm);
		
		if (!editor.commit()) {
			Log.d("DEBUG", "Error while commiting preferences");
		}
	}
	
	public void loadData() {
		SharedPreferences sharedPref;
		
		if (_mainActivity == null) {
			Log.d("DEBUG", "Configuration: Unable to load data...");
			return;
		}
		
		sharedPref = _mainActivity.getSharedPreferences(FILE_PREFERENCE, Context.MODE_PRIVATE);

		_useAudioChannel = sharedPref.getBoolean(KEY_USE_AUDIO_CHANNEL, false);
		_useVideoChannel = sharedPref.getBoolean(KEY_USE_VIDEO_CHANNEL, false);
		_useMetadataChannel = sharedPref.getBoolean(KEY_USE_METADATA_CHANNEL, false);
		_useCryptography = sharedPref.getBoolean(KEY_USE_CRYPTOGRAPHY, false);
		
		_audioAlgorithm = sharedPref.getString(KEY_AUDIO_ALGORITHM, DEFAULT_STEGANOGRAPHY_AUDIO_ALGORITHM);
		_videoAlgorithm = sharedPref.getString(KEY_VIDEO_ALGORITHM, DEFAULT_STEGANOGRAPHY_VIDEO_ALGORITHM);
		_metadataAlgorithm = sharedPref.getString(KEY_METADATA_ALGORITHM, "");
		_cryptographyAlgorithm = sharedPref.getString(KEY_CRYPTOGRAPHY_ALGORITHM, DEFAULT_CRYPTOGRAPHY_ALGORITHM);
	}
	
}
