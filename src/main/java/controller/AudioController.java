package controller;

import org.json.JSONArray;

import javax.sound.sampled.*;
import java.io.File;

public class AudioController {
    private String multipartLogMessage;


    public double calcDurationInSecs(String path) throws Exception {
        Clip clip = audioInputStreamAudioFormatDataLineClip(path);
        double durationInSecs = clip.getBufferSize() / (clip.getFormat().getFrameSize() * clip.getFormat().getFrameRate());
        return durationInSecs;
    }

    public double calcSampleRateinkHz(String path) throws Exception {
        Clip clip = audioInputStreamAudioFormatDataLineClip(path);
        double sampleRateinkHz = clip.getFormat().getSampleRate() / 1000;
        return sampleRateinkHz;
    }

    private Clip audioInputStreamAudioFormatDataLineClip(String path) throws Exception {
        AudioInputStream stream;
        stream = AudioSystem.getAudioInputStream(new File(path));
        AudioFormat format = stream.getFormat();
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                    format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2,
                    format.getFrameRate(), true); // big endian
            stream = AudioSystem.getAudioInputStream(format, stream);

        }
        DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(), ((int) stream.getFrameLength() * format.getFrameSize()));
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.close();
        stream.close();
        return clip;
    }

    public JSONArray getWordsForcedAligner(String wavFile, String requestURL) throws Exception {
        String charset = "UTF-8";
        File uploadFile1 = new File(wavFile);
        File uploadFile2 = new File(new FileController().getTxtFilePath(wavFile));
        MultipartUtility multipart = new MultipartUtility(requestURL, charset);
        multipart.addFilePart("audio", uploadFile1);
        multipart.addFilePart("transcript", uploadFile2);
        multipart.finish();
        JSONArray arr = multipart.getWords();
        setMultipartLogMessage(multipart.getLogMessage());
        return arr;
    }

    private void setMultipartLogMessage(String message) {
        this.multipartLogMessage = message;
    }

    public String getMultipartLogMessage() {
        return this.multipartLogMessage;
    }

}
