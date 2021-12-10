package controller;

import org.json.JSONArray;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileController {
    public boolean checkURL(String urlServer) {
        try {
            URL url = new URL(urlServer);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "ERROR", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkTxtWavFilePair(String pathFolder) {
        Path pathPath = Paths.get(pathFolder);
        String pathString = pathPath.toString();
        List<String> txtFiles = listAllFiles(pathString, ".*\\.txt");
        List<String> wavFiles = listAllFiles(pathString, ".*\\.wav");
        if (txtFiles.size() != wavFiles.size() || txtFiles.size() == 0 || wavFiles.size() == 0) {
            return false;
        }
        return true;
    }

    public String readTxtFile(String path) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(path)));
        if (content.length() > 100) {
            content = content.substring(0, 100);
        }
        String[] arrTokens = content.split(" ");
        double result = new AudioController().calcDurationInSecs(getWavFilePath(path)) / (arrTokens.length + 2);
        double avgDurationInMilliSecs = result * 1000;
        return ("Original transcription: " + content + "\nTotal words: " + arrTokens.length + "\n" + "AvgDurationInSecs: "
                + avgDurationInMilliSecs);
    }

    public List<String> listAllFiles(String path, final String pattern) {
        final File folder = new File(path);
        List<String> result = new ArrayList<>();
        searchFile(pattern, folder, result);
        return result;
    }

    public List<String> getListTxtFiles(String path) {
        return listAllFiles(path, ".*\\.txt");
    }

    public List<String> getListWavFiles(String path) {
        return listAllFiles(path, ".*\\.wav");
    }

    private void searchFile(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {
            if (f.isDirectory()) {
                searchFile(pattern, f, result);
            }
            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }
        }
    }

    public int getTotalFolder(final String folderPath) {
        return new File(folderPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        }).length;
    }

    // get wav file path from txt
    public String getWavFilePath(String path) {
        return path.replaceAll("\\.txt$", ".wav");
    }

    // get txt file path from wav
    public String getTxtFilePath(String path) {
        return path.replaceAll("\\.wav$", ".txt");
    }

    public String getTextGridFilePath(String path) {
        return path.replaceAll("\\.wav$", ".TextGrid");
    }

    public void createTextGridFile(JSONArray jsonArray, String path, double durationFileInSecs) throws Exception {

        int totalSuccessWords = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("case").equals("success")) {
                totalSuccessWords++;
            }
        }
        FileWriter textGridWriter = new FileWriter(path);
        textGridWriter.write("File type = \"ooTextFile\"\n" + "Object class = \"TextGrid\"\n" + "\n" + "");
        textGridWriter.write("xmin = 0 \n");
        textGridWriter.write("xmax = " + durationFileInSecs + " \n");
        textGridWriter.write("tiers? <exists> \n");
        textGridWriter.write("size = 1 \n");
        textGridWriter.write("item []: \n");
        textGridWriter.write("    item [1]:\n");
        textGridWriter.write("        class = \"IntervalTier\" \n");
        textGridWriter.write("        name = \"autoFA\" \n");
        textGridWriter.write("        xmin = 0 \n");
        textGridWriter.write("        xmax = " + durationFileInSecs + " \n");
        textGridWriter.write("        intervals: size = " + totalSuccessWords + " \n");
        int interval = 1;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("case").equals("success")) {
                textGridWriter.write("        intervals [" + (interval) + "]:\n");
                textGridWriter.write("            xmin = " + jsonArray.getJSONObject(i).getDouble("start") + " \n");
                textGridWriter.write("            xmax = " + jsonArray.getJSONObject(i).getDouble("end") + " \n");
                textGridWriter
                        .write("            text = \"" + jsonArray.getJSONObject(i).getString("word") + "\" \n");
                interval++;
            }
        }
        textGridWriter.close();
    }

}
