package com.teoinf.steganos.algorithms.data;

import com.teoinf.steganos.algorithms.IDataAlgorithm;

public class CryptographyAlgorithmData implements IDataAlgorithm {

	private String _displayName;
	private int _keyLength;
	private String _path;
	
	public CryptographyAlgorithmData() {
		_displayName = "";
		_keyLength = 0;
		_path = "";
	}
	
	public CryptographyAlgorithmData(String displayName, int keylength, String path) {
		_displayName = displayName;
		_keyLength = keylength;
		_path = path;
	}
	
	public void setKeyLength(int keylength) {
		_keyLength = keylength;
	}
	
	public int getKeyLength() {
		return _keyLength;
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
}
