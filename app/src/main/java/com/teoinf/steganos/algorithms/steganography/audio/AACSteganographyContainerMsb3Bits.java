package com.teoinf.steganos.algorithms.steganography.audio;

/**
 * Created by simone on 22/03/18.
 */

public class AACSteganographyContainerMsb3Bits extends AACSteganographyContainerMsb {

    private final int BITS_TO_HIDE_IN_ONE_BYTE = 3;

    public AACSteganographyContainerMsb3Bits() {
        super();
        _nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
    }
}

