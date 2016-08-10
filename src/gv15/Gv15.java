/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gv15;

import javafx.application.Application;
import javafx.stage.Stage;
import java.util.ArrayList;

/**
 *
 * @author ranasi01
 */
public class Gv15  extends Application {

    static Engine engine;
    public static void main(String[] args) throws Exception {
        engine = new Engine(args); 
        
        launch(args); 
    }
    
    @Override public void start(Stage stage) {
        engine.Render(stage);
    }

}
