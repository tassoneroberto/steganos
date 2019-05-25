package com.teoinf.steganos.algorithms.steganography.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.coremedia.iso.boxes.mdat.SampleList;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.teoinf.steganos.algorithms.ISteganographyContainer;
import com.teoinf.steganos.mp4.MP4MediaReader;
import com.teoinf.steganos.mp4.SteganosMemoryDataSourceImpl;

public class AACSteganographyContainer implements ISteganographyContainer {
	
	private final int MAX_SIZE_BUFFERING = 7000000; // 7 Mo
	private final String FILE_STORAGE_NAME = "aac.tmp";

	protected OutputStream _content;
	protected DataSource _dataSource;
	protected SampleList _sampleList;
	protected String _fileStreamDirectory;
	protected int _sampleListPosition;
	protected int _sampleOffset;
	protected int _sampleFrequency;
	protected int _channelConfiguration;
	
	protected byte[] _unHideData;
	
	public AACSteganographyContainer() {
		_content = null;
		_sampleList = null;
		_fileStreamDirectory = null;
		_sampleListPosition = 0;
		_sampleOffset = 0;
		_sampleFrequency = 0;
		_channelConfiguration = 0;
		
		_unHideData = null;
	}
	
	public boolean loadData(MP4MediaReader mediaReader) {
		if (mediaReader != null) {
			_content = new ByteArrayOutputStream();
			_sampleList = mediaReader.getAudioSampleList();
			_sampleListPosition = 0;
			_sampleOffset = 0;
			_sampleFrequency = mediaReader.getSamplingFrequency();
			_channelConfiguration = mediaReader.getChannelConfiguration();
			return true;
		}
		return false;
	}
	
	public void writeRemainingSamples() {
		Sample sample;
		ByteBuffer buffer;
		        
		if (_sampleList == null) {
			return;
		}
		
		for (; _sampleListPosition < _sampleList.size(); ++_sampleListPosition) {
			sample = _sampleList.get(_sampleListPosition);
			buffer = sample.asByteBuffer();

            if (_sampleOffset > 0) {
				buffer.position(_sampleOffset);
				_sampleOffset = 0;
            } else {
                this.writeHeader(buffer.capacity());
            }
            byte [] tmp = new byte[buffer.remaining()];
            buffer.get(tmp);
            this.addData(tmp);
		}
	}
	
	@Override
	public void hideData(byte[] content) {
	}
	
	@Override
	public void unHideData() {
	}

	@Override
	public long getMaxContentToHide() {
		long ret = 0;

		if (_sampleList != null) {
			for (Sample s : _sampleList) {
				ret += s.getSize();
			}
		}
		return ret;
	}

	@Override
	public byte[] getUnHideData() {
		return _unHideData;
	}

	public DataSource getDataSource() {
		DataSource dataSource;
		
		if (_content != null && _content instanceof ByteArrayOutputStream) {
			dataSource = new SteganosMemoryDataSourceImpl(((ByteArrayOutputStream)_content).toByteArray());
		} else {
			try {
				dataSource = new FileDataSourceImpl(new File(_fileStreamDirectory + FILE_STORAGE_NAME));
			} catch (FileNotFoundException e) {
				dataSource = null;
				System.err.println("[AAC Steganography container]: Unable to get data source: " +  e.getMessage());
			}
		}
		return dataSource;
	}

	public void setFileStreamDirectory(String directory) {
		_fileStreamDirectory = directory;
	}
	
	public String getFileStreamDirectory() {
		return _fileStreamDirectory;
	}

	public void cleanUpResources() {
		cleanDataSource();
		cleanContentStream();
	}
	
	protected void writeHeader(int frameLength) {
		int profile = 2;  //AAC-LC
		byte[] header = new byte[7];
		header[0] = (byte) 0xFF;
		header[1] = (byte) 0xF9;
		header[2] = ((byte) (((profile - 1) << 6) + (_sampleFrequency << 2) + (_channelConfiguration >> 2)));
		header[3] = ((byte) (((_channelConfiguration & 3) << 6) + ((frameLength + 7) >> 11)));
		header[4] = ((byte) (((frameLength + 7) & 0x7FF) >> 3));
		header[5] = ((byte) ((((frameLength + 7) & 7) << 5) + 0x1F));
		header[6] = ((byte) 0x00);
		
		this.addData(header);
	}
	
	private void switchOutputStreamToFile() {
		if (_content != null && _content instanceof ByteArrayOutputStream
			&& ((ByteArrayOutputStream)_content).size() >= MAX_SIZE_BUFFERING) {
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(new File(_fileStreamDirectory + FILE_STORAGE_NAME));
					((ByteArrayOutputStream)_content).writeTo(fos);
					_content = fos;
				} catch (FileNotFoundException e) {
					System.err.println("[AAC Steganography container]: Unable to switch stream content: " +  e.getMessage());
				} catch (IOException e) {
					System.err.println("[AAC Steganography container]: Unable to switch stream content: " +  e.getMessage());
			}
		}		
	}
	
	protected void addData(byte[] content) {
		switchOutputStreamToFile();
		if (_content != null) {
			try {
				_content.write(content);
			} catch (IOException e) {
				System.err.println("[AAC Steganography container]: Unable to add data: " +  e.getMessage());
			}
		}
	}

	// Private methods
	private void cleanDataSource() {
		if (_dataSource != null) {
			try {
				_dataSource.close();
				_dataSource = null;
			} catch (IOException e) {
				System.err.println("[AAC Steganography container]: Unable to clean data source: " +  e.getMessage());
			}
		}
		System.gc();		
	}
	
	private void cleanContentStream() {
		if (_content != null) {
			try {
				_content.close();
				_content = null;
			} catch (IOException e) {
				System.err.println("[AAC Steganography container]: Unable to clean content stream: " +  e.getMessage());
			}
		}
		System.gc();
		File file = new File(_fileStreamDirectory + FILE_STORAGE_NAME);
		file.delete();		
	}

}
