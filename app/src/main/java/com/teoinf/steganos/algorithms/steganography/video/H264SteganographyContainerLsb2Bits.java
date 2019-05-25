package com.teoinf.steganos.algorithms.steganography.video;

public final class H264SteganographyContainerLsb2Bits extends H264SteganographyContainerLsb {

	private final int BITS_TO_HIDE_IN_ONE_BYTE = 2;
	
	public H264SteganographyContainerLsb2Bits() {
		super();
		_nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
	}	
}
