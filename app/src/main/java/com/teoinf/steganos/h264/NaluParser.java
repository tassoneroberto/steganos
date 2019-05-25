package com.teoinf.steganos.h264;

import java.io.ByteArrayOutputStream;

import com.teoinf.steganos.tools.BitBufferReader;

public class NaluParser {

	private int _naluHeaderSize;
	
	// nal unit header
	private int _forbidden_zero_bit;
	private int _nal_ref_idc;
	private int _nal_unit_type;
	private int _svc_extension_flag;
	
	// svc extension 
	private int _idr_flag;
	private int _no_inter_layer_pred_flag;
	private int _dependency_id;
	private int _quality_id;
	private int _use_ref_base_pic_flag;
	private int _discardable_flag;
	private int _output_flag;
	private int _reserved_three_2bits;
	
	// mvc extension
	private int _non_idr_flag;
	private int _view_id;
	private int _anchor_pic_flag;
	private int _inter_view_flag;
	private int _reserved_one_bit;
	
	// svc and mvc extension
	private int _priority_id;
	private int _temporal_id;
	
	// RBSP
	private byte[] _rbsp;
	
	public NaluParser() {
		_naluHeaderSize = 0;
		
		_forbidden_zero_bit = 0;
		_nal_ref_idc = 0;
		_nal_unit_type = 0;
		_svc_extension_flag = 0;
		
		_idr_flag = 0;
		_no_inter_layer_pred_flag = 0;
		_dependency_id = 0;
		_quality_id = 0;
		_use_ref_base_pic_flag = 0;
		_discardable_flag = 0;
		_output_flag = 0;
		_reserved_three_2bits = 0;
		
		_non_idr_flag = 0;
		_view_id = 0;
		_anchor_pic_flag = 0;
		_inter_view_flag = 0;
		_reserved_one_bit = 0;
		
		_priority_id = 0;
		_temporal_id = 0;
		
		_rbsp = null;
	}
	
	public void parseNaluData(byte[] data) {
		ByteArrayOutputStream outputStream;
		BitBufferReader bitBufferReader = new BitBufferReader(data);
		
		_forbidden_zero_bit = (int) bitBufferReader.readNBits(1);
		_nal_ref_idc = (int) bitBufferReader.readNBits(2);
		_nal_unit_type = (int) bitBufferReader.readNBits(5);
		_naluHeaderSize++;
				
		if (_nal_unit_type == 14 || _nal_unit_type == 20 || _nal_unit_type == 21) {
			_svc_extension_flag = (int) bitBufferReader.readNBits(1);
			if (_svc_extension_flag == 1) {
				getSvcExtensionAttributes(bitBufferReader);
			} else {
				getMvcExtensionAttributes(bitBufferReader);
			}
			_naluHeaderSize += 3;
		}
		
		outputStream = new ByteArrayOutputStream();
		for (int i = _naluHeaderSize; i < data.length; ++i) {			
			if (i + 2 < data.length && data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x03) {
				outputStream.write((byte) bitBufferReader.readNBits(8));
				outputStream.write((byte) bitBufferReader.readNBits(8));
				bitBufferReader.readNBits(8);
				i += 2;
			} else {
				outputStream.write((byte) bitBufferReader.readNBits(8));
			}
		}
		_rbsp = outputStream.toByteArray();
	}
	
	private void getSvcExtensionAttributes(BitBufferReader bitBufferReader) {
		_idr_flag = (int) bitBufferReader.readNBits(1);
		_priority_id = (int) bitBufferReader.readNBits(6);
		
		_no_inter_layer_pred_flag = (int) bitBufferReader.readNBits(1);
		_dependency_id = (int) bitBufferReader.readNBits(3);
		_quality_id = (int) bitBufferReader.readNBits(4);
		
		_temporal_id = (int) bitBufferReader.readNBits(3);
		_use_ref_base_pic_flag = (int) bitBufferReader.readNBits(1);
		_discardable_flag = (int) bitBufferReader.readNBits(1);
		_output_flag = (int) bitBufferReader.readNBits(1);
		_reserved_three_2bits = (int) bitBufferReader.readNBits(2);
	}
	
	private void getMvcExtensionAttributes(BitBufferReader bitBufferReader) {
		_non_idr_flag = (int) bitBufferReader.readNBits(1);
		_priority_id = (int) bitBufferReader.readNBits(6);
		
		_view_id = (int) bitBufferReader.readNBits(10);

		_temporal_id = (int) bitBufferReader.readNBits(3);
		_anchor_pic_flag = (int) bitBufferReader.readNBits(1);
		_inter_view_flag = (int) bitBufferReader.readNBits(1);
		_reserved_one_bit = (int) bitBufferReader.readNBits(1);
	}

	public int getNaluHeaderSize() {
		return _naluHeaderSize;
	}
	
	public int getForbiddenZeroBit() {
		return _forbidden_zero_bit;
	}

	public int getNalRefIdc() {
		return _nal_ref_idc;
	}

	public int getNalUnitType() {
		return _nal_unit_type;
	}

	public int getSvcExtensionFlag() {
		return _svc_extension_flag;
	}

	public int getIdrFlag() {
		return _idr_flag;
	}

	public int getNoInterLayerPredFlag() {
		return _no_inter_layer_pred_flag;
	}

	public int getDependencyId() {
		return _dependency_id;
	}

	public int getQualityId() {
		return _quality_id;
	}

	public int getUseRefBasePicFlag() {
		return _use_ref_base_pic_flag;
	}

	public int getDiscardableFlag() {
		return _discardable_flag;
	}

	public int getOutputFlag() {
		return _output_flag;
	}

	public int getReservedThree2bits() {
		return _reserved_three_2bits;
	}

	public int getNonIdrFlag() {
		return _non_idr_flag;
	}

	public int getViewId() {
		return _view_id;
	}

	public int getAnchorPicFlag() {
		return _anchor_pic_flag;
	}

	public int getInterViewFlag() {
		return _inter_view_flag;
	}

	public int getReservedOneBit() {
		return _reserved_one_bit;
	}

	public int getPriorityId() {
		return _priority_id;
	}

	public int getTemporalId() {
		return _temporal_id;
	}

	public byte[] getRbsp() {
		return _rbsp;
	}	
}
