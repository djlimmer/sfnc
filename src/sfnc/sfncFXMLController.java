/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 *
 * @author Doug
 */
public class sfncFXMLController implements Initializable {
    
    // the creature
    Creature creature = new Creature();
    
    // Step 0 controls
    @FXML    private TextField creatureNameInput = new TextField();
    @FXML    private ComboBox creatureCRInput = new ComboBox();

    // stat block controls
    @FXML    private Label creatureNameDisplay = new Label();
    @FXML    private Label creatureCRDisplay = new Label();
    @FXML    private Label creatureXPDisplay = new Label();
    
    public void updateStatBlock() {
        // update name/CR line
        creatureNameDisplay.setText(creature.getName().toUpperCase());
        creatureCRDisplay.setText(creature.getCR().displayString());
        // update XP line
        creatureXPDisplay.setText(creature.getXPString());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        updateStatBlock();
        
        // step 0 controls
        creatureNameInput.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    creature.setName(newValue);
                    updateStatBlock();
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
