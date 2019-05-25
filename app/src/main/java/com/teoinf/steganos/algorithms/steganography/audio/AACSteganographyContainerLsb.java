package com.teoinf.steganos.algorithms.steganography.audio;

import java.nio.ByteBuffer;

import com.googlecode.mp4parser.authoring.Sample;
import com.teoinf.steganos.lsb.LSBDecode;
import com.teoinf.steganos.lsb.LSBEncode;

public class AACSteganographyContainerLsb extends AACSteganographyContainer {

	private final int BYTE_SIZE = 8;
	
	protected int _nbBitToHideInOneByte;

	public AACSteganographyContainerLsb() {
		_nbBitToHideInOneByte = 1;
	}

	//Parent methods
	@Override
	public void unHideData() {
		LSBDecode decoder = new LSBDecode();
		
		if (_sampleList == null) {
			return;
		}
		
		for (Sample sample : _sampleList) {
			byte[] frame = sampleToByteArray(sample);
			
			_unHideData = decoder.decodeFrame(frame);
			if (_unHideData != null){
				break;
			}
		}
	}
	
	@Override
	public void hideData(byte[] dataToHide) {
		
		if (_sampleList == null || dataToHide == null) {
			return;
		}
			
		LSBEncode encoder = new LSBEncode(dataToHide, _nbBitToHideInOneByte);
		for (Sample sample : _sampleList) {
			byte[] frame = sampleToByteArray(sample);

			frame = encoder.encodeNextFrame(frame);
			writeHeader(frame.length);
			this.addData(frame);
			
			_sampleListPosition++;
		}
	}
	
	@Override
	public byte[] getUnHideData() {
		return _unHideData;
	}

	@Override
	public long getMaxContentToHide() {
		long ret = 0;
		float sizeNeededToHideOneByte = (float) Math.ceil((float) BYTE_SIZE / _nbBitToHideInOneByte);
		
		if (_sampleList != null) {
			for (Sample s : _sampleList) {
				ret += s.getSize();
			}
		}

		ret -= (8 * sizeNeededToHideOneByte);
		ret /= Math.floor(sizeNeededToHideOneByte);
		return ret;
	}
	
	// Private methods
	private byte[] sampleToByteArray(Sample sample) {
		ByteBuffer buf = sample.asByteBuffer();
		byte[] frame = new byte[buf.capacity()];
		buf.get(frame);
		
		return frame;
	}

}
