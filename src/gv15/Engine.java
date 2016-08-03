package gv15;

import data.cache.ConsensusFileCache;
import htsjdk.variant.variantcontext.Allele;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public double WIDTH, HEIGHT;
    public int FLANK;
    public String DataPath;
    public String CachePath;
    public String OutputPath;
    public String ReferencePath;
    public String VariantPath;
    public String PhenotypePath;

    public double GridStartX;
    public double GridStartY;
    public double PanelSeparation;
    public int ColumnWidth;
    public int RowHeight;
    public int FragmentXOffset;     
    
    //Components
    DataManager dataManager;
    PanelManager panelManager;
    VariantManager variantManager;
    FragmentManager fragmentManager;
    ReferenceManager referenceManager;
    HashMap<String,ArrayList<Phenotype>> phenotypes = new HashMap();
       
    public Engine(String[] args){
        
        SetPrefsFile(args);
        
        //Setup Import Utils
        dataManager = new DataManager(DataPath,VariantPath,PhenotypePath);
        dataManager.ImportPhenotypes(phenotypes);
        
        //Setup Panels
        panelManager = new PanelManager();
        referenceManager = new ReferenceManager(ReferencePath);
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
        fragmentManager = new FragmentManager(DataPath,CachePath);
        try {
            fragmentManager.ProcessFragments(phenotypes,referenceManager,
                    panelManager,FLANK,variantManager.getSelectedVariant());
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
        
//        SnapshotParameters param = new SnapshotParameters();
//        param.setDepthBuffer(true);
//        param.setFill(Color.CORNSILK);
//        WritableImage image = scene.snapshot(null);
//        
//        BufferedImage tempImg = SwingFXUtils.fromFXImage(image, null);
//
//        File outputfile = new File(OutputPath+"tempImg.png");
//        try{
//            ImageIO.write(tempImg, "png", outputfile);
//        }catch(Exception e){
//            
//        }
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
    
    private File SetPrefsFile(String[] args){

        String filePath = args[0].substring(7);
        
        //Read Engine Preferences
	try (BufferedReader br = new BufferedReader(new FileReader(filePath+"\\prefs.txt")))
	{
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String parameterName = sCurrentLine.substring(0, sCurrentLine.indexOf("="));
                String parameterVal = sCurrentLine.substring(sCurrentLine.indexOf("=")+1);
                parameterVal = parameterVal.trim();
                
                //Setting the parameters as specified in the prefs file 
                switch(parameterName){
                    case "datapath": DataPath = parameterVal;
                        break;
                    case "cachepath": CachePath = parameterVal;
                        break;
                    case "outputpath": OutputPath = parameterVal;
                        break;         
                    case "referencepath": ReferencePath = parameterVal;
                        break;
                    case "variantpath": VariantPath = parameterVal;
                        break;     
                    case "phenotypepath": PhenotypePath = parameterVal;
                        break;
                    case "width": WIDTH = Double.parseDouble(parameterVal);
                        break;
                    case "height": HEIGHT = Double.parseDouble(parameterVal);
                        break; 
                    case "flank": FLANK = Integer.parseInt(parameterVal);
                        break; 
                    case "gridstartx": GridStartX = Double.parseDouble(parameterVal);
                        break;
                    case "gridstarty": GridStartY = Double.parseDouble(parameterVal);
                        break;
                    case "panelseparation": PanelSeparation = Double.parseDouble(parameterVal);
                        break;
                    case "columnwidth": ColumnWidth = Integer.parseInt(parameterVal);
                        break;
                    case "rowheight": RowHeight = Integer.parseInt(parameterVal);
                        break;
                    case "fragmentxoffset": FragmentXOffset = Integer.parseInt(parameterVal);
                        break;
                    default: System.err.println("Undeclared Parameter "+parameterName);
                }
            }

	} catch (IOException e) {
            e.printStackTrace();
	} 
               
	// Ensure the .scri-bioinf folder exists
	File fldr = new File(filePath, ".scri-bioinf");
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
