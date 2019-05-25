package com.teoinf.steganos.mp4;

import java.io.IOException;
import java.util.List;

import android.util.SparseIntArray;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ESDescriptor;
import com.googlecode.mp4parser.util.Path;
import com.teoinf.steganos.error.ErrorManager;

public class MP4MediaReader {

	private final String VIDEO_TRACKBOX_PATH = "/moov/trak/mdia/minf/stbl/stsd/avc1/../../../../../";
	private final String VIDEO_CONFIGURATION_BOX_PATH = "mdia/minf/stbl/stsd/avc1/avcC";
	private final String VIDEO_MP4_HANDLER = "vide";
	
	private final String AUDIO_TRACKBOX_PATH = "/moov/trak/mdia/minf/stbl/stsd/mp4a/../../../../../";
	private final String AUDIO_CONFIGURATION_BOX_PATH = "mdia/minf/stbl/stsd/mp4a/esds";
	
	private IsoFile _isoFile;
	
	public MP4MediaReader() {
	}
	
	public boolean loadData(String path) {
		if (path == null || path.isEmpty()) {
			return false;
		}
		try {
			_isoFile = new IsoFile(path);
		} catch (IOException exception) {
			ErrorManager.getInstance().addErrorMessage("[MP4 Media Reader]: Not able to open the file");
			return false;
		}
		return true;
	}
	
	public boolean close() {
		if (_isoFile == null) {
			return true;
		}
		
		try {
			_isoFile.close();
		} catch (IOException exception) {
			ErrorManager.getInstance().addErrorMessage("[MP4 Media Reader]: Not able to close the file");
			return false;
		} 
		return true;
	}
	
	public SampleList getVideoSampleList() {
        TrackBox trackBox; 
        SampleList sampleList;

        if (_isoFile == null) {
        	return null;
        }
        trackBox = (TrackBox) Path.getPath(_isoFile, VIDEO_TRACKBOX_PATH);
		if (trackBox == null) {
			return null;
		}
        sampleList = new SampleList(trackBox, new IsoFile[0]);
		return sampleList;
	}

	public byte[] getSequenceParameterSets() {
		TrackBox trackBox; 
		AvcConfigurationBox videoConfigurationBox;
		
		if (_isoFile == null) {
			return null;
		}
		trackBox = (TrackBox) Path.getPath(_isoFile, VIDEO_TRACKBOX_PATH);
		if (trackBox == null) {
			return null;
		}
		videoConfigurationBox = (AvcConfigurationBox) Path.getPath(trackBox, VIDEO_CONFIGURATION_BOX_PATH);
		return videoConfigurationBox.getSequenceParameterSets().get(0);
	}
	
	public byte[] getPictureParameterSets() {
		TrackBox trackBox; 
		AvcConfigurationBox videoConfigurationBox;
		
		if (_isoFile == null) {
			return null;
		}
		trackBox = (TrackBox) Path.getPath(_isoFile, VIDEO_TRACKBOX_PATH);
		if (trackBox == null) {
			return null;
		}
		videoConfigurationBox = (AvcConfigurationBox) Path.getPath(trackBox, VIDEO_CONFIGURATION_BOX_PATH);
		return videoConfigurationBox.getPictureParameterSets().get(0);
	}

	public double getFramePerSeconds() {
		Movie movie;
		List<TrackBox> trackBoxes;
		Track track;
		long duration;
		long durationPerSample;
		double fps;
		
		if (_isoFile == null) {
			return 25.0;
		}
		
		movie = new Movie();
		trackBoxes = _isoFile.getMovieBox().getBoxes(TrackBox.class);
        if (trackBoxes == null) {
        	return 25.0;
        }
		for (TrackBox trackBox : trackBoxes) {
            movie.addTrack(new Mp4TrackImpl(trackBox));
        }
    	track = movie.getTracks().get(0);
        if (movie.getTracks().get(1).getHandler().equals(VIDEO_MP4_HANDLER)) {
        	track = movie.getTracks().get(1);
        }
		duration = track.getDuration();
		durationPerSample = duration / track.getSamples().size();
        fps = (double)  track.getTrackMetaData().getTimescale() / durationPerSample;
		return fps;
	}
	
	public long getTimescale() {
		Movie movie;
		List<TrackBox> trackBoxes;
		Track track;
		
		if (_isoFile == null) {
			return 25L;
		}
		
		movie = new Movie();
		trackBoxes = _isoFile.getMovieBox().getBoxes(TrackBox.class);
		if (trackBoxes == null) {
			return 25L;
		}
        for (TrackBox trackBox : trackBoxes) {
            movie.addTrack(new Mp4TrackImpl(trackBox));
        }
    	track = movie.getTracks().get(0);
        if (movie.getTracks().get(1).getHandler().equals(VIDEO_MP4_HANDLER)) {
        	track = movie.getTracks().get(1);
        }
		return track.getTrackMetaData().getTimescale();
	}
	
	public long getDurationPerSample() {
		Movie movie;
		List<TrackBox> trackBoxes;
		Track track;
		
		if (_isoFile == null) {
			return 1L;
		}
		
		movie = new Movie();
		trackBoxes = _isoFile.getMovieBox().getBoxes(TrackBox.class);
		if (trackBoxes == null) {
			return 1L;
		}
        for (TrackBox trackBox : trackBoxes) {
            movie.addTrack(new Mp4TrackImpl(trackBox));
        }
    	track = movie.getTracks().get(0);
        if (movie.getTracks().get(1).getHandler().equals(VIDEO_MP4_HANDLER)) {
        	track = movie.getTracks().get(1);
        }
        return track.getDuration() / track.getSamples().size();
	}
	
	public int getVideoSampleLengthSize() {
		TrackBox trackBox; 
		AvcConfigurationBox videoConfigurationBox;
		
		if (_isoFile == null) {
			return 0;
		}
		trackBox = (TrackBox) Path.getPath(_isoFile, VIDEO_TRACKBOX_PATH);
		if (trackBox == null) {
			return 0;
		}
		videoConfigurationBox = (AvcConfigurationBox) Path.getPath(trackBox, VIDEO_CONFIGURATION_BOX_PATH);
		return videoConfigurationBox.getLengthSizeMinusOne();
	}
	
	public SampleList getAudioSampleList() {
		TrackBox trackBox; 
        SampleList sampleList;
        
        if (_isoFile == null) {
			return null;
		}
        trackBox = (TrackBox) Path.getPath(_isoFile, AUDIO_TRACKBOX_PATH);
        if (trackBox == null) {
        	return null;
        }
		sampleList = new SampleList(trackBox, new IsoFile[0]);
		return sampleList;
	}
	
	public int getSamplingFrequency() {
        TrackBox trackBox; 
		ESDescriptorBox esDescriptorBox;
		ESDescriptor descriptor;
		int code;
		
		
		if (_isoFile == null) {
			return 0;
		}
		trackBox = (TrackBox) Path.getPath(_isoFile, AUDIO_TRACKBOX_PATH);
		if (trackBox == null) {
			return 0;
		}
		esDescriptorBox = (ESDescriptorBox) Path.getPath(trackBox, AUDIO_CONFIGURATION_BOX_PATH);
		descriptor = (ESDescriptor) esDescriptorBox.getDescriptor();
		code = descriptor.getDecoderConfigDescriptor().getAudioSpecificInfo().getSamplingFrequency();
		return getSamplingFrequencyFromCode(code);
	}

	private int getSamplingFrequencyFromCode(int code) {
		SparseIntArray map = new SparseIntArray();

		map.put(96000, 0);
		map.put(88200, 1);
		map.put(64000, 2);
		map.put(48000, 3);
		map.put(44100, 4);
		map.put(32000, 5);
		map.put(24000, 6);
		map.put(22050, 7);
		map.put(16000, 8);
		map.put(12000, 9);
		map.put(11025, 10);
		map.put(8000, 11);
		map.put(7350, 12);
		return map.get(code, 4);
	} 
	
	public int getChannelConfiguration() {
		TrackBox trackBox; 
		ESDescriptorBox esDescriptorBox;
		ESDescriptor descriptor;

		if (_isoFile == null) {
			return 0;
		}
		trackBox = (TrackBox) Path.getPath(_isoFile, AUDIO_TRACKBOX_PATH);
		if (trackBox == null) {
			return 0;
		}
		esDescriptorBox = (ESDescriptorBox) Path.getPath(trackBox, AUDIO_CONFIGURATION_BOX_PATH);
		descriptor = (ESDescriptor) esDescriptorBox.getDescriptor();
		return descriptor.getDecoderConfigDescriptor().getAudioSpecificInfo().getChannelConfiguration();
	}
	
}
