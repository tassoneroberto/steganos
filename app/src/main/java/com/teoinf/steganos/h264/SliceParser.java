package com.teoinf.steganos.h264;

import java.util.ArrayList;
import java.util.List;

import com.teoinf.steganos.tools.BitBufferReader;
import com.teoinf.steganos.tools.Pair;

public class SliceParser {

	private SeqParameterSetParser _seqParameterSetParser;
	private PictureParameterSetParser _pictureParameterSetParser;
	private int _nalUnitType;
	private int _nalRefIdc;
	private int _chromaArrayType;
	private boolean _idrPicFlag;
	private int _sliceDataOffset;
	private List<Pair<Integer, Integer>> _macroblockDataOffset;
	private int _first_mb_in_slice;
	private int _slice_type;
	private int _pic_parameter_set_id;
	private int _colour_plane_id;
	private int _frame_num;
	private int _field_pic_flag;
	private int _bottom_field_flag;
	private int _idr_pic_id;
	private int _pic_order_cnt_lsb;
	private int _delta_pic_order_cnt_bottom;
	private int _delta_pic_order_cnt[];
	private int _redundant_pic_cnt;
	private int _direct_spatial_mv_pred_flag;
	private int _num_ref_idx_active_override_flag;
	private int _num_ref_idx_l0_active_minus1;
	private int _num_ref_idx_l1_active_minus1;
	private int _cabac_init_idc;
	private int _slice_qp_delta;
	private int _sp_for_switch_flag;
	private int _slice_qs_delta;
	private int _disable_deblocking_filter_idc;
	private int _slice_alpha_c0_offset_div2;
	private int _slice_beta_offset_div2;
	private int _slice_group_change_cycle;
	
	// Ref pic list mvc modifications
	private int _ref_pic_list_modification_flag_l0;
	private int _modification_of_pic_num_idc;
	private int _abs_diff_pic_num_minus1;
	private int _long_term_pic_num;
	private int _abs_diff_view_idx_minus1;
	private int _ref_pic_list_modification_flag_l1;
	
	// Pred weight table
	private int _luma_log2_weight_denom;
	private int _chroma_log2_weight_denom;
	private int _luma_weight_l0_flag;
	private int _luma_weight_l0[];
	private int _luma_offset_l0[];
	private int _chroma_weight_l0_flag;
	private int _chroma_weight_l0[][];
	private int _chroma_offset_l0[][];
	private int _luma_weight_l1_flag;
	private int _luma_weight_l1[];
	private int _luma_offset_l1[];
	private int _chroma_weight_l1_flag;
	private int _chroma_weight_l1[][];
	private int _chroma_offset_l1[][];
	
	// Dec ref pic marking
	private int _no_output_of_prior_pics_flag;
	private int _long_term_reference_flag;
	private int _adaptive_ref_pic_marking_mode_flag;
	private int _memory_management_control_operation;
	private int _difference_of_pic_nums_minus1;
	private int _long_term_frame_idx;
	private int _max_long_term_frame_idx_plus1;

	private int _mapUnitToSliceGroupMap[];
	private int _mbToSliceGroupMap[];


	private int _cabac_alignement_one_bit;
	private int _mb_skip_run;
	private int _mb_skip_flag;
	private int _mb_field_decoding_flag;
	private int _end_of_slice_flag;
	
	public SliceParser(SeqParameterSetParser seqParameterSetParser, PictureParameterSetParser pictureParameterSetParser, int nalUnitType, int nalRefIdc) {
		_seqParameterSetParser = seqParameterSetParser;
		_pictureParameterSetParser = pictureParameterSetParser;
		_nalUnitType = nalUnitType;
		_nalRefIdc = nalRefIdc;
		_chromaArrayType = (_seqParameterSetParser.getSeparateColourPlaneFlag() == 0 ? _seqParameterSetParser.getChromaFormatIdc() : 0);
		_idrPicFlag = (_nalUnitType == 5 );
		_macroblockDataOffset = new ArrayList<Pair<Integer,Integer>>();
	}

	public void parseSlice(byte[] data) {
		BitBufferReader bitBufferReader = new BitBufferReader(data);
		
		parseSliceHeader(bitBufferReader);
		_sliceDataOffset = (bitBufferReader.getCurrentBitOffset() == 0 ? bitBufferReader.getCurrentOffset() : bitBufferReader.getCurrentOffset() + 1);
		try {
			parseSliceData(bitBufferReader);
		} catch (Exception ex) {
			System.err.println("[Slice Parser]: " + ex.getMessage());
		}
	}
	
	private void parseSliceHeader(BitBufferReader bitBufferReader) {
		_first_mb_in_slice = bitBufferReader.readUE();
		_slice_type = bitBufferReader.readUE();
		_pic_parameter_set_id = bitBufferReader.readUE();
		
		if (_seqParameterSetParser.getSeparateColourPlaneFlag() == 1) {
			_colour_plane_id = (int) bitBufferReader.readNBits(2);
		}
		
		_frame_num = (int) bitBufferReader.readNBits(_seqParameterSetParser.getLog2MaxFrameNumMinus4() + 4);
		if (_seqParameterSetParser.getFrameMbsOnlyFlag() == 0) {
			_field_pic_flag = (int) bitBufferReader.readNBits(1);
			if (_field_pic_flag == 1) {
				_bottom_field_flag = (int) bitBufferReader.readNBits(1);
			}
		}
		
		if (_idrPicFlag) {
			_idr_pic_id = bitBufferReader.readUE();
		}
		
		if (_seqParameterSetParser.getPicOrderCntType() == 0) {
			_pic_order_cnt_lsb = (int) bitBufferReader.readNBits(_seqParameterSetParser.getLog2MaxPicOrderCntLsbMinus4() + 4);
			if (_pictureParameterSetParser.getBottomFieldPicOrderInFramePresentFlag() == 1 &&
					_field_pic_flag == 0) {
				_delta_pic_order_cnt_bottom = bitBufferReader.readSE();
			}
		}
		
		_delta_pic_order_cnt = new int[2];
		if (_seqParameterSetParser.getPicOrderCntType() == 1 && _seqParameterSetParser.getDeltaPicOrderAlwaysZeroFlag() == 0) {
			_delta_pic_order_cnt[0] = bitBufferReader.readSE();
			if (_pictureParameterSetParser.getBottomFieldPicOrderInFramePresentFlag() == 1 && _field_pic_flag == 0) {
				_delta_pic_order_cnt[1] = bitBufferReader.readSE();
			}
		}
		
		if (_pictureParameterSetParser.getRedundantPicCntPresentFlag() == 1) {
			_redundant_pic_cnt = bitBufferReader.readUE();
		}
		
		// Slice type B
		if (_slice_type == 1 || _slice_type == 6) {
			_direct_spatial_mv_pred_flag = (int) bitBufferReader.readNBits(1);
		}
		if (_slice_type == 0 || _slice_type == 5 || 
				_slice_type == 3 || _slice_type == 8 ||
				_slice_type == 1 || _slice_type == 6) {
			_num_ref_idx_active_override_flag = (int) bitBufferReader.readNBits(1);
			if (_num_ref_idx_active_override_flag == 1) {
				_num_ref_idx_l0_active_minus1 = bitBufferReader.readUE();
				if (_slice_type == 1 || _slice_type == 6) {
					_num_ref_idx_l1_active_minus1 = bitBufferReader.readUE();
				}
			}
		}
		
		if (_nalUnitType == 20 || _nalUnitType == 21) {
			readRefPicListMvcModification(bitBufferReader);
		} else {
			readRefPicListModification(bitBufferReader);
		}
		
		if ((_pictureParameterSetParser.getWeightedPredFlag() == 1 && 
				(_slice_type == 0 || _slice_type == 5 || _slice_type == 3 || _slice_type == 8)) ||
				(_pictureParameterSetParser.getWeightedBipredIdc() == 1 && (_slice_type == 1 || _slice_type == 6))) {
			readPredWeightTable(bitBufferReader);
		}
		if (_nalRefIdc != 0) {
			readDecRefPicMarking(bitBufferReader);
		}
		
		if (_pictureParameterSetParser.getEntropyCodingModeFlag() == 1 && _slice_type != 2 && 
				_slice_type != 7 && _slice_type != 4 && _slice_type != 9) {
			_cabac_init_idc = bitBufferReader.readUE();
		}
		_slice_qp_delta = bitBufferReader.readSE();
		
		if (_slice_type == 3 || _slice_type == 8 || _slice_type == 4 || _slice_type == 9) {
			if (_slice_type == 3 || _slice_type == 8) {
				_sp_for_switch_flag = (int) bitBufferReader.readNBits(1);
			}
			_slice_qs_delta = bitBufferReader.readSE();
		}
		
		if (_pictureParameterSetParser.getDeblockingFilterControlPresentFlag() == 1) {
			_disable_deblocking_filter_idc = bitBufferReader.readUE();
			if (_disable_deblocking_filter_idc != 1) {
				_slice_alpha_c0_offset_div2 = bitBufferReader.readSE();
				_slice_beta_offset_div2 = bitBufferReader.readSE();
			}
		}
		
		if (_pictureParameterSetParser.getNumSliceGroupsMinus1() > 0 && 
				_pictureParameterSetParser.getSliceGroupMapType() >= 3 && _pictureParameterSetParser.getSliceGroupMapType() <= 5) {
			_slice_group_change_cycle = (int) bitBufferReader.readNBits((int) Math.ceil(
					Math.log10((_pictureParameterSetParser.getPicSizeInMapUnitMinus1() + 1) + 
							(_pictureParameterSetParser.getSliceGroupChangeRateMinus1() + 1) / Math.log10(2))));
		}
	}
	
	private void readRefPicListMvcModification(BitBufferReader bitBufferReader) {
		if (_slice_type % 5 != 2 && _slice_type %5 != 4) {
			_ref_pic_list_modification_flag_l0 = (int) bitBufferReader.readNBits(1);
			if (_ref_pic_list_modification_flag_l0 == 1) {
				do {
					_modification_of_pic_num_idc = bitBufferReader.readUE();
					if (_modification_of_pic_num_idc == 0 || _modification_of_pic_num_idc == 1) {
						_abs_diff_pic_num_minus1 = bitBufferReader.readUE();
					} else if (_modification_of_pic_num_idc == 2) {
						_long_term_pic_num = bitBufferReader.readUE();
					} else if (_modification_of_pic_num_idc == 4 || _modification_of_pic_num_idc == 5) {
						_abs_diff_view_idx_minus1 = bitBufferReader.readUE();
					}
				} while (_modification_of_pic_num_idc != 3);
			}
		}
		
		if (_slice_type % 5 == 1) {
			_ref_pic_list_modification_flag_l1 = (int) bitBufferReader.readNBits(1);
			if (_ref_pic_list_modification_flag_l1 == 1) {
				do {
					_modification_of_pic_num_idc = bitBufferReader.readUE();
					if (_modification_of_pic_num_idc == 0 || _modification_of_pic_num_idc == 1) {
						_abs_diff_pic_num_minus1 = bitBufferReader.readUE();
					} else if (_modification_of_pic_num_idc == 2) {
						_long_term_pic_num = bitBufferReader.readUE();
					} else if (_modification_of_pic_num_idc == 4 || _modification_of_pic_num_idc == 5) {
						_abs_diff_view_idx_minus1 = bitBufferReader.readUE();
					}
				} while (_modification_of_pic_num_idc != 3);				
			}
		}
	}
	
	private void readRefPicListModification(BitBufferReader bitBufferReader) {
		if (_slice_type % 5 != 2 && _slice_type % 5 != 4) {
			_ref_pic_list_modification_flag_l0 = (int) bitBufferReader.readNBits(1);
			if (_ref_pic_list_modification_flag_l0 == 1) {
				do {
					_modification_of_pic_num_idc = bitBufferReader.readUE();
					if (_modification_of_pic_num_idc == 0 || _modification_of_pic_num_idc == 1) {
						_abs_diff_pic_num_minus1 = bitBufferReader.readUE();
					} else if (_modification_of_pic_num_idc == 2) {
						_long_term_pic_num = bitBufferReader.readUE();
					}
				} while (_modification_of_pic_num_idc != 3);
			}
		}
		
		if (_slice_type % 5 == 1) {
			_ref_pic_list_modification_flag_l1 = (int) bitBufferReader.readNBits(1);
			if (_ref_pic_list_modification_flag_l1 == 1) {
				do {
					_modification_of_pic_num_idc = bitBufferReader.readUE();
					if (_modification_of_pic_num_idc == 0 || _modification_of_pic_num_idc == 1) {
						_abs_diff_pic_num_minus1 = bitBufferReader.readUE();
					} else if (_modification_of_pic_num_idc == 2) {
						_long_term_pic_num = bitBufferReader.readUE();
					}
				} while (_modification_of_pic_num_idc != 3);				
			}
		}
	}
	
	private void readPredWeightTable(BitBufferReader bitBufferReader) {
		_luma_log2_weight_denom = bitBufferReader.readUE();
		if (_chromaArrayType != 0) {
			_chroma_log2_weight_denom = bitBufferReader.readUE();
		}
		
		_luma_weight_l0 = new int[_num_ref_idx_l0_active_minus1 + 1];
		_luma_offset_l0 = new int[_num_ref_idx_l0_active_minus1 + 1];
		_chroma_weight_l0 = new int[_num_ref_idx_l0_active_minus1 + 1][2];
		_chroma_offset_l0 = new int[_num_ref_idx_l0_active_minus1 + 1][2];
		for (int i = 0; i <= _num_ref_idx_l0_active_minus1; ++i) {
			_luma_weight_l0_flag = (int) bitBufferReader.readNBits(1);
			if (_luma_weight_l0_flag == 1) {
				_luma_weight_l0[i] = bitBufferReader.readSE();
				_luma_offset_l0[i] = bitBufferReader.readSE();
			}
			if (_chromaArrayType != 0) {
				_chroma_weight_l0_flag = (int) bitBufferReader.readNBits(1);
				if (_chroma_weight_l0_flag == 1) {
					for (int j = 0; j < 2; ++j) {
						_chroma_weight_l0[i][j] = bitBufferReader.readSE();
						_chroma_offset_l0[i][j] = bitBufferReader.readSE();
					}
				}
			}
		}
		
		if (_slice_type % 5 == 1) {
			_luma_weight_l1 = new int[_num_ref_idx_l1_active_minus1 + 1];
			_luma_offset_l1 = new int[_num_ref_idx_l1_active_minus1 + 1];
			_chroma_weight_l1 = new int[_num_ref_idx_l1_active_minus1 + 1][2];
			_chroma_offset_l1 = new int[_num_ref_idx_l1_active_minus1 + 1][2];
			for (int i = 0; i <= _num_ref_idx_l1_active_minus1; ++i) {
				_luma_weight_l1_flag = (int) bitBufferReader.readNBits(1);
				if (_luma_weight_l1_flag == 1) {
					_luma_weight_l1[i] = bitBufferReader.readSE();
					_luma_offset_l1[i] = bitBufferReader.readSE();
				}
				if (_chromaArrayType != 0) {
					_chroma_weight_l1_flag = (int) bitBufferReader.readNBits(1);
					if (_chroma_weight_l1_flag == 1) {
						for (int j = 0; j < 2; ++j) {
							_chroma_weight_l1[i][j] = bitBufferReader.readSE();
							_chroma_offset_l1[i][j] = bitBufferReader.readSE();
						}
					}
				}
			}
		}
	}
	
	private void readDecRefPicMarking(BitBufferReader bitBufferReader) {
		if (_idrPicFlag) {
			_no_output_of_prior_pics_flag = (int) bitBufferReader.readNBits(1);
			_long_term_reference_flag = (int) bitBufferReader.readNBits(1);
		} else {
			_adaptive_ref_pic_marking_mode_flag = (int) bitBufferReader.readNBits(1);
			if (_adaptive_ref_pic_marking_mode_flag == 1) {
				do {
					_memory_management_control_operation = bitBufferReader.readUE();
					if (_memory_management_control_operation == 1 || _memory_management_control_operation == 3) {
						_difference_of_pic_nums_minus1 = bitBufferReader.readUE();
					}
					if (_memory_management_control_operation == 2) {
						_long_term_pic_num = bitBufferReader.readUE();
					}
					if (_memory_management_control_operation == 3 || _memory_management_control_operation == 6) {
						_long_term_frame_idx = bitBufferReader.readUE();
					}
					if (_memory_management_control_operation == 4) {
						_max_long_term_frame_idx_plus1 = bitBufferReader.readUE();
					}
				} while (_memory_management_control_operation != 0);
			}
		}
	}
	
	private void parseSliceData(BitBufferReader bitBufferReader) {
		int mbaffFrameFlag = (_seqParameterSetParser.getMbAdaptiveFrameFieldFlag() == 1 && _field_pic_flag == 0 ? 1 : 0); 
		int currMbAddr = _first_mb_in_slice * (1 + mbaffFrameFlag);
		int entropyCodingMode = _pictureParameterSetParser.getEntropyCodingModeFlag();
		boolean moreDataFlag = true;
		boolean prevMbSkipped = false;
		
		if (entropyCodingMode == 1) {
			while (bitBufferReader.getCurrentBitOffset() != 0) {
				_cabac_alignement_one_bit = (int) bitBufferReader.readNBits(1);
			}
		}

		do {
			if (_slice_type != 2 && _slice_type != 7 && _slice_type != 4 && _slice_type != 9) {
				if (entropyCodingMode == 0) {
					_mb_skip_run = bitBufferReader.readUE();
					prevMbSkipped = (_mb_skip_run > 0);
					for (int i = 0; i < _mb_skip_run; ++i) {
						currMbAddr = nextMbAddress(currMbAddr);
					}
					if (_mb_skip_run > 0) {
						moreDataFlag = bitBufferReader.hasMoreData();
					}
				} else {
					_mb_skip_flag = bitBufferReader.readAE();
					moreDataFlag = (_mb_skip_flag == 0 ? true : false);
				}
			}

				
			if (moreDataFlag) {
				if (mbaffFrameFlag == 1 && (currMbAddr % 2 == 0 || (currMbAddr % 2 == 1 && prevMbSkipped))) {
					_mb_field_decoding_flag = (entropyCodingMode == 0 ? (int) bitBufferReader.readNBits(1) : bitBufferReader.readAE());
				}
				readMacroblockLayer(bitBufferReader);
			}
			
			if (entropyCodingMode == 0) {
				moreDataFlag = bitBufferReader.hasMoreData();
			} else {
				if (_slice_type != 2 && _slice_type != 7 && _slice_type != 4 && _slice_type != 9) {
					prevMbSkipped = (_mb_skip_flag == 0 );
				}
				if (mbaffFrameFlag == 1 && currMbAddr % 2 == 0) {
					moreDataFlag = true;
				} else {
					_end_of_slice_flag = bitBufferReader.readAE();
					moreDataFlag = (_mb_skip_flag == 0);
				}
			}
			currMbAddr = nextMbAddress(currMbAddr);
		} while (moreDataFlag);
	}
	
	private int nextMbAddress(int current) {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int frameMbsOnlyFlag = _seqParameterSetParser.getFrameMbsOnlyFlag();
		int frameHeightInMbs = (2 - frameMbsOnlyFlag) * picHeightInMapUnits;
		int picHeightInMbs = frameHeightInMbs / (1 + _field_pic_flag);
		int picSizeInMbs = picWidthInMbs * picHeightInMbs;
		int i = current + 1;
		
		_mapUnitToSliceGroupMap = new int[picSizeInMbs];
		_mbToSliceGroupMap = new int[picSizeInMbs];
		convertMacroblockToSliceGroupMap();
		convertSliceGroupMapToMacroblockGroupMap();
		while (i < picSizeInMbs && _mbToSliceGroupMap[i] != _mbToSliceGroupMap[current]) {
			++i;
		}
		return i;
	}
	
	private void convertSliceGroupMapToMacroblockGroupMap() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int frameMbsOnlyFlag = _seqParameterSetParser.getFrameMbsOnlyFlag();
		int frameHeightInMbs = (2 - frameMbsOnlyFlag) * picHeightInMapUnits;
		int picHeightInMbs = frameHeightInMbs / (1 + _field_pic_flag);
		int picSizeInMbs = picWidthInMbs * picHeightInMbs;
		int mbAffFrameFlag = (_seqParameterSetParser.getMbAdaptiveFrameFieldFlag() == 1 && _field_pic_flag == 0 ? 1 : 0); 
		
		_mbToSliceGroupMap = new int[picSizeInMbs];
		for (int i = 0; i < picSizeInMbs; ++i) {
			if (frameMbsOnlyFlag == 1 || _field_pic_flag == 1) {
				_mbToSliceGroupMap[i] = _mapUnitToSliceGroupMap[i];
			} else if (mbAffFrameFlag == 1) {
				_mbToSliceGroupMap[i] = _mapUnitToSliceGroupMap[i / 2];
			} else {
				_mbToSliceGroupMap[i] = _mapUnitToSliceGroupMap[(i / (2 * picWidthInMbs)) * picWidthInMbs + (i % picWidthInMbs)];
			}
		}
	}
	
	private void convertMacroblockToSliceGroupMap() {
		if (_pictureParameterSetParser.getNumSliceGroupsMinus1() != 0) {
			switch (_pictureParameterSetParser.getSliceGroupMapType()) {
				case 0:
					createMapUnitWithInterleavedSlice();
					break;
				case 1:
					createMapUnitWithDispersedSlice();
					break;
				case 2:
					createMapUnitWithForegroundSlice();
					break;
				case 3:
					createMapUnitWithBoxoutSlice();
					break;
				case 4:
					createMapUnitWithRasterSlice();
					break;
				case 5:
					createMapUnitWithWipeSlice();
					break;
				case 6:
					createMapUnitWithExplicitSlice();
					break;
				default:
					break;
			}
		}
	}

	private void createMapUnitWithInterleavedSlice() {
		int numSliceGroupMinus1 = _pictureParameterSetParser.getNumSliceGroupsMinus1();
		int picSizeInMapUnits = _pictureParameterSetParser.getPicSizeInMapUnitMinus1() + 1;
		int runLengthMinus1[] = _pictureParameterSetParser.getRunLengthMinus1(); 
		int i = 0;
		
		do {
			for (int iGroup = 0; iGroup <= numSliceGroupMinus1 && i < picSizeInMapUnits; 
					i += (runLengthMinus1[iGroup++] + 1)) {
				for (int j = 0; j <= runLengthMinus1[iGroup] && (i + j) < picSizeInMapUnits; j++) {
					_mapUnitToSliceGroupMap[i + j] = iGroup;
				}
			}
		} while (i < picSizeInMapUnits);
	}
	
	private void createMapUnitWithDispersedSlice() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int numSliceGroupsMinus1 = _pictureParameterSetParser.getNumSliceGroupsMinus1();
		int picSizeInMapUnits = picWidthInMbs * picHeightInMapUnits;
		
		for (int i = 0; i < picSizeInMapUnits + 1; ++i) {
			_mapUnitToSliceGroupMap[i] = ((i % picWidthInMbs) +
					(((i / picWidthInMbs) * (numSliceGroupsMinus1 + 1)) / 2)) % (numSliceGroupsMinus1 + 1);
		}
	}
	
	private void createMapUnitWithForegroundSlice() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int numSliceGroupsMinus1 = _pictureParameterSetParser.getNumSliceGroupsMinus1();
		int picSizeInMapUnits = picWidthInMbs * picHeightInMapUnits;
		int topLeft[] = _pictureParameterSetParser.getTopLeft();
		int bottomRight[] = _pictureParameterSetParser.getBottomRight();
		
		for (int i = 0; i < picSizeInMapUnits; i++) {
			_mapUnitToSliceGroupMap[i] = numSliceGroupsMinus1;
		}
		for (int iGroup = numSliceGroupsMinus1 - 1; iGroup >= 0; iGroup--) {
			int yTopLeft = topLeft[iGroup] / picWidthInMbs;
			int xTopLeft = topLeft[iGroup] % picWidthInMbs;
			int yBottomRight = bottomRight[iGroup] / picWidthInMbs;
			int xBottomRight = bottomRight[iGroup] % picWidthInMbs;
			for (int y = yTopLeft; y <= yBottomRight; y++) {
				for (int x = xTopLeft; x <= xBottomRight; x++ ) {
					_mapUnitToSliceGroupMap[y * picWidthInMbs + x] = iGroup;
				}	
			}	
		}
	}
	
	private void createMapUnitWithBoxoutSlice() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int sliceGroupChangeRate = _pictureParameterSetParser.getSliceGroupChangeRateMinus1() + 1;
		int sliceGroupChangeDirectionFlag = _pictureParameterSetParser.getSliceGroupChangeDirectionFlag();
		int picSizeInMapUnits = picWidthInMbs * picHeightInMapUnits;
		int mapUnitsInSliceGroup0 = Math.min(_slice_group_change_cycle * sliceGroupChangeRate, picSizeInMapUnits);
		int leftBound;
		int rightBound;
		int topBound;
		int bottomBound;
		int xDir;
		int yDir;
		int mapUnitVacant = 0;
		
		for (int i = 0; i < picSizeInMapUnits; i++) {
			_mapUnitToSliceGroupMap[i] = 1;
		}
		int x = (picWidthInMbs - sliceGroupChangeDirectionFlag) / 2;
		int y = (picHeightInMapUnits - sliceGroupChangeDirectionFlag) / 2;
		leftBound = x;
		topBound = y;
		rightBound = x;
		bottomBound = y;
		xDir = sliceGroupChangeDirectionFlag - 1;
		yDir = sliceGroupChangeDirectionFlag;
		
		for(int k = 0; k < mapUnitsInSliceGroup0; k += mapUnitVacant) {
			mapUnitVacant = (_mapUnitToSliceGroupMap[y * picWidthInMbs + x] == 1 ? 1 : 0);
			if (mapUnitVacant == 1) {
				_mapUnitToSliceGroupMap[y * picWidthInMbs + x] = 0; 
			}
			if (xDir == -1 && x == leftBound) {
				leftBound = Math.max(leftBound - 1, 0);
				x = leftBound;
				xDir = 0;
				yDir = 2 * sliceGroupChangeDirectionFlag - 1;
			} else if (xDir == 1 && x == rightBound) {
				rightBound = Math.min(rightBound + 1, picWidthInMbs - 1);
				x = rightBound;
				xDir = 0;
				yDir = 1 - 2 * sliceGroupChangeDirectionFlag;
			} else if (yDir == -1 && y == topBound) {
				topBound = Math.max(topBound - 1, 0);
				y = topBound;
				xDir = 1 - 2 * sliceGroupChangeDirectionFlag;
				yDir = 0;
			} else if (yDir == 1 && y == bottomBound) {
				bottomBound = Math.min(bottomBound + 1, picHeightInMapUnits - 1);
				y = bottomBound;
				xDir = 2 * sliceGroupChangeDirectionFlag - 1;
				yDir = 0;
			} else {
				x = x + xDir;
				y = y + yDir;
			}
		}
	}
	
	private void createMapUnitWithRasterSlice() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int sliceGroupChangeRate = _pictureParameterSetParser.getSliceGroupChangeRateMinus1() + 1;
		int sliceGroupChangeDirectionFlag = _pictureParameterSetParser.getSliceGroupChangeDirectionFlag();
		int picSizeInMapUnits = picWidthInMbs * picHeightInMapUnits;
		int mapUnitsInSliceGroup0 = Math.min(_slice_group_change_cycle * sliceGroupChangeRate, picSizeInMapUnits);
		int sizeOfUpperLeftGroup = (sliceGroupChangeDirectionFlag == 1 ? 
				(picSizeInMapUnits - mapUnitsInSliceGroup0) : mapUnitsInSliceGroup0);
		
		for (int i = 0; i < picSizeInMapUnits; i++) {
			if (i < sizeOfUpperLeftGroup) {
				_mapUnitToSliceGroupMap[i] = sliceGroupChangeDirectionFlag;
			} else { 
				_mapUnitToSliceGroupMap[i] = 1 - sliceGroupChangeDirectionFlag;
			}
		}
	}
	
	private void createMapUnitWithWipeSlice() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int sliceGroupChangeRate = _pictureParameterSetParser.getSliceGroupChangeRateMinus1() + 1;
		int sliceGroupChangeDirectionFlag = _pictureParameterSetParser.getSliceGroupChangeDirectionFlag();
		int picSizeInMapUnits = picWidthInMbs * picHeightInMapUnits;
		int mapUnitsInSliceGroup0 = Math.min(_slice_group_change_cycle * sliceGroupChangeRate, picSizeInMapUnits);
		int sizeOfUpperLeftGroup = (sliceGroupChangeDirectionFlag == 1 ? 
				(picSizeInMapUnits - mapUnitsInSliceGroup0) : mapUnitsInSliceGroup0);
		int k = 0;
		
		for (int j = 0; j < picWidthInMbs; j++) {
			for (int i = 0; i < picHeightInMapUnits; i++) {
				if(k++ < sizeOfUpperLeftGroup) {
					_mapUnitToSliceGroupMap[i * picWidthInMbs + j] = sliceGroupChangeDirectionFlag;
				} else {
					_mapUnitToSliceGroupMap[i * picWidthInMbs + j] = 1 - sliceGroupChangeDirectionFlag;
				}		
			}
		}
	}
	
	private void createMapUnitWithExplicitSlice() {
		int picWidthInMbs = _seqParameterSetParser.getPicWidthInMbsMinus1() + 1;
		int picHeightInMapUnits = _seqParameterSetParser.getPicHeightInMapUnitsMinus1() + 1;
		int picSizeInMapUnits = picWidthInMbs * picHeightInMapUnits;
		
		for (int i = 0; i < picSizeInMapUnits; ++i) {
			_mapUnitToSliceGroupMap[i] = _pictureParameterSetParser.getSliceGroupId()[i];
		}
	}
	
	private void readMacroblockLayer(BitBufferReader bitBufferReader) {
		MacroblockLayerParser layerParser = new MacroblockLayerParser(_seqParameterSetParser, _pictureParameterSetParser, _slice_type, _field_pic_flag, _mb_field_decoding_flag);
		layerParser.parseMacroblockLayer(bitBufferReader);
		for (Pair<Integer, Integer> p : layerParser.getMacroblockResidualOffset()) {
			_macroblockDataOffset.add(p);
		}
	}
	
	public int getSliceDataOffset() {
		return _sliceDataOffset;
	}
	
	public List<Pair<Integer, Integer>> getMacroblockDataOffset () {
		return _macroblockDataOffset;
	}

	public int getNalUnitType() {
		return _nalUnitType;
	}

	public int getNalRefIdc() {
		return _nalRefIdc;
	}

	public int getChromaArrayType() {
		return _chromaArrayType;
	}

	public boolean isIdrPicFlag() {
		return _idrPicFlag;
	}

	public List<Pair<Integer, Integer>> get_macroblockDataOffset() {
		return _macroblockDataOffset;
	}

	public int getFirstMbInSlice() {
		return _first_mb_in_slice;
	}

	public int getSliceType() {
		return _slice_type;
	}

	public int getPicParameterSetId() {
		return _pic_parameter_set_id;
	}

	public int getColourPlaneId() {
		return _colour_plane_id;
	}

	public int getFrameNum() {
		return _frame_num;
	}

	public int getFieldPicFlag() {
		return _field_pic_flag;
	}

	public int getBottomFieldFlag() {
		return _bottom_field_flag;
	}

	public int getIdrPicId() {
		return _idr_pic_id;
	}

	public int getPicOrderCntLsb() {
		return _pic_order_cnt_lsb;
	}

	public int getDeltaPicOrderCntBottom() {
		return _delta_pic_order_cnt_bottom;
	}

	public int[] getDeltaPicOrderCnt() {
		return _delta_pic_order_cnt;
	}

	public int getRedundantPicCnt() {
		return _redundant_pic_cnt;
	}

	public int getDirectSpatialMvPredFlag() {
		return _direct_spatial_mv_pred_flag;
	}

	public int getNumRefIdxActiveOverrideFlag() {
		return _num_ref_idx_active_override_flag;
	}

	public int getNumRefIdxL0ActiveMinus1() {
		return _num_ref_idx_l0_active_minus1;
	}

	public int getNumRefIdxL1ActiveMinus1() {
		return _num_ref_idx_l1_active_minus1;
	}

	public int getCabacInitIdc() {
		return _cabac_init_idc;
	}

	public int getSliceQpDelta() {
		return _slice_qp_delta;
	}

	public int getSpForSwitchFlag() {
		return _sp_for_switch_flag;
	}

	public int getSliceQsDelta() {
		return _slice_qs_delta;
	}

	public int getDisableDeblockingFilterIdc() {
		return _disable_deblocking_filter_idc;
	}

	public int getSliceAlphaC0OffsetDiv2() {
		return _slice_alpha_c0_offset_div2;
	}

	public int getSliceBetaOffsetDiv2() {
		return _slice_beta_offset_div2;
	}

	public int getSliceGroupChangeCycle() {
		return _slice_group_change_cycle;
	}

	public int getRefPicListModificationFlagL0() {
		return _ref_pic_list_modification_flag_l0;
	}

	public int getModificationOfPicNumIdc() {
		return _modification_of_pic_num_idc;
	}

	public int getAbsDiffPicNumMinus1() {
		return _abs_diff_pic_num_minus1;
	}

	public int getLongTermPicNum() {
		return _long_term_pic_num;
	}

	public int getAbsDiffViewIdxMinus1() {
		return _abs_diff_view_idx_minus1;
	}

	public int getRefPicListModificationFlagL1() {
		return _ref_pic_list_modification_flag_l1;
	}

	public int getLumaLog2WeightDenom() {
		return _luma_log2_weight_denom;
	}

	public int getChromaLog2WeightDenom() {
		return _chroma_log2_weight_denom;
	}

	public int getLumaWeightL0Flag() {
		return _luma_weight_l0_flag;
	}

	public int[] getLumaWeightL0() {
		return _luma_weight_l0;
	}

	public int[] getLumaOffsetL0() {
		return _luma_offset_l0;
	}

	public int getChromaWeightL0Flag() {
		return _chroma_weight_l0_flag;
	}

	public int[][] getChromaWeightL0() {
		return _chroma_weight_l0;
	}

	public int[][] getChromaOffsetL0() {
		return _chroma_offset_l0;
	}

	public int getLumaWeightL1Flag() {
		return _luma_weight_l1_flag;
	}

	public int[] getLumaWeightL1() {
		return _luma_weight_l1;
	}

	public int[] getLumaOffsetL1() {
		return _luma_offset_l1;
	}

	public int getChromaWeightL1Flag() {
		return _chroma_weight_l1_flag;
	}

	public int[][] getChromaWeightL1() {
		return _chroma_weight_l1;
	}

	public int[][] getChromaOffsetL1() {
		return _chroma_offset_l1;
	}

	public int getNoOutputOfPriorPicsFlag() {
		return _no_output_of_prior_pics_flag;
	}

	public int getLongTermReferenceFlag() {
		return _long_term_reference_flag;
	}

	public int getAdaptiveRefPicMarkingModeFlag() {
		return _adaptive_ref_pic_marking_mode_flag;
	}

	public int getMemoryManagementControlOperation() {
		return _memory_management_control_operation;
	}

	public int getDifferenceOfPicNumsMinus1() {
		return _difference_of_pic_nums_minus1;
	}

	public int getLongTermFrameIdx() {
		return _long_term_frame_idx;
	}

	public int getMaxLongTermFrameIdxPlus1() {
		return _max_long_term_frame_idx_plus1;
	}

	public int[] getMapUnitToSliceGroupMap() {
		return _mapUnitToSliceGroupMap;
	}

	public int[] getMbToSliceGroupMap() {
		return _mbToSliceGroupMap;
	}

	public int getCabacAlignementOneBit() {
		return _cabac_alignement_one_bit;
	}

	public int getMbSkipRun() {
		return _mb_skip_run;
	}

	public int getMbSkipFlag() {
		return _mb_skip_flag;
	}

	public int getMbFieldDecodingFlag() {
		return _mb_field_decoding_flag;
	}

	public int getEndOfSliceFlag() {
		return _end_of_slice_flag;
	}
}
