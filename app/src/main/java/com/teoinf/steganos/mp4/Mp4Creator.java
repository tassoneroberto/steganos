package com.teoinf.steganos.mp4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;

// TODO: Remove this class
public class Mp4Creator {
	
	public Mp4Creator() {
		
	}
	
	public void createMP4FromChannels(String outputPath, byte[] videoSignal, byte[] audioSignal, byte[] metadataSignal) throws FileNotFoundException, IOException {
		H264TrackImpl h264Track; 
		AACTrackImpl aacTrack; 
		Movie movie;
        Container container;
		FileOutputStream fileOutputStream;
		FileChannel fileChannel;		
		
	    h264Track = new H264TrackImpl(new SteganosMemoryDataSourceImpl(videoSignal));
	    aacTrack = new AACTrackImpl(new SteganosMemoryDataSourceImpl(audioSignal));

	    movie = new Movie();
        movie.addTrack(h264Track);
        movie.addTrack(aacTrack);
        
        container = new DefaultMp4Builder().build(movie);
        
        fileOutputStream = new FileOutputStream(new File(outputPath));
        fileChannel = fileOutputStream.getChannel();
        container.writeContainer(fileChannel);
        fileOutputStream.close();
	}
	
}
