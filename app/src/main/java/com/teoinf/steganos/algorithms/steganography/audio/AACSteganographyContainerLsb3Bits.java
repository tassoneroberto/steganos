package com.teoinf.steganos.algorithms.steganography.audio;

public final class AACSteganographyContainerLsb3Bits extends AACSteganographyContainerLsb {

	private final int BITS_TO_HIDE_IN_ONE_BYTE = 3;
	
	public AACSteganographyContainerLsb3Bits() {
		super();
		_nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
	}
}
