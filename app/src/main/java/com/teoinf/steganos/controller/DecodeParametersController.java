package com.teoinf.steganos.controller;

import com.teoinf.steganos.R;
import com.teoinf.steganos.algorithms.data.CryptographyAlgorithmData;
import com.teoinf.steganos.configuration.Configuration;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.parameters.DecodeParameters;

public class DecodeParametersController {

	private boolean _quite;
	
	public DecodeParametersController(boolean quite) {
		_quite = quite;
	}
	
	public Boolean controlAllData(DecodeParameters parameters) {
		boolean ret = true;
		
		ret &= controlSrcVideoPath(parameters);
		ret &= controlDestFilePath(parameters);
		ret &= controlCryptographyKey(parameters);
		return ret;
	}

	public Boolean controlSrcVideoPath(DecodeParameters parameters) {
		if (parameters == null) {
			return false;
		}
		if ((parameters.getVideoPath() == null || parameters.getVideoPath().isEmpty())) {
			if (!_quite) {
				ErrorManager.getInstance().addErrorMessage(R.string.error_src_video_path_empty_string);
			}
			return false;
		}
		return true;
	}
	
	public Boolean controlDestFilePath(DecodeParameters parameters) {
		if (parameters == null) {
			return false;
		}
		if (parameters.getDestinationVideoDirectory() == null || parameters.getDestinationVideoDirectory().isEmpty()) {
			if (!_quite) {
				ErrorManager.getInstance().addErrorMessage(R.string.error_dest_video_empty_string);
			}
			return false;
		}
		return true;
	}
	
	public boolean controlCryptographyKey(DecodeParameters parameters) {
		String path;
		CryptographyAlgorithmData data;
		
		if (!Preferences.getInstance().getUseCryptography())
			return true;
		
		if (parameters == null) {
			return false;
		}
		path = Preferences.getInstance().getCryptographyAlgorithm();
		if (Preferences.getInstance().getUseCryptography() && (path == null || path.isEmpty())) {
			return false;
		}
		
		data = Configuration.getInstance().getCryptographyAlgorithmDataByPath(path);
		if (parameters.getCryptographyKey() != null && data != null
				&& data.getKeyLength() == parameters.getCryptographyKey().length()) { 
			return true;
		}
		return false;
	}
}
