package gv15;

import data.cache.ConsensusFileCache;
import htsjdk.variant.variantcontext.Allele;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author ranasi01
 */
public class Engine{
    
    //Engine Settings
    public double WIDTH = 1280, HEIGHT = 950;
    public int FLANK = 7;
    public String DataPath = "C:\\Users\\ranasi01\\Documents\\Project\\Data";

    public float GridStartX = 150;
    public float GridStartY = 100;
    public float PanelSeparation = 215;
    public int ColumnWidth = 65;
    public int RowHeight = 18;
    public int FragmentXOffset = 20;     
    
    //Components
    DataManager dataManager;
    PanelManager panelManager;
    VariantManager variantManager;
    FragmentManager fragmentManager;
    ReferenceManager referenceManager;
    HashMap<String,ArrayList<Phenotype>> phenotypes = new HashMap();
       
    public Engine(){
        
        //Setup Import Utils
        dataManager = new DataManager(DataPath);
        dataManager.ImportPhenotypes(phenotypes);
        SetPrefsFile();
        
        //Setup Panels
        panelManager = new PanelManager();
        referenceManager = new ReferenceManager();
        int count = 0;
        for(String type:phenotypes.keySet()){
            panelManager.AddPanel(type, 
                    GridStartX, GridStartY + (PanelSeparation*count), FLANK, ColumnWidth, RowHeight);  
            count++;
            //break;
        }
        
        //Setup Variants
        variantManager = new VariantManager(dataManager.ImportVCFFile());       
        //Setup Fragments
        fragmentManager = new FragmentManager();
        try {
            fragmentManager.ProcessFragments(phenotypes,referenceManager,
                    panelManager,FLANK);
        } catch (Exception ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Render(Stage stage){
        Group root = new Group();
        
        panelManager.RenderPanels(root,referenceManager,
                fragmentManager.getMaxReadCount());
        
        root.getChildren().add(SetupChartTitle((int) (WIDTH/2), 50));
        
        Scene scene = new Scene(
                root,
                WIDTH, HEIGHT,
                Color.rgb(255,255,255)
        );

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        SnapshotParameters param = new SnapshotParameters();
        param.setDepthBuffer(true);
        param.setFill(Color.CORNSILK);
        WritableImage image = scene.snapshot(null);
        
        BufferedImage tempImg = SwingFXUtils.fromFXImage(image, null);

        File outputfile = new File("d:/tempImg.png");
        try{
            ImageIO.write(tempImg, "png", outputfile);
        }catch(Exception e){
            
        }
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
    
    private static File SetPrefsFile(){
	// Ensure the .scri-bioinf folder exists
	File fldr = new File("C:\\Users\\ranasi01\\Documents\\Project\\gv15", ".scri-bioinf");
	fldr.mkdirs();  

	// Cached reference file
	ConsensusFileCache.setIndexFile(new File(fldr, "tablet-refs.xml"));
        // This is the file we really want
        File file = new File(fldr, "tablet.xml");
        // So if it exists, just use it
        if (file.exists())
            return file;   
        
        return null;
    }
}
