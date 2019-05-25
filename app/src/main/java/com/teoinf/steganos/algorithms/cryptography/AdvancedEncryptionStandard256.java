package com.teoinf.steganos.algorithms.cryptography;

import com.teoinf.steganos.error.ErrorManager;


public class AdvancedEncryptionStandard256 extends AdvancedEncryptionStandard {

	private final int KEY_LENGTH = 32;
	
	public AdvancedEncryptionStandard256() {
	}

	@Override
	public byte[] encrypt(byte[] message, byte[] key) {
		boolean valid = true;
		
		if (message == null) {
			ErrorManager.getInstance().addErrorMessage("[AES 256]: Message to encrypt is null");
			valid = false;
		}
		if (key == null) {
			ErrorManager.getInstance().addErrorMessage("[AES 256]: Key is null");
			valid = false;
		} else if (!checkKeyLength(key.length)) {
			ErrorManager.getInstance().addErrorMessage("[AES 256]: Key must be " + KEY_LENGTH + " length");			
			valid = false;
		} 
		return (valid ? super.encryptionProcess(AESType.AES_256, message, key) : null);
	}

	@Override
	public byte[] decrypt(byte[] cipher, byte[] key) {
		boolean valid = true;
		
		if (cipher == null) {
			ErrorManager.getInstance().addErrorMessage("[AES 256]: Cipher to decrypt is null");
			valid = false;
		}
		if (key == null) {
			ErrorManager.getInstance().addErrorMessage("[AES 256]: Key is null");
			valid = false;
		} else if (!checkKeyLength(key.length)) {
			ErrorManager.getInstance().addErrorMessage("[AES 256]: Key must be " + KEY_LENGTH + " length");			
			valid = false;
		} 
		return (valid ? super.decryptionProcess(AESType.AES_256, cipher, key) : null);
	}

	private boolean checkKeyLength(int keylength) {
		return keylength == KEY_LENGTH;
	}
}
