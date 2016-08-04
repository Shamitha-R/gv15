package gv15;

import data.IReadManager;
import data.LineData;
import data.ReadMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ranasi01
 */
public class ReadManager {
    
    private HashMap<String,ArrayList<Read>> readCollection;
    private TabletDataHandler tabletDataHandler;
    private String referencePath;
    private String dataPath;
    
    public ReadManager(String referencePath,String dataPath,String cachePath){
        readCollection = new HashMap();
        tabletDataHandler = new TabletDataHandler(cachePath);
        this.referencePath = referencePath;
        this.dataPath = dataPath;
    }
    
    public void LoadDataFromSamples(HashMap<String,ArrayList<Phenotype>> phenotypes,
            int startCoordinate,int endCoordinate){
        for(String type:phenotypes.keySet()){
            if(type.equals("Neg_Control")){
            int sampleNo = 0;
            for(Phenotype currentPhenotype:phenotypes.get(type)){

                //Sample and reference data input for Tablet
                String[] fileNames  = new String[]{
                    dataPath + "\\" + currentPhenotype.FileName,
                    referencePath
                };
                
                //Call the Tablet library to load the sample data
                try {
                    tabletDataHandler.ExtractDataAtCoordinates(fileNames, startCoordinate, endCoordinate, 0);
                } catch (Exception ex) {
                    Logger.getLogger(ReadManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //Add the samples and its reads to the collection
                readCollection.put(currentPhenotype.FileName, new ArrayList());
                
                //Add the reads to the collection
                //TODO: Change this section when switching from Tablet
                //to a different library
                IReadManager packRows = tabletDataHandler.getReads();
                for(int rowNo = 0;rowNo<packRows.size();rowNo++){
                    
                    ArrayList<data.Read> reads =  packRows.getLine(rowNo); 
                    for(data.Read currentRead:reads){

                        Read tempRead = new Read();
                        tempRead.BaseValues = new String[currentRead.length()];
                        tempRead.ReadID = currentRead.getID();
                        tempRead.StartPosition = currentRead.s();
                        tempRead.Length = currentRead.length();
                        
                        LineData lineData = packRows.getPixelData(rowNo, currentRead.s(), 
                            currentRead.length(), 1f, true);
                        
                        ReadMetaData[] rmds = lineData.getRMDs();
                        int[] indexes = lineData.getIndexes();
                        data.Read[] readArr = lineData.getReads();
                        
                        for(int index =0;index<indexes.length;index++){
                            //For all valid indexes
                            if(indexes[index]>-1){
                                ReadMetaData currentReadMetaData = rmds[index];
                                byte rmdData = currentReadMetaData.getStateAt(indexes[index]);
                                String baseVal = UtilityFunctions.getInstance().GetBaseFromVal(rmdData);
                                tempRead.BaseValues[index] = baseVal;
                            }
                        }                    
                        //Add the read to the collection
                        readCollection.get(currentPhenotype.FileName).add(tempRead);
                    }
                }

                sampleNo++;
            }
        }//End type checking
        }
        
        System.err.println("");
    }
    
    public ArrayList<Read> GetReadsForSample(String sampleName){
        return readCollection.get(sampleName);
    }
}
