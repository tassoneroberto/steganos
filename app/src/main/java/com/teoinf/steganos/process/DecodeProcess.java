package com.teoinf.steganos.process;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.teoinf.steganos.algorithms.AlgorithmFactory;
import com.teoinf.steganos.algorithms.ICryptographyAlgorithm;
import com.teoinf.steganos.algorithms.ISteganographyContainer;
import com.teoinf.steganos.algorithms.compression.Deflate;
import com.teoinf.steganos.algorithms.compression.LZW;
import com.teoinf.steganos.configuration.Preferences;
import com.teoinf.steganos.mp4.MP4MediaReader;
import com.teoinf.steganos.parameters.DecodeParameters;
import com.teoinf.steganos.tools.Utils;

public class DecodeProcess {
    long currentTime;

    private final String DEFAULT_H264_CONTAINER = "com.teoinf.steganos.algorithms.steganography.video.H264SteganographyContainer";
    private final String DEFAULT_AAC_CONTAINER = "com.teoinf.steganos.algorithms.steganography.audio.AACSteganographyContainer";

    private MP4MediaReader _mp4MediaReader;
    private ISteganographyContainer _h264SteganographyContainer;
    private ISteganographyContainer _aacSteganographyContainer;
    private ICryptographyAlgorithm _cryptographyAlgorithm;

    private byte[] _unHideData;

    public DecodeProcess() {
        _mp4MediaReader = null;
        _h264SteganographyContainer = null;
        _aacSteganographyContainer = null;
        _cryptographyAlgorithm = null;
        _unHideData = null;
    }

    public boolean decode(DecodeParameters parameters) {
        Utils.setStartTime();
        Log.i("STEGA", "Start decode");
        byte unHideDataVideo[] = null;
        byte unHideDataAudio[] = null;

        if (!init(parameters))
            return false;

        if (Preferences.getInstance().getUseVideoChannel()) {
            Utils.printTime("Start unhide on video: ");
            _h264SteganographyContainer.unHideData();
            unHideDataVideo = _h264SteganographyContainer.getUnHideData();
            Utils.printTime("End unhide video :");
            // Nothing was found
            if (unHideDataVideo == null || unHideDataVideo.length == 0) {
                System.err.println("A problem was detected during the unhide process on H.264");
                return false;
            }
        }
        if (Preferences.getInstance().getUseAudioChannel()) {
            Utils.printTime("Start unhide on audio: ");
            _aacSteganographyContainer.unHideData();
            unHideDataAudio = _aacSteganographyContainer.getUnHideData();
            Utils.printTime("End unhide audio: ");

            // Nothing was found
            if (unHideDataAudio == null || unHideDataAudio.length == 0) {
                System.err.println("A problem was detected during the unhide process on AAC");
                return false;
            }
        }

        processContentWithCryptography(parameters, unHideDataAudio);
        processContentWithCryptography(parameters, unHideDataVideo);
        unshuffleUnhideContent(unHideDataAudio, unHideDataVideo);

        if (_unHideData != null && _unHideData.length > 0) {
            this.finalise(parameters);
            currentTime = System.currentTimeMillis();
            Utils.printTime("End encode: ");
            return true;
        }

        return false;
    }

    // Private methods
    // Init methods
    private boolean initCryptographyAlgorithm() {
        Preferences pref = Preferences.getInstance();
        boolean ret = true;

        if (pref.getUseCryptography()) {
            _cryptographyAlgorithm = AlgorithmFactory.getCryptographyAlgorithmInstanceFromName(pref.getCryptographyAlgorithm());
            if (_cryptographyAlgorithm == null) {
                System.err.println("Unable to load Cryptography algorithm");
                ret = false;
            }
        }
        return ret;
    }

    private boolean initMp4Components(DecodeParameters parameters) {
        Utils.printTime("Start load file: ");
        _mp4MediaReader = new MP4MediaReader();
        if (!_mp4MediaReader.loadData(parameters.getVideoPath())) {
            System.err.println("Unable to load data from orignal MP4");
            return false;
        }

        if (!_h264SteganographyContainer.loadData(_mp4MediaReader)
                || !_aacSteganographyContainer.loadData(_mp4MediaReader)) {
            System.err.println("Unable to load channel from original MP4");
            return false;
        }
        Utils.printTime("End load file: ");
        return true;
    }

    private boolean initSteganographyContainer(DecodeParameters parameters) {
        Preferences prefs = Preferences.getInstance();

        if (prefs.getUseVideoChannel()) {
            _h264SteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(prefs.getVideoAlgorithm());
        } else {
            _h264SteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(DEFAULT_H264_CONTAINER);
        }
        if (_h264SteganographyContainer == null) {
            System.err.println("Unable to load video steganography algorithm");
            return false;
        }

        if (prefs.getUseAudioChannel()) {
            _aacSteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(prefs.getAudioAlgorithm());
        } else {
            _aacSteganographyContainer = AlgorithmFactory.getSteganographyContainerInstanceFromName(DEFAULT_AAC_CONTAINER);
        }
        if (_aacSteganographyContainer == null) {
            System.err.println("Unable to load audio steganography algorithm");
            return false;
        }

        return true;
    }

    private boolean init(DecodeParameters parameters) {
        return initCryptographyAlgorithm() && initSteganographyContainer(parameters)
                && initMp4Components(parameters);
    }

    // Process methods
    private void processContentWithCryptography(DecodeParameters parameters, byte[] content) {
        Utils.printTime("Start decrypt: ");
        if (content == null) {
            return;
        }

        if (_cryptographyAlgorithm != null && parameters != null) {

            int padding = content.length % _cryptographyAlgorithm.getBlockSize();
            if (padding > 0) {
                content = Arrays.copyOf(content, content.length + (_cryptographyAlgorithm.getBlockSize() - padding));
            }

            for (int i = 0; i < content.length; i += _cryptographyAlgorithm.getBlockSize()) {
                byte[] tmp = new byte[_cryptographyAlgorithm.getBlockSize()];
                System.arraycopy(content, i, tmp, 0, _cryptographyAlgorithm.getBlockSize());

                if (_cryptographyAlgorithm != null && parameters != null) {
                    tmp = _cryptographyAlgorithm.decrypt(tmp, parameters.getCryptographyKey().getBytes());
                }

                System.arraycopy(tmp, 0, content, i, _cryptographyAlgorithm.getBlockSize());
            }
        }
        Utils.printTime("End decrypt: ");
    }

    private void unshuffleUnhideContent(byte audioContent[], byte videoContent[]) {
        ByteArrayOutputStream unhideDataStream = new ByteArrayOutputStream();
        int idxAudioContent = 0;
        int idxVideoContent = 0;
        boolean done = false;

        while (!done) {
            if (audioContent != null && idxAudioContent < audioContent.length) {
                unhideDataStream.write(audioContent[idxAudioContent]);
                idxAudioContent++;
            }
            if (videoContent != null && idxVideoContent < videoContent.length) {
                unhideDataStream.write(videoContent[idxVideoContent]);
                idxVideoContent++;
            }
            if ((audioContent == null || idxAudioContent >= audioContent.length) &&
                    (videoContent == null || idxVideoContent >= videoContent.length)) {
                done = true;
            }
        }
        _unHideData = unhideDataStream.toByteArray();
    }

    // Finalise method
    private void finalise(DecodeParameters parameters) {
        Utils.printTime("Start text decompression: ");
        try {
            String textToShow = null;
            if (parameters.getCompressLZW()) {
                textToShow = new String(_unHideData, "ISO-8859-1");
                textToShow = textToShow.replaceAll("[^\\d|\\s]", "");
                textToShow = LZW.decompress(textToShow);
            } else if (parameters.getCompressDeflate()) {
                textToShow = Deflate.decompress(_unHideData);
            }
            Utils.printTime("End text decompression: ");
            if (parameters.getDisplay()) {

                parameters.setDisplayText(textToShow);

                // if ther is a long message, we create a file, so no return
                //if (parameters.getDisplayText().length() < Utils.MAX_CHAR_BEFORE_CREATE_FILE_ON_DECODE)
                return;
            }

            String filename = parameters.getDestinationVideoDirectory() + "steg_decoded_" + Utils.getCurrentDateAndTime() + ".txt";
            FileOutputStream output = new FileOutputStream(filename);
            output.write(textToShow.getBytes());
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
