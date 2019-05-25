package com.teoinf.steganos.algorithms.steganography.video;

public final class H264SteganographyContainerLsb4Bits extends H264SteganographyContainerLsb {

	private final int BITS_TO_HIDE_IN_ONE_BYTE = 4;
	
	public H264SteganographyContainerLsb4Bits() {
		super();
		_nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
	}	
}
