package com.teoinf.steganos.process;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.googlecode.mp4parser.DataSource;
import com.teoinf.steganos.algorithms.AlgorithmFactory;
import com.teoinf.steganos.algorithms.ICryptographyAlgorithm;
import com.teoinf.steganos.algorithms.ISteganographyContainer;
import com.teoinf.steganos.algorithms.compression.Deflate;
import com.teoinf.steganos.algorithms.compression.LZW;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.error.ErrorManager;
import com.teoinf.steganos.mp4.MP4MediaReader;
import com.teoinf.steganos.mp4.MP4MediaWriter;
import com.teoinf.steganos.parameters.EncodeParameters;
import com.teoinf.steganos.tools.Pair;
import com.teoinf.steganos.tools.Utils;


public class EncodeProcess {


    private final int DEFAULT_BLOCK_SIZE = 16;

    private final String DEFAULT_H264_CONTAINER = "com.teoinf.steganos.algorithms.steganography.video.H264SteganographyContainer";
    private final String DEFAULT_AAC_CONTAINER = "com.teoinf.steganos.algorithms.steganography.audio.AACSteganographyContainer";

    private MP4MediaReader _mp4MediaReader;
    private ISteganographyContainer _h264SteganographyContainer;
    private ISteganographyContainer _aacSteganographyContainer;
    private ICryptographyAlgorithm _cryptographyAlgorithm;

    private InputStream _contentToHideStream;
    private byte[] _encryptedBytesForVideo;
    private byte[] _encryptedBytesForAudio;
    private int _blockSize;

    public EncodeProcess() {
        _mp4MediaReader = null;
        _h264SteganographyContainer = null;
        _aacSteganographyContainer = null;
        _cryptographyAlgorithm = null;
        _contentToHideStream = null;
        _encryptedBytesForVideo = null;
        _encryptedBytesForAudio = null;
        _blockSize = DEFAULT_BLOCK_SIZE;
    }

    public boolean encode(EncodeParameters parameters) {
        Utils.setStartTime();
        Log.i("STEGA", "Start encode");
        Preferences prefs = Preferences.getInstance();

        if (!this.init(parameters) || !this.checkMemoryRequirementForOperation()) {
            return false;
        }

        processContentWithCryptography(parameters, _encryptedBytesForVideo);

        processContentWithCryptography(parameters, _encryptedBytesForAudio);

        if (prefs.getUseAudioChannel()) {
            Utils.printTime("Start hiding data audio: ");
            _aacSteganographyContainer.hideData(_encryptedBytesForAudio);
            Utils.printTime("End hiding data audio: ");
        }
        if (prefs.getUseVideoChannel()) {
            Utils.printTime("Start hiding data video: ");
            _h264SteganographyContainer.hideData(_encryptedBytesForVideo);
            Utils.printTime("End hiding data video: ");
        }

        finalise(parameters);
        Utils.printTime("End encode : ");
        return true;
    }

    // Private methods
    // Init methods
    private boolean init(EncodeParameters parameters) {
        return initCryptographyAlgorithm() && initContentToHideStream(parameters) &&
                initSteganographyContainer() && initMp4Components(parameters) &&
                initContentToHideFromStream();
    }

    private boolean initCryptographyAlgorithm() {
        Preferences pref = Preferences.getInstance();
        boolean ret = true;

        if (pref.getUseCryptography()) {

            _cryptographyAlgorithm = AlgorithmFactory.getCryptographyAlgorithmInstanceFromName(pref.getCryptographyAlgorithm());
            if (_cryptographyAlgorithm == null) {
                ErrorManager.getInstance().addErrorMessage("Unable to load Cryptography algorithm");
                ret = false;
            }
            _blockSize = _cryptographyAlgorithm.getBlockSize();

        }

        return ret;
    }

    private boolean initContentToHideStream(EncodeParameters parameters) {

        boolean ret = true;

        long size = Utils.getFileSize(parameters.getFileToHidePath());
        if (parameters.isHidingText()) {
            String textToHide = parameters.getTextToHide();
            Utils.printTime("Start text compression: ");
            if (parameters.getCompressLZW()) {
                textToHide = LZW.compress(textToHide);
                _contentToHideStream = new ByteArrayInputStream(textToHide.getBytes());
            } else if (parameters.getCompressDeflate()) {
                _contentToHideStream = new ByteArrayInputStream(Deflate.compress(textToHide));
            } else {
                _contentToHideStream = new ByteArrayInputStream(textToHide.getBytes());
            }
            Utils.printTime("End text compression: ");
            size = parameters.getTextToHide().getBytes().length;
        } else {
            try {
                _contentToHideStream = new FileInputStream(parameters.getFileToHidePath());
            } catch (FileNotFoundException e) {
                _contentToHideStream = null;
                ErrorManager.getInstance().addErrorMessage("Unable to load content to hide");
                ret = false;
            }
        }

        if (size > Utils.MAX_BYTE_TO_HIDE) {
            ErrorManager.getInstance().addErrorMessage("The content you want to hide is to big (30Mb max)");
            ret = false;
        }
        return ret;
    }



    private boolean initSteganographyContainer() {
        Preferences prefs = Preferences.getInstance();

        if (prefs.getUseVideoChannel()) {
            _h264SteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(prefs.getVideoAlgorithm());
        } else {
            _h264SteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(DEFAULT_H264_CONTAINER);
        }
        if (_h264SteganographyContainer == null) {
            ErrorManager.getInstance().addErrorMessage("Unable to load video steganography algorithm");
            return false;
        }

        if (prefs.getUseAudioChannel()) {
            _aacSteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(prefs.getAudioAlgorithm());
        } else {
            _aacSteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(DEFAULT_AAC_CONTAINER);
        }
        if (_aacSteganographyContainer == null) {
            ErrorManager.getInstance().addErrorMessage("Unable to load audio steganography algorithm");
            return false;
        }

        return true;
    }

    private boolean initMp4Components(EncodeParameters parameters) {
        Utils.printTime("Start preparing data: ");
        _mp4MediaReader = new MP4MediaReader();
        if (!_mp4MediaReader.loadData(parameters.getSourceVideoPath())) {
            ErrorManager.getInstance().addErrorMessage("Unable to load data from orignal MP4");
            return false;
        }

        _h264SteganographyContainer.setFileStreamDirectory(parameters.getDestinationVideoDirectory());
        _aacSteganographyContainer.setFileStreamDirectory(parameters.getDestinationVideoDirectory());

        if (!_h264SteganographyContainer.loadData(_mp4MediaReader)
                || !_aacSteganographyContainer.loadData(_mp4MediaReader)) {
            ErrorManager.getInstance().addErrorMessage("Unable to load channel(s) from original MP4");
            return false;
        }
        return true;
    }

    private boolean initContentToHideFromStream() {

        byte bytes[];

        try {
            bytes = IOUtils.toByteArray(_contentToHideStream);
            if (!checkEnoughSpaceInSamplesToHideData(bytes.length)) {
                return false;
            }
            shuffleContentToHide(bytes);
            addPaddingToContent();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Utils.printTime("End preparing data: ");
        return true;

    }

    // Process methods
    private void processContentWithCryptography(EncodeParameters parameters, byte content[]) {
        Utils.printTime("Start cryptography: ");
        if (content == null) {
            return;
        }

        for (int i = 0; i < content.length; i += _blockSize) {
            byte[] tmp = new byte[_blockSize];
            System.arraycopy(content, i, tmp, 0, _blockSize);

            if (_cryptographyAlgorithm != null && parameters != null) {
                tmp = _cryptographyAlgorithm.encrypt(tmp, parameters.getCryptographyKey().getBytes());
            }

            System.arraycopy(tmp, 0, content, i, _blockSize);
        }
        Utils.printTime("End cryptography: ");
    }

    private void shuffleContentToHide(byte dataToHide[]) {
        ByteArrayOutputStream videoContent = new ByteArrayOutputStream();
        ByteArrayOutputStream audioContent = new ByteArrayOutputStream();
        List<Pair<ByteArrayOutputStream, Long>> list = new ArrayList<Pair<ByteArrayOutputStream, Long>>();
        long audioMaxContent = _aacSteganographyContainer.getMaxContentToHide();
        long videoMaxContent = _h264SteganographyContainer.getMaxContentToHide();
        int indexList = 0;

        audioMaxContent -= (audioMaxContent % _blockSize);
        videoMaxContent -= (videoMaxContent % _blockSize);

        if (Preferences.getInstance().getUseAudioChannel()) {
            list.add(new Pair<ByteArrayOutputStream, Long>(audioContent, audioMaxContent));
        }
        if (Preferences.getInstance().getUseVideoChannel()) {
            list.add(new Pair<ByteArrayOutputStream, Long>(videoContent, videoMaxContent));
        }

        for (int i = 0; i < dataToHide.length; ++i) {
            if (list.get(indexList).getFirst().size() < list.get(indexList).getSecond()) {
                list.get(indexList).getFirst().write(dataToHide[i]);
            } else {
                --i;
            }
            indexList++;
            if (indexList >= list.size()) {
                indexList = 0;
            }
        }

        _encryptedBytesForAudio = audioContent.toByteArray();
        _encryptedBytesForVideo = videoContent.toByteArray();
    }

    private void addPaddingToContent() {
        int padding;

        if (_encryptedBytesForAudio != null && _encryptedBytesForAudio.length > 0) {
            padding = _encryptedBytesForAudio.length % _blockSize;
            if (padding > 0) {
                _encryptedBytesForAudio = Arrays.copyOf(_encryptedBytesForAudio, _encryptedBytesForAudio.length + (_blockSize - padding));
            }
        }

        if (_encryptedBytesForVideo != null && _encryptedBytesForVideo.length > 0) {
            padding = _encryptedBytesForVideo.length % _blockSize;
            if (padding > 0) {
                _encryptedBytesForVideo = Arrays.copyOf(_encryptedBytesForVideo, _encryptedBytesForVideo.length + (_blockSize - padding));
            }
        }
    }

    // Check methods
    private boolean checkEnoughSpaceInSamplesToHideData(int dataLength) {
        Preferences prefs = Preferences.getInstance();
        long videoSteganographyLength = 0;
        long audioSteganographyLength = 0;

        if (prefs.getUseVideoChannel() && _h264SteganographyContainer != null) {
            videoSteganographyLength = _h264SteganographyContainer.getMaxContentToHide();
            if (videoSteganographyLength % _blockSize != 0) {
                videoSteganographyLength -= (videoSteganographyLength % _blockSize);
            }
        }
        if (prefs.getUseAudioChannel() && _aacSteganographyContainer != null) {
            audioSteganographyLength = _aacSteganographyContainer.getMaxContentToHide();
            if (audioSteganographyLength % _blockSize != 0) {
                audioSteganographyLength -= (audioSteganographyLength % _blockSize);
            }
        }

        long maxContentToHide = videoSteganographyLength + audioSteganographyLength;
        if (maxContentToHide < dataLength) {
            String msg = "Not enough space in video to hide data with selected channel(s). You can hide a maximum of " + maxContentToHide + " bytes.";
            if (maxContentToHide == 0)
                msg += " Have you selected a channel in the settings?";
            ErrorManager.getInstance().addErrorMessage(msg);
            System.err.println("[Encode process] Not enough space in video to hide data with selected channel(s)");
            return false;
        }
        return true;
    }

    private boolean checkMemoryRequirementForOperation() {
        return true;
    }

    // Finalise method
    private void finalise(EncodeParameters parameters) {
        Utils.printTime("Start saving file: ");
        MP4MediaWriter mp4MediaWriter;
        DataSource h264DataSource;
        DataSource aacDataSource;
        String outputVideoName;

        if (_h264SteganographyContainer != null) {
            _h264SteganographyContainer.writeRemainingSamples();
        }
        if (_aacSteganographyContainer != null) {
            _aacSteganographyContainer.writeRemainingSamples();
        }
        outputVideoName = "steg_" + Utils.getCurrentDateAndTime() + ".mp4";
        parameters.set_fileName(outputVideoName);
        h264DataSource = _h264SteganographyContainer.getDataSource();
        aacDataSource = _aacSteganographyContainer.getDataSource();
        mp4MediaWriter = new MP4MediaWriter(parameters.getDestinationVideoDirectory() + outputVideoName,
                _mp4MediaReader.getTimescale(), (int) _mp4MediaReader.getDurationPerSample(), h264DataSource, aacDataSource);
        mp4MediaWriter.create();
        mp4MediaWriter.cleanUpResources();

        _h264SteganographyContainer.cleanUpResources();
        _aacSteganographyContainer.cleanUpResources();
        Utils.printTime("End saving file: ");

    }

}
