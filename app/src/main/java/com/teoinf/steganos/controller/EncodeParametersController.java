package com.teoinf.steganos.controller;

import com.teoinf.steganos.R;
import com.teoinf.steganos.algorithms.data.CryptographyAlgorithmData;
import com.teoinf.steganos.configuration.Configuration;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.parameters.EncodeParameters;
import com.teoinf.steganos.tools.Utils;

public class EncodeParametersController {

	private boolean _quite;
	
	public EncodeParametersController(boolean quite) {
		_quite = quite;
	}

	public Boolean controlAllData(EncodeParameters parameters) {
		boolean ret = true;
		
		ret &= controlSrcVideoPath(parameters);
		ret &= controlDestVideoPath(parameters);
		ret &= controlTextToHide(parameters);
		ret &= controlFileToHide(parameters);
		return ret;
	}

	public Boolean controlSrcVideoPath(EncodeParameters parameters) {
		if (parameters == null) {
			return false;
		}
		if (parameters.getSourceVideoPath() == null || parameters.getSourceVideoPath().isEmpty()) {
			if (!_quite) {
				ErrorManager.getInstance().addErrorMessage(R.string.error_src_video_path_empty_string);
			}
			return false;
		}
		return controlEnoughSpaceOnSdCard(parameters);
	}
	
	public Boolean controlDestVideoPath(EncodeParameters parameters) {
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
		
	public boolean controlContentToHide(EncodeParameters parameters) {
		if (parameters == null) {
			return false;
		}
		if (parameters.isHidingText()) {
			return controlTextToHide(parameters);
		}
		return controlFileToHide(parameters);
	}
	
	private Boolean controlTextToHide(EncodeParameters parameters) {
		if (parameters.isHidingText() && (parameters.getTextToHide() == null || parameters.getTextToHide().isEmpty())) {
			if (!_quite) {
				ErrorManager.getInstance().addErrorMessage(R.string.error_text_to_hide_empty_string);
			}
			return false;
		}
		return true;
	}
	
	private Boolean controlFileToHide(EncodeParameters parameters) {
		if (!parameters.isHidingText() && (parameters.getFileToHidePath() == null || parameters.getFileToHidePath().isEmpty())) {
			if (!_quite) {
				ErrorManager.getInstance().addErrorMessage(R.string.error_file_to_hide_empty_string);
			}
			return false;
		}
		return true;
	}

	private Boolean controlEnoughSpaceOnSdCard(EncodeParameters parameters) {
		long free = Utils.getAvailableBytesOnSdcard();
		long required = Utils.getFileSize(parameters.getSourceVideoPath());
		
		if (required == -1) {
			return false;
		}
		
		if (required > 10000000) { // 10 MO
			required *= 2;
		}
		if (required >= free) {
			if (!_quite) {
				ErrorManager.getInstance().addErrorMessage("Not enough space on SD Card! This operation requires at least " + required + " bytes free on the SD Card");
			}
			return false;
		}
		return true;
	}
	
	public boolean controlCryptographyKey(EncodeParameters parameters) {
		String path;
		CryptographyAlgorithmData data;
		
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
