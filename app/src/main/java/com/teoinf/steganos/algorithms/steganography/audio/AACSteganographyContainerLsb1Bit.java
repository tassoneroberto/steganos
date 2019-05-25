package com.teoinf.steganos.algorithms.steganography.audio;

public final class AACSteganographyContainerLsb1Bit extends AACSteganographyContainerLsb {

	private final int BITS_TO_HIDE_IN_ONE_BYTE = 1;
	
	public AACSteganographyContainerLsb1Bit() {
		super();
		_nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
	}
}
