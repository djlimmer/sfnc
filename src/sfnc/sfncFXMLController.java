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
import java.util.Optional;
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
import static sfnc.Ability.setOfAbilities;

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
        if (hasAbilitiesByLocation(Location.SENSES))
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
        if (hasAbilitiesByLocation(Location.IMMUNITIES))
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
    private Label creatureAlignmentDisplay = new Label();
    private Label creatureSizeDisplay = new Label();
    private Label creatureTypeDisplay = new Label();
    private Label creatureInitLabel = new Label("Init ");
    private Label creatureInitDisplay = new Label();
    private Label creatureSensesLabel = new Label("Senses ");
    private Label creatureSensesDisplay = new Label();
    private Label creaturePerceptionLabel = new Label("Perception ");
    private Label creaturePerceptionDisplay = new Label();
    private Label creatureAuraLabel = new Label("Aura ");
    private Label creatureAuraDisplay = new Label();
    @FXML   private Label creatureHPDisplay = new Label();
    private Label creatureRPLabel = new Label("RP ");
    private Label creatureRPDisplay = new Label();
    @FXML   private Label creatureEACDisplay = new Label();
    @FXML   private Label creatureKACDisplay = new Label();
    @FXML   private Label creatureFortDisplay = new Label();
    @FXML   private Label creatureRefDisplay = new Label();
    @FXML   private Label creatureWillDisplay = new Label();
    @FXML   private TextFlow creatureDefensiveAbilitiesBlock = new TextFlow();
    private Label creatureDefensiveAbilitiesLabel = new Label("Defensive Abilities ");
    private Label creatureDefensiveAbilitiesDisplay = new Label();
    private Label creatureDRLabel = new Label("DR ");
    private Label creatureDRDisplay = new Label();
    private Label creatureImmunitiesLabel = new Label("Immunities ");
    private Label creatureImmunitiesDisplay = new Label();
    private Label creatureResistancesLabel = new Label("Resistances: ");
    private Label creatureResistancesDisplay = new Label();
    private Label creatureSRLabel = new Label("SR ");
    private Label creatureSRDisplay = new Label();
    private Label creatureWeaknessesLabel = new Label("Weaknesses ");
    private Label creatureWeaknessesDisplay = new Label();
    @FXML   private TextFlow creatureOffensiveAbilitiesBlock = new TextFlow();
    private Label creatureSpeedLabel = new Label("Speed ");
    private Label creatureSpeedDisplay = new Label();
    // melee
    // multiattack
    // ranged
    // space and reach
    private Label creatureOffensiveAbilitiesLabel = new Label("Offensive Abilities ");
    private Label creatureOffensiveAbilitiesDisplay = new Label();
    // spell-like abilities
    // spells known
    @FXML   private TextFlow creatureStatisticsBlock = new TextFlow();
    // ability score modifiers
    // feats
    // skills
    private Label creatureLanguagesLabel = new Label("Languages ");
    private Label creatureLanguagesDisplay = new Label();
    private Label creatureOtherAbilitiesLabel = new Label("Other Abilities ");
    private Label creatureOtherAbilitiesDisplay = new Label();
    // gear and augmentations

    
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
    
    private void addSenseToAbilitySet(String senseName,Integer range) {
        Optional<Ability> optionalSense = setOfAbilities.stream()
                .filter((Ability a) -> a.getId().equals(senseName))
                .findAny();
        if (!optionalSense.isPresent())
            abilitySet.add(new Sense(senseName,range));
        else {
            Ability sense = optionalSense.get();
            if (sense instanceof Sense)
                if (((Sense) sense).range < range)
                    ((Sense) sense).range = range;
        }
    }
    
    private void addImmunityToAbilitySet(String name) {
        Optional<Ability> optionalImmunity = setOfAbilities.stream()
                .filter((Ability a) -> a.getId().equals(name))
                .findAny();
        if (!optionalImmunity.isPresent())
            abilitySet.add(new Immunity(name));
    }
    
    private void addResistanceToAbilitySet(String resistanceName,Integer value) {
        Optional<Ability> optionalResistance = setOfAbilities.stream()
                .filter((Ability a) -> a.getId().equals(resistanceName))
                .findAny();
        if (!optionalResistance.isPresent())
            abilitySet.add(new Resistance(resistanceName,value));
        else {
            Ability resistance = optionalResistance.get();
            if (resistance instanceof Resistance)
                if (((Resistance) resistance).getAmount() < value)
                    ((Resistance) resistance).setAmount(value);
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
                    addSenseToAbilitySet("darkvision",60);
                    if (useTypeAdjustments) {
                        array.will += 2;
                    }
                    break;
                case "Animal":
                    addSenseToAbilitySet("low-light vision",0);
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                    }
                    break;
                case "Construct":
                    addSenseToAbilitySet("darkvision",60);
                    addSenseToAbilitySet("low-light vision",0);
                    addImmunityToAbilitySet("construct immunities");
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
                    addSenseToAbilitySet("darkvision",60);
                    addSenseToAbilitySet("low-light vision",0);
                    addImmunityToAbilitySet("paralysis");
                    addImmunityToAbilitySet("sleep");
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.will += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Fey":
                    addSenseToAbilitySet("low-light vision",0);
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
                    addSenseToAbilitySet("darkvision",60);
                    addSenseToAbilitySet("low-light vision",0);
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Monstrous Humanoid":
                    addSenseToAbilitySet("darkvision",60);
                    if (useTypeAdjustments) {
                        array.ref += 2;
                        array.will += 2;
                        array.highAttackBonus += 1;
                        array.lowAttackBonus += 1;
                    }
                    break;
                case "Ooze":
                    addSenseToAbilitySet("blindsight (unspecified)",60);
                    addSenseToAbilitySet("sightless",0);
                    abilitySet.add(Ability.getAbility("mindless"));
                    addImmunityToAbilitySet("ooze immunities");
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref -= 2;
                        array.will -= 2;
                        // no master or good skills unless natural
                    }
                    break;
                case "Outsider":
                    addSenseToAbilitySet("darkvision",60);
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
                    addSenseToAbilitySet("low-light vision",0);
                    addImmunityToAbilitySet("plant immunities");
                    if (useTypeAdjustments) {
                            array.fort += 2;
                    }
                    break;
                case "Undead":
                    addSenseToAbilitySet("darkvision",60);
                    addImmunityToAbilitySet("undead immunities");
                    abilitySet.add(Ability.getAbility("unliving"));
                    abilitySet.add(Ability.getAbility("noConScore"));
                    if (useTypeAdjustments) {
                        array.will += 2;
                    }
                    break;
                case "Vermin":
                    addSenseToAbilitySet("darkvision",60);
                    abilitySet.add(Ability.getAbility("mindless"));
                    if (useTypeAdjustments) {
                        array.fort += 2;
                    }
                    break;
            }
            // handle subtypes
            List<String> subtypes = creature.getAllSubtypes();
            if (subtypes.contains("aeon")) {
                addImmunityToAbilitySet("cold");
                addImmunityToAbilitySet("critical hits");
                addImmunityToAbilitySet("poison");
                addResistanceToAbilitySet("electricity",10);
                addResistanceToAbilitySet("fire",10);
                abilitySet.add(Ability.getAbility("extension of all"));
                abilitySet.add(Ability.getAbility("telepathy 100 ft. (non-verbal)"));
                abilitySet.add(Ability.getAbility("bonus to recall knowledge"));
            }
            if (subtypes.contains("agathion")) {
                addSenseToAbilitySet("low-light vision",0);
                abilitySet.add(Ability.getAbility("+4 vs. poison"));
                addImmunityToAbilitySet("petrification");
                addImmunityToAbilitySet("electricity");
                addResistanceToAbilitySet("cold",10);
                addResistanceToAbilitySet("sonic",10);
                abilitySet.add(Ability.getAbility("healing channel"));
                abilitySet.add(Ability.getAbility("truespeech"));
                abilitySet.add(Ability.getAbility("speak with animals"));
            }
            if (subtypes.contains("air")) {
                // supernatural fly speed, usually with perfect maneuverability
                // Acrobatics as master or good skill
            }
            if (subtypes.contains("android")) {
                // most gain darkvision 60 ft. and low-light vision
                // if android race, get constructed, flat affect, upgrade slot
            }
            if (subtypes.contains("angel")) {
                addSenseToAbilitySet("darkvision",60);
                addSenseToAbilitySet("low-light vision",0);
                abilitySet.add(Ability.getAbility("protective aura"));
                abilitySet.add(Ability.getAbility("+4 vs. poison"));
                addImmunityToAbilitySet("acid");
                addImmunityToAbilitySet("cold");
                addImmunityToAbilitySet("petrification");
                addResistanceToAbilitySet("electricity",10);
                addResistanceToAbilitySet("fire",10);
                abilitySet.add(Ability.getAbility("truespeech"));
            }
            if (subtypes.contains("aquatic")) {
                // swim speed
                abilitySet.add(Ability.getAbility("water breathing"));
                // Athletics as a master or good skill
                // optionally, amphibious
            }
            if (subtypes.contains("archon")) {
                addSenseToAbilitySet("darkvision",60);
                addSenseToAbilitySet("low-light vision",0);
                abilitySet.add(Ability.getAbility("aura of menace"));
                abilitySet.add(Ability.getAbility("+4 vs. poison"));
                addImmunityToAbilitySet("electricity");
                addImmunityToAbilitySet("petrification");
                abilitySet.add(Ability.getAbility("truespeech"));
                // many get teleport as an at-will SLA (CL = CR)
            }
            if (subtypes.contains("azata")) {
                addSenseToAbilitySet("darkvision",60);
                addSenseToAbilitySet("low-light vision",0);
                addImmunityToAbilitySet("electricity");
                addImmunityToAbilitySet("petrification");
                addResistanceToAbilitySet("cold",10);
                addResistanceToAbilitySet("fire",10);
                abilitySet.add(Ability.getAbility("truespeech"));
            }
            if (subtypes.contains("cold")) {
                addImmunityToAbilitySet("cold");
                abilitySet.add(Ability.getAbility("vulnerable to fire"));
            }
            if (subtypes.contains("daemon")) {
                addImmunityToAbilitySet("acid");
                addImmunityToAbilitySet("death effects");
                addImmunityToAbilitySet("disease");
                addImmunityToAbilitySet("poison");
                addResistanceToAbilitySet("cold",10);
                addResistanceToAbilitySet("electricity",10);
                addResistanceToAbilitySet("fire",10);
                abilitySet.add(Ability.getAbility("summon allies"));
                abilitySet.add(Ability.getAbility("telepathy"));
            }
            if (subtypes.contains("demon")) {
                addImmunityToAbilitySet("electricity");
                addImmunityToAbilitySet("poison");
                addResistanceToAbilitySet("acid",10);
                addResistanceToAbilitySet("cold",10);
                addResistanceToAbilitySet("fire",10);
                abilitySet.add(Ability.getAbility("summon allies"));
                abilitySet.add(Ability.getAbility("telepathy"));
            }
            if (subtypes.contains("devil")) {
                addSenseToAbilitySet("see in darkness",0);
                addImmunityToAbilitySet("fire");
                addImmunityToAbilitySet("poison");
                addResistanceToAbilitySet("acid",10);
                addResistanceToAbilitySet("cold",10);
                abilitySet.add(Ability.getAbility("summon allies"));
                abilitySet.add(Ability.getAbility("telepathy"));
            }
            if (subtypes.contains("dwarf")) {
                // most gain darkvision 60 ft.
                // dwarf race gains slow but steady, stonecunning, traditional enemies, weapon familiarity
            }
            if (subtypes.contains("earth")) {
                // burrow speed
                // blindsense (vibration) or blindsight (vibration) with varied range
            }
            if (subtypes.contains("elemental")) {
                addImmunityToAbilitySet("elemental immunities");
            }
            if (subtypes.contains("elf")) {
                // most gain low-light vision
                // most gain Perception as additional master skill
                // drow get darkvision 60 ft. instead of low-light vision, drow immunities, drow magic, light blindness
                // elf race gets elven immunities, elven magic, Mysticism as master skill
                // half-elf race gets elven blood, extra good skill
            }
            if (subtypes.contains("fire")) {
                addImmunityToAbilitySet("fire");
                abilitySet.add(Ability.getAbility("vulnerable to cold"));
            }
            if (subtypes.contains("giant")) {
                addSenseToAbilitySet("low-light vision",0);
                // many gain Intimidate and Perception as master skills
            }
            if (subtypes.contains("gnome")) {
                addSenseToAbilitySet("low-light vision",0);
                // gnome race gets eternal hope, gnome magic, Culture as master skill
            }
            if (subtypes.contains("goblinoid")) {
                addSenseToAbilitySet("darkvision",60);
                // space goblin race gets fast, tinker, Engineering and Stealth as master skills, Survival as good skill
            }
            if (subtypes.contains("gray")) {
                addSenseToAbilitySet("darkvision",60);
                // gray race gets phase, telepathy 100 ft.
            }
            if (subtypes.contains("halfling")) {
                // halfling race gets halfling luck, sneaky, Perception and Stealth as master skills, Athletics and Acrobatics as good skills
            }
            if (subtypes.contains("human")) {
                // human race gets additional special ability and additional good skill
            }
            if (subtypes.contains("ikeshti")) {
                // most gain climb speed
                // ikeshti race gets desert survivor, shed skin, squirt blood
            }
            if (subtypes.contains("incorporeal")) {
                abilitySet.add(Ability.getAbility("incorporeal"));
            }
            if (subtypes.contains("inevitable")) {
                addSenseToAbilitySet("darkvision",60);
                addSenseToAbilitySet("low-light vision",0);
                abilitySet.add(Ability.getAbility("constructed"));
                abilitySet.add(Ability.getAbility("regeneration (suppressed by chaotic-aligned attacks)"));
                abilitySet.add(Ability.getAbility("truespeech"));
            }
            if (subtypes.contains("kasatha")) {
                // kasatha race gets desert stride, four-armed, Acrobatics and Athletics as master skills, Culture as good skill
            }
            if (subtypes.contains("lashunta")) {
                // lashunta race gets limited telepathy, SLAs: 1/day detect thoughs, at will daze, psychokinetic hand
            }
            if (subtypes.contains("maraquoi")) {
                addSenseToAbilitySet("low-light vision",0);
                // maraquoi race gets blindsense (sound) 30 ft., climb 20 ft., prehensile tail, Survival as master skill
            }
            if (subtypes.contains("orc")) {
                // most gain darkvision 60 ft. and ferocity
                // half-orc race also gets Intimidate and Survival as master skills
            }
            if (subtypes.contains("plantlike")) {
                // most gain plantlike
            }
            if (subtypes.contains("protean")) {
                // blindsense (type and distance vary)
                addImmunityToAbilitySet("acid");
                addResistanceToAbilitySet("electricity",10);
                addResistanceToAbilitySet("sonic",10);
                // supernatural flight speed
                abilitySet.add(Ability.getAbility("amorphous"));
                abilitySet.add(Ability.getAbility("change shape"));
                abilitySet.add(Ability.getAbility("grab"));
            }
            if (subtypes.contains("reptoid")) {
                addSenseToAbilitySet("low-light vision",0);
                // reptoid race gets change shape, cold-blooded, natural weapons
            }
            if (subtypes.contains("ryphorian")) {
                addSenseToAbilitySet("low-light vision",0);
                // ryphorian race gets trimorphic, additional special ability, Perception as master skill
            }
            if (subtypes.contains("sarcesian")) {
                addSenseToAbilitySet("low-light vision",0);
                // sarcesian race gets void flyer, additional good skill
            }
            if (subtypes.contains("shapechanger")) {
                abilitySet.add(Ability.getAbility("change shape"));
            }
            if (subtypes.contains("shirren")) {
                addSenseToAbilitySet("blindsense (vibration)",30);
                // shirren race gets communalism, limited telepathy, Culture and Diplomacy as good skills
            }
            if (subtypes.contains("skittermander")) {
                addSenseToAbilitySet("low-light vision",0);
                // skittermander race gets grappler, hyper, six-armed
            }
            if (subtypes.contains("swarm")) {
                abilitySet.add(Ability.getAbility("swarm defenses"));
                addImmunityToAbilitySet("swarm immunities");
                abilitySet.add(Ability.getAbility("distraction"));
                abilitySet.add(Ability.getAbility("swarm attack"));
            }
            if (subtypes.contains("verthani")) {
                addSenseToAbilitySet("low-light vision",0);
                // verthani race gets easily augmented, skin mimic, additional good skill
            }
            if (subtypes.contains("vesk")) {
                addSenseToAbilitySet("low-light vision",0);
                // vesk race gets armor savant, fearless, natural weapons
            }
            if (subtypes.contains("water")) {
                // swim speed
                // Athletics as master or good skill
            }
            if (subtypes.contains("ysoki")) {
                addSenseToAbilitySet("darkvision",60);
                // ysoki race gets cheek pouches, moxie, Engineering and Stealth as master skills, Survival as good skill
            }
        }
    }

    public Boolean hasAbilitiesByLocation(Location loc) {
        if (abilitySet == null)
            return false;
        return abilitySet.stream().anyMatch(a -> (a.getLocation() == loc));
    }
        
    public String makeAbilityStringByLocation(Location l) {
        List<String> abilitiesAtLocation = new ArrayList();
        abilitySet.stream().filter(a -> (a.getLocation() == l)).forEachOrdered(a -> abilitiesAtLocation.add(a.toString()));
        java.util.Collections.sort(abilitiesAtLocation);
        
        String abilityString = String.join(", ",abilitiesAtLocation);
        // replace ~c~
        //System.out.println("next line should handle ~c~");
        abilityString = abilityString.replace("~c~", Integer.toString(Integer.max(0, creature.getCR().getCRValue())));
        
        return abilityString;
    }
    
    public void updateStatBlock() {
        Boolean addSemicolon = false;
        
        updateArray();
        // update id/CR line
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
        }
        // update init/senses/perception line
        creatureSensesBlock.getChildren().add(new Text("\n"));
        creatureInitDisplay.setText("+0");
        creatureSensesBlock.getChildren().addAll(creatureInitLabel,creatureInitDisplay);
        creatureSensesBlock.getChildren().add(new Text("; "));
        if (hasAbilitiesByLocation(Location.SENSES)) {
            creatureSensesDisplay.setText(makeAbilityStringByLocation(Location.SENSES));
            creatureSensesBlock.getChildren().addAll(creatureSensesLabel,creatureSensesDisplay);
            creatureSensesBlock.getChildren().add(new Text("; "));
        }
        creaturePerceptionDisplay.setText(bonusString((array == null) ? 0 : array.goodSkillBonus));
        creatureSensesBlock.getChildren().addAll(creaturePerceptionLabel,creaturePerceptionDisplay);
        if (hasAbilitiesByLocation(Location.AURA)) {
            creatureSensesBlock.getChildren().add(new Text("\n"));
            creatureAuraDisplay.setText(makeAbilityStringByLocation(Location.AURA));
            creatureSensesBlock.getChildren().addAll(creatureAuraLabel,creatureAuraDisplay);
        }

        // update defenses block
        // RP goes here, sort of
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
        if (hasAbilitiesByLocation(Location.DEFENSIVE_ABILITIES)) {
            creatureDefensiveAbilitiesDisplay.setText(makeAbilityStringByLocation(Location.DEFENSIVE_ABILITIES));
            creatureDefensiveAbilitiesBlock.getChildren().addAll(creatureDefensiveAbilitiesLabel,creatureDefensiveAbilitiesDisplay);
            addSemicolon = true;
        }
        // DR goes here
        if (hasAbilitiesByLocation(Location.IMMUNITIES)) {
            if (addSemicolon)
                creatureDefensiveAbilitiesBlock.getChildren().add(new Text("; "));
            creatureImmunitiesDisplay.setText(makeAbilityStringByLocation(Location.IMMUNITIES));
            creatureDefensiveAbilitiesBlock.getChildren().addAll(creatureImmunitiesLabel,creatureImmunitiesDisplay);
            addSemicolon = true;
        }
        if (hasAbilitiesByLocation(Location.RESISTANCES)) {
            if (addSemicolon)
                creatureDefensiveAbilitiesBlock.getChildren().add(new Text("; "));
            creatureResistancesDisplay.setText(makeAbilityStringByLocation(Location.RESISTANCES));
            creatureDefensiveAbilitiesBlock.getChildren().addAll(creatureResistancesLabel,creatureResistancesDisplay);
            addSemicolon = true;
        }
        // SR goes here
        if (hasAbilitiesByLocation(Location.WEAKNESSES)) {
            if (addSemicolon)
                creatureDefensiveAbilitiesBlock.getChildren().add(new Text("; "));
            creatureWeaknessesDisplay.setText(makeAbilityStringByLocation(Location.WEAKNESSES));
            creatureDefensiveAbilitiesBlock.getChildren().addAll(creatureWeaknessesLabel,creatureWeaknessesDisplay);
        }
        
        //update offensive abilities block
        creatureOffensiveAbilitiesBlock.getChildren().clear();
        addSemicolon = false;
        // speed goes here
        // melee goes here
        // multiattack goes here
        // ranged goes here
        // space and reach go here
        if (hasAbilitiesByLocation(Location.OFFENSIVE_ABILITIES)) {
            // remove comment line after speed is in
            // creatureOffensiveAbilitiesBlock.getChildren().addAll(new Text("\n"));
            creatureOffensiveAbilitiesDisplay.setText(makeAbilityStringByLocation(Location.OFFENSIVE_ABILITIES));
            creatureOffensiveAbilitiesBlock.getChildren().addAll(creatureOffensiveAbilitiesLabel,creatureOffensiveAbilitiesDisplay);
        }
        // SLAs go here
        // spells known goes here

        //update statistics block
        creatureStatisticsBlock.getChildren().clear();
        // ability score modifiers go here
        // feats go here
        // skills go here
        if (hasAbilitiesByLocation(Location.LANGUAGES)) {
            // remove comment line after ability scores are in
            // creatureStatisticsBlock.getChildren().addAll(new Text("\n"));
            creatureLanguagesDisplay.setText(makeAbilityStringByLocation(Location.LANGUAGES));
            creatureStatisticsBlock.getChildren().addAll(creatureLanguagesLabel,creatureLanguagesDisplay);
        }
        if (hasAbilitiesByLocation(Location.OTHER_ABILITIES)) {
            creatureStatisticsBlock.getChildren().addAll(new Text("\n"));
            creatureOtherAbilitiesDisplay.setText(makeAbilityStringByLocation(Location.OTHER_ABILITIES));
            creatureStatisticsBlock.getChildren().addAll(creatureOtherAbilitiesLabel,creatureOtherAbilitiesDisplay);
        }
        // gear and augmentations go here
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
        aboutDialog.setContentText("Starfinder NPC/Alien Creator\nversion 1.3.0");
        aboutDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // set up Labels and TextFlows
        creatureInitLabel.setStyle("-fx-font-weight: bold");
        creatureSensesLabel.setStyle("-fx-font-weight: bold");
        creaturePerceptionLabel.setStyle("-fx-font-weight: bold");
        creatureRPLabel.setStyle("-fx-font-weight: bold");
        creatureAuraLabel.setStyle("-fx-font-weight: bold");
        creatureDefensiveAbilitiesLabel.setStyle("-fx-font-weight: bold");
        creatureDRLabel.setStyle("-fx-font-weight: bold");
        creatureImmunitiesLabel.setStyle("-fx-font-weight: bold");
        creatureResistancesLabel.setStyle("-fx-font-weight: bold");
        creatureSRLabel.setStyle("-fx-font-weight: bold");
        creatureWeaknessesLabel.setStyle("-fx-font-weight: bold");
        creatureSpeedLabel.setStyle("-fx-font-weight: bold");
        creatureOffensiveAbilitiesLabel.setStyle("-fx-font-weight: bold");
        creatureLanguagesLabel.setStyle("-fx-font-weight: bold");
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
            
            // abilityLine: id|output format|location|cost
            
            while (!"EOF".equals(abilityLine = bufferedReader.readLine())) {
                abilityParts = abilityLine.split("\\|",4);
                Ability.setOfAbilities.add(
                        new Ability(abilityParts[0],
                                Location.valueOf(abilityParts[2]),
                                abilityParts[1], 
                                Integer.valueOf(abilityParts[3])));
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
