/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;

/**
 *
 * @author Doug
 */
public class sfncFXMLController implements Initializable {
    
    // the creature
    Creature creature = new Creature();
    
    // where-to-save info
    File currentExportDirectory = new File(".");
    File currentSaveDirectory = new File(".");
    File currentSaveFile;

    // main AnchorPane
    @FXML private VBox top;
    
    // menu bar controls
    @FXML private MenuItem newMenuItem; // action defined as lambda expression
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;    // action defined as lambda expression
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem exportTextMenuItem;
    @FXML private MenuItem quitMenuItem;    // action defined as lambda expression
    @FXML private MenuItem aboutMenuItem;   // action defined as lambda expression

    // dialog boxes for menu
    Dialog aboutDialog = new Dialog();
    Alert fileChangeAlert = new Alert(Alert.AlertType.CONFIRMATION);
    Alert openErrorAlert = new Alert(Alert.AlertType.ERROR);
    
    @FXML public void openAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("sfnc files (*.sfnc)", "*.sfnc"));
        fileChooser.setTitle("Open ...");
        fileChooser.setInitialDirectory(currentSaveDirectory);
        File file = fileChooser.showOpenDialog(top.getScene().getWindow());
        if (file != null) {
            currentSaveDirectory = file.getParentFile();
            currentSaveFile = file;
            switch (creature.openCreature(file)) {
                case 0:
                    setControls();
                    updateStatBlock();
                    creature.clearChange();
                    break;
                case 1: // invalid file format alert
                    openErrorAlert.setTitle("Invalid File Format");
                    openErrorAlert.setHeaderText("");
                    openErrorAlert.setContentText("File format is invalid.  Data may be incorrect.");
                    currentSaveFile = null;
                    break;
                case -1: // IOException alert
                    openErrorAlert.setTitle("IO Exception");
                    openErrorAlert.setHeaderText("");
                    openErrorAlert.setContentText("IO Exception when reading file.  Data not loaded.");
                    currentSaveFile = null;
                    break;
            }
        }
        updateWindowTitle();
    }
    
    @FXML public void saveAsAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("sfnc files (*.sfnc)", "*.sfnc"));
        fileChooser.setTitle("Save as ...");
        fileChooser.setInitialDirectory(currentSaveDirectory);
        fileChooser.setInitialFileName(creature.getName());
        File file = fileChooser.showSaveDialog(top.getScene().getWindow());
        if (file != null) {
            currentSaveDirectory = file.getParentFile();
            currentSaveFile = file;
            creature.saveCreature(file);
            updateWindowTitle();
        }
    }
    
    @FXML public void exportTextAction(ActionEvent actionEvent) {
        
        //creature.calculateInternals();
        
        final int EXPORTLINEWIDTH = 70;
        
        // create the output lines
        String nameLine = creature.getName();
        String CROutput = "CR " + creature.CR.displayString();
        // Needs Java 11, I think:  nameLine += " ".repeat(EXPORTLINEWIDTH - nameLine.length() - CROutput.length());
        for (int i=0; i < EXPORTLINEWIDTH - nameLine.length() - CROutput.length(); i++) {
            nameLine += " ";
        }
        nameLine += CROutput;
        String xpLine = "XP " + creature.getXPString();

        // get the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
        fileChooser.setTitle("Export to ...");
        fileChooser.setInitialDirectory(currentExportDirectory);
        fileChooser.setInitialFileName(creature.getName());
        File file = fileChooser.showSaveDialog(top.getScene().getWindow());
        if (file != null) {
            currentExportDirectory = file.getParentFile();
        }
        
        // print all the lines
        try {
            PrintWriter writer = new PrintWriter(file.getPath(),"UTF-8");
            writer.println(nameLine);
            writer.println(xpLine);
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (exporting to text)");
        }
     }

    // Step 0 controls
    @FXML    private TextField creatureNameInput = new TextField();
    @FXML    private ComboBox creatureCRInput = new ComboBox();

    // stat block controls
    @FXML    private Label creatureNameDisplay = new Label();
    @FXML    private Label creatureCRDisplay = new Label();
    @FXML    private Label creatureXPDisplay = new Label();
    
    public void setControls() {
        creatureNameInput.setText(creature.getName());
        creatureCRInput.setValue(creature.getCRDisplayString());
    }
    
    public void updateStatBlock() {
        // update name/CR line
        creatureNameDisplay.setText(creature.getName().toUpperCase());
        creatureCRDisplay.setText(creature.getCR().displayString());
        // update XP line
        creatureXPDisplay.setText(creature.getXPString());
    }
    
    public void updateWindowTitle() {
        Sfnc.getInstance().updateTitle((creature.hasChanged() ? "*" : "")
            + ((currentSaveFile == null) ? "Untitled" : currentSaveFile.getName())
            + " - sfnc"
        );
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        updateStatBlock();
  
        // set up window
        updateWindowTitle();

        // menu controls
        newMenuItem.setOnAction((ActionEvent actionEvent) -> {
            if ( (!creature.hasChanged()) || 
                    (fileChangeAlert.showAndWait().get() == ButtonType.OK)) {
                creature = new Creature();
                setControls();
                updateStatBlock();
                currentSaveFile = null;
                updateWindowTitle();
            }
        });
        
        saveMenuItem.setOnAction((ActionEvent actionEvent) -> {
            if (currentSaveFile != null) {
                creature.saveCreature(currentSaveFile);
                updateWindowTitle();
            }
            else {
                saveAsAction(actionEvent);
            }
        });
        
        quitMenuItem.setOnAction((ActionEvent actionEvent) -> {
            if ( (!creature.hasChanged()) || 
                    (fileChangeAlert.showAndWait().get() == ButtonType.OK)) {
                Platform.exit();
            }
        });
        
        aboutMenuItem.setOnAction((ActionEvent actionEvent) -> aboutDialog.showAndWait());
        
        // set up file change alert
        fileChangeAlert.setTitle("Monster has changed!");
        fileChangeAlert.setHeaderText("");
        fileChangeAlert.setContentText("Press OK to lose changes and continue.");
        
        // set up about dialog box
        aboutDialog.initStyle(StageStyle.UTILITY);
        aboutDialog.setTitle("About sfnc");
        aboutDialog.setContentText("Starfinder NPC/Alien Creator\nversion 1.0.0");
        aboutDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // step 0 controls
        creatureNameInput.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    creature.setName(newValue);
                    updateStatBlock();
                    creature.setChange();
                    updateWindowTitle();
                }
            }
        );
        
        creatureCRInput.setItems(FXCollections.observableArrayList(
                Arrays.stream(ChallengeRating.values())
                        .map(ChallengeRating::displayString)
                        .collect(Collectors.toList())  
        ));

        creatureCRInput.getSelectionModel().selectedIndexProperty().addListener(
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setCRFromComboBox(newValue.intValue());
                    updateStatBlock();
                }
            }
        );
    
    }
    
}
