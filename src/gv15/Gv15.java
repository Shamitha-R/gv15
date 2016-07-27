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
    
   
    static int Flank = 7;
    static int ShiftVal = 0;
    static int VariancePos = 871334;

    private static ArrayList<Phenotype> Phenotypes = new ArrayList();

    static Engine engine;
    public static void main(String[] args) throws Exception {
        engine = new Engine(); 
        
        launch(args);
    }
    
    @Override public void start(Stage stage) {
        engine.Render(stage);
    }

}
