package com.teoinf.steganos.algorithms;

public interface ICryptographyAlgorithm {

	public byte[] encrypt(byte[] message, byte[] key);
	public byte[] decrypt(byte[] cipher, byte[] key);

	public int getBlockSize();
}
