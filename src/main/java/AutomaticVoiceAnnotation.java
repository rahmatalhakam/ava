import controller.MainController;
import model.AudioModel;
import view.MainView;

import javax.swing.*;
import java.awt.*;

public class AutomaticVoiceAnnotation {

    public static void main(String[] args) {
        AudioModel audioModel = new AudioModel();
        MainView mainView = new MainView();
        MainController mainController = new MainController(audioModel, mainView);
        mainController.setUpViewEvents();
        JFrame frame = new JFrame("AVA");
        frame.setContentPane(mainView.getPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(350, 500));
        frame.pack();
        frame.setVisible(true);
    }
}
