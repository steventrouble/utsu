package com.utsusynth.utsu;

import java.io.File;
import java.util.ResourceBundle;
import com.google.inject.Inject;
import com.utsusynth.utsu.common.i18n.Localizable;
import com.utsusynth.utsu.common.i18n.Localizer;
import com.utsusynth.utsu.engine.Engine;
import com.utsusynth.utsu.model.song.SongManager;
import com.utsusynth.utsu.model.voicebank.Voicebank;
import com.utsusynth.utsu.model.voicebank.VoicebankReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * 'PropertiesScene.fxml' Controller Class
 */
public class PropertiesController implements Localizable {
    private final SongManager songManager;
    private final Engine engine;
    private final Localizer localizer;
    private VoicebankReader voicebankReader;

    private File resamplerPath;
    private File wavtoolPath;
    private Voicebank voicebank;

    @FXML // fx:id="root"
    private BorderPane root; // Value injected by FXMLLoader

    @FXML // fx:id="projectNameLabel"
    private Label projectNameLabel;

    @FXML // fx:id="projectNameTF"
    private TextField projectNameTF;

    @FXML // fx:id="outputFileLabel"
    private Label outputFileLabel;

    @FXML // fx:id="outputFileTF"
    private TextField outputFileTF;

    @FXML // fx:id="flagsLabel"
    private Label flagsLabel;

    @FXML // fx:id="flagsTF"
    private TextField flagsTF;

    @FXML // fx:id="resamplerLabel"
    private Label resamplerLabel;

    @FXML // fx:id="resamplerName"
    private TextField resamplerName; // Value injected by FXMLLoader

    @FXML // fx:id="wavtoolLabel"
    private Label wavtoolLabel;

    @FXML // fx:id="wavtoolName"
    private TextField wavtoolName; // Value injected by FXMLLoader

    @FXML // fx:id="voicebankLabel"
    private Label voicebankLabel;

    @FXML // fx:id="voicebankName"
    private TextField voicebankName; // Value injected by FXMLLoader

    @FXML // fx:id="tempoLabel"
    private Label tempoLabel;

    @FXML // fx:id="tempoSlider"
    private Slider tempoSlider; // Value injected by FXMLLoader

    @FXML // fx:id="curTempo"
    private Label curTempo; // Value injected by FXMLLoader

    @Inject
    public PropertiesController(
            SongManager songManager,
            Engine engine,
            Localizer localizer,
            VoicebankReader voicebankReader) {
        this.songManager = songManager;
        this.engine = engine;
        this.localizer = localizer;
        this.voicebankReader = voicebankReader;
    }

    public void initialize() {
        // Set values to save.
        resamplerPath = engine.getResamplerPath();
        wavtoolPath = engine.getWavtoolPath();
        voicebank = songManager.getSong().getVoicebank();

        // Set text boxes.
        projectNameTF.setText(songManager.getSong().getProjectName());
        outputFileTF.setText(songManager.getSong().getOutputFile().getAbsolutePath());
        flagsTF.setText(songManager.getSong().getFlags());
        resamplerName.setText(resamplerPath.getName());
        wavtoolName.setText(wavtoolPath.getName());
        voicebankName.setText(voicebank.getName());

        // Setup tempo slider.
        tempoSlider.valueProperty().addListener((event) -> {
            int sliderValue = (int) Math.round(tempoSlider.getValue());
            tempoSlider.setValue(sliderValue);
            curTempo.setText(Integer.toString(sliderValue));
        });
        tempoSlider.setValue(songManager.getSong().getTempo());

        // Set up localization.
        localizer.localize(this);
    }

    @Override
    public void localize(ResourceBundle bundle) {
        projectNameLabel.setText(bundle.getString("properties.projectName"));
        outputFileLabel.setText(bundle.getString("properties.outputFile"));
        flagsLabel.setText(bundle.getString("properties.flags"));
        resamplerLabel.setText(bundle.getString("properties.resampler"));
        wavtoolLabel.setText(bundle.getString("properties.wavtool"));
        voicebankLabel.setText(bundle.getString("properties.voicebank"));
        tempoLabel.setText(bundle.getString("properties.tempo"));
    }

    @FXML
    void changeResampler(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select executable file");
        fc.getExtensionFilters().addAll(
                new ExtensionFilter("Executables", "*", "*.exe"),
                new ExtensionFilter("OSX Executables", "*.out", "*.app"),
                new ExtensionFilter("All Files", "*.*"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            resamplerPath = file;
            resamplerName.setText(resamplerPath.getName());
        }
    }

    @FXML
    void changeWavtool(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select executable file");
        fc.getExtensionFilters().addAll(
                new ExtensionFilter("Executables", "*", "*.exe"),
                new ExtensionFilter("OSX Executables", "*.out", "*.app"),
                new ExtensionFilter("All Files", "*.*"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            wavtoolPath = file;
            wavtoolName.setText(wavtoolPath.getName());
        }
    }

    @FXML
    void changeVoicebank(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select voicebank");
        File file = dc.showDialog(null);
        if (file != null) {
            voicebank = voicebankReader.loadVoicebankFromDirectory(file);
            voicebankName.setText(voicebank.getName());
        }
    }

    @FXML
    void applyProperties(ActionEvent event) {
        songManager.setSong(
                songManager.getSong().toBuilder().setProjectName(projectNameTF.getText())
                        .setOutputFile(new File(outputFileTF.getText())).setFlags(flagsTF.getText())
                        .setVoiceDirectory(voicebank.getPathToVoicebank())
                        .setTempo((int) Math.round(tempoSlider.getValue())).build());
        engine.setResamplerPath(resamplerPath);
        engine.setWavtoolPath(wavtoolPath);
        Stage currentStage = (Stage) root.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void closeProperties(ActionEvent event) {
        Stage currentStage = (Stage) root.getScene().getWindow();
        currentStage.close();
    }
}
