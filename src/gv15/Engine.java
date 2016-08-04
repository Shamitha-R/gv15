package gv15;

import com.opencsv.CSVReader;
import data.cache.ConsensusFileCache;
import htsjdk.variant.variantcontext.Allele;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    public String OutputType;

    public double GridStartX;
    public double GridStartY;
    public double PanelSeparation;
    public int ColumnWidth;
    public int RowHeight;
    public int FragmentXOffset;  
    
    //Debug Commands
    boolean TESTINGPANELS = false;
    
    //Components
    DataManager dataManager;
    PanelManager panelManager;
    VariantManager variantManager;
    FragmentManager fragmentManager;
    ReferenceManager referenceManager;
    TabletDataHandler tabletDataHandler;
    HashMap<String,ArrayList<Phenotype>> phenotypes = new HashMap();
       
    public Engine(String[] args){
        if(!TESTINGPANELS){
            SetPrefsFile(args);

            //Setup Import Utils
            dataManager = new DataManager(DataPath,VariantPath,PhenotypePath);
            dataManager.ImportPhenotypes(phenotypes);

            //Setup Panels
            panelManager = new PanelManager();
            referenceManager = new ReferenceManager(ReferencePath);
            int count = 0;
            for(String type:phenotypes.keySet()){
                panelManager.AddPanel(type, GridStartX, GridStartY + (PanelSeparation*count), 
                        FLANK, ColumnWidth, RowHeight,FragmentXOffset);  
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
    }

    public void Render(Stage stage){
        
        if(TESTINGPANELS)
            CreateTestPanel();
        
        Group root = new Group();
        
        panelManager.RenderPanels(root,referenceManager,
                fragmentManager.getMaxReadCount());
        
        if(!TESTINGPANELS)
            root.getChildren().add(SetupChartTitle((int) (WIDTH/2), 50));
        
        Scene scene = new Scene(
                root,
                WIDTH, HEIGHT,
                Color.rgb(255,255,255)
        );

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        OutputResultsToImage(scene);
        //stage.setMaximized(true);        
    }
    
    private void OutputResultsToImage(Scene scene){
        WritableImage snapshot = scene.snapshot(null);
        if(OutputType.equals("png")){
            BufferedImage tempImg = SwingFXUtils.fromFXImage(snapshot, null);
            File outputfile = new File(OutputPath+"results.png");
            try{
                ImageIO.write(tempImg, "png", outputfile);
            }catch(Exception e){

            }
        }else if(OutputType.equals("jpeg")){
           File fa = new File(OutputPath+"results.jpg");
           RenderedImage renderedImage = SwingFXUtils.fromFXImage(snapshot, null);
           BufferedImage image2 = new BufferedImage((int)WIDTH, (int)HEIGHT, BufferedImage.TYPE_INT_RGB); 
           image2.setData(renderedImage.getData());
           try{
               ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("jpeg").next();
               ImageWriteParam iwp = writer.getDefaultWriteParam();
               iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
               iwp.setCompressionQuality(1);   // a float between 0 and 1
               // 1 specifies minimum compression and maximum quality
               FileImageOutputStream output = new FileImageOutputStream(fa);
               writer.setOutput(output);
               IIOImage iioimage = new IIOImage(image2, null, null);
               writer.write(null, iioimage, iwp);
               writer.dispose();

           }catch (Exception e){

           }     
        }        
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
                    case "outputtype": OutputType = parameterVal;
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
    
    private void CreateTestPanel(){
        
        SetupDebugParameters();
        
        referenceManager = new ReferenceManager(ReferencePath);
        panelManager = new PanelManager();
        fragmentManager = new FragmentManager(DataPath, CachePath);
        String[] refData;
        Map<String,FragmentNode>[] tempFragments;
        int maxReadCount = -1;
        
        //Read Json panel Data
        JSONParser parser = new JSONParser();

	try {
		Object obj = parser.parse(new FileReader("C:\\Users\\ranasi01\\Documents\\Project\\gv15\\test.json"));

		JSONObject jsonObject = (JSONObject) obj;
                tempFragments = new HashMap[jsonObject.size()];
                refData = new String[jsonObject.size()];

                for(Object objHeader:jsonObject.keySet()){
                    
                    //Add to reference
                    String headerVal = (String)objHeader;
                    int index = headerVal.indexOf("-");
                    int colVal = Integer.parseInt(headerVal.substring(0, index));
                    String refVal = headerVal.substring(index+1);
                    refData[colVal] = refVal;
                      
                    //Add to panel
                    tempFragments[colVal] = new HashMap();
                    JSONObject baseJSON = (JSONObject)jsonObject.get(objHeader);
                    int currentReadCount = 0;
                    for(Object baseData:baseJSON.keySet()){
                        String baseVal = (String)baseData;
                        String baseType = baseVal.substring(0, 1);
                        int readCount = Integer.parseInt(baseVal.substring(2));
                        
                        FragmentNode tempNode = new FragmentNode();
                        tempNode.ReadCount = readCount;
                        currentReadCount+=readCount;
                        tempNode.ConnectedFragments = new HashMap();

                        JSONArray connectedJSON = (JSONArray) baseJSON.get(baseData);
                        Iterator<String> iterator = connectedJSON.iterator();

                        while (iterator.hasNext()) {
                            String connectedData = iterator.next();
                            int connectedValIndex = connectedData.indexOf("[");
                            String connectedVal = connectedData.substring(0, connectedValIndex);
                            
                            //Add connected Columns
                            String connectedColumnsRaw = connectedData.substring(connectedValIndex+1,
                                    connectedData.length()-1);
                            String[] connectedCols = connectedColumnsRaw.split(",");     
                            HashSet<Integer> connectedVals = new HashSet();
                            for(String val:connectedCols){
                                connectedVals.add(Integer.parseInt(val));
                            }
                            
                            tempNode.ConnectedFragments.put(connectedVal, connectedVals);
                        }
                        
                        tempFragments[colVal].put(baseType, tempNode);
                    }  
                    if(currentReadCount>maxReadCount)
                        maxReadCount = currentReadCount;
                }                
            //Create Panel
            panelManager.AddPanel("TestPanel",GridStartX, GridStartY + (PanelSeparation), 
                    FLANK, ColumnWidth, RowHeight,FragmentXOffset); 
            panelManager.GetPanelFromPhenotype("TestPanel").Fragments = tempFragments;
            fragmentManager.maxReadCount = maxReadCount;

            //Add to reference Data
            referenceManager.AddReference("TestPanel", new ArrayList<String>(Arrays.asList(refData)));
            referenceManager.ShiftVals = new HashMap();
            referenceManager.ShiftVals.put("TestPanel", 0);
            
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		e.printStackTrace();
	}

    }
    
    private void SetupDebugParameters(){
        WIDTH = 1000;
        HEIGHT = 500;
        GridStartX = 200;
        GridStartY = 100;
        PanelSeparation = 0;
        ColumnWidth = 90;
        RowHeight = 18;
        FragmentXOffset = 20;
        OutputType = "png";
        FLANK = 3;
    }
}
