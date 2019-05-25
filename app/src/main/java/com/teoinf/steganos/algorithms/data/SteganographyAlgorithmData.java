package com.teoinf.steganos.algorithms.data;

import com.teoinf.steganos.algorithms.IDataAlgorithm;

public class SteganographyAlgorithmData implements IDataAlgorithm {

	public enum SteganographyChannelType{
		AUDIO,
		VIDEO,
		METADATA,
		NONE
	}
	
	private String _displayName;
	private String _path;
	private SteganographyChannelType _steganographyChannel;
	
	public SteganographyAlgorithmData() {
		_displayName = "";
		_path = "";
		_steganographyChannel = SteganographyChannelType.NONE;
	}
	
	public SteganographyAlgorithmData(String displayName, String path, SteganographyChannelType steganographyChannel) {
		_displayName = displayName;
		_path = path;
		_steganographyChannel = steganographyChannel;
	}
	
	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}
	
	public String getDisplayName() {
		return _displayName;
	}
	
	public void setPath(String path) {
		_path = path;
	}
	
	public String getPath() {
		return _path;
	}
	
	public void setSteganographyChannelType(SteganographyChannelType steganographyChannelType) {
		_steganographyChannel = steganographyChannelType;
	}
	
	public SteganographyChannelType getSteganographyChannelType() {
		return _steganographyChannel;
	}
	
}
