package com.teoinf.steganos.h264;

import java.util.List;

import com.teoinf.steganos.tools.BitBufferReader;

public class SeqParameterSetParser {

	// Default attributes
	private int _profile_idc;
	private int _constraint_set0_flag;
	private int _constraint_set1_flag;
	private int _constraint_set2_flag;
	private int _constraint_set3_flag;
	private int _constraint_set4_flag;
	private int _constraint_set5_flag;
	private int _reserved_zero_2bits;
	private int _level_idc;
	private int _seq_parameter_set_id;
	
	private int _log2_max_frame_num_minus4;
	private int _pic_order_cnt_type;
	private int _log2_max_pic_order_cnt_lsb_minus4;
	private int _delta_pic_order_always_zero_flag;
	private int _offset_for_non_ref_pic;
	private int _offset_for_top_to_bottom_field;
	private int _num_ref_frames_in_pic_order_cnt_cycle;
	private int _offset_for_ref_frame[];
	
	private int _max_num_ref_frames;
	private int _gaps_in_frame_num_value_allowed_flag;
	private int _pic_width_in_mbs_minus1;
	private int _pic_height_in_map_units_minus1;
	private int _frame_mbs_only_flag;
	private int _mb_adaptive_frame_field_flag;
	private int _direct_8x8_interference_flag;
	
	private int _frame_cropping_flag;
	private int _frame_crop_left_offset;
	private int _frame_crop_right_offset;
	private int _frame_crop_top_offset;
	private int _frame_crop_bottom_offset;
	private int _vui_parameters_present_flag;
	
	
	// Chroma profile
	private int _chroma_format_idc;
	private int _separate_colour_plane_flag;
	private int _bit_depth_luma_minus8;
	private int _bit_depth_chroma_minus8;
	private int _qpprime_y_zero_transform_bypass_flag;
	private int _seq_scaling_matrix_present_flag;
	private List<Integer> _seq_scaling_list_present_flag;

	// ScalingList
	private int _scaling_list4x4[];
	private int _scaling_list8x8[];
	private int _use_default_scaling_matrix4x4_flag[];
	private int _use_default_scaling_matrix8x8_flag[];
	private int _delta_scale;
	
	// Vui parameters
	private int _aspect_ratio_info_present_flag;
	private int _aspect_ratio_idc;
	private int _sar_width;
	private int _sar_height;
	private int _overscan_info_present_flag;
	private int _overscan_appropriate_flag;
	private int _video_signal_type_present_flag;
	private int _video_format;
	private int _video_full_range_flag;
	private int _colour_description_present_flag;
	private int _colour_primaries;
	private int _transfer_characteristics;
	private int _matrix_coefficients;
	private int _chroma_loc_info_present_flag;
	private int _chroma_sample_loc_type_top_field;
	private int _chroma_sample_loc_type_bottom_field;
	private int _timing_info_present_flag;
	private int _num_units_in_tick;
	private int _time_scale;
	private int _fixed_frame_rate_flag;
	private int _nal_hrd_parameters_present_flag;
	private int _vcl_hrd_parameters_present_flag;
	private int _low_delay_hrd_flag;
	private int _pic_struct_present_flag;
	private int _bitstream_restriction_flag;
	private int _motion_vector_over_pic_boundaries_flag;
	private int _max_bytes_per_pic_denom;
	private int _max_bits_per_mb_denom;
	private int _log2_max_mv_length_horizontal;
	private int _log2_max_mv_length_vertical;
	private int _max_num_reorder_frames;
	private int _max_dec_frame_buffering;
	
	// HRD Parameters
	private int _cpb_cnt_minus1;
	private int _bit_rate_scale;
	private int _cpb_size_scale;
	private int _bit_rate_value_minus1[];
	private int _cpb_size_value_minus1[];
	private int _cbr_flag[];	
	private int _initial_cpb_removal_delay_length_minus1;
	private int _cpb_removal_delay_length_minus1;
	private int _dpb_output_delay_length_minus1;
	private int _time_offset_length;
	
	public SeqParameterSetParser() {
	} 
	
	public void parseSeqParameterSetData(byte[] data) {
		BitBufferReader bitBufferReader = new BitBufferReader(data);
		
		_profile_idc = (int) bitBufferReader.readNBits(8);
		
		_constraint_set0_flag = (int) bitBufferReader.readNBits(1);
		_constraint_set1_flag = (int) bitBufferReader.readNBits(1);
		_constraint_set2_flag = (int) bitBufferReader.readNBits(1);
		_constraint_set3_flag = (int) bitBufferReader.readNBits(1);
		_constraint_set4_flag = (int) bitBufferReader.readNBits(1);
		_constraint_set5_flag = (int) bitBufferReader.readNBits(1);
		_reserved_zero_2bits = (int) bitBufferReader.readNBits(2);
		
		_level_idc = (int) bitBufferReader.readNBits(8);

		_seq_parameter_set_id = bitBufferReader.readUE();
		
		readChromaProfile(bitBufferReader);
	
		_log2_max_frame_num_minus4 = bitBufferReader.readUE();
		_pic_order_cnt_type = bitBufferReader.readUE();
		if (_pic_order_cnt_type == 0) {
			_log2_max_pic_order_cnt_lsb_minus4 = bitBufferReader.readUE();
		} else if (_pic_order_cnt_type == 1) {
			_delta_pic_order_always_zero_flag = (int) bitBufferReader.readNBits(1);
			_offset_for_non_ref_pic = bitBufferReader.readSE();
			_offset_for_top_to_bottom_field = bitBufferReader.readSE();
			_num_ref_frames_in_pic_order_cnt_cycle = bitBufferReader.readUE();
			
			_offset_for_ref_frame = new int[_num_ref_frames_in_pic_order_cnt_cycle];
			for (int i = 0; i < _num_ref_frames_in_pic_order_cnt_cycle; ++i) {
				_offset_for_ref_frame[i] = bitBufferReader.readSE();
			}
		}
		
		_max_num_ref_frames = bitBufferReader.readUE();
		_gaps_in_frame_num_value_allowed_flag = (int) bitBufferReader.readNBits(1);
		_pic_width_in_mbs_minus1 = bitBufferReader.readUE();
		_pic_height_in_map_units_minus1 = bitBufferReader.readUE();

		_frame_mbs_only_flag = (int) bitBufferReader.readNBits(1);
		if (_frame_mbs_only_flag == 0) {
			_mb_adaptive_frame_field_flag = (int) bitBufferReader.readNBits(1);
		}
		_direct_8x8_interference_flag = (int) bitBufferReader.readNBits(1);
		
		_frame_cropping_flag = (int) bitBufferReader.readNBits(1);
		if (_frame_cropping_flag == 1) {
			_frame_crop_left_offset = bitBufferReader.readUE();
			_frame_crop_right_offset = bitBufferReader.readUE();
			_frame_crop_top_offset = bitBufferReader.readUE();
			_frame_crop_bottom_offset = bitBufferReader.readUE();
		}

		_vui_parameters_present_flag = (int) bitBufferReader.readNBits(1);
		if (_vui_parameters_present_flag == 1) {
			readVuiParameters(bitBufferReader);
		}
		
		bitBufferReader.readNBits(1);
		while (bitBufferReader.getCurrentBitOffset() != 0) {
			bitBufferReader.readNBits(1);
		}
	}

	private void readChromaProfile(BitBufferReader bitBufferReader) {
		int tmp;
		
		if (_profile_idc == 100 || _profile_idc == 110 || _profile_idc == 122 ||
				_profile_idc == 244 || _profile_idc == 44 || _profile_idc == 83 ||
				_profile_idc == 86 || _profile_idc == 118 || _profile_idc == 128 || _profile_idc == 138) {
			_chroma_format_idc = bitBufferReader.readUE();
			
			if (_chroma_format_idc == 3) {
				_separate_colour_plane_flag = (int) bitBufferReader.readNBits(1);
			}
			_bit_depth_luma_minus8 = bitBufferReader.readUE();
			_bit_depth_chroma_minus8 = bitBufferReader.readUE();
			_qpprime_y_zero_transform_bypass_flag = (int) bitBufferReader.readNBits(1);
			_seq_scaling_matrix_present_flag = (int) bitBufferReader.readNBits(1);
			
			if (_seq_scaling_matrix_present_flag == 1) {
				for (int i = 0; i < ((_chroma_format_idc != 3) ? 8 : 12); ++i) {
					tmp = (int) bitBufferReader.readNBits(1);
					_seq_scaling_list_present_flag.add(tmp);
					
					if (tmp == 1) {
						if (i < 6) {
							// Scaling list 4 * 4
							System.out.println("Scaling list 4 * 4");
						} else {
							// Scaling list 8 * 8
							System.out.println("Scaling list 8 * 8");
						}
					}
				}
			}
		}		
	}
		
	private void readVuiParameters(BitBufferReader bitBufferReader) {
		_aspect_ratio_info_present_flag = (int) bitBufferReader.readNBits(1);
		if (_aspect_ratio_info_present_flag == 1) {
			_aspect_ratio_idc = (int) bitBufferReader.readNBits(8);
			if (_aspect_ratio_idc == 255) {
				_sar_width = (int) bitBufferReader.readNBits(16);
				_sar_height = (int) bitBufferReader.readNBits(16);
			}
		}

		_overscan_info_present_flag = (int) bitBufferReader.readNBits(1);
		if (_overscan_info_present_flag == 1) {
			_overscan_appropriate_flag = (int) bitBufferReader.readNBits(1);
		}
		
		_video_signal_type_present_flag = (int) bitBufferReader.readNBits(1);
		if (_video_signal_type_present_flag == 1) {
			_video_format = (int) bitBufferReader.readNBits(3);
			_video_full_range_flag = (int) bitBufferReader.readNBits(1);
			_colour_description_present_flag = (int) bitBufferReader.readNBits(1);
			if (_colour_description_present_flag == 1) {
				_colour_primaries = (int) bitBufferReader.readNBits(8);
				_transfer_characteristics = (int) bitBufferReader.readNBits(8);
				_matrix_coefficients = (int) bitBufferReader.readNBits(8);
			}
		}
		
		_chroma_loc_info_present_flag = (int) bitBufferReader.readNBits(1);
		if (_chroma_format_idc == 1) {
			_chroma_sample_loc_type_top_field = bitBufferReader.readUE();
			_chroma_sample_loc_type_bottom_field = bitBufferReader.readUE();
		}
		
		_timing_info_present_flag = (int) bitBufferReader.readNBits(1);
		if (_timing_info_present_flag == 1) {
			_num_units_in_tick = (int) bitBufferReader.readNBits(32);
			_time_scale = (int) bitBufferReader.readNBits(32);
			_fixed_frame_rate_flag = (int) bitBufferReader.readNBits(1);
		}
		
		_nal_hrd_parameters_present_flag = (int) bitBufferReader.readNBits(1);
		if (_nal_hrd_parameters_present_flag == 1) {
			readHrdParameters(bitBufferReader);
		}
		
		_vcl_hrd_parameters_present_flag = (int) bitBufferReader.readNBits(1);
		if (_vcl_hrd_parameters_present_flag == 1) {
			readHrdParameters(bitBufferReader);
		}
		
		if (_nal_hrd_parameters_present_flag == 1 || _vcl_hrd_parameters_present_flag == 1) {
			_low_delay_hrd_flag = (int) bitBufferReader.readNBits(1);
		}
		_pic_struct_present_flag = (int) bitBufferReader.readNBits(1);
		
		_bitstream_restriction_flag = (int) bitBufferReader.readNBits(1);
		if (_bitstream_restriction_flag == 1) {
			_motion_vector_over_pic_boundaries_flag = (int) bitBufferReader.readNBits(1);
			_max_bytes_per_pic_denom = bitBufferReader.readUE();
			_max_bits_per_mb_denom = bitBufferReader.readUE();
			_log2_max_mv_length_horizontal = bitBufferReader.readUE();
			_log2_max_mv_length_vertical = bitBufferReader.readUE();
			_max_num_reorder_frames = bitBufferReader.readUE();
			_max_dec_frame_buffering = bitBufferReader.readUE();
		}
	}
	
	private void readHrdParameters(BitBufferReader bitBufferReader) {
		_cpb_cnt_minus1 = bitBufferReader.readUE();
		_bit_rate_scale = (int) bitBufferReader.readNBits(4);
		_cpb_size_scale = (int) bitBufferReader.readNBits(4);
		
		_bit_rate_value_minus1 = new int[_cpb_cnt_minus1 + 1];
		_cpb_size_value_minus1 = new int[_cpb_cnt_minus1 + 1];
		_cbr_flag = new int[_cpb_cnt_minus1 + 1];
		for (int i = 0; i <= _cpb_cnt_minus1; ++i) {
			_bit_rate_value_minus1[i] = bitBufferReader.readUE();
			_cpb_size_value_minus1[i] = bitBufferReader.readUE();
			_cbr_flag[i] = (int) bitBufferReader.readNBits(1);
		}
		
		_initial_cpb_removal_delay_length_minus1 = (int) bitBufferReader.readNBits(5);
		_cpb_removal_delay_length_minus1 = (int) bitBufferReader.readNBits(5);
		_dpb_output_delay_length_minus1 = (int) bitBufferReader.readNBits(5);
		_time_offset_length = (int) bitBufferReader.readNBits(5);
	}

	public int getProfileIdc() {
		return _profile_idc;
	}

	public int getConstraintSet0Flag() {
		return _constraint_set0_flag;
	}

	public int getConstraintSet1Flag() {
		return _constraint_set1_flag;
	}

	public int getConstraintSet2Flag() {
		return _constraint_set2_flag;
	}

	public int getConstraintSet3Flag() {
		return _constraint_set3_flag;
	}

	public int getConstraintSet4Flag() {
		return _constraint_set4_flag;
	}

	public int getConstraintSet5Flag() {
		return _constraint_set5_flag;
	}

	public int getReservedZero2bits() {
		return _reserved_zero_2bits;
	}

	public int getLevelIdc() {
		return _level_idc;
	}

	public int getSeqParameterSetId() {
		return _seq_parameter_set_id;
	}

	public int getLog2MaxFrameNumMinus4() {
		return _log2_max_frame_num_minus4;
	}

	public int getPicOrderCntType() {
		return _pic_order_cnt_type;
	}

	public int getLog2MaxPicOrderCntLsbMinus4() {
		return _log2_max_pic_order_cnt_lsb_minus4;
	}

	public int getDeltaPicOrderAlwaysZeroFlag() {
		return _delta_pic_order_always_zero_flag;
	}

	public int getOffsetForNonRefPic() {
		return _offset_for_non_ref_pic;
	}

	public int getOffsetForTopToBottomField() {
		return _offset_for_top_to_bottom_field;
	}

	public int getNumRefFramesInPicOrderCntCycle() {
		return _num_ref_frames_in_pic_order_cnt_cycle;
	}

	public int[] getOffsetForRefFrame() {
		return _offset_for_ref_frame;
	}

	public int getMaxNumRefFrames() {
		return _max_num_ref_frames;
	}

	public int getGapsInFrameNumValueAllowedFlag() {
		return _gaps_in_frame_num_value_allowed_flag;
	}

	public int getPicWidthInMbsMinus1() {
		return _pic_width_in_mbs_minus1;
	}

	public int getPicHeightInMapUnitsMinus1() {
		return _pic_height_in_map_units_minus1;
	}

	public int getFrameMbsOnlyFlag() {
		return _frame_mbs_only_flag;
	}

	public int getMbAdaptiveFrameFieldFlag() {
		return _mb_adaptive_frame_field_flag;
	}

	public int getDirect8x8InterferenceFlag() {
		return _direct_8x8_interference_flag;
	}

	public int getFrameCroppingFlag() {
		return _frame_cropping_flag;
	}

	public int getFrameCropLeftOffset() {
		return _frame_crop_left_offset;
	}

	public int getFrameCropRightOffset() {
		return _frame_crop_right_offset;
	}

	public int getFrameCropTopOffset() {
		return _frame_crop_top_offset;
	}

	public int getFrameCropBottomOffset() {
		return _frame_crop_bottom_offset;
	}

	public int getVuiParametersPresentFlag() {
		return _vui_parameters_present_flag;
	}

	public int getChromaFormatIdc() {
		return _chroma_format_idc;
	}

	public int getSeparateColourPlaneFlag() {
		return _separate_colour_plane_flag;
	}

	public int getBitDepthLumaMinus8() {
		return _bit_depth_luma_minus8;
	}

	public int getBitDepthChromaMinus8() {
		return _bit_depth_chroma_minus8;
	}

	public int getQpprimeYZeroTransformBypassFlag() {
		return _qpprime_y_zero_transform_bypass_flag;
	}

	public int getSeqScalingMatrixPresentFlag() {
		return _seq_scaling_matrix_present_flag;
	}

	public List<Integer> getSeqScalingListPresentFlag() {
		return _seq_scaling_list_present_flag;
	}

	public int[] getScalingList4x4() {
		return _scaling_list4x4;
	}

	public int[] getScalingList8x8() {
		return _scaling_list8x8;
	}

	public int[] getUseDefaultScalingMatrix4x4Flag() {
		return _use_default_scaling_matrix4x4_flag;
	}

	public int[] getUseDefaultScalingMatrix8x8Flag() {
		return _use_default_scaling_matrix8x8_flag;
	}

	public int getDeltaScale() {
		return _delta_scale;
	}

	public int getAspectRatioInfoPresentFlag() {
		return _aspect_ratio_info_present_flag;
	}

	public int getAspectRatioIdc() {
		return _aspect_ratio_idc;
	}

	public int getSarWidth() {
		return _sar_width;
	}

	public int getSarHeight() {
		return _sar_height;
	}

	public int getOverscanInfoPresentFlag() {
		return _overscan_info_present_flag;
	}

	public int getOverscanAppropriateFlag() {
		return _overscan_appropriate_flag;
	}

	public int getVideoSignalTypePresentFlag() {
		return _video_signal_type_present_flag;
	}

	public int getVideoFormat() {
		return _video_format;
	}

	public int getVideoFullRangeFlag() {
		return _video_full_range_flag;
	}

	public int getColourDescriptionPresentFlag() {
		return _colour_description_present_flag;
	}

	public int getColourPrimaries() {
		return _colour_primaries;
	}

	public int getTransferCharacteristics() {
		return _transfer_characteristics;
	}

	public int getMatrixCoefficients() {
		return _matrix_coefficients;
	}

	public int getChromaLocInfoPresentFlag() {
		return _chroma_loc_info_present_flag;
	}

	public int getChromaSampleLocTypeTopField() {
		return _chroma_sample_loc_type_top_field;
	}

	public int getChromaSampleLocTypeBottomField() {
		return _chroma_sample_loc_type_bottom_field;
	}

	public int getTimingInfoPresentFlag() {
		return _timing_info_present_flag;
	}

	public int getNumUnitsInTick() {
		return _num_units_in_tick;
	}

	public int getTimeScale() {
		return _time_scale;
	}

	public int getFixedFrameRateFlag() {
		return _fixed_frame_rate_flag;
	}

	public int getNalHrdParametersPresentFlag() {
		return _nal_hrd_parameters_present_flag;
	}

	public int getVclHrdParametersPresentFlag() {
		return _vcl_hrd_parameters_present_flag;
	}

	public int getLowDelayHrdFlag() {
		return _low_delay_hrd_flag;
	}

	public int getPicStructPresentFlag() {
		return _pic_struct_present_flag;
	}

	public int getBitstreamRestrictionFlag() {
		return _bitstream_restriction_flag;
	}

	public int getMotionVectorOverPicBoundariesFlag() {
		return _motion_vector_over_pic_boundaries_flag;
	}

	public int getMaxBytesPerPicDenom() {
		return _max_bytes_per_pic_denom;
	}

	public int getMaxBitsPerMbDenom() {
		return _max_bits_per_mb_denom;
	}

	public int getLog2MaxMvLengthHorizontal() {
		return _log2_max_mv_length_horizontal;
	}

	public int getLog2MaxMvLengthVertical() {
		return _log2_max_mv_length_vertical;
	}

	public int getMaxNumReorderFrames() {
		return _max_num_reorder_frames;
	}

	public int getMaxDecFrameBuffering() {
		return _max_dec_frame_buffering;
	}

	public int getCpbCntMinus1() {
		return _cpb_cnt_minus1;
	}

	public int getBitRateScale() {
		return _bit_rate_scale;
	}

	public int getCpbSizeScale() {
		return _cpb_size_scale;
	}

	public int[] getBitRateValueMinus1() {
		return _bit_rate_value_minus1;
	}

	public int[] getCpbSizeValueMinus1() {
		return _cpb_size_value_minus1;
	}

	public int[] getCbrFlag() {
		return _cbr_flag;
	}

	public int getInitialCpbRemovalDelayLengthMinus1() {
		return _initial_cpb_removal_delay_length_minus1;
	}

	public int getCpbRemovalDelayLengthMinus1() {
		return _cpb_removal_delay_length_minus1;
	}

	public int getDpbOutputDelayLengthMinus1() {
		return _dpb_output_delay_length_minus1;
	}

	public int getTimeOffsetLength() {
		return _time_offset_length;
	}
	
	
}
