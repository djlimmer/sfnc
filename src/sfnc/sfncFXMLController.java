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
import javafx.scene.control.Button;
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
import javafx.scene.control.Tab;
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
    
    List<String> generalSubtypes = new ArrayList<>();
    List<String> humanoidSubtypes = new ArrayList<>();
    List<String> outsiderSubtypes = new ArrayList<>();

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
        
        final int EXPORTLINEWIDTH = 76;
        Boolean addSemicolon = false;
        
        updateArray();
        // create id/CR line
        String nameLine = creature.getName();
        String CROutput = "CR " + creature.CR;
        // Needs Java 11, I think:  nameLine += " ".repeat(EXPORTLINEWIDTH - nameLine.length() - CROutput.length());
        int numSpaces = EXPORTLINEWIDTH - nameLine.length() - CROutput.length();
        for (int i=0; i < numSpaces; i++) {
            nameLine += " ";
        }
        nameLine += CROutput;

        // create XP line
        String xpLine = "XP " + creature.getXPString() + " (" + creature.getArray() + ")";
        // create type line
        String typeLine = "";
        typeLine += creature.getAlignment().getAbbrev() + " ";
        typeLine += creature.getSize() + " ";
        if (!"".equals(creature.getType())) {
            typeLine += creature.getType();
            List<String> subtypes = creature.getAllSubtypes();
            if (subtypes != null) {
                Collections.sort(subtypes);
                String subtypeDisplayString = String.join(", ",subtypes);
                if (!"".equals(subtypeDisplayString))
                    typeLine += " (" + subtypeDisplayString + ")";
            }
        }
        // create init/senses/perception line
        String sensesLine = "";
        sensesLine += "Init +0; ";
        if (hasAbilitiesByLocation(Location.SENSES))
            sensesLine += "Senses " + makeAbilityStringByLocation(Location.SENSES) + "; ";
        sensesLine += "Perception " + makeSkillBonusString(creature.perception);
        // create aura line
        String auraLine = "";
        if (hasAbilitiesByLocation(Location.AURA))
            auraLine += "Aura " + makeAbilityStringByLocation(Location.AURA);
        
        // create defense/HP/RP line
        String HPOutput = (array == null) ? "" : "HP " + array.hitPoints.toString();
        // RP goes here
        String defenseLine = "DEFENSE";
        numSpaces = EXPORTLINEWIDTH - defenseLine.length() - HPOutput.length();
        for (int i=0; i < numSpaces; i++) {
            defenseLine += " ";
        }
        defenseLine += HPOutput;
        // create AC line
        String ACLine = (array == null) ? "" : "EAC " + array.EAC.toString() + "; KAC " + array.KAC.toString();
        // create save line
        String SaveLine = (array == null) ? "" : "Fort " + bonusString(array.fort) + "; Ref " + bonusString(array.ref)
                + "; Will " + bonusString(array.will);
        // create defensive abilities line
        String defensiveAbilitiesLine = "";
        if (hasAbilitiesByLocation(Location.DEFENSIVE_ABILITIES)) {
            defensiveAbilitiesLine += "Defensive Abilities " + makeAbilityStringByLocation(Location.DEFENSIVE_ABILITIES);
            addSemicolon = true;
        }
        // DR goes here
        if (hasAbilitiesByLocation(Location.IMMUNITIES)) {
            if (addSemicolon)
                defensiveAbilitiesLine += "; ";
            defensiveAbilitiesLine += "Immunities " + makeAbilityStringByLocation(Location.IMMUNITIES);
            addSemicolon = true;
        }
        if (hasAbilitiesByLocation(Location.RESISTANCES)) {
            if (addSemicolon)
                defensiveAbilitiesLine += "; ";
            defensiveAbilitiesLine += "Resistances " + makeAbilityStringByLocation(Location.RESISTANCES);
            addSemicolon = true;
        }
        // SR goes here
        if (hasAbilitiesByLocation(Location.WEAKNESSES)) {
            if (addSemicolon)
                defensiveAbilitiesLine += "; ";
            defensiveAbilitiesLine += "Weaknesses " + makeAbilityStringByLocation(Location.WEAKNESSES);
        }
        
        // speed goes here
        // melee goes here
        // multiattack goes here
        // ranged goes here
        String spaceLine = "";
        // needs to handle tall vs. long reach, which means a UI dongle.
        // consider having the monster handle basic space and reach, and this function
        //  adds any modifiers
        if ((!creature.size.getSpace().equals("5 ft.")) || (creature.size.getReachTall() != 5)) {
            spaceLine += "Space " + creature.size.getSpace()
                    + "; " + "Reach " + creature.getReach() + " ft.";
        }
        addSemicolon = false;
        String offensiveAbilitiesLine = "";
        if (hasAbilitiesByLocation(Location.OFFENSIVE_ABILITIES)) {
            offensiveAbilitiesLine += "Offensive Abilities " + makeAbilityStringByLocation(Location.OFFENSIVE_ABILITIES);
        }
        // SLAs go here
        // Spells go here
        
        // ability score modifiers go here
        // feats go here
        String skillsLine = "";
        if (hasSkills()) {
            skillsLine += "Skills " + makeSkillsString();
        }
        String languagesLine = "";
        if (hasAbilitiesByLocation(Location.LANGUAGES)) {
            languagesLine += "Languages " + makeAbilityStringByLocation(Location.LANGUAGES);
        }
        String otherAbilitiesLine = "";
        if (hasAbilitiesByLocation(Location.OTHER_ABILITIES)) {
            languagesLine += "Other Abilities " + makeAbilityStringByLocation(Location.OTHER_ABILITIES);
        }
        // gear and augmentations go here
        
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
            writer.println(typeLine);
            if (!"".equals(sensesLine))
                writer.println(sensesLine);
            if (!"".equals(auraLine))
                writer.println(auraLine);
            writer.println(defenseLine);
            writer.println(ACLine);
            writer.println(SaveLine);
            if (!"".equals(defensiveAbilitiesLine))
                writer.println(defensiveAbilitiesLine);
            writer.println("OFFENSE");
            // writer.println(speedLine);
            // writer.println(meleeLine);
            // writer.println(multiattackLine);
            // writer.println(rangedLine);
             if (!"".equals(spaceLine))
                 writer.println(spaceLine);
            if (!"".equals(offensiveAbilitiesLine))
                writer.println(offensiveAbilitiesLine);
            // if (!"".equals(SLABlock)
            //  writer.println(SLABlock);
            // if (!"".equals(SpellsBlock)
            //  writer.println(SpellsBlock);
            // writer.println(abilityScoreLine);
            // if (!"".equals(featsLine)
            //  writer.println(featsLine);
            writer.println("STATISTICS");
            writer.println(skillsLine);
            if (!"".equals(languagesLine))
                writer.println(languagesLine);
            if (!"".equals(otherAbilitiesLine))
                writer.println(otherAbilitiesLine);
            // if (!"".equals(gearLine)
            //  writer.println(gearLine);

            writer.close();
            
        } catch (IOException e) {
            System.err.println("Something went wrong (exporting to text)");
        }
     }

    // Step 0 controls
    @FXML   private Tab step0 = new Tab();
    @FXML   private TextField creatureNameInput = new TextField();
    @FXML   private ComboBox creatureCRInput = new ComboBox();
    @FXML   private ComboBox creatureAlignmentInput = new ComboBox();
    @FXML   private ComboBox creatureSizeInput = new ComboBox();
    private ToggleGroup reachOptionsGroup = new ToggleGroup();
    @FXML   private RadioButton creatureTallReach = new RadioButton();
    @FXML   private RadioButton creatureLongReach = new RadioButton();

    // Step 1 controls
    @FXML   private Tab step1 = new Tab();
    @FXML   private ChoiceBox creatureArrayInput = new ChoiceBox();
    private ToggleGroup highStatGroup = new ToggleGroup();
    private ToggleGroup midStatGroup = new ToggleGroup();
    private ToggleGroup lowStatGroup = new ToggleGroup();
    @FXML   private RadioButton creatureHighStrength = new RadioButton();
    @FXML   private RadioButton creatureMidStrength = new RadioButton();
    @FXML   private RadioButton creatureLowStrength = new RadioButton();
    @FXML   private RadioButton creatureHighDexterity = new RadioButton();
    @FXML   private RadioButton creatureMidDexterity = new RadioButton();
    @FXML   private RadioButton creatureLowDexterity = new RadioButton();
    @FXML   private RadioButton creatureHighConstitution = new RadioButton();
    @FXML   private RadioButton creatureMidConstitution = new RadioButton();
    @FXML   private RadioButton creatureLowConstitution = new RadioButton();
    @FXML   private RadioButton creatureHighIntelligence = new RadioButton();
    @FXML   private RadioButton creatureMidIntelligence = new RadioButton();
    @FXML   private RadioButton creatureLowIntelligence = new RadioButton();
    @FXML   private RadioButton creatureHighWisdom = new RadioButton();
    @FXML   private RadioButton creatureMidWisdom = new RadioButton();
    @FXML   private RadioButton creatureLowWisdom = new RadioButton();
    @FXML   private RadioButton creatureHighCharisma = new RadioButton();
    @FXML   private RadioButton creatureMidCharisma = new RadioButton();
    @FXML   private RadioButton creatureLowCharisma = new RadioButton();
    @FXML   private TextField creatureStrengthCustomValue = new TextField();
    @FXML   private TextField creatureDexterityCustomValue = new TextField();
    @FXML   private TextField creatureConstitutionCustomValue = new TextField();
    @FXML   private TextField creatureIntelligenceCustomValue = new TextField();
    @FXML   private TextField creatureWisdomCustomValue = new TextField();
    @FXML   private TextField creatureCharismaCustomValue = new TextField();
    
    // Step 2 controls
    @FXML   private Tab step2 = new Tab();
    @FXML   private ComboBox creatureTypeInput = new ComboBox();
    @FXML   private CheckBox creatureTypeAdjustmentUse = new CheckBox();
    private ToggleGroup typeOptionsGroup = new ToggleGroup();
    @FXML   private RadioButton creatureTypeOption1 = new RadioButton();
    @FXML   private RadioButton creatureTypeOption2 = new RadioButton();
    @FXML   private RadioButton creatureTypeOption3 = new RadioButton();
    
    // Step 3 controls
    @FXML   private Tab step3 = new Tab();
    @FXML   private Label creatureSubtypeWarning = new Label();
    @FXML   private ListView<String> creatureGeneralSubtypesInput = new ListView<String>();
    @FXML   private ListView<String> creatureHumanoidSubtypesInput = new ListView<String>();
    @FXML   private ListView<String> creatureOutsiderSubtypesInput = new ListView<String>();
    @FXML   private TextField creatureFreeformSubtypesInput = new TextField();
    
    // Step 4 controls
    @FXML   private Tab step4 = new Tab();
    
    // Step 5 controls
    @FXML   private Tab step5 = new Tab();
    
    // Step 6 controls
    @FXML   private Tab step6 = new Tab();
    @FXML   private Label creatureAbilityChoicesAvailable = new Label();
    @FXML   private Label creatureAbilityChoicesMade = new Label();
    @FXML   private ListView<String> creatureAbilityInput = new ListView<String>();
    @FXML   private ListView<String> creatureAbilitiesChosen = new ListView<String>();
    @FXML   private TextField creatureCustomAbilityNameInput = new TextField();
    @FXML   private ComboBox creatureCustomAbilityLocationInput = new ComboBox();
    @FXML   private Button creatureAddCustomAbilityButton = new Button();

    // Step 7 controls
    @FXML   private Tab step7 = new Tab();
    @FXML   private Label creatureMasterSkillsAvailable = new Label();
    @FXML   private Label creatureMasterSkillsTaken = new Label();
    @FXML   private Label creatureGoodSkillsAvailable = new Label();
    @FXML   private Label creatureGoodSkillsTaken = new Label();
    private ToggleGroup creatureAcrobaticsGroup = new ToggleGroup();
    @FXML   private RadioButton creatureAcrobaticsMaster = new RadioButton();
    @FXML   private RadioButton creatureAcrobaticsGood = new RadioButton();
    @FXML   private RadioButton creatureAcrobaticsNone = new RadioButton();
    @FXML   private RadioButton creatureAcrobaticsCustom = new RadioButton();
    @FXML   private TextField creatureAcrobaticsCustomValue = new TextField();
    private ToggleGroup creatureAthleticsGroup = new ToggleGroup();
    @FXML   private RadioButton creatureAthleticsMaster = new RadioButton();
    @FXML   private RadioButton creatureAthleticsGood = new RadioButton();
    @FXML   private RadioButton creatureAthleticsNone = new RadioButton();
    @FXML   private RadioButton creatureAthleticsCustom = new RadioButton();
    @FXML   private TextField creatureAthleticsCustomValue = new TextField();
    private ToggleGroup creatureBluffGroup = new ToggleGroup();
    @FXML   private RadioButton creatureBluffMaster = new RadioButton();
    @FXML   private RadioButton creatureBluffGood = new RadioButton();
    @FXML   private RadioButton creatureBluffNone = new RadioButton();
    @FXML   private RadioButton creatureBluffCustom = new RadioButton();
    @FXML   private TextField creatureBluffCustomValue = new TextField();
    private ToggleGroup creatureComputersGroup = new ToggleGroup();
    @FXML   private RadioButton creatureComputersMaster = new RadioButton();
    @FXML   private RadioButton creatureComputersGood = new RadioButton();
    @FXML   private RadioButton creatureComputersNone = new RadioButton();
    @FXML   private RadioButton creatureComputersCustom = new RadioButton();
    @FXML   private TextField creatureComputersCustomValue = new TextField();
    private ToggleGroup creatureCultureGroup = new ToggleGroup();
    @FXML   private RadioButton creatureCultureMaster = new RadioButton();
    @FXML   private RadioButton creatureCultureGood = new RadioButton();
    @FXML   private RadioButton creatureCultureNone = new RadioButton();
    @FXML   private RadioButton creatureCultureCustom = new RadioButton();
    @FXML   private TextField creatureCultureCustomValue = new TextField();
    private ToggleGroup creatureDiplomacyGroup = new ToggleGroup();
    @FXML   private RadioButton creatureDiplomacyMaster = new RadioButton();
    @FXML   private RadioButton creatureDiplomacyGood = new RadioButton();
    @FXML   private RadioButton creatureDiplomacyNone = new RadioButton();
    @FXML   private RadioButton creatureDiplomacyCustom = new RadioButton();
    @FXML   private TextField creatureDiplomacyCustomValue = new TextField();
    private ToggleGroup creatureDisguiseGroup = new ToggleGroup();
    @FXML   private RadioButton creatureDisguiseMaster = new RadioButton();
    @FXML   private RadioButton creatureDisguiseGood = new RadioButton();
    @FXML   private RadioButton creatureDisguiseNone = new RadioButton();
    @FXML   private RadioButton creatureDisguiseCustom = new RadioButton();
    @FXML   private TextField creatureDisguiseCustomValue = new TextField();
    private ToggleGroup creatureEngineeringGroup = new ToggleGroup();
    @FXML   private RadioButton creatureEngineeringMaster = new RadioButton();
    @FXML   private RadioButton creatureEngineeringGood = new RadioButton();
    @FXML   private RadioButton creatureEngineeringNone = new RadioButton();
    @FXML   private RadioButton creatureEngineeringCustom = new RadioButton();
    @FXML   private TextField creatureEngineeringCustomValue = new TextField();
    private ToggleGroup creatureIntimidateGroup = new ToggleGroup();
    @FXML   private RadioButton creatureIntimidateMaster = new RadioButton();
    @FXML   private RadioButton creatureIntimidateGood = new RadioButton();
    @FXML   private RadioButton creatureIntimidateNone = new RadioButton();
    @FXML   private RadioButton creatureIntimidateCustom = new RadioButton();
    @FXML   private TextField creatureIntimidateCustomValue = new TextField();
    private ToggleGroup creatureLifeScienceGroup = new ToggleGroup();
    @FXML   private RadioButton creatureLifeScienceMaster = new RadioButton();
    @FXML   private RadioButton creatureLifeScienceGood = new RadioButton();
    @FXML   private RadioButton creatureLifeScienceNone = new RadioButton();
    @FXML   private RadioButton creatureLifeScienceCustom = new RadioButton();
    @FXML   private TextField creatureLifeScienceCustomValue = new TextField();
    private ToggleGroup creatureMedicineGroup = new ToggleGroup();
    @FXML   private RadioButton creatureMedicineMaster = new RadioButton();
    @FXML   private RadioButton creatureMedicineGood = new RadioButton();
    @FXML   private RadioButton creatureMedicineNone = new RadioButton();
    @FXML   private RadioButton creatureMedicineCustom = new RadioButton();
    @FXML   private TextField creatureMedicineCustomValue = new TextField();
    private ToggleGroup creatureMysticismGroup = new ToggleGroup();
    @FXML   private RadioButton creatureMysticismMaster = new RadioButton();
    @FXML   private RadioButton creatureMysticismGood = new RadioButton();
    @FXML   private RadioButton creatureMysticismNone = new RadioButton();
    @FXML   private RadioButton creatureMysticismCustom = new RadioButton();
    @FXML   private TextField creatureMysticismCustomValue = new TextField();
    private ToggleGroup creaturePerceptionGroup = new ToggleGroup();
    @FXML   private RadioButton creaturePerceptionMaster = new RadioButton();
    @FXML   private RadioButton creaturePerceptionGood = new RadioButton();
    @FXML   private RadioButton creaturePerceptionNone = new RadioButton();
    @FXML   private RadioButton creaturePerceptionCustom = new RadioButton();
    @FXML   private TextField creaturePerceptionCustomValue = new TextField();
    private ToggleGroup creaturePhysicalScienceGroup = new ToggleGroup();
    @FXML   private RadioButton creaturePhysicalScienceMaster = new RadioButton();
    @FXML   private RadioButton creaturePhysicalScienceGood = new RadioButton();
    @FXML   private RadioButton creaturePhysicalScienceNone = new RadioButton();
    @FXML   private RadioButton creaturePhysicalScienceCustom = new RadioButton();
    @FXML   private TextField creaturePhysicalScienceCustomValue = new TextField();
    private ToggleGroup creaturePilotingGroup = new ToggleGroup();
    @FXML   private RadioButton creaturePilotingMaster = new RadioButton();
    @FXML   private RadioButton creaturePilotingGood = new RadioButton();
    @FXML   private RadioButton creaturePilotingNone = new RadioButton();
    @FXML   private RadioButton creaturePilotingCustom = new RadioButton();
    @FXML   private TextField creaturePilotingCustomValue = new TextField();
    private ToggleGroup creatureProfessionGroup = new ToggleGroup();
    @FXML   private RadioButton creatureProfessionMaster = new RadioButton();
    @FXML   private RadioButton creatureProfessionGood = new RadioButton();
    @FXML   private RadioButton creatureProfessionNone = new RadioButton();
    @FXML   private RadioButton creatureProfessionCustom = new RadioButton();
    @FXML   private TextField creatureProfessionCustomValue = new TextField();
    private ToggleGroup creatureSenseMotiveGroup = new ToggleGroup();
    @FXML   private RadioButton creatureSenseMotiveMaster = new RadioButton();
    @FXML   private RadioButton creatureSenseMotiveGood = new RadioButton();
    @FXML   private RadioButton creatureSenseMotiveNone = new RadioButton();
    @FXML   private RadioButton creatureSenseMotiveCustom = new RadioButton();
    @FXML   private TextField creatureSenseMotiveCustomValue = new TextField();
    private ToggleGroup creatureSleightOfHandGroup = new ToggleGroup();
    @FXML   private RadioButton creatureSleightOfHandMaster = new RadioButton();
    @FXML   private RadioButton creatureSleightOfHandGood = new RadioButton();
    @FXML   private RadioButton creatureSleightOfHandNone = new RadioButton();
    @FXML   private RadioButton creatureSleightOfHandCustom = new RadioButton();
    @FXML   private TextField creatureSleightOfHandCustomValue = new TextField();
    private ToggleGroup creatureStealthGroup = new ToggleGroup();
    @FXML   private RadioButton creatureStealthMaster = new RadioButton();
    @FXML   private RadioButton creatureStealthGood = new RadioButton();
    @FXML   private RadioButton creatureStealthNone = new RadioButton();
    @FXML   private RadioButton creatureStealthCustom = new RadioButton();
    @FXML   private TextField creatureStealthCustomValue = new TextField();
    private ToggleGroup creatureSurvivalGroup = new ToggleGroup();
    @FXML   private RadioButton creatureSurvivalMaster = new RadioButton();
    @FXML   private RadioButton creatureSurvivalGood = new RadioButton();
    @FXML   private RadioButton creatureSurvivalNone = new RadioButton();
    @FXML   private RadioButton creatureSurvivalCustom = new RadioButton();
    @FXML   private TextField creatureSurvivalCustomValue = new TextField();

    // Step 8 controls
    @FXML   private Tab step8 = new Tab();
    
    // Step 9 controls
    @FXML   private Tab step9 = new Tab();
    
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
    private Label creatureSpaceLabel = new Label("Space ");
    private Label creatureSpaceDisplay = new Label();
    private Label creatureReachLabel = new Label("Reach ");
    private Label creatureReachDisplay = new Label();
    private Label creatureOffensiveAbilitiesLabel = new Label("Offensive Abilities ");
    private Label creatureOffensiveAbilitiesDisplay = new Label();
    // spell-like abilities
    // spells known
    @FXML   private TextFlow creatureStatisticsBlock = new TextFlow();
    @FXML   private Label creatureStrengthModifier = new Label("");
    @FXML   private Label creatureDexterityModifier = new Label("");
    @FXML   private Label creatureConstitutionModifier = new Label("");
    @FXML   private Label creatureIntelligenceModifier = new Label("");
    @FXML   private Label creatureWisdomModifier = new Label("");
    @FXML   private Label creatureCharismaModifier = new Label("");
    // ability score modifiers
    // feats
    private Label creatureSkillsLabel = new Label("Skills ");
    private Label creatureSkillsDisplay = new Label();
    private Label creatureLanguagesLabel = new Label("Languages ");
    private Label creatureLanguagesDisplay = new Label();
    private Label creatureOtherAbilitiesLabel = new Label("Other Abilities ");
    private Label creatureOtherAbilitiesDisplay = new Label();
    // gear and augmentations

    
    public void setControls() {
        // step 0
        creatureNameInput.setText(creature.getName());
        creatureCRInput.setValue(creature.getCRDisplayString());
        creatureAlignmentInput.setValue(creature.getAlignment());
        creatureSizeInput.setValue(creature.getSize());
        if (creature.hasLongReach())
            reachOptionsGroup.selectToggle(creatureLongReach);
        else
            reachOptionsGroup.selectToggle(creatureTallReach);
        // step 1
        creatureArrayInput.setValue(creature.getArray());
        switch(creature.getHighStat()) {
            case "strength":
                highStatGroup.selectToggle(creatureHighStrength);
                break;
            case "dexterity":
                highStatGroup.selectToggle(creatureHighDexterity);
                break;
            case "constitution":
                highStatGroup.selectToggle(creatureHighConstitution);
                break;
            case "intelligence":
                highStatGroup.selectToggle(creatureHighIntelligence);
                break;
            case "wisdom":
                highStatGroup.selectToggle(creatureHighWisdom);
                break;
            case "charisma":
                highStatGroup.selectToggle(creatureHighCharisma);
                break;
            default:
                highStatGroup.selectToggle(null);
        }
        switch(creature.getMidStat()) {
            case "strength":
                midStatGroup.selectToggle(creatureMidStrength);
                break;
            case "dexterity":
                midStatGroup.selectToggle(creatureMidDexterity);
                break;
            case "constitution":
                midStatGroup.selectToggle(creatureMidConstitution);
                break;
            case "intelligence":
                midStatGroup.selectToggle(creatureMidIntelligence);
                break;
            case "wisdom":
                midStatGroup.selectToggle(creatureMidWisdom);
                break;
            case "charisma":
                midStatGroup.selectToggle(creatureMidCharisma);
                break;
            default:
                midStatGroup.selectToggle(null);
        }
        switch(creature.getLowStat()) {
            case "strength":
                lowStatGroup.selectToggle(creatureLowStrength);
                break;
            case "dexterity":
                lowStatGroup.selectToggle(creatureLowDexterity);
                break;
            case "constitution":
                lowStatGroup.selectToggle(creatureLowConstitution);
                break;
            case "intelligence":
                lowStatGroup.selectToggle(creatureLowIntelligence);
                break;
            case "wisdom":
                lowStatGroup.selectToggle(creatureLowWisdom);
                break;
            case "charisma":
                lowStatGroup.selectToggle(creatureLowCharisma);
                break;
            default:
                lowStatGroup.selectToggle(null);
        }
        creatureStrengthCustomValue.setText(
                (creature.charisma.getAbilityModifierChoice() != AbilityModifierChoice.CUSTOM) ? 
                        "" : bonusString(creature.charisma.getCustomValue()));
        creatureDexterityCustomValue.setText(
                (creature.charisma.getAbilityModifierChoice() != AbilityModifierChoice.CUSTOM) ? 
                        "" : bonusString(creature.charisma.getCustomValue()));
        creatureConstitutionCustomValue.setText(
                (creature.charisma.getAbilityModifierChoice() != AbilityModifierChoice.CUSTOM) ? 
                        "" : bonusString(creature.charisma.getCustomValue()));
        creatureIntelligenceCustomValue.setText(
                (creature.charisma.getAbilityModifierChoice() != AbilityModifierChoice.CUSTOM) ? 
                        "" : bonusString(creature.charisma.getCustomValue()));
        creatureWisdomCustomValue.setText(
                (creature.charisma.getAbilityModifierChoice() != AbilityModifierChoice.CUSTOM) ? 
                        "" : bonusString(creature.charisma.getCustomValue()));
        creatureCharismaCustomValue.setText(
                (creature.charisma.getAbilityModifierChoice() != AbilityModifierChoice.CUSTOM) ? 
                        "" : bonusString(creature.charisma.getCustomValue()));
        // step 2
        creatureTypeInput.setValue(creature.getType());
        creatureTypeAdjustmentUse.setSelected(creature.useTypeAdjustments());
        switch (creature.getType()) {
            case "Animal":
                showAnimalTypeOptions();
                break;
            case "Humanoid":
            case "Outsider":
                showSaveBonusTypeOptions();
                break;
            default:
                hideTypeOptions();
                break;
        }
        // step 3
        setSubtypeWarning();
        creature.getGeneralSubtypes().stream().forEach(creatureGeneralSubtypesInput.getSelectionModel()::select);
        creature.getHumanoidSubtypes().stream().forEach(creatureHumanoidSubtypesInput.getSelectionModel()::select);
        creature.getOutsiderSubtypes().stream().forEach(creatureOutsiderSubtypesInput.getSelectionModel()::select);
        creatureFreeformSubtypesInput.setText(String.join(",",creature.getFreeformSubtypes()));
        // step 4
        // step 5
        // step 6
        setAbilityControls();
        // step 7
        setSkillControls();
        updateTabStatus();
    }
    
    private void updateTabStatus() {
        step1.setDisable((creature.getCR()==ChallengeRating.NONE) || (creature.getCR()==null));
        step2.setDisable((creature.getArray() == null) || (creature.getArray().equals("")) || step1.isDisable());
        step3.setDisable((creature.getType() == null) || (creature.getType().equals("")) || step2.isDisable());
        step6.setDisable((creature.getType() == null) || (creature.getType().equals("")) || step2.isDisable());
        step7.setDisable((creature.getType() == null) || (creature.getType().equals("")) || step2.isDisable());
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
    
    private void setAbilityControls() {
        creatureAbilityChoicesAvailable.setText(Integer.toString(array.specialAbilities));
        creatureAbilityChoicesMade.setText(Integer.toString(creature.getChosenAbilities().size()));
        List<String> chosenAbilitiesDisplay = new ArrayList<>();
        creature.getChosenAbilities().stream().forEach((a) -> {
            chosenAbilitiesDisplay.add(a.getId());
            creatureAbilityInput.getSelectionModel().select(a.getId());
        });
        creatureAbilitiesChosen.setItems(FXCollections.observableArrayList(
                chosenAbilitiesDisplay));
    }

    private Integer countMasterSkills() {
        Integer masterSkillCount = 0;
        
        masterSkillCount += (creature.acrobatics.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.athletics.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.bluff.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.computers.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.culture.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.diplomacy.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.disguise.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.engineering.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.intimidate.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.lifeScience.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.medicine.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.mysticism.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.perception.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.physicalScience.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.piloting.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.profession.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.senseMotive.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.sleightOfHand.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.stealth.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        masterSkillCount += (creature.survival.getSkillChoice() == SkillChoice.MASTER) ? 1 : 0;
        
        return masterSkillCount;
    }
    
    private Integer countGoodSkills() {
        Integer goodSkillCount = 0;
        
        goodSkillCount += (creature.acrobatics.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.athletics.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.bluff.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.computers.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.culture.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.diplomacy.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.disguise.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.engineering.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.intimidate.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.lifeScience.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.medicine.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.mysticism.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.physicalScience.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.piloting.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.profession.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.senseMotive.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.sleightOfHand.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.stealth.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        goodSkillCount += (creature.survival.getSkillChoice() == SkillChoice.GOOD) ? 1 : 0;
        
        return goodSkillCount;
    }
    
    private void setSkillControls() {
        creatureMasterSkillsAvailable.setText(Integer.toString(array.masterSkillNumber));
        creatureGoodSkillsAvailable.setText(Integer.toString(array.goodSkillNumber));
        creatureMasterSkillsTaken.setText(Integer.toString(countMasterSkills()));
        creatureGoodSkillsTaken.setText(Integer.toString(countGoodSkills()));
        
        if (creature.acrobatics == null) {
            creatureAcrobaticsGroup.selectToggle(creatureAcrobaticsNone);
            creatureAcrobaticsCustomValue.setText("");
        }
        else {
            switch(creature.acrobatics.getSkillChoice()) {
                case MASTER:
                    creatureAcrobaticsGroup.selectToggle(creatureAcrobaticsMaster);
                    break;
                case GOOD:
                    creatureAcrobaticsGroup.selectToggle(creatureAcrobaticsGood);
                    break;
                case NONE:
                    creatureAcrobaticsGroup.selectToggle(creatureAcrobaticsNone);
                    break;
                case CUSTOM:
                    creatureAcrobaticsGroup.selectToggle(creatureAcrobaticsCustom);
                    break;
            }
            creatureAcrobaticsCustomValue.setText(Integer.toString(creature.acrobatics.getCustomValue()));
        }
        if (creature.athletics == null) {
            creatureAthleticsGroup.selectToggle(creatureAthleticsNone);
            creatureAthleticsCustomValue.setText("");
        }
        else {
            switch(creature.athletics.getSkillChoice()) {
                case MASTER:
                    creatureAthleticsGroup.selectToggle(creatureAthleticsMaster);
                    break;
                case GOOD:
                    creatureAthleticsGroup.selectToggle(creatureAthleticsGood);
                    break;
                case NONE:
                    creatureAthleticsGroup.selectToggle(creatureAthleticsNone);
                    break;
                case CUSTOM:
                    creatureAthleticsGroup.selectToggle(creatureAthleticsCustom);
                    break;
            }
            creatureAthleticsCustomValue.setText(Integer.toString(creature.athletics.getCustomValue()));
        }
        if (creature.bluff == null) {
            creatureBluffGroup.selectToggle(creatureBluffNone);
            creatureBluffCustomValue.setText("");
        }
        else {
            switch(creature.bluff.getSkillChoice()) {
                case MASTER:
                    creatureBluffGroup.selectToggle(creatureBluffMaster);
                    break;
                case GOOD:
                    creatureBluffGroup.selectToggle(creatureBluffGood);
                    break;
                case NONE:
                    creatureBluffGroup.selectToggle(creatureBluffNone);
                    break;
                case CUSTOM:
                    creatureBluffGroup.selectToggle(creatureBluffCustom);
                    break;
            }
            creatureBluffCustomValue.setText(Integer.toString(creature.bluff.getCustomValue()));
        }
        if (creature.computers == null) {
            creatureComputersGroup.selectToggle(creatureComputersNone);
            creatureComputersCustomValue.setText("");
        }
        else {
            switch(creature.computers.getSkillChoice()) {
                case MASTER:
                    creatureComputersGroup.selectToggle(creatureComputersMaster);
                    break;
                case GOOD:
                    creatureComputersGroup.selectToggle(creatureComputersGood);
                    break;
                case NONE:
                    creatureComputersGroup.selectToggle(creatureComputersNone);
                    break;
                case CUSTOM:
                    creatureComputersGroup.selectToggle(creatureComputersCustom);
                    break;
            }
            creatureComputersCustomValue.setText(Integer.toString(creature.computers.getCustomValue()));
        }
        if (creature.culture == null) {
            creatureCultureGroup.selectToggle(creatureCultureNone);
            creatureCultureCustomValue.setText("");
        }
        else {
            switch(creature.culture.getSkillChoice()) {
                case MASTER:
                    creatureCultureGroup.selectToggle(creatureCultureMaster);
                    break;
                case GOOD:
                    creatureCultureGroup.selectToggle(creatureCultureGood);
                    break;
                case NONE:
                    creatureCultureGroup.selectToggle(creatureCultureNone);
                    break;
                case CUSTOM:
                    creatureCultureGroup.selectToggle(creatureCultureCustom);
                    break;
            }
            creatureCultureCustomValue.setText(Integer.toString(creature.culture.getCustomValue()));
        }
        if (creature.diplomacy == null) {
            creatureDiplomacyGroup.selectToggle(creatureDiplomacyNone);
            creatureDiplomacyCustomValue.setText("");
        }
        else {
            switch(creature.diplomacy.getSkillChoice()) {
                case MASTER:
                    creatureDiplomacyGroup.selectToggle(creatureDiplomacyMaster);
                    break;
                case GOOD:
                    creatureDiplomacyGroup.selectToggle(creatureDiplomacyGood);
                    break;
                case NONE:
                    creatureDiplomacyGroup.selectToggle(creatureDiplomacyNone);
                    break;
                case CUSTOM:
                    creatureDiplomacyGroup.selectToggle(creatureDiplomacyCustom);
                    break;
            }
            creatureDiplomacyCustomValue.setText(Integer.toString(creature.diplomacy.getCustomValue()));
        }
        if (creature.disguise == null) {
            creatureDisguiseGroup.selectToggle(creatureDisguiseNone);
            creatureDisguiseCustomValue.setText("");
        }
        else {
            switch(creature.disguise.getSkillChoice()) {
                case MASTER:
                    creatureDisguiseGroup.selectToggle(creatureDisguiseMaster);
                    break;
                case GOOD:
                    creatureDisguiseGroup.selectToggle(creatureDisguiseGood);
                    break;
                case NONE:
                    creatureDisguiseGroup.selectToggle(creatureDisguiseNone);
                    break;
                case CUSTOM:
                    creatureDisguiseGroup.selectToggle(creatureDisguiseCustom);
                    break;
            }
            creatureDisguiseCustomValue.setText(Integer.toString(creature.disguise.getCustomValue()));
        }
        if (creature.engineering == null) {
            creatureEngineeringGroup.selectToggle(creatureEngineeringNone);
            creatureEngineeringCustomValue.setText("");
        }
        else {
            switch(creature.engineering.getSkillChoice()) {
                case MASTER:
                    creatureEngineeringGroup.selectToggle(creatureEngineeringMaster);
                    break;
                case GOOD:
                    creatureEngineeringGroup.selectToggle(creatureEngineeringGood);
                    break;
                case NONE:
                    creatureEngineeringGroup.selectToggle(creatureEngineeringNone);
                    break;
                case CUSTOM:
                    creatureEngineeringGroup.selectToggle(creatureEngineeringCustom);
                    break;
            }
            creatureEngineeringCustomValue.setText(Integer.toString(creature.engineering.getCustomValue()));
        }
        if (creature.intimidate == null) {
            creatureIntimidateGroup.selectToggle(creatureIntimidateNone);
            creatureIntimidateCustomValue.setText("");
        }
        else {
            switch(creature.intimidate.getSkillChoice()) {
                case MASTER:
                    creatureIntimidateGroup.selectToggle(creatureIntimidateMaster);
                    break;
                case GOOD:
                    creatureIntimidateGroup.selectToggle(creatureIntimidateGood);
                    break;
                case NONE:
                    creatureIntimidateGroup.selectToggle(creatureIntimidateNone);
                    break;
                case CUSTOM:
                    creatureIntimidateGroup.selectToggle(creatureIntimidateCustom);
                    break;
            }
            creatureIntimidateCustomValue.setText(Integer.toString(creature.intimidate.getCustomValue()));
        }
        if (creature.lifeScience == null) {
            creatureLifeScienceGroup.selectToggle(creatureLifeScienceNone);
            creatureLifeScienceCustomValue.setText("");
        }
        else {
            switch(creature.lifeScience.getSkillChoice()) {
                case MASTER:
                    creatureLifeScienceGroup.selectToggle(creatureLifeScienceMaster);
                    break;
                case GOOD:
                    creatureLifeScienceGroup.selectToggle(creatureLifeScienceGood);
                    break;
                case NONE:
                    creatureLifeScienceGroup.selectToggle(creatureLifeScienceNone);
                    break;
                case CUSTOM:
                    creatureLifeScienceGroup.selectToggle(creatureLifeScienceCustom);
                    break;
            }
            creatureLifeScienceCustomValue.setText(Integer.toString(creature.lifeScience.getCustomValue()));
        }
        if (creature.medicine == null) {
            creatureMedicineGroup.selectToggle(creatureMedicineNone);
            creatureMedicineCustomValue.setText("");
        }
        else {
            switch(creature.medicine.getSkillChoice()) {
                case MASTER:
                    creatureMedicineGroup.selectToggle(creatureMedicineMaster);
                    break;
                case GOOD:
                    creatureMedicineGroup.selectToggle(creatureMedicineGood);
                    break;
                case NONE:
                    creatureMedicineGroup.selectToggle(creatureMedicineNone);
                    break;
                case CUSTOM:
                    creatureMedicineGroup.selectToggle(creatureMedicineCustom);
                    break;
            }
            creatureMedicineCustomValue.setText(Integer.toString(creature.medicine.getCustomValue()));
        }
        if (creature.mysticism == null) {
            creatureMysticismGroup.selectToggle(creatureMysticismNone);
            creatureMysticismCustomValue.setText("");
        }
        else {
            switch(creature.mysticism.getSkillChoice()) {
                case MASTER:
                    creatureMysticismGroup.selectToggle(creatureMysticismMaster);
                    break;
                case GOOD:
                    creatureMysticismGroup.selectToggle(creatureMysticismGood);
                    break;
                case NONE:
                    creatureMysticismGroup.selectToggle(creatureMysticismNone);
                    break;
                case CUSTOM:
                    creatureMysticismGroup.selectToggle(creatureMysticismCustom);
                    break;
            }
            creatureMysticismCustomValue.setText(Integer.toString(creature.mysticism.getCustomValue()));
        }
        if (creature.perception == null) {
            creaturePerceptionGroup.selectToggle(creaturePerceptionNone);
            creaturePerceptionCustomValue.setText("");
        }
        else {
            switch(creature.perception.getSkillChoice()) {
                case MASTER:
                    creaturePerceptionGroup.selectToggle(creaturePerceptionMaster);
                    break;
                case GOOD:
                    creaturePerceptionGroup.selectToggle(creaturePerceptionGood);
                    break;
                case NONE:
                    creaturePerceptionGroup.selectToggle(creaturePerceptionNone);
                    break;
                case CUSTOM:
                    creaturePerceptionGroup.selectToggle(creaturePerceptionCustom);
                    break;
            }
            creaturePerceptionCustomValue.setText(Integer.toString(creature.perception.getCustomValue()));
        }
        if (creature.physicalScience == null) {
            creaturePhysicalScienceGroup.selectToggle(creaturePhysicalScienceNone);
            creaturePhysicalScienceCustomValue.setText("");
        }
        else {
            switch(creature.physicalScience.getSkillChoice()) {
                case MASTER:
                    creaturePhysicalScienceGroup.selectToggle(creaturePhysicalScienceMaster);
                    break;
                case GOOD:
                    creaturePhysicalScienceGroup.selectToggle(creaturePhysicalScienceGood);
                    break;
                case NONE:
                    creaturePhysicalScienceGroup.selectToggle(creaturePhysicalScienceNone);
                    break;
                case CUSTOM:
                    creaturePhysicalScienceGroup.selectToggle(creaturePhysicalScienceCustom);
                    break;
            }
            creaturePhysicalScienceCustomValue.setText(Integer.toString(creature.physicalScience.getCustomValue()));
        }
        if (creature.piloting == null) {
            creaturePilotingGroup.selectToggle(creaturePilotingNone);
            creaturePilotingCustomValue.setText("");
        }
        else {
            switch(creature.piloting.getSkillChoice()) {
                case MASTER:
                    creaturePilotingGroup.selectToggle(creaturePilotingMaster);
                    break;
                case GOOD:
                    creaturePilotingGroup.selectToggle(creaturePilotingGood);
                    break;
                case NONE:
                    creaturePilotingGroup.selectToggle(creaturePilotingNone);
                    break;
                case CUSTOM:
                    creaturePilotingGroup.selectToggle(creaturePilotingCustom);
                    break;
            }
            creaturePilotingCustomValue.setText(Integer.toString(creature.piloting.getCustomValue()));
        }
        if (creature.profession == null) {
            creatureProfessionGroup.selectToggle(creatureProfessionNone);
            creatureProfessionCustomValue.setText("");
        }
        else {
            switch(creature.profession.getSkillChoice()) {
                case MASTER:
                    creatureProfessionGroup.selectToggle(creatureProfessionMaster);
                    break;
                case GOOD:
                    creatureProfessionGroup.selectToggle(creatureProfessionGood);
                    break;
                case NONE:
                    creatureProfessionGroup.selectToggle(creatureProfessionNone);
                    break;
                case CUSTOM:
                    creatureProfessionGroup.selectToggle(creatureProfessionCustom);
                    break;
            }
            creatureProfessionCustomValue.setText(Integer.toString(creature.profession.getCustomValue()));
        }
        if (creature.senseMotive == null) {
            creatureSenseMotiveGroup.selectToggle(creatureSenseMotiveNone);
            creatureSenseMotiveCustomValue.setText("");
        }
        else {
            switch(creature.senseMotive.getSkillChoice()) {
                case MASTER:
                    creatureSenseMotiveGroup.selectToggle(creatureSenseMotiveMaster);
                    break;
                case GOOD:
                    creatureSenseMotiveGroup.selectToggle(creatureSenseMotiveGood);
                    break;
                case NONE:
                    creatureSenseMotiveGroup.selectToggle(creatureSenseMotiveNone);
                    break;
                case CUSTOM:
                    creatureSenseMotiveGroup.selectToggle(creatureSenseMotiveCustom);
                    break;
            }
            creatureSenseMotiveCustomValue.setText(Integer.toString(creature.senseMotive.getCustomValue()));
        }
        if (creature.sleightOfHand == null) {
            creatureSleightOfHandGroup.selectToggle(creatureSleightOfHandNone);
            creatureSleightOfHandCustomValue.setText("");
        }
        else {
            switch(creature.sleightOfHand.getSkillChoice()) {
                case MASTER:
                    creatureSleightOfHandGroup.selectToggle(creatureSleightOfHandMaster);
                    break;
                case GOOD:
                    creatureSleightOfHandGroup.selectToggle(creatureSleightOfHandGood);
                    break;
                case NONE:
                    creatureSleightOfHandGroup.selectToggle(creatureSleightOfHandNone);
                    break;
                case CUSTOM:
                    creatureSleightOfHandGroup.selectToggle(creatureSleightOfHandCustom);
                    break;
            }
            creatureSleightOfHandCustomValue.setText(Integer.toString(creature.sleightOfHand.getCustomValue()));
        }
        if (creature.stealth == null) {
            creatureStealthGroup.selectToggle(creatureStealthNone);
            creatureStealthCustomValue.setText("");
        }
        else {
            switch(creature.stealth.getSkillChoice()) {
                case MASTER:
                    creatureStealthGroup.selectToggle(creatureStealthMaster);
                    break;
                case GOOD:
                    creatureStealthGroup.selectToggle(creatureStealthGood);
                    break;
                case NONE:
                    creatureStealthGroup.selectToggle(creatureStealthNone);
                    break;
                case CUSTOM:
                    creatureStealthGroup.selectToggle(creatureStealthCustom);
                    break;
            }
            creatureStealthCustomValue.setText(Integer.toString(creature.stealth.getCustomValue()));
        }
        if (creature.survival == null) {
            creatureSurvivalGroup.selectToggle(creatureSurvivalNone);
            creatureSurvivalCustomValue.setText("");
        }
        else {
            switch(creature.survival.getSkillChoice()) {
                case MASTER:
                    creatureSurvivalGroup.selectToggle(creatureSurvivalMaster);
                    break;
                case GOOD:
                    creatureSurvivalGroup.selectToggle(creatureSurvivalGood);
                    break;
                case NONE:
                    creatureSurvivalGroup.selectToggle(creatureSurvivalNone);
                    break;
                case CUSTOM:
                    creatureSurvivalGroup.selectToggle(creatureSurvivalCustom);
                    break;
            }
            creatureSurvivalCustomValue.setText(Integer.toString(creature.survival.getCustomValue()));
        }
    }
    
    private void addSenseToAbilitySet(String senseName,Integer range) {
        Optional<Ability> optionalSense = abilitySet.stream()
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

    private void addSenseToAbilitySet(String senseName) {
        Optional<Ability> optionalSense = abilitySet.stream()
                .filter((Ability a) -> a.getId().equals(senseName))
                .findAny();
        if (!optionalSense.isPresent())
            abilitySet.add(new Sense(senseName));
    }
    
    private void addImmunityToAbilitySet(String name) {
        Optional<Ability> optionalImmunity = abilitySet.stream()
                .filter((Ability a) -> a.getId().equals(name))
                .findAny();
        if (!optionalImmunity.isPresent())
            abilitySet.add(new Immunity(name));
    }
    
    private void addResistanceToAbilitySet(String resistanceName,Integer value) {
        Optional<Ability> optionalResistance = abilitySet.stream()
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
                    addSenseToAbilitySet("low-light vision");
                    if (useTypeAdjustments) {
                        array.fort += 2;
                        array.ref += 2;
                    }
                    break;
                case "Construct":
                    addSenseToAbilitySet("darkvision",60);
                    addSenseToAbilitySet("low-light vision");
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
                    addSenseToAbilitySet("low-light vision");
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
                    addSenseToAbilitySet("low-light vision");
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
                    addSenseToAbilitySet("low-light vision");
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
                    addSenseToAbilitySet("sightless");
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
                    addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
                abilitySet.add(Ability.getAbility("aura of menace"));
                abilitySet.add(Ability.getAbility("+4 vs. poison"));
                addImmunityToAbilitySet("electricity");
                addImmunityToAbilitySet("petrification");
                abilitySet.add(Ability.getAbility("truespeech"));
                // many get teleport as an at-will SLA (CL = CR)
            }
            if (subtypes.contains("azata")) {
                addSenseToAbilitySet("darkvision",60);
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("see in darkness");
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
                addSenseToAbilitySet("low-light vision");
                // many gain Intimidate and Perception as master skills
            }
            if (subtypes.contains("gnome")) {
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
                // reptoid race gets change shape, cold-blooded, natural weapons
            }
            if (subtypes.contains("ryphorian")) {
                addSenseToAbilitySet("low-light vision");
                // ryphorian race gets trimorphic, additional special ability, Perception as master skill
            }
            if (subtypes.contains("sarcesian")) {
                addSenseToAbilitySet("low-light vision");
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
                addSenseToAbilitySet("low-light vision");
                // skittermander race gets grappler, hyper, six-armed
            }
            if (subtypes.contains("swarm")) {
                abilitySet.add(Ability.getAbility("swarm defenses"));
                addImmunityToAbilitySet("swarm immunities");
                abilitySet.add(Ability.getAbility("distraction"));
                abilitySet.add(Ability.getAbility("swarm attack"));
            }
            if (subtypes.contains("verthani")) {
                addSenseToAbilitySet("low-light vision");
                // verthani race gets easily augmented, skin mimic, additional good skill
            }
            if (subtypes.contains("vesk")) {
                addSenseToAbilitySet("low-light vision");
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
            abilitySet.addAll(creature.getChosenAbilities());
            
            // handle abilities that modify the array
            if (abilitySet.contains(Ability.getAbility("brute")))
                // no effect until attacks are in place
                ;
            if (hasAbilityByID("extra hit points"))
                array.hitPoints += array.hitPoints/5;
            if (hasAbilityByID("save boost (+1 to all)")) {
                array.fort += 1;
                array.ref += 1;
                array.will += 1;
            }
            if (hasAbilityByID("save boost (+3 fort)"))
                array.fort += 3;
            if (hasAbilityByID("save boost (+3 ref)"))
                array.ref += 3;
            if (hasAbilityByID("save boost (+3 will)"))
                array.will += 3;
            if (hasAbilityByID("skillful")) {
                array.goodSkillBonus += 1;
                array.masterSkillBonus += 1;
            }
        }
    }

    public Boolean hasAbilityByID(String id) {
        if (abilitySet == null)
            return false;
        return abilitySet.stream().anyMatch(a -> (a.getId().equals(id)));
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
        abilityString = abilityString.replace("~c~", Integer.toString(Integer.max(0, creature.getCR().getCRValue())));
        
        return abilityString;
    }
    
    public Boolean isSkillVisible(Skill s) {
        return(s.getSkillChoice() != SkillChoice.NONE);
    }
    
    private Integer getSkillValue(Skill s) {
        if(array == null)
            return 0;
        if(s.skillChoice == SkillChoice.MASTER)
            return array.masterSkillBonus;
        if (s.skillChoice == SkillChoice.GOOD)
            return array.goodSkillBonus;
        if (s.skillChoice == SkillChoice.CUSTOM)
            return s.getCustomValue();
        
        return 0;
    }
    
    public String makeSkillBonusString(Skill s) {
        return bonusString(getSkillValue(s));
    }
    
    public Boolean hasSkills() {
        return ((isSkillVisible(creature.acrobatics))
                || (isSkillVisible(creature.athletics))
                || (isSkillVisible(creature.bluff))
                || (isSkillVisible(creature.computers))
                || (isSkillVisible(creature.culture))
                || (isSkillVisible(creature.diplomacy))
                || (isSkillVisible(creature.disguise))
                || (isSkillVisible(creature.engineering))
                || (isSkillVisible(creature.intimidate))
                || (isSkillVisible(creature.lifeScience))
                || (isSkillVisible(creature.medicine))
                || (isSkillVisible(creature.mysticism))
                || (isSkillVisible(creature.physicalScience))
                || (isSkillVisible(creature.piloting))
                || (isSkillVisible(creature.profession))
                || (isSkillVisible(creature.senseMotive))
                || (isSkillVisible(creature.sleightOfHand))
                || (isSkillVisible(creature.stealth))
                || (isSkillVisible(creature.survival))
                );
    }
    
    public String makeSkillsString() {
        String skillsString = "";
        Boolean addComma = false;
        
        if (isSkillVisible(creature.acrobatics)) {
            skillsString += "Acrobatics " + makeSkillBonusString(creature.acrobatics);
            addComma = true;
        }
        if (isSkillVisible(creature.athletics)) {
            if (addComma) skillsString += ", ";
            skillsString += "Athletics " + makeSkillBonusString(creature.athletics);
            addComma = true;
        }
        if (isSkillVisible(creature.bluff)) {
            if (addComma) skillsString += ", ";
            skillsString += "Bluff " + makeSkillBonusString(creature.bluff);
            addComma = true;
        }
        if (isSkillVisible(creature.computers)) {
            if (addComma) skillsString += ", ";
            skillsString += "Computers " + makeSkillBonusString(creature.computers);
            addComma = true;
        }
        if (isSkillVisible(creature.culture)) {
            if (addComma) skillsString += ", ";
            skillsString += "Culture " + makeSkillBonusString(creature.culture);
            addComma = true;
        }
        if (isSkillVisible(creature.diplomacy)) {
            if (addComma) skillsString += ", ";
            skillsString += "Diplomacy " + makeSkillBonusString(creature.diplomacy);
            addComma = true;
        }
        if (isSkillVisible(creature.disguise)) {
            if (addComma) skillsString += ", ";
            skillsString += "Disguise " + makeSkillBonusString(creature.disguise);
            addComma = true;
        }
        if (isSkillVisible(creature.engineering)) {
            if (addComma) skillsString += ", ";
            skillsString += "Engineering " + makeSkillBonusString(creature.engineering);
            addComma = true;
        }
        if (isSkillVisible(creature.intimidate)) {
            if (addComma) skillsString += ", ";
            skillsString += "Intimidate " + makeSkillBonusString(creature.intimidate);
            addComma = true;
        }
        if (isSkillVisible(creature.lifeScience)) {
            if (addComma) skillsString += ", ";
            skillsString += "Life Science " + makeSkillBonusString(creature.lifeScience);
            addComma = true;
        }
        if (isSkillVisible(creature.medicine)) {
            if (addComma) skillsString += ", ";
            skillsString += "Medicine " + makeSkillBonusString(creature.medicine);
            addComma = true;
        }
        if (isSkillVisible(creature.mysticism)) {
            if (addComma) skillsString += ", ";
            skillsString += "Mysticism " + makeSkillBonusString(creature.mysticism);
            addComma = true;
        }
        if (isSkillVisible(creature.physicalScience)) {
            if (addComma) skillsString += ", ";
            skillsString += "Physical Science " + makeSkillBonusString(creature.physicalScience);
            addComma = true;
        }
        if (isSkillVisible(creature.piloting)) {
            if (addComma) skillsString += ", ";
            skillsString += "Piloting " + makeSkillBonusString(creature.piloting);
            addComma = true;
        }
        if (isSkillVisible(creature.profession)) {
            if (addComma) skillsString += ", ";
            skillsString += "Profession " + makeSkillBonusString(creature.profession);
            addComma = true;
        }
        if (isSkillVisible(creature.senseMotive)) {
            if (addComma) skillsString += ", ";
            skillsString += "Sense Motive " + makeSkillBonusString(creature.senseMotive);
            addComma = true;
        }
        if (isSkillVisible(creature.sleightOfHand)) {
            if (addComma) skillsString += ", ";
            skillsString += "Sleight of Hand " + makeSkillBonusString(creature.sleightOfHand);
            addComma = true;
        }
        if (isSkillVisible(creature.stealth)) {
            if (addComma) skillsString += ", ";
            skillsString += "Stealth " + makeSkillBonusString(creature.stealth);
            addComma = true;
        }
        if (isSkillVisible(creature.survival)) {
            if (addComma) skillsString += ", ";
            skillsString += "Survival " + makeSkillBonusString(creature.survival);
            addComma = true;
        }
        
        return skillsString;
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
            String typeDisplayString = creature.alignment.getAbbrev() + " " 
                    + creature.getSize() + " "
                    + creature.getType();
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
        creaturePerceptionDisplay.setText(makeSkillBonusString(creature.perception));
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
        Boolean addNewLine = false;
        // speed goes here
        // melee goes here
        // multiattack goes here
        // ranged goes here
        if ((!creature.size.getSpace().equals("5 ft.")) || (creature.getReach() != 5)) {
            if (addNewLine)
                creatureOffensiveAbilitiesBlock.getChildren().addAll(new Text("\n"));
            creatureSpaceDisplay.setText(creature.size.getSpace());
            creatureReachDisplay.setText(Integer.toString(creature.getReach())+" ft.");
            creatureOffensiveAbilitiesBlock.getChildren().addAll(
                    creatureSpaceLabel, creatureSpaceDisplay,
                    new Text("; "),
                    creatureReachLabel, creatureReachDisplay
            );
            addNewLine = true;
        }
        if (hasAbilitiesByLocation(Location.OFFENSIVE_ABILITIES)) {
            if (addNewLine)
                creatureOffensiveAbilitiesBlock.getChildren().addAll(new Text("\n"));
            creatureOffensiveAbilitiesDisplay.setText(makeAbilityStringByLocation(Location.OFFENSIVE_ABILITIES));
            creatureOffensiveAbilitiesBlock.getChildren().addAll(creatureOffensiveAbilitiesLabel,creatureOffensiveAbilitiesDisplay);
        }
        // SLAs go here
        // spells known goes here

        //update statistics block
        creatureStatisticsBlock.getChildren().clear();
        String statMod;
        switch(creature.strength.getAbilityModifierChoice()) {
            case HIGH: statMod = bonusString(array.abilityScoreModifier1); break;
            case MID: statMod = bonusString(array.abilityScoreModifier2); break;
            case LOW: statMod = bonusString(array.abilityScoreModifier3); break;
            case CUSTOM: statMod = bonusString(creature.getStrength().getCustomValue()); break;
            default: statMod = "+0";
        }
        creatureStrengthModifier.setText(statMod);
        switch(creature.dexterity.getAbilityModifierChoice()) {
            case HIGH: statMod = bonusString(array.abilityScoreModifier1); break;
            case MID: statMod = bonusString(array.abilityScoreModifier2); break;
            case LOW: statMod = bonusString(array.abilityScoreModifier3); break;
            case CUSTOM: statMod = bonusString(creature.getStrength().getCustomValue()); break;
            default: statMod = "+0";
        }
        creatureDexterityModifier.setText(statMod);
        switch(creature.constitution.getAbilityModifierChoice()) {
            case HIGH: statMod = bonusString(array.abilityScoreModifier1); break;
            case MID: statMod = bonusString(array.abilityScoreModifier2); break;
            case LOW: statMod = bonusString(array.abilityScoreModifier3); break;
            case CUSTOM: statMod = bonusString(creature.getStrength().getCustomValue()); break;
            default: statMod = "+0";
        }
        creatureConstitutionModifier.setText(statMod);
        switch(creature.intelligence.getAbilityModifierChoice()) {
            case HIGH: statMod = bonusString(array.abilityScoreModifier1); break;
            case MID: statMod = bonusString(array.abilityScoreModifier2); break;
            case LOW: statMod = bonusString(array.abilityScoreModifier3); break;
            case CUSTOM: statMod = bonusString(creature.getStrength().getCustomValue()); break;
            default: statMod = "+0";
        }
        creatureIntelligenceModifier.setText(statMod);
        switch(creature.wisdom.getAbilityModifierChoice()) {
            case HIGH: statMod = bonusString(array.abilityScoreModifier1); break;
            case MID: statMod = bonusString(array.abilityScoreModifier2); break;
            case LOW: statMod = bonusString(array.abilityScoreModifier3); break;
            case CUSTOM: statMod = bonusString(creature.getStrength().getCustomValue()); break;
            default: statMod = "+0";
        }
        creatureWisdomModifier.setText(statMod);
        switch(creature.charisma.getAbilityModifierChoice()) {
            case HIGH: statMod = bonusString(array.abilityScoreModifier1); break;
            case MID: statMod = bonusString(array.abilityScoreModifier2); break;
            case LOW: statMod = bonusString(array.abilityScoreModifier3); break;
            case CUSTOM: statMod = bonusString(creature.getStrength().getCustomValue()); break;
            default: statMod = "+0";
        }
        creatureCharismaModifier.setText(statMod);
        // feats go here
        if (hasSkills()) {
            // remove comment line after ability scores are in
            // creatureStatisticsBlock.getChildren().addAll(new Text("\n"));
            creatureSkillsDisplay.setText(makeSkillsString());
            creatureStatisticsBlock.getChildren().addAll(creatureSkillsLabel,creatureSkillsDisplay);
        }
        if (hasAbilitiesByLocation(Location.LANGUAGES)) {
            creatureStatisticsBlock.getChildren().addAll(new Text("\n"));
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
    
    public void updateAbilityModifiers() {
        // look through the controls and update ability modifiers as appropriate
        AbilityModifier stat = creature.getStrength();
        try {
            Integer customScore = Integer.valueOf(creatureStrengthCustomValue.getText());
            stat.setAbilityModifierChoice(AbilityModifierChoice.CUSTOM);
            stat.setCustomValue(customScore);
        } catch (NumberFormatException e) {
            if (creatureHighStrength.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.HIGH);
            else if (creatureMidStrength.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.MID);
            else if (creatureLowStrength.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.LOW);
            else
                stat.setAbilityModifierChoice(AbilityModifierChoice.NONE);
        }
        stat = creature.getDexterity();
        try {
            Integer customScore = Integer.valueOf(creatureDexterityCustomValue.getText());
            stat.setAbilityModifierChoice(AbilityModifierChoice.CUSTOM);
            stat.setCustomValue(customScore);
        } catch (NumberFormatException e) {
            if (creatureHighDexterity.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.HIGH);
            else if (creatureMidDexterity.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.MID);
            else if (creatureLowDexterity.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.LOW);
            else
                stat.setAbilityModifierChoice(AbilityModifierChoice.NONE);
        }
        stat = creature.getConstitution();
        try {
            Integer customScore = Integer.valueOf(creatureConstitutionCustomValue.getText());
            stat.setAbilityModifierChoice(AbilityModifierChoice.CUSTOM);
            stat.setCustomValue(customScore);
        } catch (NumberFormatException e) {
            if (creatureHighConstitution.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.HIGH);
            else if (creatureMidConstitution.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.MID);
            else if (creatureLowConstitution.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.LOW);
            else
                stat.setAbilityModifierChoice(AbilityModifierChoice.NONE);
        }
        stat = creature.getIntelligence();
        try {
            Integer customScore = Integer.valueOf(creatureIntelligenceCustomValue.getText());
            stat.setAbilityModifierChoice(AbilityModifierChoice.CUSTOM);
            stat.setCustomValue(customScore);
        } catch (NumberFormatException e) {
            if (creatureHighIntelligence.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.HIGH);
            else if (creatureMidIntelligence.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.MID);
            else if (creatureLowIntelligence.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.LOW);
            else
                stat.setAbilityModifierChoice(AbilityModifierChoice.NONE);
        }
        stat = creature.getWisdom();
        try {
            Integer customScore = Integer.valueOf(creatureWisdomCustomValue.getText());
            stat.setAbilityModifierChoice(AbilityModifierChoice.CUSTOM);
            stat.setCustomValue(customScore);
        } catch (NumberFormatException e) {
            if (creatureHighWisdom.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.HIGH);
            else if (creatureMidWisdom.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.MID);
            else if (creatureLowWisdom.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.LOW);
            else
                stat.setAbilityModifierChoice(AbilityModifierChoice.NONE);
        }
        stat = creature.getCharisma();
        try {
            Integer customScore = Integer.valueOf(creatureCharismaCustomValue.getText());
            stat.setAbilityModifierChoice(AbilityModifierChoice.CUSTOM);
            stat.setCustomValue(customScore);
        } catch (NumberFormatException e) {
            if (creatureHighCharisma.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.HIGH);
            else if (creatureMidCharisma.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.MID);
            else if (creatureLowCharisma.isSelected())
                stat.setAbilityModifierChoice(AbilityModifierChoice.LOW);
            else
                stat.setAbilityModifierChoice(AbilityModifierChoice.NONE);
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
        creatureSkillsLabel.setStyle("-fx-font-weight: bold");
        creatureLanguagesLabel.setStyle("-fx-font-weight: bold");
        creatureOtherAbilitiesLabel.setStyle("-fx-font-weight: bold");
        creatureSpaceLabel.setStyle("-fx-font-weight: bold");
        creatureReachLabel.setStyle("-fx-font-weight: bold");
        
        updateTabStatus();

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
                    updateTabStatus();
                }
            }
        );
        
        creatureAlignmentInput.setItems(FXCollections.observableArrayList(
                Arrays.stream(Alignment.values())
                        .map(Alignment::toString)
                        .collect(Collectors.toList())  
        ));

        creatureAlignmentInput.getSelectionModel().selectedIndexProperty().addListener(
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setAlignmentFromComboBox(newValue.intValue());
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        
        creatureSizeInput.setItems(FXCollections.observableArrayList(
                Arrays.stream(Size.values())
                        .map(Size::toString)
                        .collect(Collectors.toList())  
        ));

        creatureSizeInput.getSelectionModel().selectedIndexProperty().addListener(
            new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue observable,
                        Number oldValue, Number newValue) {
                    creature.setSizeFromComboBox(newValue.intValue());
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        
        creatureLongReach.setToggleGroup(reachOptionsGroup);
        creatureTallReach.setToggleGroup(reachOptionsGroup);
        
        reachOptionsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (reachOptionsGroup.getSelectedToggle() != null)
                    creature.setLongReach(creatureLongReach.isSelected());
                updateStatBlock();
                updateWindowTitle();
            }
        });

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
                    setAbilityControls();
                    setSkillControls();
                    updateTabStatus();
                }
            }
        );
        
        creatureHighStrength.setToggleGroup(highStatGroup);
        creatureHighDexterity.setToggleGroup(highStatGroup);
        creatureHighConstitution.setToggleGroup(highStatGroup);
        creatureHighIntelligence.setToggleGroup(highStatGroup);
        creatureHighWisdom.setToggleGroup(highStatGroup);
        creatureHighCharisma.setToggleGroup(highStatGroup);
        creatureMidStrength.setToggleGroup(midStatGroup);
        creatureMidDexterity.setToggleGroup(midStatGroup);
        creatureMidConstitution.setToggleGroup(midStatGroup);
        creatureMidIntelligence.setToggleGroup(midStatGroup);
        creatureMidWisdom.setToggleGroup(midStatGroup);
        creatureMidCharisma.setToggleGroup(midStatGroup);
        creatureLowStrength.setToggleGroup(lowStatGroup);
        creatureLowDexterity.setToggleGroup(lowStatGroup);
        creatureLowConstitution.setToggleGroup(lowStatGroup);
        creatureLowIntelligence.setToggleGroup(lowStatGroup);
        creatureLowWisdom.setToggleGroup(lowStatGroup);
        creatureLowCharisma.setToggleGroup(lowStatGroup);
        
        highStatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (newToggle == null) return;
                if (newToggle.equals(creatureHighStrength)) {
                    creatureMidStrength.setSelected(false);
                    creatureLowStrength.setSelected(false);
                }
                if (newToggle.equals(creatureHighDexterity)) {
                    creatureMidDexterity.setSelected(false);
                    creatureLowDexterity.setSelected(false);
                }
                if (newToggle.equals(creatureHighConstitution)) {
                    creatureMidConstitution.setSelected(false);
                    creatureLowConstitution.setSelected(false);
                }
                if (newToggle.equals(creatureHighIntelligence)) {
                    creatureMidIntelligence.setSelected(false);
                    creatureLowIntelligence.setSelected(false);
                }
                if (newToggle.equals(creatureHighWisdom)) {
                    creatureMidWisdom.setSelected(false);
                    creatureLowWisdom.setSelected(false);
                }
                if (newToggle.equals(creatureHighCharisma)) {
                    creatureMidCharisma.setSelected(false);
                    creatureLowCharisma.setSelected(false);
                }
                updateAbilityModifiers();
                updateStatBlock();
                updateWindowTitle();
            }
        });
        midStatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (newToggle == null) return;
                if (newToggle.equals(creatureMidStrength)) {
                    creatureHighStrength.setSelected(false);
                    creatureLowStrength.setSelected(false);
                }
                if (newToggle.equals(creatureMidDexterity)) {
                    creatureHighDexterity.setSelected(false);
                    creatureLowDexterity.setSelected(false);
                }
                if (newToggle.equals(creatureMidConstitution)) {
                    creatureHighConstitution.setSelected(false);
                    creatureLowConstitution.setSelected(false);
                }
                if (newToggle.equals(creatureMidIntelligence)) {
                    creatureHighIntelligence.setSelected(false);
                    creatureLowIntelligence.setSelected(false);
                }
                if (newToggle.equals(creatureMidWisdom)) {
                    creatureHighWisdom.setSelected(false);
                    creatureLowWisdom.setSelected(false);
                }
                if (newToggle.equals(creatureMidCharisma)) {
                    creatureHighCharisma.setSelected(false);
                    creatureLowCharisma.setSelected(false);
                }
                updateAbilityModifiers();
                updateStatBlock();
                updateWindowTitle();
            }
        });
        lowStatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (newToggle == null) return;
                if (newToggle.equals(creatureLowStrength)) {
                    creatureMidStrength.setSelected(false);
                    creatureHighStrength.setSelected(false);
                }
                if (newToggle.equals(creatureLowDexterity)) {
                    creatureMidDexterity.setSelected(false);
                    creatureHighDexterity.setSelected(false);
                }
                if (newToggle.equals(creatureLowConstitution)) {
                    creatureMidConstitution.setSelected(false);
                    creatureHighConstitution.setSelected(false);
                }
                if (newToggle.equals(creatureLowIntelligence)) {
                    creatureMidIntelligence.setSelected(false);
                    creatureHighIntelligence.setSelected(false);
                }
                if (newToggle.equals(creatureLowWisdom)) {
                    creatureMidWisdom.setSelected(false);
                    creatureHighWisdom.setSelected(false);
                }
                if (newToggle.equals(creatureLowCharisma)) {
                    creatureMidCharisma.setSelected(false);
                    creatureHighCharisma.setSelected(false);
                }
                updateAbilityModifiers();
                updateStatBlock();
                updateWindowTitle();
            }
        });
        creatureStrengthCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    updateAbilityModifiers();
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        creatureDexterityCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    updateAbilityModifiers();
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        creatureConstitutionCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    updateAbilityModifiers();
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        creatureIntelligenceCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    updateAbilityModifiers();
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        creatureWisdomCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    updateAbilityModifiers();
                    updateStatBlock();
                    updateWindowTitle();
                }
            }
        );
        creatureCharismaCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    updateAbilityModifiers();
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
                    updateTabStatus();
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
        List<String> availableAbilitiesDisplay = new ArrayList<>();
        Ability.setOfAbilities.stream().forEach((a) -> {
            availableAbilitiesDisplay.add(a.getId());
        });
        java.util.Collections.sort(availableAbilitiesDisplay);
        creatureAbilityInput.setItems(FXCollections.observableArrayList(
                availableAbilitiesDisplay));
        creatureAbilityInput.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        creatureAbilityInput.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                creature.setChosenAbilities(creatureAbilityInput.getSelectionModel().getSelectedItems());
                updateStatBlock();
                updateWindowTitle();
                setAbilityControls();
            }
        });
        
        // step 7 controls
        creatureAcrobaticsMaster.setToggleGroup(creatureAcrobaticsGroup);
        creatureAcrobaticsGood.setToggleGroup(creatureAcrobaticsGroup);
        creatureAcrobaticsNone.setToggleGroup(creatureAcrobaticsGroup);    
        creatureAcrobaticsCustom.setToggleGroup(creatureAcrobaticsGroup);   
        creatureAcrobaticsGroup.selectToggle(creatureAcrobaticsNone);
        
        creatureAcrobaticsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureAcrobaticsGroup.getSelectedToggle() != null) {
                    if (creatureAcrobaticsMaster.isSelected())
                        creature.acrobatics.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureAcrobaticsGood.isSelected())
                        creature.acrobatics.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureAcrobaticsNone.isSelected())
                        creature.acrobatics.setSkillChoice(SkillChoice.NONE);
                    else if (creatureAcrobaticsCustom.isSelected())
                        creature.acrobatics.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.acrobatics.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureAcrobaticsCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.acrobatics.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.acrobatics.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureAthleticsMaster.setToggleGroup(creatureAthleticsGroup);
        creatureAthleticsGood.setToggleGroup(creatureAthleticsGroup);
        creatureAthleticsNone.setToggleGroup(creatureAthleticsGroup);    
        creatureAthleticsCustom.setToggleGroup(creatureAthleticsGroup);   
        creatureAthleticsGroup.selectToggle(creatureAthleticsNone);
        
        creatureAthleticsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureAthleticsGroup.getSelectedToggle() != null) {
                    if (creatureAthleticsMaster.isSelected())
                        creature.athletics.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureAthleticsGood.isSelected())
                        creature.athletics.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureAthleticsNone.isSelected())
                        creature.athletics.setSkillChoice(SkillChoice.NONE);
                    else if (creatureAthleticsCustom.isSelected())
                        creature.athletics.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.athletics.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureAthleticsCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.athletics.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.athletics.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );
        
        creatureBluffMaster.setToggleGroup(creatureBluffGroup);
        creatureBluffGood.setToggleGroup(creatureBluffGroup);
        creatureBluffNone.setToggleGroup(creatureBluffGroup);    
        creatureBluffCustom.setToggleGroup(creatureBluffGroup);   
        creatureBluffGroup.selectToggle(creatureBluffNone);
        
        creatureBluffGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureBluffGroup.getSelectedToggle() != null) {
                    if (creatureBluffMaster.isSelected())
                        creature.bluff.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureBluffGood.isSelected())
                        creature.bluff.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureBluffNone.isSelected())
                        creature.bluff.setSkillChoice(SkillChoice.NONE);
                    else if (creatureBluffCustom.isSelected())
                        creature.bluff.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.bluff.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureBluffCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.bluff.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
            }
                    else try {
                        creature.bluff.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureComputersMaster.setToggleGroup(creatureComputersGroup);
        creatureComputersGood.setToggleGroup(creatureComputersGroup);
        creatureComputersNone.setToggleGroup(creatureComputersGroup);    
        creatureComputersCustom.setToggleGroup(creatureComputersGroup);   
        creatureComputersGroup.selectToggle(creatureComputersNone);
        
        creatureComputersGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureComputersGroup.getSelectedToggle() != null) {
                    if (creatureComputersMaster.isSelected())
                        creature.computers.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureComputersGood.isSelected())
                        creature.computers.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureComputersNone.isSelected())
                        creature.computers.setSkillChoice(SkillChoice.NONE);
                    else if (creatureComputersCustom.isSelected())
                        creature.computers.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.computers.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureComputersCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.computers.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.computers.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureCultureMaster.setToggleGroup(creatureCultureGroup);
        creatureCultureGood.setToggleGroup(creatureCultureGroup);
        creatureCultureNone.setToggleGroup(creatureCultureGroup);    
        creatureCultureCustom.setToggleGroup(creatureCultureGroup);   
        creatureCultureGroup.selectToggle(creatureCultureNone);
        
        creatureCultureGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureCultureGroup.getSelectedToggle() != null) {
                    if (creatureCultureMaster.isSelected())
                        creature.culture.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureCultureGood.isSelected())
                        creature.culture.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureCultureNone.isSelected())
                        creature.culture.setSkillChoice(SkillChoice.NONE);
                    else if (creatureCultureCustom.isSelected())
                        creature.culture.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.culture.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureCultureCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.culture.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.culture.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureDiplomacyMaster.setToggleGroup(creatureDiplomacyGroup);
        creatureDiplomacyGood.setToggleGroup(creatureDiplomacyGroup);
        creatureDiplomacyNone.setToggleGroup(creatureDiplomacyGroup);    
        creatureDiplomacyCustom.setToggleGroup(creatureDiplomacyGroup);   
        creatureDiplomacyGroup.selectToggle(creatureDiplomacyNone);
        
        creatureDiplomacyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureDiplomacyGroup.getSelectedToggle() != null) {
                    if (creatureDiplomacyMaster.isSelected())
                        creature.diplomacy.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureDiplomacyGood.isSelected())
                        creature.diplomacy.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureDiplomacyNone.isSelected())
                        creature.diplomacy.setSkillChoice(SkillChoice.NONE);
                    else if (creatureDiplomacyCustom.isSelected())
                        creature.diplomacy.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.diplomacy.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureDiplomacyCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.diplomacy.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.diplomacy.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureDisguiseMaster.setToggleGroup(creatureDisguiseGroup);
        creatureDisguiseGood.setToggleGroup(creatureDisguiseGroup);
        creatureDisguiseNone.setToggleGroup(creatureDisguiseGroup);    
        creatureDisguiseCustom.setToggleGroup(creatureDisguiseGroup);   
        creatureDisguiseGroup.selectToggle(creatureDisguiseNone);
        
        creatureDisguiseGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureDisguiseGroup.getSelectedToggle() != null) {
                    if (creatureDisguiseMaster.isSelected())
                        creature.disguise.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureDisguiseGood.isSelected())
                        creature.disguise.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureDisguiseNone.isSelected())
                        creature.disguise.setSkillChoice(SkillChoice.NONE);
                    else if (creatureDisguiseCustom.isSelected())
                        creature.disguise.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.disguise.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureDisguiseCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.disguise.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.disguise.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureEngineeringMaster.setToggleGroup(creatureEngineeringGroup);
        creatureEngineeringGood.setToggleGroup(creatureEngineeringGroup);
        creatureEngineeringNone.setToggleGroup(creatureEngineeringGroup);    
        creatureEngineeringCustom.setToggleGroup(creatureEngineeringGroup);   
        creatureEngineeringGroup.selectToggle(creatureEngineeringNone);
        
        creatureEngineeringGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureEngineeringGroup.getSelectedToggle() != null) {
                    if (creatureEngineeringMaster.isSelected())
                        creature.engineering.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureEngineeringGood.isSelected())
                        creature.engineering.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureEngineeringNone.isSelected())
                        creature.engineering.setSkillChoice(SkillChoice.NONE);
                    else if (creatureEngineeringCustom.isSelected())
                        creature.engineering.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.engineering.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureEngineeringCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.engineering.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.engineering.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureIntimidateMaster.setToggleGroup(creatureIntimidateGroup);
        creatureIntimidateGood.setToggleGroup(creatureIntimidateGroup);
        creatureIntimidateNone.setToggleGroup(creatureIntimidateGroup);    
        creatureIntimidateCustom.setToggleGroup(creatureIntimidateGroup);   
        creatureIntimidateGroup.selectToggle(creatureIntimidateNone);
        
        creatureIntimidateGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureIntimidateGroup.getSelectedToggle() != null) {
                    if (creatureIntimidateMaster.isSelected())
                        creature.intimidate.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureIntimidateGood.isSelected())
                        creature.intimidate.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureIntimidateNone.isSelected())
                        creature.intimidate.setSkillChoice(SkillChoice.NONE);
                    else if (creatureIntimidateCustom.isSelected())
                        creature.intimidate.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.intimidate.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureIntimidateCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.intimidate.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.intimidate.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureLifeScienceMaster.setToggleGroup(creatureLifeScienceGroup);
        creatureLifeScienceGood.setToggleGroup(creatureLifeScienceGroup);
        creatureLifeScienceNone.setToggleGroup(creatureLifeScienceGroup);    
        creatureLifeScienceCustom.setToggleGroup(creatureLifeScienceGroup);   
        creatureLifeScienceGroup.selectToggle(creatureLifeScienceNone);
        
        creatureLifeScienceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureLifeScienceGroup.getSelectedToggle() != null) {
                    if (creatureLifeScienceMaster.isSelected())
                        creature.lifeScience.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureLifeScienceGood.isSelected())
                        creature.lifeScience.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureLifeScienceNone.isSelected())
                        creature.lifeScience.setSkillChoice(SkillChoice.NONE);
                    else if (creatureLifeScienceCustom.isSelected())
                        creature.lifeScience.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.lifeScience.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureLifeScienceCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.lifeScience.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.lifeScience.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureMedicineMaster.setToggleGroup(creatureMedicineGroup);
        creatureMedicineGood.setToggleGroup(creatureMedicineGroup);
        creatureMedicineNone.setToggleGroup(creatureMedicineGroup);    
        creatureMedicineCustom.setToggleGroup(creatureMedicineGroup);   
        creatureMedicineGroup.selectToggle(creatureMedicineNone);
        
        creatureMedicineGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureMedicineGroup.getSelectedToggle() != null) {
                    if (creatureMedicineMaster.isSelected())
                        creature.medicine.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureMedicineGood.isSelected())
                        creature.medicine.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureMedicineNone.isSelected())
                        creature.medicine.setSkillChoice(SkillChoice.NONE);
                    else if (creatureMedicineCustom.isSelected())
                        creature.medicine.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.medicine.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureMedicineCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.medicine.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.medicine.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureMysticismMaster.setToggleGroup(creatureMysticismGroup);
        creatureMysticismGood.setToggleGroup(creatureMysticismGroup);
        creatureMysticismNone.setToggleGroup(creatureMysticismGroup);    
        creatureMysticismCustom.setToggleGroup(creatureMysticismGroup);   
        creatureMysticismGroup.selectToggle(creatureMysticismNone);
        
        creatureMysticismGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureMysticismGroup.getSelectedToggle() != null) {
                    if (creatureMysticismMaster.isSelected())
                        creature.mysticism.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureMysticismGood.isSelected())
                        creature.mysticism.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureMysticismNone.isSelected())
                        creature.mysticism.setSkillChoice(SkillChoice.NONE);
                    else if (creatureMysticismCustom.isSelected())
                        creature.mysticism.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.mysticism.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureMysticismCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.mysticism.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.mysticism.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creaturePerceptionMaster.setToggleGroup(creaturePerceptionGroup);
        creaturePerceptionGood.setToggleGroup(creaturePerceptionGroup);
        creaturePerceptionNone.setToggleGroup(creaturePerceptionGroup);    
        creaturePerceptionCustom.setToggleGroup(creaturePerceptionGroup);   
        creaturePerceptionGroup.selectToggle(creaturePerceptionGood);
        
        creaturePerceptionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creaturePerceptionGroup.getSelectedToggle() != null) {
                    if (creaturePerceptionMaster.isSelected())
                        creature.perception.setSkillChoice(SkillChoice.MASTER);
                    else if (creaturePerceptionGood.isSelected())
                        creature.perception.setSkillChoice(SkillChoice.GOOD);
                    else if (creaturePerceptionNone.isSelected())
                        creature.perception.setSkillChoice(SkillChoice.NONE);
                    else if (creaturePerceptionCustom.isSelected())
                        creature.perception.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.perception.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creaturePerceptionCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.perception.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.perception.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creaturePhysicalScienceMaster.setToggleGroup(creaturePhysicalScienceGroup);
        creaturePhysicalScienceGood.setToggleGroup(creaturePhysicalScienceGroup);
        creaturePhysicalScienceNone.setToggleGroup(creaturePhysicalScienceGroup);    
        creaturePhysicalScienceCustom.setToggleGroup(creaturePhysicalScienceGroup);   
        creaturePhysicalScienceGroup.selectToggle(creaturePhysicalScienceNone);
        
        creaturePhysicalScienceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creaturePhysicalScienceGroup.getSelectedToggle() != null) {
                    if (creaturePhysicalScienceMaster.isSelected())
                        creature.physicalScience.setSkillChoice(SkillChoice.MASTER);
                    else if (creaturePhysicalScienceGood.isSelected())
                        creature.physicalScience.setSkillChoice(SkillChoice.GOOD);
                    else if (creaturePhysicalScienceNone.isSelected())
                        creature.physicalScience.setSkillChoice(SkillChoice.NONE);
                    else if (creaturePhysicalScienceCustom.isSelected())
                        creature.physicalScience.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.physicalScience.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creaturePhysicalScienceCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.physicalScience.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.physicalScience.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creaturePilotingMaster.setToggleGroup(creaturePilotingGroup);
        creaturePilotingGood.setToggleGroup(creaturePilotingGroup);
        creaturePilotingNone.setToggleGroup(creaturePilotingGroup);    
        creaturePilotingCustom.setToggleGroup(creaturePilotingGroup);   
        creaturePilotingGroup.selectToggle(creaturePilotingNone);
        
        creaturePilotingGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creaturePilotingGroup.getSelectedToggle() != null) {
                    if (creaturePilotingMaster.isSelected())
                        creature.piloting.setSkillChoice(SkillChoice.MASTER);
                    else if (creaturePilotingGood.isSelected())
                        creature.piloting.setSkillChoice(SkillChoice.GOOD);
                    else if (creaturePilotingNone.isSelected())
                        creature.piloting.setSkillChoice(SkillChoice.NONE);
                    else if (creaturePilotingCustom.isSelected())
                        creature.piloting.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.piloting.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creaturePilotingCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.piloting.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.piloting.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureProfessionMaster.setToggleGroup(creatureProfessionGroup);
        creatureProfessionGood.setToggleGroup(creatureProfessionGroup);
        creatureProfessionNone.setToggleGroup(creatureProfessionGroup);    
        creatureProfessionCustom.setToggleGroup(creatureProfessionGroup);   
        creatureProfessionGroup.selectToggle(creatureProfessionNone);
        
        creatureProfessionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureProfessionGroup.getSelectedToggle() != null) {
                    if (creatureProfessionMaster.isSelected())
                        creature.profession.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureProfessionGood.isSelected())
                        creature.profession.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureProfessionNone.isSelected())
                        creature.profession.setSkillChoice(SkillChoice.NONE);
                    else if (creatureProfessionCustom.isSelected())
                        creature.profession.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.profession.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureProfessionCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.profession.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.profession.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureSenseMotiveMaster.setToggleGroup(creatureSenseMotiveGroup);
        creatureSenseMotiveGood.setToggleGroup(creatureSenseMotiveGroup);
        creatureSenseMotiveNone.setToggleGroup(creatureSenseMotiveGroup);    
        creatureSenseMotiveCustom.setToggleGroup(creatureSenseMotiveGroup);   
        creatureSenseMotiveGroup.selectToggle(creatureSenseMotiveNone);
        
        creatureSenseMotiveGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureSenseMotiveGroup.getSelectedToggle() != null) {
                    if (creatureSenseMotiveMaster.isSelected())
                        creature.senseMotive.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureSenseMotiveGood.isSelected())
                        creature.senseMotive.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureSenseMotiveNone.isSelected())
                        creature.senseMotive.setSkillChoice(SkillChoice.NONE);
                    else if (creatureSenseMotiveCustom.isSelected())
                        creature.senseMotive.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.senseMotive.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureSenseMotiveCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.senseMotive.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.senseMotive.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureSleightOfHandMaster.setToggleGroup(creatureSleightOfHandGroup);
        creatureSleightOfHandGood.setToggleGroup(creatureSleightOfHandGroup);
        creatureSleightOfHandNone.setToggleGroup(creatureSleightOfHandGroup);    
        creatureSleightOfHandCustom.setToggleGroup(creatureSleightOfHandGroup);   
        creatureSleightOfHandGroup.selectToggle(creatureSleightOfHandNone);
        
        creatureSleightOfHandGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureSleightOfHandGroup.getSelectedToggle() != null) {
                    if (creatureSleightOfHandMaster.isSelected())
                        creature.sleightOfHand.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureSleightOfHandGood.isSelected())
                        creature.sleightOfHand.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureSleightOfHandNone.isSelected())
                        creature.sleightOfHand.setSkillChoice(SkillChoice.NONE);
                    else if (creatureSleightOfHandCustom.isSelected())
                        creature.sleightOfHand.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.sleightOfHand.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureSleightOfHandCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.sleightOfHand.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.sleightOfHand.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureStealthMaster.setToggleGroup(creatureStealthGroup);
        creatureStealthGood.setToggleGroup(creatureStealthGroup);
        creatureStealthNone.setToggleGroup(creatureStealthGroup);    
        creatureStealthCustom.setToggleGroup(creatureStealthGroup);   
        creatureStealthGroup.selectToggle(creatureStealthNone);
        
        creatureStealthGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureStealthGroup.getSelectedToggle() != null) {
                    if (creatureStealthMaster.isSelected())
                        creature.stealth.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureStealthGood.isSelected())
                        creature.stealth.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureStealthNone.isSelected())
                        creature.stealth.setSkillChoice(SkillChoice.NONE);
                    else if (creatureStealthCustom.isSelected())
                        creature.stealth.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.stealth.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureStealthCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.stealth.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.stealth.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

        creatureSurvivalMaster.setToggleGroup(creatureSurvivalGroup);
        creatureSurvivalGood.setToggleGroup(creatureSurvivalGroup);
        creatureSurvivalNone.setToggleGroup(creatureSurvivalGroup);    
        creatureSurvivalCustom.setToggleGroup(creatureSurvivalGroup);   
        creatureSurvivalGroup.selectToggle(creatureSurvivalNone);
        
        creatureSurvivalGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle oldToggle, Toggle newToggle) {
                if (creatureSurvivalGroup.getSelectedToggle() != null) {
                    if (creatureSurvivalMaster.isSelected())
                        creature.survival.setSkillChoice(SkillChoice.MASTER);
                    else if (creatureSurvivalGood.isSelected())
                        creature.survival.setSkillChoice(SkillChoice.GOOD);
                    else if (creatureSurvivalNone.isSelected())
                        creature.survival.setSkillChoice(SkillChoice.NONE);
                    else if (creatureSurvivalCustom.isSelected())
                        creature.survival.setSkillChoice(SkillChoice.CUSTOM);
                }
                else
                        creature.survival.setSkillChoice(SkillChoice.NONE);
                    ;
                updateStatBlock();
                updateWindowTitle();
                setSkillControls();
            }
        });
        creatureSurvivalCustomValue.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if ("".equals(newValue)) {
                        creature.survival.setCustomValue(0);
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    }
                    else try {
                        creature.survival.setCustomValue(Integer.valueOf(newValue));
                        updateStatBlock();
                        updateWindowTitle();
                        setSkillControls();
                    } 
                    catch(NumberFormatException e) {
                        // it's not a number; don't change anything.
                        return;
                    }
                }
            }
        );

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
