/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vizanalyzerv2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author KH2183
 */
public class VizAnalyzerV2 extends Application {
    
   static Stage mainWindow;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("VizAnalyzerUI.fxml"));
            Scene myScene = new Scene(root);
            mainWindow = primaryStage;
            primaryStage.setScene(myScene);
            primaryStage.resizableProperty().set(false);
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(VizAnalyzerV2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
