package controller;

import model.AudioModel;
import model.ErrorList;
import org.json.JSONArray;
import view.MainView;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;


public class MainController {
    private MainView mainView;
    private AudioModel audioModel;
    private Task task;
    private AudioController audioController;
    private FileController fileController;
    private boolean done = false;

    public MainController(AudioModel audioModel, MainView mainView) {
        this.mainView = mainView;
        this.audioModel = audioModel;
        this.audioController = new AudioController();
        this.fileController = new FileController();
    }

    public void setUpViewEvents() {
        mainView.getBrowseButton().addActionListener(e -> getPath());
        mainView.getGenerateButton().addActionListener(e -> generate());
    }

    private void getPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.showOpenDialog(null);
        File f = chooser.getSelectedFile();
        mainView.setTextField1(f.getAbsolutePath());
    }

    private void generate() {
        audioModel.setPath(mainView.getTextField1().getText());
        audioModel.setUrl(mainView.getTextField2().getText());
        if (audioModel.getPath().equals("") || audioModel.getUrl().equals("")) {
            JOptionPane.showMessageDialog(null, "Fill path and URL. Don't leave it empty!", "WARNING",
                    JOptionPane.WARNING_MESSAGE);
        } else if (fileController.checkURL(audioModel.getUrl())) {
            mainView.setCursorWait();
            try {
                audioModel.setLog(audioModel.getPath(), System.nanoTime());
                task = new Task();
            } catch (Exception e) {
                e.printStackTrace();
                audioModel.setTextMessage(e.toString());
                audioModel.getLogger().severe(e.toString());
                audioModel.setError(true);
            }
            task.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!done) {
                        int progress = task.getProgress();
                        mainView.getProgressBar1().setValue(progress);
                    }
                }
            });
            task.execute();
        }
    }

    private void addLogMessages(String messages) {
        appendToPane(mainView.getTextArea1(), messages + "\n", Color.BLACK);
        audioModel.getLogger().info(messages);
    }

    private void addLogMessages(String messages, Color color) {
        appendToPane(mainView.getTextArea1(), messages + "\n", color);
        audioModel.getLogger().severe(messages);
    }

    private void appendToPane(JTextArea textArea, String msg, Color c) {
        int len = textArea.getDocument().getLength();
        textArea.setCaretPosition(len);
        textArea.append(msg);
    }

    class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            audioModel.setError(false);
            long startTime = System.nanoTime();
            int progress = 0;

            mainView.getBrowseButton().setEnabled(false);
            mainView.getGenerateButton().setEnabled(false);
            mainView.getTextField1().setEditable(false);
            mainView.getTextField2().setEditable(false);
            if (audioModel.getPath().length() < 25) {
                addLogMessages("Path string: " + audioModel.getPath());
            } else {
                addLogMessages("Path string: ..." + audioModel.getPath().substring(audioModel.getPath().length() - 25, audioModel.getPath().length()));
            }
            if (fileController.checkTxtWavFilePair(audioModel.getPath())) {
                addLogMessages("No missing Wav-Txt file pairs.");
            } else {
                audioModel.setTextMessage("Missing txt-wav pair files.");
                audioModel.setError(true);
            }

            if (audioModel.isError()) {
                addLogMessages(audioModel.getTextMessage(), Color.RED);
                throw new CancellationException();
            }
            int totalFolder = fileController.getTotalFolder(audioModel.getPath());

            List<String> wavFiles = fileController.getListWavFiles(audioModel.getPath());
            List<String> txtFiles = fileController.getListTxtFiles(audioModel.getPath());
            List<ErrorList> errorLists = new ArrayList<ErrorList>();
            Collections.sort(wavFiles);
            Collections.sort(txtFiles);
            setProgress(0);
            int a = 100 / wavFiles.size();
            addLogMessages("===========================");
            for (int i = 0; i < wavFiles.size(); i++) {
                ErrorList error = new ErrorList();
                audioModel.setError(false);
                int inc = i;
                addLogMessages("Processing file: " + (inc + 1));
                try {
                    audioModel.setDurationInSecs(audioController.calcDurationInSecs(wavFiles.get(i)));
                    addLogMessages("duration in secs: " + audioModel.getDurationInSecs());
                    audioModel.setSampleRateinkHz(audioController.calcSampleRateinkHz(wavFiles.get(i)));
                    addLogMessages("Sample Rate: " + audioModel.getSampleRateinkHz());
                    addLogMessages(fileController.readTxtFile(txtFiles.get(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                    audioModel.setError(true);
                    addLogMessages(e.toString(), Color.RED);
                }

                JSONArray jsonArray = null;
                try {
                    jsonArray = audioController.getWordsForcedAligner(wavFiles.get(i), mainView.getTextField2().getText());
                    addLogMessages(audioController.getMultipartLogMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    audioModel.setError(true);
                    addLogMessages(e.toString(), Color.RED);
                }

                if (audioModel.isError()) {
                    addLogMessages(audioModel.getTextMessage(), Color.red);
                    error.setData(audioModel.getTextMessage(), wavFiles.get(i));
                    errorLists.add(error);
                    addLogMessages("Failed to create TextGrid file!", Color.RED);
                } else {
                    addLogMessages(audioModel.getTextMessage());
                    String textGridFilePath = fileController.getTextGridFilePath(wavFiles.get(i));
                    addLogMessages("Get TextGrid File Path successed");
                    try {
                        fileController.createTextGridFile(jsonArray, textGridFilePath, audioModel.getDurationInSecs());
                        addLogMessages("Successfully wrote to the TextGrid File.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        addLogMessages(e.toString(), Color.RED);
                        addLogMessages("Failed to write the TextGrid File.");
                        audioModel.setError(true);
                    }
                    if (audioModel.isError()) {
                        addLogMessages(audioModel.getTextMessage(), Color.RED);
                        error.setData(audioModel.getTextMessage(), wavFiles.get(i));
                        errorLists.add(error);
                        addLogMessages("Failed to create TextGrid file!", Color.RED);
                    } else {
                        addLogMessages(audioModel.getTextMessage(), Color.GREEN);
                    }
                }
                setProgress(a * (inc + 1));
                addLogMessages("Processing file: " + (inc + 1) + " done!");
                addLogMessages("===========================");
            }
            if (progress < 100) {
                setProgress(100);
            }
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            Color opt;
            if (errorLists.size() > 0) {
                opt = Color.RED;
            } else {
                opt = Color.GREEN;
            }
            addLogMessages("========== SUMMARY ==========");
            addLogMessages("TOTAL FILES PROCESSED: " + wavFiles.size(), opt);
            addLogMessages("TOTAL FOLDERS PROCESSED: " + totalFolder, opt);
            addLogMessages("TextGrid FILES GENERATED: " + (wavFiles.size() - errorLists.size()), opt);
            addLogMessages("FAILED: " + errorLists.size(), opt);
            addLogMessages("Failed files:", opt);
            if (errorLists.size() > 0) {
                for (ErrorList errorList : errorLists) {
                    addLogMessages("ERROR: " + errorList.getErrMsg(), opt);
                    addLogMessages("PATH: " + errorList.getPath(), opt);
                }
            } else {
                addLogMessages("Not Found! Enjoy your life :)", opt);
            }
            addLogMessages("TOTAL TIME: " + totalTime / 1000000000 + " seconds", opt);
            addLogMessages("=============================");
            return null;
        }

        @Override
        public void done() {
            try {
                done = true;
                mainView.getBrowseButton().setEnabled(true);
                mainView.getGenerateButton().setEnabled(true);
                mainView.getTextField1().setEditable(true);
                mainView.getTextField2().setEditable(true);
                mainView.setCursorDefault();
                addLogMessages("Done");
                audioModel.getLogger().setUseParentHandlers(false);
                audioModel.getFh().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
