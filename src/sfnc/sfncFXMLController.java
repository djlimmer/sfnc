/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;

/**
 *
 * @author Doug
 */
public class sfncFXMLController implements Initializable {

    // not sure how to make this more dynamic; not sure I need to
    static final int NUMBER_OF_ARRAYS = 3;
    MainArray[][] mainArrays = new MainArray[NUMBER_OF_ARRAYS][ChallengeRating.values().length];
    String[] arrayNames = new String[NUMBER_OF_ARRAYS];
    Integer chosenArray;
        
    // the creature
    Creature creature = new Creature();
    
    // workspaces for creature
    MainArray array;
    Boolean useTypeAdjustments = true;
    Boolean useClassAdjustments = false;
    Integer initiative;
    List<Ability> abilityList;
            
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
                    chosenArray = Arrays.asList(arrayNames).indexOf(creature.getArray());
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
        
        final int EXPORTLINEWIDTH = 70;
        
        updateArray();
        // create the output lines
        String nameLine = creature.getName();
        String CROutput = "CR " + creature.CR;
        // Needs Java 11, I think:  nameLine += " ".repeat(EXPORTLINEWIDTH - nameLine.length() - CROutput.length());
        int numSpaces = EXPORTLINEWIDTH - nameLine.length() - CROutput.length();
        for (int i=0; i < numSpaces; i++) {
            nameLine += " ";
        }
        nameLine += CROutput;

        String xpLine = "XP " + creature.getXPString() + " (" + creature.getArray() + ")";
        String sensesLine = "";
        if (hasSenses())
            sensesLine += "Senses " + makeSensesString();
        String HPOutput = (array == null) ? "" : "HP " + array.hitPoints.toString();
        
        String defenseLine = "DEFENSE";
        numSpaces = EXPORTLINEWIDTH - defenseLine.length() - HPOutput.length();
        for (int i=0; i < numSpaces; i++) {
            defenseLine += " ";
        }
        defenseLine += HPOutput;
        
        String ACLine = (array == null) ? "" : "EAC " + array.EAC.toString() + "; KAC " + array.KAC.toString();
        String SaveLine = (array == null) ? "" : "Fort " + bonusString(array.fort) + "; Ref " + bonusString(array.ref)
                + "; Will " + bonusString(array.will);
        String defensiveAbilitiesLine = "";
        if (hasImmunities())
            defensiveAbilitiesLine += "Immunities " + makeImmunitiesString();
        
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
            if (!"".equals(sensesLine))
                writer.println(sensesLine);
            writer.println(defenseLine);
            writer.println(ACLine);
            writer.println(SaveLine);
            if (!"".equals(defensiveAbilitiesLine))
                writer.println(defensiveAbilitiesLine);
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (exporting to text)");
        }
     }

    // Step 0 controls
    @FXML   private TextField creatureNameInput = new TextField();
    @FXML   private ComboBox creatureCRInput = new ComboBox();

    // Step 1 controls
    @FXML   private ChoiceBox creatureArrayInput = new ChoiceBox();
    
    // Step 2 controls
    @FXML   private ComboBox creatureTypeInput = new ComboBox();
    @FXML   private CheckBox creatureTypeAdjustmentUse = new CheckBox();
    
    // stat block controls
    @FXML   private Label creatureNameDisplay = new Label();
    @FXML   private Label creatureCRDisplay = new Label();
    @FXML   private Label creatureXPDisplay = new Label();
    @FXML   private TextFlow creatureSensesBlock = new TextFlow();
    private Text creatureTypeDisplay = new Text();
    private Label creatureSensesLabel = new Label("Senses ");
    private Text creatureSensesDisplay = new Text();
    @FXML   private Label creatureHPDisplay = new Label();
    @FXML   private Label creatureEACDisplay = new Label();
    @FXML   private Label creatureKACDisplay = new Label();
    @FXML   private Label creatureFortDisplay = new Label();
    @FXML   private Label creatureRefDisplay = new Label();
    @FXML   private Label creatureWillDisplay = new Label();
    @FXML   private TextFlow creatureDefensiveAbilitiesBlock = new TextFlow();
    private Label creatureImmunitiesLabel = new Label("Immunities ");
    private Text creatureImmunitiesDisplay = new Text();
    
    
    public void setControls() {
        creatureNameInput.setText(creature.getName());
        creatureCRInput.setValue(creature.getCRDisplayString());
        creatureArrayInput.setValue(creature.getArray());
        creatureTypeInput.setValue(creature.getType());
        creatureTypeAdjustmentUse.setSelected(creature.useTypeAdjustments());
    }
    
    private void updateArray() {
        if (chosenArray == null) {
            array = null;
        } else  {
            useTypeAdjustments = creature.useTypeAdjustments();
            array = new MainArray(mainArrays[chosenArray][creature.getCR().ordinal()]);
            abilityList = new ArrayList();
            switch(creature.getType()) {
                case "Aberration":
                    abilityList.add(new Sense("darkvision",60));
                    if (useTypeAdjustments) {
                        array.will += 2;
                    }
                    break;
                case "Animal":
                    abilityList.add(new Sense("low-light vision",0));
                    // set intelligence to -4 or -5 (put choice in UI)
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                    }
                    break;
                case "Construct":
                    abilityList.add(new Sense("darkvision",60));
                    abilityList.add(new Sense("low-light vision",0));
                    abilityList.add(new Immunity("construct immunities"));
                    // set Constitution to -
                    // add subtype magical or technological (put choice in UI)
                    // if mindless set Intelligence to - and add mindless
                    //      (put choice in UI)
                    if (useTypeAdjustments) {
                        array.fort -= 2;
                        array.ref -= 2;
                        array.will -= 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Dragon":
                    abilityList.add(new Sense("darkvision",60));
                    abilityList.add(new Sense("low-light vision",0));
                    abilityList.add(new Immunity("paralysis"));
                    abilityList.add(new Immunity("sleep"));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.will += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Fey":
                    abilityList.add(new Sense("low-light vision",0));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.highAttackBonus -= 1;
                        array.lowAttackBonus -= 1;
                    }
                    break;
                case "Humanoid":
                    // requires a subtype
                    if (useTypeAdjustments) {
                        // give it +2 to one save (put choice in UI)
                    }
                    break;
                case "Magical Beast":
                    abilityList.add(new Sense("darkvision",60));
                    abilityList.add(new Sense("low-light vision",0));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Monstrous Humanoid":
                    abilityList.add(new Sense("darkvision",60));
                    if (useTypeAdjustments) {
                        array.ref += 2;
                        array.will += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Ooze":
                    abilityList.add(new Sense("blindsight",60));
                    abilityList.add(new Sense("sightless",0));
                    // give it mindless
                    abilityList.add(new Immunity("ooze immunities"));
                    // set Intelligence to - (option to turn off?)
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref -= 2;
                        array.will -= 2;
                        // no master or good skills unless natural
                    }
                    break;
                case "Outsider":
                    abilityList.add(new Sense("darkvision",60));
                    // must have subtype if member of a race
                    if (useTypeAdjustments) {
                        // give it +2 to one save (put choice in UI)
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Plant":
                    abilityList.add(new Sense("low-light vision",0));
                    abilityList.add(new Immunity("plant immunities"));
                    if (useTypeAdjustments) {
                            array.fort += 2;
                    }
                    break;
                case "Undead":
                    abilityList.add(new Sense("darkvision",60));
                    abilityList.add(new Immunity("undead immunities"));
                    // give it unliving
                    // set Constitution to -
                    if (useTypeAdjustments) {
                        array.will += 2;
                    }
                    break;
                case "Vermin":
                    abilityList.add(new Sense("darkvision",60));
                    // give it mindless
                    // set Intelligence to -
                    if (useTypeAdjustments) {
                        array.fort += 2;
                    }
                    break;
            }
        }
    }
    
    public Boolean hasSenses() {
        if (abilityList == null)
            return false;
        return abilityList.stream().anyMatch((a) -> (a.getLocation() == Location.SENSES));
    }

    public String makeSensesString() {    
        if (!hasSenses()) return "";
        
        List<String> senseList = new ArrayList();
        abilityList.stream().filter((a) -> (a.getLocation() == Location.SENSES)).forEachOrdered((a) -> {
            senseList.add(a.toString());
        });
        java.util.Collections.sort(senseList);

        return String.join(", ", senseList);
    }
    
    public Boolean hasImmunities() {
        if (abilityList == null)
            return false;
        return abilityList.stream().anyMatch((a) -> (a.getLocation() == Location.IMMUNITIES));
    }
    
    public String makeImmunitiesString() {
        if (!hasImmunities()) return "";
        
        List<String> immunityList = new ArrayList();
        abilityList.stream().filter((a) -> (a.getLocation() == Location.IMMUNITIES)).forEachOrdered((a) -> {
            immunityList.add(a.toString());
        });
        java.util.Collections.sort(immunityList);

        return String.join(", ", immunityList);
    }
    
    public void updateStatBlock() {
        updateArray();
        // update name/CR line
        creatureNameDisplay.setText(creature.getName().toUpperCase());
        creatureCRDisplay.setText(creature.getCR().toString());
        // update XP line
        creatureXPDisplay.setText(creature.getXPString() 
                + " (" + creature.getArray() + ")");    
        // update init/senses/perception line
        creatureSensesBlock.getChildren().clear();
        if (!"".equals(creature.getType())) {
            creatureTypeDisplay.setText(creature.getType());   
            creatureSensesBlock.getChildren().add(creatureTypeDisplay);
        }
        if (hasSenses()) {
            // adding a "\n" is a stopgap until all the Init display is in
            creatureSensesBlock.getChildren().add(new Text("\n"));
            creatureSensesDisplay.setText(makeSensesString());
            creatureSensesBlock.getChildren().addAll(creatureSensesLabel,creatureSensesDisplay);
        }
        creatureSensesDisplay.setText(makeSensesString());
        
        // update defenses block
        creatureHPDisplay.setText(
                (array == null) ? "" : array.hitPoints.toString());
        creatureEACDisplay.setText(
                (array == null) ? "" : array.EAC.toString());
        creatureKACDisplay.setText(
                (array == null) ? "" : array.KAC.toString());
        creatureFortDisplay.setText(
                (array == null) ? "" : bonusString(array.fort));
        creatureRefDisplay.setText(
                (array == null) ? "" : bonusString(array.ref));
        creatureWillDisplay.setText(
                (array == null) ? "" : bonusString(array.will));
        //update defensive abilities line
        creatureDefensiveAbilitiesBlock.getChildren().clear();
        if (hasImmunities()) {
            creatureImmunitiesDisplay.setText(makeImmunitiesString());
            creatureDefensiveAbilitiesBlock.getChildren().addAll(creatureImmunitiesLabel,creatureImmunitiesDisplay);
        }
    }
    
    public void updateWindowTitle() {
        Sfnc.getInstance().updateTitle((creature.hasChanged() ? "*" : "")
            + ((currentSaveFile == null) ? "Untitled" : currentSaveFile.getName())
            + " - sfnc"
        );
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        if (loadArrays() != 0) {
            System.err.println("Error in loading arrays!");
            return;
        }
        
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
        aboutDialog.setContentText("Starfinder NPC/Alien Creator\nversion 1.1.1");
        aboutDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // set up Text and TextFlows
        creatureSensesLabel.setStyle("-fx-font-weight: bold");
        creatureImmunitiesLabel.setStyle("-fx-font-weight: bold");

        // step 0 controls
        creatureNameInput.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    creature.setName(newValue);
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        
        creatureCRInput.setItems(FXCollections.observableArrayList(
                Arrays.stream(ChallengeRating.values())
                        .map(ChallengeRating::toString)
                        .collect(Collectors.toList())  
        ));

        creatureCRInput.getSelectionModel().selectedIndexProperty().addListener(
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setCRFromComboBox(newValue.intValue());
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        
        // step 1 controls
        creatureArrayInput.setItems(FXCollections.observableArrayList(
                arrayNames));
        
        creatureArrayInput.getSelectionModel().selectedIndexProperty().addListener(
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setArray(arrayNames[newValue.intValue()]);
                    chosenArray = newValue.intValue();
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        
        // step 2 controls
        String[] typeNames = {
            "Aberration", "Animal", "Construct", "Dragon", "Fey", "Humanoid",
            "Magical Beast", "Monstrous Humanoid", "Ooze", "Outsider", "Plant",
            "Undead", "Vermin"
        };
        creatureTypeInput.setItems(FXCollections.observableArrayList(
                typeNames));
        
        creatureTypeInput.getSelectionModel().selectedIndexProperty().addListener(
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setType(typeNames[newValue.intValue()]);
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );

        creatureTypeAdjustmentUse.selectedProperty().addListener(
            new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue observable,
                        Boolean oldValue, Boolean newValue) {
                    useTypeAdjustments = newValue;
                    creature.setUseTypeAdjustments(newValue);
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        creatureTypeAdjustmentUse.setSelected(true);
    
    }

    private int loadArrays() {
        try {
            FileReader fileReader = new FileReader("arrays.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String statString;
            String[] stats;
            Integer[] intArray = new Integer[MainArray.NUMBER_OF_STATS];
            Dice[] diceArray = new Dice[MainArray.NUMBER_OF_DAMAGES];
            
            for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
                arrayNames[i] = bufferedReader.readLine();
                for (int j=1; j < ChallengeRating.values().length; j++) {
                    statString = bufferedReader.readLine();
                    stats = statString.split(" +");
                    for (int k=0; k < MainArray.NUMBER_OF_STATS; k++) {
                        intArray[k] = Integer.valueOf(stats[k]);
                    }
                    for (int k=0; k < MainArray.NUMBER_OF_DAMAGES; k++) {
                        diceArray[k] = new Dice(stats[k+MainArray.NUMBER_OF_STATS]);
                    }
                    mainArrays[i][j] = new MainArray(intArray,diceArray); 
                }
            }
            
            bufferedReader.close();
            return 0;
        }
        catch(FileNotFoundException ex) {
            System.err.println("arrays.txt not found!");
        }
        catch(IOException ex) {
            System.err.println("Error reading arrays.txt!");
        }
        return 1;
    }

    private String bonusString(Integer n) {
        return ((n >= 0) ? "+" : "") + n.toString();
    }
    
}
