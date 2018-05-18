/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Doug
 */
public class Sfnc extends Application {
    
    private static Sfnc instance;
    public static Sfnc getInstance() {
        return instance;
    }

    private Stage myStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        instance = this;
        myStage = stage;
        
        Parent root = FXMLLoader.load(getClass().getResource("sfncFXML.fxml"));
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }
    
    public void updateTitle(String title) {
        myStage.setTitle(title);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
