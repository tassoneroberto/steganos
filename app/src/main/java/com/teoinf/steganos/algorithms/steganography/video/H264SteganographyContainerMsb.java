package com.teoinf.steganos.algorithms.steganography.video;

/**
 * Created by simone on 22/03/18.
 */

import com.coremedia.iso.IsoTypeReaderVariable;
import com.googlecode.mp4parser.authoring.Sample;
import com.teoinf.steganos.msb.MSBDecode;
import com.teoinf.steganos.msb.MSBEncode;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;




public class H264SteganographyContainerMsb extends H264SteganographyContainer {

    private final int BYTE_SIZE = 8;

    protected int _nbBitToHideInOneByte;

    private long _maxContentToHide;

    public H264SteganographyContainerMsb() {
        super();
        _nbBitToHideInOneByte = 1;
        _maxContentToHide = -1;
    }

    // Parent methods
    @Override
    public void hideData(byte[] dataToHide) {
        MSBEncode encoder;
        ByteBuffer currentSampleBuffer;
        byte sample[];
        int currentSampleLength;
        int sliceDataOffset;

        if (_sampleList == null || dataToHide == null) {
            return;
        }

        encoder = new MSBEncode(dataToHide, _nbBitToHideInOneByte);
        for (Sample s : _sampleList) {
            currentSampleBuffer = s.asByteBuffer();
            while (currentSampleBuffer.hasRemaining()) {
                currentSampleLength = (int) IsoTypeReaderVariable.read(currentSampleBuffer, _sampleLengthSize);
                this.addData(new byte[] {0x00, 0x00, 0x01});
                sliceDataOffset = this.getSliceLayerWithoutPartitioningIdrDataOffset((ByteBuffer) currentSampleBuffer.slice().limit(currentSampleLength));

                if (sliceDataOffset == -1) {
                    this.addData((ByteBuffer) currentSampleBuffer.slice().limit(currentSampleLength));
                    currentSampleBuffer.position(currentSampleBuffer.position() + currentSampleLength);
                    continue;
                }

                this.addData((ByteBuffer) currentSampleBuffer.slice().limit(sliceDataOffset));
                currentSampleBuffer.position(currentSampleBuffer.position() + sliceDataOffset);

                // Sample
                sample = new byte[currentSampleLength - sliceDataOffset];
                currentSampleBuffer.get(sample);
                encoder.encodeNextFrame(sample);
                sample = insertEscapeSequence(sample);
                this.addData(sample);
                _subSampleIdx++;
            }
            _subSampleIdx = 0;
            _sampleListPosition++;
        }
    }

    @Override
    public void unHideData() {
        MSBDecode decoder;
        ByteBuffer currentSampleBuffer;
        byte[] sample;
        int currentSampleLength;
        int sliceDataOffset;

        if (_sampleList == null) {
            return;
        }
        decoder = new MSBDecode();
        for (Sample s : _sampleList) {
            currentSampleBuffer = s.asByteBuffer();
            while (currentSampleBuffer.hasRemaining()) {
                currentSampleLength = (int) IsoTypeReaderVariable.read(currentSampleBuffer, _sampleLengthSize);
                sliceDataOffset = this.getSliceLayerWithoutPartitioningIdrDataOffset((ByteBuffer) currentSampleBuffer.slice().limit(currentSampleLength));

                if (sliceDataOffset == -1) {
                    currentSampleBuffer.position(currentSampleBuffer.position() + currentSampleLength);
                    continue;
                }

                currentSampleBuffer.position(currentSampleBuffer.position() + sliceDataOffset);

                // Sample
                sample = new byte[currentSampleLength - sliceDataOffset];
                currentSampleBuffer.get(sample);
                sample = removeEscapeSequence(sample);
                _unHideData = decoder.decodeFrame(sample);
                if (_unHideData != null) {
                    return;
                }
            }
        }
    }

    @Override
    public long getMaxContentToHide() {
        if (_maxContentToHide == -1) {
            reckonMaxContentToHide();
        }
        return _maxContentToHide;
    }

    // Private methods
    private byte[] insertEscapeSequence(byte sample[]) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (sample == null) {
            return byteArrayOutputStream.toByteArray();
        }

        for (int i = 0; i < sample.length; ++i) {
            if (i + 2 < sample.length && sample[i] == 0x00 && sample[i + 1] == 0x00 && sample[i + 2] == 0x01) {
                byteArrayOutputStream.write(sample[i]);
                byteArrayOutputStream.write(sample[i + 1]);
                byteArrayOutputStream.write(0x03);
                byteArrayOutputStream.write(sample[i + 2]);
                i += 2;
            } else {
                byteArrayOutputStream.write(sample[i]);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] removeEscapeSequence(byte sample[]) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (sample == null) {
            return byteArrayOutputStream.toByteArray();
        }
        for (int i = 0; i < sample.length; ++i) {
            if (i + 2 < sample.length && sample[i] == 0x00 && sample[i + 1] == 0x00 && sample[i + 2] == 0x03) {
                byteArrayOutputStream.write(sample[i]);
                byteArrayOutputStream.write(sample[i + 1]);
                i += 2;
            } else {
                byteArrayOutputStream.write(sample[i]);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void reckonMaxContentToHide() {
        ByteBuffer currentSampleBuffer;
        int currentSampleLength;
        int sliceDataOffset;
        long ret = 0;
        float sizeNeededToHideOneByte = (float) Math.ceil((float) BYTE_SIZE / _nbBitToHideInOneByte);

        if (_sampleList != null) {
            for (Sample s : _sampleList) {
                currentSampleBuffer = s.asByteBuffer();
                while (currentSampleBuffer.hasRemaining()) {
                    currentSampleLength = (int) IsoTypeReaderVariable.read(currentSampleBuffer, _sampleLengthSize);
                    sliceDataOffset = this.getSliceLayerWithoutPartitioningIdrDataOffset((ByteBuffer) currentSampleBuffer.slice().limit(currentSampleLength));
                    currentSampleBuffer.position(currentSampleBuffer.position() + currentSampleLength);

                    if (sliceDataOffset == -1) {
                        continue;
                    }
                    ret += (currentSampleLength - sliceDataOffset);
                }
            }
        }
        ret -= (8 * sizeNeededToHideOneByte);
        ret /= Math.floor(sizeNeededToHideOneByte);
        _maxContentToHide = ret;
    }
}
