package com.teoinf.steganos.tools;

import java.nio.ByteBuffer;

public class BitBufferReader {

	private byte[] _data;
	private int _currentOffset;
	private int _currentBitOffset;
	
	public BitBufferReader(ByteBuffer buffer) {
		_currentOffset = 0;
		_currentBitOffset = 0;
		_data = new byte[buffer.remaining()];
		buffer.get(_data);
	}
	
	public BitBufferReader(byte[] data) {
		_currentOffset = 0;
		_currentBitOffset = 0;
		_data = data;
	}
	
	public long readNBits(int size) {
		long value = 0;
		
		if (_data == null || _currentOffset >= _data.length) {
			return value;
		}
				
		for (int i = 0; i < size; ++i) {
			value <<= 1;
			value |= readBit();
		}
		return value;
	}
	
	public int readUE() {
		int leadingZeroBits = 0;
		int res = 0;
        
		while (readBit() == 0 && hasMoreData()) {
			leadingZeroBits++;
		}
		
		res = (1 << leadingZeroBits) - 1 + (int) readNBits(leadingZeroBits);
		
        return res;
	}
	
	public int readSE() {
        int val = readUE();
        int sign = ((val & 0x1) << 1) - 1;

        val = ((val >> 1) + (val & 0x1)) * sign;

        return val;
    }
	
	public int readAE() {
		throw new UnsupportedOperationException();
	}
	
	public int readCE() {
		throw new UnsupportedOperationException();
	}
	
	public boolean hasMoreData() {
		return _currentOffset < _data.length;
	}
	
	private int readBit() {
		byte current;
		int value = 0;
		
		if (_data == null || _currentOffset >= _data.length) {
			return value;
		}
		
		current = _data[_currentOffset];
		value = (current >> (7 - _currentBitOffset)) & 0x01; 
        _currentBitOffset++;
        if (_currentBitOffset >= 8) {
        	_currentBitOffset = 0;
        	_currentOffset++;
        }
        return value;
	}

	public int getCurrentOffset() {
		return _currentOffset;
	}

	public int getCurrentBitOffset() {
		return _currentBitOffset;
	}
	
	
}
