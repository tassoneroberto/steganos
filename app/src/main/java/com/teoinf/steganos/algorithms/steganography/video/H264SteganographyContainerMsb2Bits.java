package com.teoinf.steganos.algorithms.steganography.video;

/**
 * Created by simone on 22/03/18.
 */

public class H264SteganographyContainerMsb2Bits extends H264SteganographyContainerMsb{

    private final int BITS_TO_HIDE_IN_ONE_BYTE = 2;

    public H264SteganographyContainerMsb2Bits() {
        super();
        _nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
    }
}
