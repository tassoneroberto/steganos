package com.teoinf.steganos.algorithms.steganography.audio;

public final class AACSteganographyContainerLsb4Bits extends AACSteganographyContainerLsb {

	private final int BITS_TO_HIDE_IN_ONE_BYTE = 4;
	
	public AACSteganographyContainerLsb4Bits() {
		super();
		_nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
	}
}
