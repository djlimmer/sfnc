/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    
    // stat block controls
    @FXML    private Label creatureNameDisplay = new Label();
    @FXML    private Label creatureCRDisplay = new Label();
    
    public void updateStatBlock() {

        // update name/CR line
        creatureNameDisplay.setText(creature.getName().toUpperCase());
        creatureCRDisplay.setText(creature.getCR().displayString());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
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
    
    }    
    
}
