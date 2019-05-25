package com.teoinf.steganos.algorithms.steganography.audio;

/**
 * Created by simone on 22/03/18.
 */

public class AACSteganographyContainerMsb1Bit extends AACSteganographyContainerMsb {

    private final int BITS_TO_HIDE_IN_ONE_BYTE = 1;

    public AACSteganographyContainerMsb1Bit() {
        super();
        _nbBitToHideInOneByte = BITS_TO_HIDE_IN_ONE_BYTE;
    }
}
