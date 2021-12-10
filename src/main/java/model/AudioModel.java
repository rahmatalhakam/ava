package model;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AudioModel {
    private Logger logger;
    private boolean error = false;
    private String textMessage = "";
    private String path;
    private String url;
    private List<String> wavFiles;
    private List<String> txtFiles;
    private double durationInSecs;
    private double sampleRateinkHz;
    private FileHandler fh;

    public FileHandler getFh() {
        return fh;
    }

    public void setFh(FileHandler fh) {
        this.fh = fh;
    }

    public double getDurationInSecs() {
        return durationInSecs;
    }

    public void setDurationInSecs(double durationInSecs) {
        this.durationInSecs = durationInSecs;
    }

    public double getSampleRateinkHz() {
        return sampleRateinkHz;
    }

    public void setSampleRateinkHz(double sampleRateinkHz) {
        this.sampleRateinkHz = sampleRateinkHz;
    }

    public List<String> getWavFiles() {
        return wavFiles;
    }

    public void setWavFiles(List<String> wavFiles) {
        this.wavFiles = wavFiles;
    }

    public List<String> getTxtFiles() {
        return txtFiles;
    }

    public void setTxtFiles(List<String> txtFiles) {
        this.txtFiles = txtFiles;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLog(String path, long startTime) throws Exception {
        this.logger = Logger.getLogger("AutoVoiceApplication");
        this.fh = new FileHandler(path + "/" + startTime + ".log");
        this.logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        this.fh.setFormatter(formatter);
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

}
