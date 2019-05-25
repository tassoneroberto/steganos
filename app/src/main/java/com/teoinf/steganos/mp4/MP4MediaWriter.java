package com.teoinf.steganos.mp4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;

public class MP4MediaWriter {

	private final String DEFAULT_LANGUAGE = "eng";
	
	private H264TrackImpl _h264TrackImpl;
	private AACTrackImpl _aacTrackImpl;
	private String _outputPath;
	
	public MP4MediaWriter(String outputPath, DataSource h264, DataSource aac) {
		_h264TrackImpl = null;
		_aacTrackImpl = null;

		_outputPath = outputPath;
		try {
			if (h264 != null && h264.size() > 0) {
				_h264TrackImpl = new H264TrackImpl(h264);
			}
			if (aac != null && aac.size() > 0) {
				_aacTrackImpl = new AACTrackImpl(aac);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MP4MediaWriter(String outputPath, long timescale, int frametick, DataSource h264, DataSource aac) {
		_h264TrackImpl = null;
		_aacTrackImpl = null;

		_outputPath = outputPath;
		try {
			if (h264 != null && h264.size() > 0) {
				_h264TrackImpl = new H264TrackImpl(h264, DEFAULT_LANGUAGE, timescale, frametick);
			}
			if (aac != null && aac.size() > 0) {
				_aacTrackImpl = new AACTrackImpl(aac);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void create() {
		Movie movie = new Movie();
		Container container;
		FileChannel fileChannel;
		
		if (_h264TrackImpl != null) {
			movie.addTrack(_h264TrackImpl);
		}
        if (_aacTrackImpl != null) {
        	movie.addTrack(_aacTrackImpl);
        }
        
        container = new DefaultMp4Builder().build(movie);
        try {
			fileChannel = new RandomAccessFile(_outputPath, "rw").getChannel();
	        container.writeContainer(fileChannel);
	        fileChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}        
	}

	public void cleanUpResources() {
        _h264TrackImpl = null;
        _aacTrackImpl = null;
        System.gc();
	}
	
}
