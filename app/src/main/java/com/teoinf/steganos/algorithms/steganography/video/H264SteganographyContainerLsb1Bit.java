package com.teoinf.steganos.algorithms.steganography.video;

public final class H264SteganographyContainerLsb1Bit extends H264SteganographyContainerLsb{

	private final int BITS_TO_HIDE_IN_ONE_BYTE = 1;
	
	public H264SteganographyContainerLsb1Bit() {
		super();
		_nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
	}	
}
