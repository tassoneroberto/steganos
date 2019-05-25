package com.teoinf.steganos.algorithms.cryptography;

import com.teoinf.steganos.algorithms.ICryptographyAlgorithm;

abstract class AdvancedEncryptionStandard implements ICryptographyAlgorithm {

	private final int BLOCK_SIZE = 16;
	
	protected enum AESType {
		AES_128(10, 4),
		AES_192(12, 6),
		AES_256(14, 8),
		AES_NONE(0, 0);
		
		private int _rounds;
		private int _keyColumns;
		
		private AESType(int round, int keyColumns) {
			_rounds = round;
			_keyColumns = keyColumns;
		}
		
		public int getRoundsNumber() {
			return _rounds;
		}
		
		public int getKeyColumnsNumber() {
			return _keyColumns;
		}
	};
	
	private final byte[][] matrixMixColumns = {
			{0x02, 0x01, 0x01, 0x03},
			{0x03, 0x02, 0x01, 0x01},
			{0x01, 0x03, 0x02, 0x01},
			{0x01, 0x01, 0x03, 0x02}};
	
	private final byte[][] invMatrixMixColumns = {
			{0x0e, 0x09, 0x0d, 0x0b},
			{0x0b, 0x0e, 0x09, 0x0d},
			{0x0d, 0x0b, 0x0e, 0x09},
			{0x09, 0x0d, 0x0b, 0x0e}};
	
	private final byte[][] sbox = {
			{0x63,(byte) 0xca, (byte) 0xb7, 0x04, 0x09, 0x53, (byte) 0xd0, 0x51, (byte) 0xcd, 0x60,(byte) 0xe0, (byte) 0xe7,(byte) 0xba, 0x70,(byte) 0xe1,(byte) 0x8c},
			{0x7c,(byte) 0x82,(byte) 0xfd,(byte) 0xc7,(byte) 0x83,(byte) 0xd1,(byte) 0xef,(byte) 0xa3, 0x0c,(byte) 0x81, 0x32,(byte) 0xc8, 0x78, 0x3e,(byte) 0xf8,(byte) 0xa1},
			{0x77,(byte) 0xc9,(byte) 0x93, 0x23, 0x2c, 0x00,(byte) 0xaa, 0x40, 0x13, 0x4f, 0x3a, 0x37, 0x25,(byte) 0xb5,(byte) 0x98,(byte) 0x89},
			{0x7b, 0x7d, 0x26,(byte) 0xc3, 0x1a,(byte) 0xed,(byte) 0xfb,(byte) 0x8f,(byte) 0xec,(byte) 0xdc, 0x0a, 0x6d, 0x2e, 0x66, 0x11, 0x0d},
			{(byte) 0xf2,(byte) 0xfa, 0x36, 0x18, 0x1b, 0x20, 0x43,(byte) 0x92, 0x5f, 0x22, 0x49,(byte) 0x8d, 0x1c, 0x48, 0x69,(byte) 0xbf},
			{0x6b, 0x59, 0x3f,(byte) 0x96, 0x6e,(byte) 0xfc, 0x4d,(byte) 0x9d,(byte) 0x97, 0x2a, 0x06,(byte) 0xd5,(byte) 0xa6, 0x03,(byte) 0xd9,(byte) 0xe6},
			{0x6f, 0x47,(byte) 0xf7, 0x05, 0x5a,(byte) 0xb1, 0x33, 0x38, 0x44,(byte) 0x90, 0x24, 0x4e,(byte) 0xb4,(byte) 0xf6,(byte) 0x8e, 0x42},
			{(byte) 0xc5,(byte) 0xf0,(byte) 0xcc,(byte) 0x9a,(byte) 0xa0, 0x5b,(byte) 0x85,(byte) 0xf5, 0x17,(byte) 0x88, 0x5c,(byte) 0xa9,(byte) 0xc6, 0x0e,(byte) 0x94, 0x68},
			{0x30,(byte) 0xad, 0x34, 0x07, 0x52, 0x6a, 0x45,(byte) 0xbc,(byte) 0xc4, 0x46,(byte) 0xc2, 0x6c,(byte) 0xe8, 0x61,(byte) 0x9b, 0x41},
			{0x01,(byte) 0xd4,(byte) 0xa5, 0x12, 0x3b,(byte) 0xcb,(byte) 0xf9,(byte) 0xb6,(byte) 0xa7,(byte) 0xee,(byte) 0xd3, 0x56,(byte) 0xdd, 0x35, 0x1e,(byte) 0x99},
			{0x67,(byte) 0xa2,(byte) 0xe5,(byte) 0x80,(byte) 0xd6,(byte) 0xbe, 0x02,(byte) 0xda, 0x7e,(byte) 0xb8,(byte) 0xac,(byte) 0xf4, 0x74, 0x57,(byte) 0x87, 0x2d},
			{(byte) 0x2b,(byte) 0xaf,(byte) 0xf1,(byte) 0xe2,(byte) 0xb3, 0x39, 0x7f, 0x21, 0x3d, 0x14, 0x62,(byte) 0xea, 0x1f,(byte) 0xb9,(byte) 0xe9, 0x0f},
			{(byte) 0xfe,(byte) 0x9c, 0x71,(byte) 0xeb, 0x29, 0x4a, 0x50, 0x10, 0x64,(byte) 0xde,(byte) 0x91, 0x65, 0x4b,(byte) 0x86,(byte) 0xce,(byte) 0xb0},
			{(byte) 0xd7,(byte) 0xa4,(byte) 0xd8, 0x27,(byte) 0xe3, 0x4c, 0x3c,(byte) 0xff, 0x5d, 0x5e,(byte) 0x95, 0x7a,(byte) 0xbd,(byte) 0xc1, 0x55, 0x54},
			{(byte) 0xab, 0x72, 0x31,(byte) 0xb2, 0x2f, 0x58,(byte) 0x9f,(byte) 0xf3, 0x19, 0x0b,(byte) 0xe4,(byte) 0xae,(byte) 0x8b, 0x1d, 0x28,(byte) 0xbb},
			{0x76,(byte) 0xc0, 0x15, 0x75,(byte) 0x84,(byte) 0xcf,(byte) 0xa8,(byte) 0xd2, 0x73,(byte) 0xdb, 0x79, 0x08,(byte) 0x8a,(byte) 0x9e,(byte) 0xdf, 0x16}};

	private final byte[][] invSbox = {
			{0x52, 0x7c, 0x54, 0x08, 0x72, 0x6c,(byte) 0x90,(byte) 0xd0, 0x3a,(byte) 0x96, 0x47,(byte) 0xfc, 0x1f, 0x60,(byte) 0xa0, 0x17},
			{0x09,(byte) 0xe3, 0x7b, 0x2e,(byte) 0xf8, 0x70,(byte) 0xd8, 0x2c,(byte) 0x91,(byte) 0xac,(byte) 0xf1, 0x56,(byte) 0xdd, 0x51,(byte) 0xe0, 0x2b},
			{0x6a, 0x39,(byte) 0x94,(byte) 0xa1,(byte) 0xf6, 0x48,(byte) 0xab, 0x1e, 0x11, 0x74, 0x1a, 0x3e,(byte) 0xa8, 0x7f, 0x3b, 0x04},
			{(byte) 0xd5,(byte) 0x82, 0x32, 0x66, 0x64, 0x50, 0x00,(byte) 0x8f, 0x41, 0x22, 0x71, 0x4b, 0x33,(byte) 0xa9, 0x4d, 0x7e},
			{0x30,(byte) 0x9b,(byte) 0xa6, 0x28,(byte) 0x86,(byte) 0xfd,(byte) 0x8c,(byte) 0xca, 0x4f,(byte) 0xe7, 0x1d,(byte) 0xc6,(byte) 0x88, 0x19,(byte) 0xae,(byte) 0xba},
			{0x36, 0x2f,(byte) 0xc2,(byte) 0xd9, 0x68,(byte) 0xed,(byte) 0xbc, 0x3f, 0x67,(byte) 0xad, 0x29,(byte) 0xd2, 0x07,(byte) 0xb5, 0x2a, 0x77},
			{(byte) 0xa5,(byte) 0xff, 0x23, 0x24,(byte) 0x98,(byte) 0xb9,(byte) 0xd3, 0x0f,(byte) 0xdc, 0x35,(byte) 0xc5, 0x79,(byte) 0xc7, 0x4a,(byte) 0xf5,(byte) 0xd6},
			{0x38,(byte) 0x87, 0x3d,(byte) 0xb2, 0x16,(byte) 0xda, 0x0a, 0x02,(byte) 0xea,(byte) 0x85,(byte) 0x89, 0x20, 0x31, 0x0d,(byte) 0xb0, 0x26},
			{(byte) 0xbf, 0x34,(byte) 0xee, 0x76,(byte) 0xd4, 0x5e,(byte) 0xf7,(byte) 0xc1,(byte) 0x97,(byte) 0xe2, 0x6f,(byte) 0x9a,(byte) 0xb1, 0x2d,(byte) 0xc8,(byte) 0xe1},
			{0x40,(byte) 0x8e, 0x4c, 0x5b,(byte) 0xa4, 0x15,(byte) 0xe4,(byte) 0xaf,(byte) 0xf2,(byte) 0xf9,(byte) 0xb7,(byte) 0xdb, 0x12,(byte) 0xe5,(byte) 0xeb, 0x69},
			{(byte) 0xa3, 0x43,(byte) 0x95,(byte) 0xa2, 0x5c, 0x46, 0x58,(byte) 0xbd,(byte) 0xcf, 0x37, 0x62,(byte) 0xc0, 0x10, 0x7a,(byte) 0xbb, 0x14},
			{(byte) 0x9e, 0x44, 0x0b, 0x49,(byte) 0xcc, 0x57, 0x05, 0x03,(byte) 0xce,(byte) 0xe8, 0x0e,(byte) 0xfe, 0x59,(byte) 0x9f, 0x3c, 0x63},
			{(byte) 0x81,(byte) 0xc4, 0x42, 0x6d, 0x5d,(byte) 0xa7,(byte) 0xb8, 0x01,(byte) 0xf0, 0x1c,(byte) 0xaa, 0x78, 0x27,(byte) 0x93,(byte) 0x83, 0x55},
			{(byte) 0xf3,(byte) 0xde,(byte) 0xfa,(byte) 0x8b, 0x65,(byte) 0x8d,(byte) 0xb3, 0x13,(byte) 0xb4, 0x75, 0x18,(byte) 0xcd,(byte) 0x80,(byte) 0xc9, 0x53, 0x21},
			{(byte) 0xd7,(byte) 0xe9,(byte) 0xc3,(byte) 0xd1,(byte) 0xb6,(byte) 0x9d, 0x45,(byte) 0x8a,(byte) 0xe6,(byte) 0xdf,(byte) 0xbe, 0x5a,(byte) 0xec,(byte) 0x9c,(byte) 0x99, 0x0c},
			{(byte) 0xfb,(byte) 0xcb, 0x4e, 0x25,(byte) 0x92,(byte) 0x84, 0x06, 0x6b, 0x73, 0x6e, 0x1b,(byte) 0xf4, 0x5f,(byte) 0xef, 0x61, 0x7d}};

	public AdvancedEncryptionStandard() {
	}
	
	public int getBlockSize() {
		return BLOCK_SIZE;
	}
	
	protected byte[] encryptionProcess(AESType type, byte[] message, byte[] key) {
		byte[][] cipher;
		byte[][][] roundKeys;
		AESType aesType;
		
		aesType = type;		
		cipher = mapInputData(message);
		roundKeys = constructRoundKeys(key, aesType);
		
		addRoundKey(cipher, roundKeys[0]);

		for (int i = 0; i < aesType.getRoundsNumber() - 1; ++i) {
			subBytes(cipher);
			shiftRows(cipher);
			mixColumns(cipher);
			addRoundKey(cipher, roundKeys[i + 1]);
		}
		
		subBytes(cipher);
		shiftRows(cipher);
		addRoundKey(cipher, roundKeys[roundKeys.length - 1]);
		
		return unmapData(cipher);
	}
	
	protected byte[] decryptionProcess(AESType type, byte[] cipher, byte[] key) {
		byte[][] message;
		byte[][][] roundKeys;
		AESType aesType;
		
		aesType = type;
		message = mapInputData(cipher);
		roundKeys = constructRoundKeys(key, aesType);
		
		addRoundKey(message, roundKeys[roundKeys.length - 1]);
		invSubBytes(message);
		invShiftRows(message);

		for (int i = aesType.getRoundsNumber() - 1; i > 0; --i) {
			addRoundKey(message, roundKeys[i]);
			invMixColumns(message);
			invShiftRows(message);
			invSubBytes(message);
		}
		
		addRoundKey(message, roundKeys[0]);

		return unmapData(message);	
	}
	
	// Private methods
	private byte[][] mapInputData(byte[] input) {
		byte[][] mappedData = new byte[4][4];
		int k = 0;
				
		for (int i = 0; i < mappedData.length; ++i) {
			for (int j = 0; j < mappedData[i].length; ++j) {
				if (k >= 16 || k >= input.length) { 
					break;
				}
				mappedData[i][j] = input[k];
				k++;
			}
		}
		return mappedData;
	}

	private byte[][][] mapKey(byte[] key, AESType type) {
		byte[][][] mappedKey = new byte[type.getRoundsNumber() + 1][4][4];
		int l = 0;
		boolean done = false;
		
		for (int i = 0; i < mappedKey.length && !done; ++i) {
			for (int j = 0; j < mappedKey[i].length && !done; ++j) {
				for (int k = 0; k < mappedKey[i][j].length; ++k) {
					if (l >= key.length) {
						done = true;
						break;
					}
					mappedKey[i][j][k] = key[l];
					l++;
				}
			}
		}
		return mappedKey;
	}
	
	private byte[] unmapData(byte[][] input) {
		byte[] result = new byte[16];
		int k = 0;
		
		for (int i = 0; i < input.length; ++i) {
			for (int j = 0; j < input[i].length; ++j) {
				if (k >= 16) { 
					break;
				}
				result[k] = input[i][j];
				k++;
			}
		}
		return result;
	}
		
	// Encryption Process functions
	private void subBytes(byte[][] input) {
		for (int i = 0; i < input.length; ++i) {
			subBytes(input[i]);
		}
	}

	private void subBytes(byte[] input) {
		int x;
		int y;

		for (int j = 0; j < input.length; ++j) {
			y = input[j] & 0x0F;
			x = (input[j] >> 4) & 0x0F;
			if (x < sbox[0].length && y < sbox.length) {
				input[j] = sbox[y][x];
			}
		}
		
	}
	
	private void shiftRows(byte[][] input) {
		int column = 0;
		byte tmp;
		
		while (column < 4) {
			for (int turn = 0; turn < column; ++turn) {
				for (int i = 0; i < input.length - 1; ++i) {
					if (i - 1 < 0) {
						tmp = input[input.length - 1][column];
						input[input.length - 1][column] = input[i][column];
					} else {
						tmp = input[i - 1][column];
						input[i - 1][column] = input[i][column];
					}
					input[i][column] = tmp;
				}
			}
			column++;
		}
	}
	
	private byte gfmul(byte a, byte b) {
		byte result = 0;
		byte highA;
		
		for (int i = 0; i < 8; ++i) {
			if ((b & 0x01) == 0x01) {
				result ^= a;
			}
			highA = (byte) ((a >> 7) & 0x01);
			a <<= 1;
			if (highA == 0x01) {
				a ^= 0x1b;
			}
			b >>= 1;
		}
		return result;
	}
	
	private void mixColumns(byte[][] input) {
		int columnMatrix = 0;
		int row;
		byte[] array;
		byte tmp;
		
		for (int i = 0; i < input.length; ++i) {
			row = 0;
			array = input[i].clone();
			while (row < 4) {
				tmp = 0;
				for (int j = 0; j < array.length; ++j) {
					tmp ^= gfmul(matrixMixColumns[columnMatrix][row], array[j]);
					columnMatrix++;
				}
				input[i][row] = tmp;
				row++;
				columnMatrix = 0;
			}
		}
	}
	
	private void xorInputArray(byte[] input, byte[] key) {
		for (int i = 0; i < input.length && i < key.length; ++i) {
			input[i] ^= key[i];
		}
	}

	private void addRoundKey(byte[][] data, byte[][] roundKey) {
		for (int i = 0; i < data.length && i < roundKey.length; ++i) {
			xorInputArray(data[i], roundKey[i]);
		}
	}	
	
	// Decryption Process functions
	private void invSubBytes(byte[][] input) {
		for (int i = 0; i < input.length; ++i) {
			invSubBytes(input[i]);
		}
	}

	private void invSubBytes(byte[] input) {
		int x;
		int y;

		for (int j = 0; j < input.length; ++j) {
			y = input[j] & 0x0F;
			x = (input[j] >> 4) & 0x0F;
			if (x < invSbox[0].length && y < invSbox.length) {
				input[j] = invSbox[y][x];
			}
		}
		
	}
	
	private void invShiftRows(byte[][] input) {
		int column = 0;
		byte tmp;
		
		while (column < 4) {
			for (int turn = 0; turn < column; ++turn) {
				for (int i = input.length - 1; i > 0; --i) {
					if (i + 1 >= input.length) {
						tmp = input[0][column];
						input[0][column] = input[i][column];
					} else {
						tmp = input[i + 1][column];
						input[i + 1][column] = input[i][column];
					}
					input[i][column] = tmp;
				}
			}
			column++;
		}
	}
	
	private void invMixColumns(byte[][] input) {
		int columnMatrix = 0;
		int row;
		byte[] array;
		byte tmp;
		
		for (int i = 0; i < input.length; ++i) {
			row = 0;
			array = input[i].clone();
			while (row < 4) {
				tmp = 0;
				for (int j = 0; j < array.length; ++j) {
					tmp ^= gfmul(invMatrixMixColumns[columnMatrix][row], array[j]);
					columnMatrix++;
				}
				input[i][row] = tmp;
				row++;
				columnMatrix = 0;
			}
		}
	}
	
	// Key Schedule functions 
	private void rotWord(byte[] input) {
		byte tmp;
		
		for (int i = 0; i < input.length - 1; ++i) {
			if (i - 1 < 0) {
				tmp = input[input.length - 1];
				input[input.length - 1] = input[i];
			} else {
				tmp = input[i - 1];
				input[i - 1] = input[i];
			}
			input[i] = tmp;
		}
	}
	
	private byte[] getRcon(int i) {
		byte rcon[] = new byte[4];
		byte value = 1;
				
		if (i <= 0) {
			return rcon;
		}
		while (i != 1) {
			value = gfmul(value,(byte) 0x02);
			i--;
		}
		rcon[0] = value;
		return rcon;
	}
	
	private byte[][][] constructRoundKeys(byte[] key, AESType aesType) {
		byte[][][] roundKeys = mapKey(key, aesType);
		int count = aesType.getKeyColumnsNumber();
		int j = aesType.getKeyColumnsNumber() % 4;
		
		for (int i = aesType.getKeyColumnsNumber() / 4; i < roundKeys.length; ++i) {
			for (; j < roundKeys[i].length; ++j) {
				if (j - 1 < 0) {
					roundKeys[i][j] = roundKeys[i - 1][roundKeys[i - 1].length - 1].clone();
				} else {
					roundKeys[i][j] = roundKeys[i][j - 1].clone();
				}
				if (count % aesType.getKeyColumnsNumber() == 0) {
					rotWord(roundKeys[i][j]);
					subBytes(roundKeys[i][j]);
					xorInputArray(roundKeys[i][j], getRcon(count / aesType.getKeyColumnsNumber()));
				} else if (aesType == AESType.AES_256 && count % 4 == 0) {
					subBytes(roundKeys[i][j]);
				}
				xorInputArray(roundKeys[i][j], roundKeys[(count - aesType.getKeyColumnsNumber()) / 4][(count - aesType.getKeyColumnsNumber()) % 4]);
				count++;
			}
			j = 0;
		}
		return roundKeys;
	}
}
