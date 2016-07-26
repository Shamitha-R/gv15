package gv15;

import static gv15.Gv15.testReference;
import htsjdk.variant.variantcontext.Allele;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author ranasi01
 */
public class Engine extends Application {
    
    //Engine Settings
    public double WIDTH = 1280, HEIGHT = 768;
    public int FLANK = 7;
    public String Path = "";

    public float GridStartX = 150;
    public float GridStartY = 100;
    public float PanelSeparation = 50;
    public int ColumnWidth = 65;
    public int RowHeight = 20;
    public int FragmentXOffset = 20;     
    
    //Components
    DataManager dataManager;
    PanelManager panelManager;
    VariantManager variantManager;
    FragmentManager fragmentManager;
    ReferenceManager referenceManager;
       
    public Engine(String[] args){
        
        //Launch the JavaFX application
        launch(args); 
    }

    @Override public void start(Stage stage) {
        Group root = new Group();
        
        //testPanel.RenderPanel(root,referenceManager.getReference(),MAXREADCOUNT);
        //testPanel2.RenderPanel(root, referenceManager.getReference(), MAXREADCOUNT);
        
        root.getChildren().add(SetupChartTitle((int) (WIDTH/2), 50));
        
        Scene scene = new Scene(
                root,
                WIDTH, HEIGHT,
                Color.rgb(255,255,255)
        );

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        //stage.setMaximized(true);        
    }
    
    private Text SetupChartTitle(int startX,int startY){
        Text details = new Text(startX,startY,"");
        details.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        details.setFill(Color.LIGHTSLATEGREY);
        //details.setA
        
        String displayText = variantManager.getSelectedVariant().getContig() +
                ":" + variantManager.getSelectedVariant().getStart() + " ";
        
        for(Allele curAllele:variantManager.getSelectedVariant().getAlleles()){
            for(byte base:curAllele.getBases()){
                displayText+=Character.toString ((char) base);
            }
            displayText+=">";
        }

        details.setText(displayText.substring(0, displayText.length()-1));

        return details;
    }   
}
