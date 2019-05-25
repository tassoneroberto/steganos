package com.teoinf.steganos.h264;

import com.teoinf.steganos.tools.BitBufferReader;

public class PictureParameterSetParser {

	private SeqParameterSetParser _parameterSetParser;
	
	private int _pic_parameter_set_id;
	private int _seq_parameter_set_id;
	private int _entropy_coding_mode_flag;
	private int _bottom_field_pic_order_in_frame_present_flag;
	private int _num_slice_groups_minus1;
	private int _slice_group_map_type;

	private int _run_length_minus1[];
	private int _top_left[];
	private int _bottom_right[];
	
	private int _slice_group_change_direction_flag;
	private int _slice_group_change_rate_minus1;
	private int _pic_size_in_map_unit_minus1;
	private int _slice_group_id[];
	
	private int _num_ref_idx_l0_default_active_minus1;
	private int _num_ref_idx_l1_default_active_minus1;
	private int _weighted_pred_flag;
	private int _weighted_bipred_idc;
	private int _pic_init_qp_minus26;
	private int _pic_init_qs_minus26;
	private int _chroma_qp_index_offset;
	private int _deblocking_filter_control_present_flag;
	private int _constrained_intra_pred_flag;
	private int _redundant_pic_cnt_present_flag;
	
	private int _transform_8x8_mode_flag;
	private int _pic_scaling_matrix_present_flag;
	private int _pic_scaling_list_present_flag[];
	private int _second_chroma_qp_index_offset;
	
	public PictureParameterSetParser(SeqParameterSetParser parser) {
		_parameterSetParser = parser;
	}
	
	public void parsePictureParameterSet(byte data[]) {
		BitBufferReader bitBufferReader = new BitBufferReader(data);
		
		_pic_parameter_set_id = bitBufferReader.readUE();
		_seq_parameter_set_id = bitBufferReader.readUE();
		_entropy_coding_mode_flag = (int) bitBufferReader.readNBits(1);
		_bottom_field_pic_order_in_frame_present_flag = (int) bitBufferReader.readNBits(1);
		_num_slice_groups_minus1 = bitBufferReader.readUE();

		if (_num_slice_groups_minus1 > 0) {
			_slice_group_map_type = bitBufferReader.readUE();
			if (_slice_group_map_type == 0) {
				_run_length_minus1 = new int[_num_slice_groups_minus1 + 1];
				for (int i = 0; i <= _num_slice_groups_minus1; ++i) {
					_run_length_minus1[i] = bitBufferReader.readUE();
				}
			} else if (_slice_group_map_type == 2) {
				_top_left = new int[_num_slice_groups_minus1];
				_bottom_right = new int[_num_slice_groups_minus1];
				for (int i = 0; i < _num_slice_groups_minus1; ++i) {
					_top_left[i] = bitBufferReader.readUE();
					_bottom_right[i] = bitBufferReader.readUE();
				}
			} else if (_slice_group_map_type == 3 || _slice_group_map_type == 4 || _slice_group_map_type == 5) {
				_slice_group_change_direction_flag = (int) bitBufferReader.readNBits(1);
				_slice_group_change_rate_minus1 = bitBufferReader.readUE();
			} else if (_slice_group_map_type == 6) {
				_pic_size_in_map_unit_minus1 = bitBufferReader.readUE();
				_slice_group_id = new int[_pic_size_in_map_unit_minus1 + 1];
				for (int i = 0; i <= _pic_size_in_map_unit_minus1; ++i) {
					_slice_group_id[i] = (int) bitBufferReader.readNBits((int)(Math.ceil(Math.log10(_num_slice_groups_minus1 + 1) / Math.log10(2))));
				}
			}
		}
		
		_num_ref_idx_l0_default_active_minus1 = bitBufferReader.readUE();
		_num_ref_idx_l1_default_active_minus1 = bitBufferReader.readUE();
		_weighted_pred_flag = (int) bitBufferReader.readNBits(1);
		_weighted_bipred_idc = (int) bitBufferReader.readNBits(2);
		_pic_init_qp_minus26 = bitBufferReader.readSE();
		_pic_init_qs_minus26 = bitBufferReader.readSE();
		_chroma_qp_index_offset = bitBufferReader.readSE();
		_deblocking_filter_control_present_flag = (int) bitBufferReader.readNBits(1);
		_constrained_intra_pred_flag = (int) bitBufferReader.readNBits(1);
		_redundant_pic_cnt_present_flag = (int) bitBufferReader.readNBits(1);
		
		if (bitBufferReader.hasMoreData()) {
			_transform_8x8_mode_flag = (int) bitBufferReader.readNBits(1);
			_pic_scaling_matrix_present_flag = (int) bitBufferReader.readNBits(1);
			if (_pic_scaling_matrix_present_flag == 1) {
				_pic_scaling_list_present_flag = new int[6 + ((_parameterSetParser.getChromaFormatIdc() != 3) ? 2 : 6) * _transform_8x8_mode_flag];
				for (int i = 0; i < 6 + ((_parameterSetParser.getChromaFormatIdc() != 3) ? 2 : 6) * _transform_8x8_mode_flag; ++i) {
					_pic_scaling_list_present_flag[i] = (int) bitBufferReader.readNBits(1);
					if (_pic_scaling_list_present_flag[i] == 1) {
						if (i < 6) {
							System.out.println("Scaling list 4 * 4");
						} else {
							System.out.println("Scaling list 8 * 8");							
						}
					}
				}
			}
			_second_chroma_qp_index_offset = bitBufferReader.readSE();
		}

		//RBSP Trailing bits
		bitBufferReader.readNBits(1);
		while (bitBufferReader.getCurrentBitOffset() != 0) {
			bitBufferReader.readNBits(1);
		}
	}

	public int getPicparameterSetId() {
		return _pic_parameter_set_id;
	}

	public int getSeqParameterSetId() {
		return _seq_parameter_set_id;
	}

	public int getEntropyCodingModeFlag() {
		return _entropy_coding_mode_flag;
	}

	public int getBottomFieldPicOrderInFramePresentFlag() {
		return _bottom_field_pic_order_in_frame_present_flag;
	}

	public int getNumSliceGroupsMinus1() {
		return _num_slice_groups_minus1;
	}

	public int getSliceGroupMapType() {
		return _slice_group_map_type;
	}

	public int[] getRunLengthMinus1() {
		return _run_length_minus1;
	}

	public int[] getTopLeft() {
		return _top_left;
	}

	public int[] getBottomRight() {
		return _bottom_right;
	}

	public int getSliceGroupChangeDirectionFlag() {
		return _slice_group_change_direction_flag;
	}

	public int getSliceGroupChangeRateMinus1() {
		return _slice_group_change_rate_minus1;
	}

	public int getPicSizeInMapUnitMinus1() {
		return _pic_size_in_map_unit_minus1;
	}

	public int[] getSliceGroupId() {
		return _slice_group_id;
	}

	public int getNumRefIdxL0DefaultActiveMinus1() {
		return _num_ref_idx_l0_default_active_minus1;
	}

	public int getNumRefIdxL1DefaultActiveMinus1() {
		return _num_ref_idx_l1_default_active_minus1;
	}

	public int getWeightedPredFlag() {
		return _weighted_pred_flag;
	}

	public int getWeightedBipredIdc() {
		return _weighted_bipred_idc;
	}

	public int getPicInitQpMinus26() {
		return _pic_init_qp_minus26;
	}

	public int getPicInitQsMinus26() {
		return _pic_init_qs_minus26;
	}

	public int getChromaQpIndexOffset() {
		return _chroma_qp_index_offset;
	}

	public int getDeblockingFilterControlPresentFlag() {
		return _deblocking_filter_control_present_flag;
	}

	public int getConstrainedIntraPredFlag() {
		return _constrained_intra_pred_flag;
	}

	public int getRedundantPicCntPresentFlag() {
		return _redundant_pic_cnt_present_flag;
	}

	public int getTransform8x8ModeFlag() {
		return _transform_8x8_mode_flag;
	}

	public int getPicScalingMatrixPresentFlag() {
		return _pic_scaling_matrix_present_flag;
	}

	public int[] getPicScalingListPresentFlag() {
		return _pic_scaling_list_present_flag;
	}

	public int getSecondChromaQpIndexOffset() {
		return _second_chroma_qp_index_offset;
	}
	
}
