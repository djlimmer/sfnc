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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
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
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
    
    List<String> generalSubtypes = new ArrayList<String>();
    List<String> humanoidSubtypes = new ArrayList<String>();
    List<String> outsiderSubtypes = new ArrayList<String>();
        
    // the creature
    Creature creature = new Creature();
    
    // workspaces for creature
    MainArray array;
    Boolean useTypeAdjustments = true;
    Boolean useClassAdjustments = false;
    Integer initiative;
    Set<Ability> abilitySet;
    
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
        if (creature.hasChanged() &&
                (fileChangeAlert.showAndWait().get() != ButtonType.OK))
            return;

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
            sensesLine += "Senses " + makeAbilityStringByLocation(Location.SENSES);
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
            defensiveAbilitiesLine += "Immunities " + makeAbilityStringByLocation(Location.IMMUNITIES);
        
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
    private ToggleGroup typeOptionsGroup = new ToggleGroup();
    @FXML   private RadioButton creatureTypeOption1 = new RadioButton();
    @FXML   private RadioButton creatureTypeOption2 = new RadioButton();
    @FXML   private RadioButton creatureTypeOption3 = new RadioButton();
    
    // Step 3 controls
    @FXML   private Label creatureSubtypeWarning = new Label();
    @FXML   private ListView<String> creatureGeneralSubtypesInput = new ListView<String>();
    @FXML   private ListView<String> creatureHumanoidSubtypesInput = new ListView<String>();
    @FXML   private ListView<String> creatureOutsiderSubtypesInput = new ListView<String>();
    @FXML   private TextField creatureFreeformSubtypesInput = new TextField();

    // stat block controls
    @FXML   private Label creatureNameDisplay = new Label();
    @FXML   private Label creatureCRDisplay = new Label();
    @FXML   private Label creatureXPDisplay = new Label();
    @FXML   private TextFlow creatureSensesBlock = new TextFlow();
    private Label creatureTypeDisplay = new Label();
    private Label creatureSensesLabel = new Label("Senses ");
    private Label creatureSensesDisplay = new Label();
    @FXML   private Label creatureHPDisplay = new Label();
    @FXML   private Label creatureEACDisplay = new Label();
    @FXML   private Label creatureKACDisplay = new Label();
    @FXML   private Label creatureFortDisplay = new Label();
    @FXML   private Label creatureRefDisplay = new Label();
    @FXML   private Label creatureWillDisplay = new Label();
    @FXML   private TextFlow creatureDefensiveAbilitiesBlock = new TextFlow();
    private Label creatureImmunitiesLabel = new Label("Immunities ");
    private Label creatureImmunitiesDisplay = new Label();
    @FXML   private TextFlow creatureOffensiveAbilitiesBlock = new TextFlow();
    private Label creatureOffensiveAbilitiesLabel = new Label("Offensive Abilities ");
    private Label creatureOffensiveAbilitiesDisplay = new Label();
    @FXML   private TextFlow creatureStatisticsBlock = new TextFlow();
    private Label creatureOtherAbilitiesLabel = new Label("Other Abilities ");
    private Label creatureOtherAbilitiesDisplay = new Label();
    
    
    public void setControls() {
        creatureNameInput.setText(creature.getName());
        creatureCRInput.setValue(creature.getCRDisplayString());
        creatureArrayInput.setValue(creature.getArray());
        creatureTypeInput.setValue(creature.getType());
        creatureTypeAdjustmentUse.setSelected(creature.useTypeAdjustments());
        if (creature.getType().equals("Animal")) {
            showAnimalTypeOptions();
        }
        else if (creature.getType().equals("Humanoid") || creature.getType().equals("Outsider")) {
            showSaveBonusTypeOptions();
        }
        else {
            hideTypeOptions();
        }
        setSubtypeWarning();
    }
    
    private void showAnimalTypeOptions() {
        creatureTypeOption1.setVisible(true);
        creatureTypeOption1.setText("Intelligence -4");
        creatureTypeOption2.setVisible(true);
        creatureTypeOption2.setText("Intelligence -5");
        creatureTypeOption3.setVisible(false);

        Integer currentOption = creature.getTypeOption();
        creatureTypeOption1.setSelected((currentOption == 1));
        creatureTypeOption2.setSelected((currentOption == 2));
    }

    private void showSaveBonusTypeOptions() {
        creatureTypeOption1.setVisible(true);
        creatureTypeOption1.setText("+2 fort");
        creatureTypeOption2.setVisible(true);
        creatureTypeOption2.setText("+2 ref");
        creatureTypeOption3.setVisible(true);
        creatureTypeOption3.setText("+2 will");

        Integer currentOption = creature.getTypeOption();
        creatureTypeOption1.setSelected((currentOption == 1));
        creatureTypeOption2.setSelected((currentOption == 2));
        creatureTypeOption3.setSelected((currentOption == 3));
    }
    
    private void hideTypeOptions() {
        creatureTypeOption1.setVisible(false);
        creatureTypeOption2.setVisible(false);
        creatureTypeOption3.setVisible(false);     
    }
    
    private void setSubtypeWarning() {
                switch(creature.getType()) {
            case "Construct":
                creatureSubtypeWarning.setText("Use 'magical' or 'technological'");
                break;
            case "Humanoid":
                creatureSubtypeWarning.setText("Must have a humanoid subtype");
                break;
            case "Outsider":
                creatureSubtypeWarning.setText("May need an outsider subtype");
                break;
            default:
                creatureSubtypeWarning.setText("");
        }
    }
    
    private void updateArray() {
        if (chosenArray == null || creature.getCR().ordinal()==0) {
            array = null;
        } else  {
            useTypeAdjustments = creature.useTypeAdjustments();
            array = new MainArray(mainArrays[chosenArray][creature.getCR().ordinal()]);
            abilitySet = new HashSet();
            switch(creature.getType()) {
                case "Aberration":
                    abilitySet.add(new Sense("darkvision",60));
                    if (useTypeAdjustments) {
                        array.will += 2;
                    }
                    break;
                case "Animal":
                    abilitySet.add(new Sense("low-light vision",0));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                    }
                    break;
                case "Construct":
                    abilitySet.add(new Sense("darkvision",60));
                    abilitySet.add(new Sense("low-light vision",0));
                    abilitySet.add(new Immunity("construct immunities"));
                    abilitySet.add(Ability.getAbility("noConScore"));
                    if (useTypeAdjustments) {
                        array.fort -= 2;
                        array.ref -= 2;
                        array.will -= 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Dragon":
                    abilitySet.add(new Sense("darkvision",60));
                    abilitySet.add(new Sense("low-light vision",0));
                    abilitySet.add(new Immunity("paralysis"));
                    abilitySet.add(new Immunity("sleep"));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.will += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Fey":
                    abilitySet.add(new Sense("low-light vision",0));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.highAttackBonus -= 1;
                        array.lowAttackBonus -= 1;
                    }
                    break;
                case "Humanoid":
                    if (useTypeAdjustments) {
                        switch(creature.getTypeOption()) {
                            case 1: array.fort += 2; break;
                            case 2: array.ref += 2; break;
                            case 3: array.will += 2; break;
                        }
                    }
                    break;
                case "Magical Beast":
                    abilitySet.add(new Sense("darkvision",60));
                    abilitySet.add(new Sense("low-light vision",0));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Monstrous Humanoid":
                    abilitySet.add(new Sense("darkvision",60));
                    if (useTypeAdjustments) {
                        array.ref += 2;
                        array.will += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Ooze":
                    abilitySet.add(new Sense("blindsight",60));
                    abilitySet.add(new Sense("sightless",0));
                    abilitySet.add(Ability.getAbility("mindless"));
                    abilitySet.add(new Immunity("ooze immunities"));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref -= 2;
                        array.will -= 2;
                        // no master or good skills unless natural
                    }
                    break;
                case "Outsider":
                    abilitySet.add(new Sense("darkvision",60));
                    if (useTypeAdjustments) {
                        switch(creature.getTypeOption()) {
                            case 1: array.fort += 2; break;
                            case 2: array.ref += 2; break;
                            case 3: array.will += 2; break;
                        }
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Plant":
                    abilitySet.add(new Sense("low-light vision",0));
                    abilitySet.add(new Immunity("plant immunities"));
                    if (useTypeAdjustments) {
                            array.fort += 2;
                    }
                    break;
                case "Undead":
                    abilitySet.add(new Sense("darkvision",60));
                    abilitySet.add(new Immunity("undead immunities"));
                    abilitySet.add(Ability.getAbility("unliving"));
                    abilitySet.add(Ability.getAbility("noConScore"));
                    if (useTypeAdjustments) {
                        array.will += 2;
                    }
                    break;
                case "Vermin":
                    abilitySet.add(new Sense("darkvision",60));
                    abilitySet.add(Ability.getAbility("mindless"));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                    }
                    break;
            }
            // handle subtypes
            List<String> subtypes = creature.getAllSubtypes();
            if (subtypes.contains("aeon")) {
                // put handling here
            }
            if (subtypes.contains("agathion")) {
                // put handling here
            }
            if (subtypes.contains("air")) {
                // put handling here
            }
            if (subtypes.contains("android")) {
                // put handling here
            }
            if (subtypes.contains("angel")) {
                // put handling here
            }
            if (subtypes.contains("aquatic")) {
                // put handling here
            }
            if (subtypes.contains("archon")) {
                // put handling here
            }
            if (subtypes.contains("azata")) {
                // put handling here
            }
            if (subtypes.contains("cold")) {
                // put handling here
            }
            if (subtypes.contains("daemon")) {
                // put handling here
            }
            if (subtypes.contains("demon")) {
                // put handling here
            }
            if (subtypes.contains("devil")) {
                // put handling here
            }
            if (subtypes.contains("dwarf")) {
                // put handling here
            }
            if (subtypes.contains("earth")) {
                // put handling here
            }
            if (subtypes.contains("elemental")) {
                // put handling here
            }
            if (subtypes.contains("elf")) {
                // put handling here
            }
            if (subtypes.contains("fire")) {
                // put handling here
            }
            if (subtypes.contains("giant")) {
                // put handling here
            }
            if (subtypes.contains("gnome")) {
                // put handling here
            }
            if (subtypes.contains("goblinoid")) {
                // put handling here
            }
            if (subtypes.contains("gray")) {
                // put handling here
            }
            if (subtypes.contains("halfling")) {
                // put handling here
            }
            if (subtypes.contains("human")) {
                // put handling here
            }
            if (subtypes.contains("ikeshti")) {
                // put handling here
            }
            if (subtypes.contains("incorporeal")) {
                // put handling here
            }
            if (subtypes.contains("inevitable")) {
                // put handling here
            }
            if (subtypes.contains("kasatha")) {
                // put handling here
            }
            if (subtypes.contains("lashunta")) {
                // put handling here
            }
            if (subtypes.contains("maraquoi")) {
                // put handling here
            }
            if (subtypes.contains("orc")) {
                // put handling here
            }
            if (subtypes.contains("plantlike")) {
                // put handling here
            }
            if (subtypes.contains("protean")) {
                // put handling here
            }
            if (subtypes.contains("reptoid")) {
                // put handling here
            }
            if (subtypes.contains("ryphorian")) {
                // put handling here
            }
            if (subtypes.contains("sarcesian")) {
                // put handling here
            }
            if (subtypes.contains("shapechanger")) {
                // put handling here
            }
            if (subtypes.contains("shirren")) {
                // put handling here
            }
            if (subtypes.contains("skittermander")) {
                // put handling here
            }
            if (subtypes.contains("swarm")) {
                // put handling here
            }
            if (subtypes.contains("verthani")) {
                // put handling here
            }
            if (subtypes.contains("vesk")) {
                // put handling here
            }
            if (subtypes.contains("water")) {
                // put handling here
            }
            if (subtypes.contains("ysoki")) {
                // put handling here
            }
        }
    }
    
    public Boolean hasSenses() {
        if (abilitySet == null)
            return false;
        return abilitySet.stream().anyMatch((a) -> (a.getLocation() == Location.SENSES));
    }
    
    public Boolean hasImmunities() {
        if (abilitySet == null)
            return false;
        return abilitySet.stream().anyMatch((a) -> (a.getLocation() == Location.IMMUNITIES));
    }
    
    public Boolean hasOffensiveAbilities() {
        if (abilitySet == null)
            return false;
        return abilitySet.stream().anyMatch((a) -> (a.getLocation() == Location.OFFENSIVE_ABILITIES));
    }
    
    public Boolean hasOtherAbilities() {
        if (abilitySet == null)
            return false;
        return abilitySet.stream().anyMatch((a) -> (a.getLocation() == Location.OTHER_ABILITIES));
    }
        
    public String makeAbilityStringByLocation(Location l) {
        List<String> abilitiesAtLocation = new ArrayList();
        abilitySet.stream().filter(a -> (a.getLocation() == l)).forEachOrdered(a -> abilitiesAtLocation.add(a.toString()));
        java.util.Collections.sort(abilitiesAtLocation);
        
        return String.join(", ", abilitiesAtLocation);
    }
    
    public void updateStatBlock() {
        updateArray();
        // update name/CR line
        creatureNameDisplay.setText(creature.getName().toUpperCase());
        creatureCRDisplay.setText(creature.getCR().toString());
        // update XP line
        creatureXPDisplay.setText(creature.getXPString() 
                + " (" + creature.getArray() + ")");    
        // update type/senses/etc block
        creatureSensesBlock.getChildren().clear();
        // set up type & subtype display
        if (!"".equals(creature.getType())) {
            String typeDisplayString = creature.getType();
            List<String> subtypes = creature.getAllSubtypes();
            if (subtypes != null) {
                Collections.sort(subtypes);
                String subtypeDisplayString = String.join(", ", subtypes);
                if (!"".equals(subtypeDisplayString))
                    typeDisplayString += " (" + subtypeDisplayString + ")";
            }
            creatureTypeDisplay.setText(typeDisplayString);  
            creatureSensesBlock.getChildren().add(creatureTypeDisplay);
            // TODO: for some unknownreason, Humanoid type doesn't update the creatureTypeDisplay.
            // is it because it doesn't have senses??
        }
        // update init/senses/perception line
        if (hasSenses()) {
            // adding a "\n" is a stopgap until the Init display is in
            creatureSensesBlock.getChildren().add(new Text("\n"));
            creatureSensesDisplay.setText(makeAbilityStringByLocation(Location.SENSES));
            creatureSensesBlock.getChildren().addAll(creatureSensesLabel,creatureSensesDisplay);
        }
        
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
        creatureOffensiveAbilitiesBlock.getChildren().clear();
        creatureStatisticsBlock.getChildren().clear();
        if (hasImmunities()) {
            creatureImmunitiesDisplay.setText(makeAbilityStringByLocation(Location.IMMUNITIES));
            creatureDefensiveAbilitiesBlock.getChildren().addAll(creatureImmunitiesLabel,creatureImmunitiesDisplay);
        }
        if (hasOffensiveAbilities()) {
            creatureOffensiveAbilitiesDisplay.setText(makeAbilityStringByLocation(Location.OFFENSIVE_ABILITIES));
            creatureOffensiveAbilitiesBlock.getChildren().addAll(creatureOffensiveAbilitiesLabel,creatureOffensiveAbilitiesDisplay);
        }
        if (hasOtherAbilities()) {
            creatureOtherAbilitiesDisplay.setText(makeAbilityStringByLocation(Location.OTHER_ABILITIES));
            creatureStatisticsBlock.getChildren().addAll(creatureOtherAbilitiesLabel,creatureOtherAbilitiesDisplay);
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
        
        if (loadAbilities() != 0) {
            System.err.println("Error in loading arrays!");
            return;
        }

        if (loadSubtypes() != 0) {
            System.err.println("Error in loading subtypes!");
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
        aboutDialog.setContentText("Starfinder NPC/Alien Creator\nversion 1.2.1");
        aboutDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // set up Labels and TextFlows
        creatureSensesLabel.setStyle("-fx-font-weight: bold");
        creatureImmunitiesLabel.setStyle("-fx-font-weight: bold");
        creatureOffensiveAbilitiesLabel.setStyle("-fx-font-weight: bold");
        creatureOtherAbilitiesLabel.setStyle("-fx-font-weight: bold");

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
        
        creatureTypeInput.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setType(typeNames[newValue.intValue()]);
                    switch(creature.getType()) {
                        case "Animal": 
                            showAnimalTypeOptions(); 
                            break;
                        case "Humanoid":
                        case "Outsider":
                            showSaveBonusTypeOptions();
                            break;
                        default:
                            hideTypeOptions();
                    }
                    updateStatBlock();
                    updateWindowTitle();
                    setSubtypeWarning();
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
        
        creatureTypeOption1.setToggleGroup(typeOptionsGroup);
        creatureTypeOption2.setToggleGroup(typeOptionsGroup);
        creatureTypeOption3.setToggleGroup(typeOptionsGroup);    
        
        typeOptionsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (typeOptionsGroup.getSelectedToggle() != null) {
                    if (creatureTypeOption1.isSelected())
                        creature.setTypeOption(1);
                    else if (creatureTypeOption2.isSelected())
                        creature.setTypeOption(2);
                    else if (creatureTypeOption3.isSelected())
                        creature.setTypeOption(3);
                }
                updateStatBlock();
                updateWindowTitle();
            }
        });
        
        // step 3 controls
        creatureGeneralSubtypesInput.setItems(FXCollections.observableArrayList(generalSubtypes));
        creatureGeneralSubtypesInput.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        creatureGeneralSubtypesInput.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                creature.setGeneralSubtypes(creatureGeneralSubtypesInput.getSelectionModel().getSelectedItems());
                updateStatBlock();
                updateWindowTitle();
            }
        });
        creatureHumanoidSubtypesInput.setItems(FXCollections.observableArrayList(humanoidSubtypes));
        creatureHumanoidSubtypesInput.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        creatureHumanoidSubtypesInput.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                creature.setHumanoidSubtypes(creatureHumanoidSubtypesInput.getSelectionModel().getSelectedItems());
                updateStatBlock();
                updateWindowTitle();
            }
        });
        creatureOutsiderSubtypesInput.setItems(FXCollections.observableArrayList(outsiderSubtypes));
        creatureOutsiderSubtypesInput.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        creatureOutsiderSubtypesInput.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                creature.setOutsiderSubtypes(creatureOutsiderSubtypesInput.getSelectionModel().getSelectedItems());
                updateStatBlock();
                updateWindowTitle();
            }
        });
        creatureFreeformSubtypesInput.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue))
                        creature.setFreeformSubtypes(new ArrayList<>());
                    else
                        creature.setFreeformSubtypes(new ArrayList<>(Arrays.asList(newValue.split(","))));
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        
        // step 4 controls
        
        // step 5 controls
        
        // step 6 controls
        
        // step 7 controls
        
        // step 8 controls
        
        // step 9 controls

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

    private int loadAbilities() {
        try {
            FileReader fileReader = new FileReader("abilities.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String abilityLine;
            String[] abilityParts;
            
            // abilityLine: name first, outputFormat second, location third
            
            while (!"EOF".equals(abilityLine = bufferedReader.readLine())) {
                abilityParts = abilityLine.split(" +");
                Ability.setOfAbilities.add(
                        new Ability(abilityParts[0],
                                Location.valueOf(abilityParts[2]),
                                abilityParts[1]));
            }
           
            bufferedReader.close();
            return 0;
        }
        catch(FileNotFoundException ex) {
            System.err.println("abilities.txt not found!");
        }
        catch(IOException ex) {
            System.err.println("Error reading abilities.txt!");
        }
        return 1;
    }

    private int loadSubtypes() {
        try {
            FileReader fileReader = new FileReader("subtypes.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String subtypeLine;
            String[] subtypeParts;
            
            while (!"EOF".equals(subtypeLine = bufferedReader.readLine())) {
                subtypeParts = subtypeLine.split(" +");
                switch(subtypeParts[1]) {
                    case "humanoid":
                        humanoidSubtypes.add(subtypeParts[0]);
                        break;
                    case "outsider":
                        outsiderSubtypes.add(subtypeParts[0]);
                        break;
                    default:
                        generalSubtypes.add(subtypeParts[0]);
                }
            }
           
            bufferedReader.close();
            java.util.Collections.sort(humanoidSubtypes);
            java.util.Collections.sort(outsiderSubtypes);
            java.util.Collections.sort(generalSubtypes);
            return 0;
        }
        catch(FileNotFoundException ex) {
            System.err.println("subtypes.txt not found!");
        }
        catch(IOException ex) {
            System.err.println("Error reading abilities.txt!");
        }
        return 1;
    }

    private String bonusString(Integer n) {
        return ((n >= 0) ? "+" : "") + n.toString();
    }
    
}
