package gv15;

import data.IReadManager;
import data.LineData;
import data.ReadMetaData;
import data.auxiliary.CigarEvent;
import data.auxiliary.CigarFeature;
import data.auxiliary.CigarInsertEvent;
import data.auxiliary.Feature;
import htsjdk.variant.variantcontext.VariantContext;
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
    private HashMap<String,ArrayList<InsertFeature>> InsertFeatures;
    private String dataPath;
    
    public HashMap<String,ArrayList<String>> loadedReferences;   
    public TabletDataHandler tabletDataHandler;
    public HashMap<String,int[]> InsertionArrays;
    
    public ReadManager(String referencePath,String dataPath,String cachePath){
        readCollection = new HashMap();
        tabletDataHandler = new TabletDataHandler(cachePath);
        loadedReferences = new HashMap();
        this.dataPath = dataPath;
        InsertFeatures = new HashMap();
        InsertionArrays = new HashMap();
    }
    
    public void LoadDataFromSamples(HashMap<String,ArrayList<Phenotype>> phenotypes,
            int startCoordinate,int endCoordinate,ReferenceManager referenceManager,
            VariantContext currentVariant){
        for(String type:phenotypes.keySet()){
            //if(type.equals("Neg_Control")){
            ArrayList<Phenotype> unhandledSamples = new ArrayList();
            int sampleNo = 0;
            for(Phenotype currentSample:phenotypes.get(type)){

                //Sample and reference data input for Tablet
                String[] fileNames  = new String[]{
                    dataPath + currentSample.FileName,
                    referenceManager.getReferencePath()
                };

                //Call the Tablet library to load the sample data
                try {
                    int contigNumber = (Integer.parseInt(currentVariant.getContig().substring(3))-1);
                    tabletDataHandler.ExtractDataAtCoordinates(fileNames, startCoordinate, endCoordinate, 
                            contigNumber,currentSample.FileName);
                } catch (Exception ex) {
                    System.out.println("Error Reading Sample "+currentSample.FileName+". Sample Skipped.");
                    unhandledSamples.add(currentSample);
                    sampleNo++;
                    continue;
                }
                
                //Add the samples and its reads to the collection
                readCollection.put(currentSample.FileName, new ArrayList());
                
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
                        readCollection.get(currentSample.FileName).add(tempRead);
                    }
                }
                
                //Collect all of the insert features for the current sample
                ArrayList<Feature> extractedFeatures = tabletDataHandler.getLoadedFeatures().
                        get(phenotypes.get(type).get(sampleNo).FileName);
                for(int i= 0;i<extractedFeatures.size();i++){
                    Feature currentFeature = extractedFeatures.get(i);
                    
                    boolean filter = false;
                    if(UtilityFunctions.getInstance().InsertionsOnlyAtVariant)
                        filter = (currentFeature.getDataPS() > currentVariant.getStart() - 3
                            && currentFeature.getDataPS() < currentVariant.getStart());
                    else
                        filter = true;
                    
                    if(currentFeature.getGFFType().equals("CIGAR-I") &&
                            currentFeature.getDataPS() >= startCoordinate-2 && 
                            currentFeature.getDataPE() <= endCoordinate &&
                            filter){
                        CigarFeature cf = (CigarFeature)currentFeature;
                        for(CigarEvent e : cf.getEvents()){
                            CigarInsertEvent insEv = (CigarInsertEvent)e;
                            
                            //Add the cuurent feature as and insert
                            InsertFeature tempInsert = new InsertFeature();
                      
                            tempInsert.TargetReadID = insEv.getRead().getID();
                            tempInsert.StartCoodinate = currentFeature.getDataPS();
                            tempInsert.InsertedBases = GetInsertedBases(insEv.getInsertedBases());
                            
                            if(!InsertFeatures.containsKey(currentSample.FileName))
                                InsertFeatures.put(currentSample.FileName, new ArrayList());
                            InsertFeatures.get(currentSample.FileName).add(tempInsert);
                        }
                    }
                }
                sampleNo++;
            }
            //TODO: Read reference only once without reloading when reading 
            //different samples
            referenceManager.AddReference(type, tabletDataHandler.getLoadedReference());
        //}//End type checking
            loadedReferences.put(type, tabletDataHandler.getLoadedReference());
            
                    
            //Remove Samples which failed to load
            for(Phenotype pheno:unhandledSamples){
                phenotypes.get(type).remove(pheno);
            }
                
        }

    }
    
    public void CreateInsertionArrays(HashMap<String,ArrayList<Phenotype>> phenotypes,int startCoordinate){
        for(String type:phenotypes.keySet()){
            //if(type.equals("Neg_Control")){
                
                InsertionArrays.put(type, new int[loadedReferences.get(type).size()]);
                
                for(Phenotype currentSample:phenotypes.get(type)){
                    //If the current sample has any Inserts
                    if(InsertFeatures.containsKey(currentSample.FileName)){
                        for(InsertFeature feature:InsertFeatures.get(currentSample.FileName)){
                            
                            int insertedPos = (feature.StartCoodinate+1) - startCoordinate;
                            
                            if(insertedPos>-1){
                                
                                if(feature.InsertedBases.size() == 6)
                                    System.err.println("");
                                
                                if(InsertionArrays.get(type)[insertedPos] < feature.InsertedBases.size())
                                    InsertionArrays.get(type)[insertedPos] = feature.InsertedBases.size();
                            }
                        }                        
                    }
                }
            //}//End Type Check
        }
    }
    
    public ArrayList<Read> GetReadsForSample(String sampleName){
        return readCollection.get(sampleName);
    }
    
    public int[] getInsertionArray(String phenotype){
        return InsertionArrays.get(phenotype);
    }
    
    public ArrayList<InsertFeature> GetInsertsForReadAtPosition(Read read,String sampleName,
            int columnPosition,int startCoordinate){
        ArrayList<InsertFeature> inserts = new ArrayList();
        if(InsertFeatures.containsKey(sampleName)){
            for(InsertFeature feature:InsertFeatures.get(sampleName)){
                if(feature.TargetReadID == read.ReadID && ((feature.StartCoodinate+1)-startCoordinate)==columnPosition)
                    inserts.add(feature);
            }
        }
        
        return inserts;
    }
    
    private ArrayList<String> GetInsertedBases(String inserts){
        ArrayList<String> bases = new ArrayList();
        
        for(int index = 0;index<inserts.length();index++){
            bases.add(Character.toString(inserts.charAt(index)));
        }
        
        return bases;
    }
}
