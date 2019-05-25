package com.teoinf.steganos.h264;

import java.util.ArrayList;
import java.util.List;

import com.teoinf.steganos.tools.BitBufferReader;
import com.teoinf.steganos.tools.Pair;

public class MacroblockLayerParser {

	public enum PredictionMode {
		BiPred,
		Direct,
		Intra_4x4,
		Intra_8x8,
		Intra_16x16,
		None,
		Pred_L0,
		Pred_L1
	}
	
	private SeqParameterSetParser _seqParameterSetParser;
	private PictureParameterSetParser _pictureParameterSetParser;
	private int _sliceType;
	private int _fieldPicFlag;
	private int _mbFieldDecodingFlag;
	private List<Pair<Integer, Integer>> _macroblockResidualOffset;
	
	// Layer Attributes
	private int _mb_type;
	private int _pcm_alignment_zero_bit;
	private int _pcm_sample_luma[];
	private int _pcm_sample_chroma[];
	private int _transform_size_8x8_flag;
	private int _coded_block_pattern;
	private int _mb_qp_delta;
	
	// Prediction Attributes
	private int _prev_intra4x4_pred_mode_flag[];
	private int _rem_intra4x4_pred_mode[];
	private int _prev_intra8x8_pred_mode_flag[];
	private int _rem_intra8x8_pred_mode[];
	private int _intra_chroma_pred_mode;
	private int _ref_idx_l0[];
	private int _ref_idx_l1[];
	private int _mvd_l0[][][];
	private int _mvd_l1[][][];
	
	// Sub Prediction Attributes
	private int _sub_mb_type[];
	
	// Residual
	private int _i16x16DClevel[];
	private int _i16x16AClevel[][];
	private int _level4x4[][];
	private int _level8x8[][];
	private int _coeff_token;
	private int _trailing_ones_sign_flag;
	private int _level_prefix;
	private int _level_suffix;
	
	public MacroblockLayerParser(SeqParameterSetParser seqParameterSetParser, PictureParameterSetParser pictureParameterSetParser, int sliceType, int fieldPicFlag, int mbFieldDecodingFlag) {
		_seqParameterSetParser = seqParameterSetParser;
		_pictureParameterSetParser = pictureParameterSetParser;
		_sliceType = sliceType;
		_fieldPicFlag = fieldPicFlag;
		_mbFieldDecodingFlag = mbFieldDecodingFlag;
		_macroblockResidualOffset = new ArrayList<Pair<Integer, Integer>>();
	}
	
	public void parseMacroblockLayer(BitBufferReader bitBufferReader) {
		int entropyCodingMode = _pictureParameterSetParser.getEntropyCodingModeFlag();
		int mbWidthC = (_seqParameterSetParser.getChromaFormatIdc() == 0 || 
				_seqParameterSetParser.getSeparateColourPlaneFlag() == 1 ? 0 : 16 / getSubWidthC());
		int mbHeightC = (_seqParameterSetParser.getChromaFormatIdc() == 0 || 
				_seqParameterSetParser.getSeparateColourPlaneFlag() == 1 ? 0 : 16 / getSubHeightC());
		int direct8x8InterferenceFlag = _seqParameterSetParser.getDirect8x8InterferenceFlag();
		int transform8x8ModeFlag = _pictureParameterSetParser.getTransform8x8ModeFlag();
		boolean noSubMbPartSizeLessThan8x8Flag = false;
		
		_mb_type = (entropyCodingMode == 0 ? (int) bitBufferReader.readUE() : bitBufferReader.readAE());
		if (_mb_type == 25) {
			while (bitBufferReader.getCurrentBitOffset() != 0) {
				_pcm_alignment_zero_bit = (int) bitBufferReader.readNBits(1);
			}

			_pcm_sample_luma = new int[256];
			for (int i = 0; i < 256; i++) {
				_pcm_sample_luma[i] = (int) bitBufferReader.readNBits(8 + _seqParameterSetParser.getBitDepthLumaMinus8());
			}
			
			_pcm_sample_chroma = new int[2 * mbWidthC * mbHeightC];
			for (int i = 0; i < 2 * mbWidthC * mbHeightC; i++) {
				_pcm_sample_chroma[i] = (int) bitBufferReader.readNBits(8 + _seqParameterSetParser.getBitDepthChromaMinus8());
			}
		} else {
			noSubMbPartSizeLessThan8x8Flag = true;
			if (_mb_type != 0 && mbPartPredMode(_mb_type, 0) != PredictionMode.Intra_16x16 && 
					getNumMbPart(_mb_type) == 4) {
				readSubMacroblockPrediction(bitBufferReader, _mb_type);
				for (int mbPartIdx = 0; mbPartIdx < 4; mbPartIdx++ ) {
					if (_sub_mb_type[mbPartIdx] != 0) {
						if (getNumSubMbPart(_sub_mb_type[mbPartIdx]) > 1) {
							noSubMbPartSizeLessThan8x8Flag = false;
						}
					} else if (direct8x8InterferenceFlag == 0) {
						noSubMbPartSizeLessThan8x8Flag = false;
					}
				}
			} else {
				if(transform8x8ModeFlag == 1 && _mb_type == 0) {
					_transform_size_8x8_flag = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(1) : bitBufferReader.readAE());
				}
				readMacroblockPred(bitBufferReader, _mb_type);
			}
			if (mbPartPredMode(_mb_type, 0) != PredictionMode.Intra_16x16) {
				_coded_block_pattern = (entropyCodingMode == 0 ? (int) bitBufferReader.readUE() : bitBufferReader.readAE());
				if (_coded_block_pattern % 16 > 0 && transform8x8ModeFlag == 1 && _mb_type != 0 &&
					noSubMbPartSizeLessThan8x8Flag && (_mb_type != 0 || direct8x8InterferenceFlag == 1)) {
					_transform_size_8x8_flag = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(1) : bitBufferReader.readAE());
				}
			}
			if (_coded_block_pattern % 16 > 0 || _coded_block_pattern / 16 > 0 ||
				mbPartPredMode(_mb_type, 0) == PredictionMode.Intra_16x16) {
				_mb_qp_delta = (entropyCodingMode == 0 ? (int) bitBufferReader.readSE() : bitBufferReader.readAE());
//				residual(0, 15);
				Pair<Integer, Integer> offset = new Pair<Integer, Integer>();
				offset.setFirst(bitBufferReader.getCurrentOffset());
				for (int i = 0; i < 32; ++i) {
					bitBufferReader.readNBits(8);
				}
				offset.setSecond(bitBufferReader.getCurrentOffset());
				_macroblockResidualOffset.add(offset);
			}
		}
	}
	
	private void readMacroblockPred(BitBufferReader bitBufferReader, int mbType) {
		int entropyCodingMode = _pictureParameterSetParser.getEntropyCodingModeFlag();
		int chromaArrayType = (_seqParameterSetParser.getSeparateColourPlaneFlag() == 0 ? _seqParameterSetParser.getChromaFormatIdc() : 0);
		int numRefIdxL0ActiveMinus1 = _pictureParameterSetParser.getNumRefIdxL0DefaultActiveMinus1();
		int numRefIdxL1ActiveMinus1 = _pictureParameterSetParser.getNumRefIdxL1DefaultActiveMinus1();
		
		if (mbPartPredMode(mbType, 0) == PredictionMode.Intra_4x4 || 
				mbPartPredMode(mbType, 0) == PredictionMode.Intra_8x8 ||
				mbPartPredMode(mbType, 0) == PredictionMode.Intra_16x16) {
			if (mbPartPredMode(mbType, 0) == PredictionMode.Intra_4x4) {
				_prev_intra4x4_pred_mode_flag = new int[16];
				_rem_intra4x4_pred_mode = new int[16];
				for (int i = 0; i < 16; ++i) {
					_prev_intra4x4_pred_mode_flag[i] = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(1) : bitBufferReader.readAE());
					if (_prev_intra4x4_pred_mode_flag[i] == 0) {
						_rem_intra4x4_pred_mode[i] = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(3) : bitBufferReader.readAE());
					}
				}
			}
			if (mbPartPredMode(mbType, 0) == PredictionMode.Intra_8x8) {
				_prev_intra8x8_pred_mode_flag = new int[4];
				_rem_intra8x8_pred_mode = new int[4];
				for (int i = 0; i < 4; ++i) {
					_prev_intra8x8_pred_mode_flag[i] = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(1) : bitBufferReader.readAE());
					if (_prev_intra8x8_pred_mode_flag[i] == 0) {
						_rem_intra8x8_pred_mode[i] = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(3) : bitBufferReader.readAE());
					}
				}
			}
			if (chromaArrayType == 1 || chromaArrayType == 2) {
				_intra_chroma_pred_mode = (entropyCodingMode == 0 ? bitBufferReader.readUE() : bitBufferReader.readAE());
			}
		} else if (mbPartPredMode(mbType, 0) != PredictionMode.Direct) {
			_ref_idx_l0 = new int[getNumMbPart(mbType)];
			for (int i = 0; i < getNumMbPart(mbType); ++i) {
				if ((numRefIdxL0ActiveMinus1 > 0 || _mbFieldDecodingFlag != _fieldPicFlag) && 
						mbPartPredMode(mbType, i) != PredictionMode.Pred_L1) {
					_ref_idx_l0[i] = (entropyCodingMode == 0 ? bitBufferReader.readUE() : bitBufferReader.readAE());
				}
			}

			_ref_idx_l1 = new int[getNumMbPart(mbType)];
			for (int i = 0; i < getNumMbPart(mbType); ++i) {
				if ((numRefIdxL1ActiveMinus1 > 0 || _mbFieldDecodingFlag != _fieldPicFlag) && 
						mbPartPredMode(mbType, i) != PredictionMode.Pred_L0) {
					_ref_idx_l1[i] = (entropyCodingMode == 0 ? bitBufferReader.readUE() : bitBufferReader.readAE());
				}
			}
			
			_mvd_l0 = new int[getNumMbPart(mbType)][1][2];
			for (int i = 0; i < getNumMbPart(mbType); ++i) {
				if (mbPartPredMode(mbType, i) != PredictionMode.Pred_L1) {
					for (int j = 0; j < 2; ++j) {
						_mvd_l0[i][0][j] = (entropyCodingMode == 0 ? bitBufferReader.readSE() : bitBufferReader.readAE());
					}
				}
			}

			_mvd_l1 = new int[getNumMbPart(mbType)][1][2];
			for (int i = 0; i < getNumMbPart(mbType); ++i) {
				if (mbPartPredMode(mbType, i) != PredictionMode.Pred_L0) {
					for (int j = 0; j < 2; ++j) {
						_mvd_l1[i][0][j] = (entropyCodingMode == 0 ? bitBufferReader.readSE() : bitBufferReader.readAE());
					}
				}
			}
		}
	}
	
	private void readSubMacroblockPrediction(BitBufferReader bitBufferReader, int mbType) {
		int entropyCodingMode = _pictureParameterSetParser.getEntropyCodingModeFlag();
		int numRefIdxL0ActiveMinus1 = _pictureParameterSetParser.getNumRefIdxL0DefaultActiveMinus1();
		int numRefIdxL1ActiveMinus1 = _pictureParameterSetParser.getNumRefIdxL1DefaultActiveMinus1();

		_sub_mb_type = new int[4];
		for (int mbPartIdx = 0; mbPartIdx < 4; mbPartIdx++) {
			_sub_mb_type[mbPartIdx] = (entropyCodingMode == 0 ? (int) bitBufferReader.readUE() : bitBufferReader.readAE());
		}
		
		_ref_idx_l0 = new int[4];
		for (int mbPartIdx = 0; mbPartIdx < 4; mbPartIdx++) {
			if ((numRefIdxL0ActiveMinus1 > 0 || _mbFieldDecodingFlag != _fieldPicFlag ) &&
			_mb_type != 4 && _sub_mb_type[mbPartIdx] != 0 && getSubMbPredMode(_sub_mb_type[mbPartIdx]) != PredictionMode.Pred_L1) {
				_ref_idx_l0[mbPartIdx] = (entropyCodingMode == 0 ? (int) bitBufferReader.readUE() : bitBufferReader.readAE());
			}
		}

		_ref_idx_l1 = new int[4];
		for (int mbPartIdx = 0; mbPartIdx < 4; mbPartIdx++) {
			if ((numRefIdxL1ActiveMinus1 > 0 || _mbFieldDecodingFlag != _fieldPicFlag ) &&
			_sub_mb_type[mbPartIdx] != 0 && getSubMbPredMode(_sub_mb_type[mbPartIdx]) != PredictionMode.Pred_L0) {
				_ref_idx_l1[mbPartIdx] = (entropyCodingMode == 0 ? (int) bitBufferReader.readUE() : bitBufferReader.readAE());
			}
		}
		
		_mvd_l0 = new int[4][0][0];
		for (int mbPartIdx = 0; mbPartIdx < 4; mbPartIdx++) {
			if(_sub_mb_type[mbPartIdx] != 0 && getSubMbPredMode(_sub_mb_type[mbPartIdx]) != PredictionMode.Pred_L1) {
				_mvd_l0[mbPartIdx] = new int[getNumSubMbPart(_sub_mb_type[mbPartIdx])][2];
				for (int subMbPartIdx = 0; subMbPartIdx < getNumSubMbPart(_sub_mb_type[mbPartIdx]); subMbPartIdx++) {
					for (int compIdx = 0; compIdx < 2; compIdx++ ) {
						_mvd_l0[mbPartIdx][subMbPartIdx][compIdx] = (entropyCodingMode == 0 ? (int) bitBufferReader.readSE() : bitBufferReader.readAE());
					}
				}
			}
		}
		
		_mvd_l1 = new int[4][0][0];
		for (int mbPartIdx = 0; mbPartIdx < 4; mbPartIdx++) {
			if (_sub_mb_type[mbPartIdx] != 0 && getSubMbPredMode(_sub_mb_type[mbPartIdx]) != PredictionMode.Pred_L0) {
				_mvd_l1[mbPartIdx] = new int[getNumSubMbPart(_sub_mb_type[mbPartIdx])][2];
				for (int subMbPartIdx = 0; subMbPartIdx < getNumSubMbPart(_sub_mb_type[mbPartIdx]); subMbPartIdx++) {
					for (int compIdx = 0; compIdx < 2; compIdx++ ) {
						_mvd_l1[mbPartIdx][subMbPartIdx][compIdx] = (entropyCodingMode == 0 ? (int) bitBufferReader.readSE() : bitBufferReader.readAE());
					}
				}
			}
		}
	}

	private int getNumMbPart(int mbType) {
		if (_sliceType == 1 || _sliceType == 6) {
			if (_mb_type == 0) {
				return 0;
			} else if (_mb_type > 0 && _mb_type < 4) {
				return 1;
			} else if (_mb_type > 3 && _mb_type < 22) {
				return 2;
			} else if (_mb_type == 22) {
				return 4;
			} 
			return 0;
		} else if (_sliceType == 0 || _sliceType == 5 || _sliceType == 3 || _sliceType == 8) {
			if (_mb_type == 1 || _mb_type == 2) {
				return 2;
			} else if (_mb_type == 3 || _mb_type == 4) {
				return 4;
			}
			return 1;
		}
		return 0;
	}
	
	private int getNumSubMbPart(int mbType) {
		if (_sliceType == 0 || _sliceType == 5) {
			if (mbType == 2) {
				return 2;
			}
			return mbType + 1;
		} else if (_sliceType == 1 || _sliceType == 6) {
			if (mbType == 0 || mbType > 9) {
				return 4;
			} else if (mbType >= 1 && mbType <= 3) {
				return 1;
			} else if (mbType >= 4 && mbType <= 9) {
				return 2;
			}
			return 4;
		}
		return -1;
	}
	
	private PredictionMode getSubMbPredMode(int subMbType) {
		if (_sliceType == 0 || _sliceType == 5) {
			if (subMbType >= 0 && subMbType <= 3) {
				return PredictionMode.Pred_L0;
			}
			return PredictionMode.None;
		} else if (_sliceType == 1 || _sliceType == 6) {
			if (subMbType == 1 || subMbType == 4 || subMbType == 5 || subMbType == 10) {
				return PredictionMode.Pred_L0;
			} else if (subMbType == 2 || subMbType == 6 || subMbType == 7 || subMbType == 11) {
				return PredictionMode.Pred_L1;
			} else if (subMbType == 3 || subMbType == 8 || subMbType == 9 || subMbType == 12) {
				return PredictionMode.BiPred;
			}
			return PredictionMode.Direct;
		}
		return PredictionMode.None;
	}
	
	private PredictionMode mbPartPredMode(int mbType, int value) {
		PredictionMode ret = PredictionMode.None;
		
		if (_sliceType == 7 || _sliceType == 2) {
			ret = mbPartPredModeISlice(mbType, value);
		} else if (_sliceType == 4 || _sliceType == 9) {
			ret = mbPartPredModeSISlice(mbType, value);
		} else if (_sliceType == 0 || _sliceType == 5 || _sliceType == 3 || _sliceType == 8) {
			ret = mbPartPredModePAndSPSlice(mbType, value);
		} else if (_sliceType == 1 || _sliceType == 6) {
			ret = mbPartPredModeBSlice(mbType, value);
		}
		return ret;
	}
	
	private PredictionMode mbPartPredModeISlice(int mbType, int value) {
		if (value == 0) {
			if (mbType == 0) {
				return (_pictureParameterSetParser.getTransform8x8ModeFlag() == 0 ? 
						PredictionMode.Intra_4x4 : PredictionMode.Intra_8x8);
			} else if (mbType > 0 && mbType < 25) {
				return PredictionMode.Intra_16x16;
			}
		}
		return PredictionMode.None;
	}
	
	private PredictionMode mbPartPredModeSISlice(int mbType, int value) {
		return PredictionMode.Intra_4x4;
	}
	
	private PredictionMode mbPartPredModePAndSPSlice(int mbType, int value) {
		if (value == 0) {
			if ((mbType >= 0 && mbType < 3)) {
				return PredictionMode.Pred_L0;
			} else if (mbType == 3 || mbType == 4) {
				return PredictionMode.None;
			}
			return PredictionMode.Pred_L0;
		} else if (value == 1) {
			if ((mbType > 0 && mbType < 3)) {
				return PredictionMode.Pred_L0;
			}
			return PredictionMode.None;			
		}
		return PredictionMode.None;			
	}
	
	private PredictionMode mbPartPredModeBSlice(int mbType, int value) {
		if (value == 0) {
			if (mbType == 0 || mbType > 22) {
				return PredictionMode.Direct;
			} else if (mbType == 3 || (mbType >= 16 && mbType < 22)) {
				return PredictionMode.BiPred;
			} else if (mbType == 1 || mbType == 4 || mbType == 5 || mbType == 8 || mbType == 9 ||
					mbType == 12 || mbType == 13) {
				return PredictionMode.Pred_L0;
			} 
			return PredictionMode.Pred_L1;
		} else if (value == 1) {
			if ((mbType >= 6 && mbType <= 9) || mbType == 18 || mbType == 19) {
				return PredictionMode.Pred_L1;
			} else if ((mbType >= 12 && mbType <= 15) || mbType == 20 || mbType == 21) {
				return PredictionMode.BiPred;
			} else if (mbType == 4 || mbType == 5 || mbType == 10 || mbType == 11 || mbType == 16 || mbType == 17) {
				return PredictionMode.Pred_L0;
			}
			return PredictionMode.None;
		}
		return PredictionMode.None;
	}
	
	private int getSubWidthC() {
		int ret = 0;
		int chromaFormatIdc = _seqParameterSetParser.getChromaFormatIdc();
		int separateColourPlaneFlag = _seqParameterSetParser.getSeparateColourPlaneFlag();
		
		if (chromaFormatIdc == 0 || separateColourPlaneFlag == 1) {
			ret = 0;
		} else if (chromaFormatIdc == 1 || chromaFormatIdc == 2) {
			ret = 2;
		} else {
			ret = 1;
		}
		return ret;
	}
	
	private int getSubHeightC() {
		int ret = 0;
		int chromaFormatIdc = _seqParameterSetParser.getChromaFormatIdc();
		int separateColourPlaneFlag = _seqParameterSetParser.getSeparateColourPlaneFlag();
		
		if (chromaFormatIdc == 0 || separateColourPlaneFlag == 1) {
			ret = 0;
		} else if (chromaFormatIdc == 1) {
			ret = 2;
		} else {
			ret = 1;
		}
		return ret;		
	}
	
	public List<Pair<Integer, Integer>> getMacroblockResidualOffset () {
		return _macroblockResidualOffset;
	}

	public SeqParameterSetParser get_seqParameterSetParser() {
		return _seqParameterSetParser;
	}

	public PictureParameterSetParser get_pictureParameterSetParser() {
		return _pictureParameterSetParser;
	}

	public int get_sliceType() {
		return _sliceType;
	}

	public int get_fieldPicFlag() {
		return _fieldPicFlag;
	}

	public int get_mbFieldDecodingFlag() {
		return _mbFieldDecodingFlag;
	}

	public List<Pair<Integer, Integer>> get_macroblockResidualOffset() {
		return _macroblockResidualOffset;
	}

	public int getMbType() {
		return _mb_type;
	}

	public int getPcmAlignmentZeroBit() {
		return _pcm_alignment_zero_bit;
	}

	public int[] getPcmSampleLuma() {
		return _pcm_sample_luma;
	}

	public int[] getPcmSampleChroma() {
		return _pcm_sample_chroma;
	}

	public int getTransformSize8x8Flag() {
		return _transform_size_8x8_flag;
	}

	public int getCodedBlockPattern() {
		return _coded_block_pattern;
	}

	public int getMbQpDelta() {
		return _mb_qp_delta;
	}

	public int[] getPrevIntra4x4PredModeFlag() {
		return _prev_intra4x4_pred_mode_flag;
	}

	public int[] getRemIntra4x4PredMode() {
		return _rem_intra4x4_pred_mode;
	}

	public int[] getPrevIntra8x8PredModeFlag() {
		return _prev_intra8x8_pred_mode_flag;
	}

	public int[] getRemIntra8x8PredMode() {
		return _rem_intra8x8_pred_mode;
	}

	public int getIntraChromaPredMode() {
		return _intra_chroma_pred_mode;
	}

	public int[] getRefIdxL0() {
		return _ref_idx_l0;
	}

	public int[] getRefIdxL1() {
		return _ref_idx_l1;
	}

	public int[][][] getMvdL0() {
		return _mvd_l0;
	}

	public int[][][] getMvdL1() {
		return _mvd_l1;
	}

	public int[] getSubMbType() {
		return _sub_mb_type;
	}

	public int[] getI16x16DClevel() {
		return _i16x16DClevel;
	}

	public int[][] getI16x16AClevel() {
		return _i16x16AClevel;
	}

	public int[][] getLevel4x4() {
		return _level4x4;
	}

	public int[][] getLevel8x8() {
		return _level8x8;
	}

	public int getCoeffToken() {
		return _coeff_token;
	}

	public int getTrailingOnesSignFlag() {
		return _trailing_ones_sign_flag;
	}

	public int getLevelPrefix() {
		return _level_prefix;
	}

	public int getLevelSuffix() {
		return _level_suffix;
	}
}
