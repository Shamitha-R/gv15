package gv15;

import data.Read;
import data.auxiliary.CigarEvent;
import data.auxiliary.CigarFeature;
import data.auxiliary.CigarInsertEvent;
import data.auxiliary.Feature;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author ranasi01
 */
public class FragmentManager {
    
    private int maxReadCount;
    private String dataPath;
    private String cachePath;
    private ReadManager readManager;
    
    public FragmentManager(String dataPath,String cachePath){
        this.dataPath = dataPath;
        this.cachePath = cachePath;
        this.maxReadCount = 0;
    }
    
    public void ProcessFragments(HashMap<String,ArrayList<Phenotype>> phenotypes,ReferenceManager referenceManager,
            PanelManager panelManager,int flank,VariantContext currentVariant) throws Exception{
        
        int startCoord = currentVariant.getStart() - flank;
        int endCoord  = currentVariant.getStart() + flank;
        
        //Load the Read data for all Samples
        readManager = new ReadManager(referenceManager.getReferencePath(),
                dataPath,cachePath);
        readManager.LoadDataFromSamples(phenotypes, startCoord, endCoord,referenceManager,currentVariant);
        readManager.CreateInsertionArrays(phenotypes,startCoord);
                        
        //Adjust references for insertions
        referenceManager.AdjustReferences(readManager.InsertionArrays);

        //Create the Panel with Fragments using the extracted Data
        for(String type:phenotypes.keySet()){
            //if(type.equals("Neg_Control")){
 
                int maxReadCountForPhenotype = -1;
                
                int panelSize = referenceManager.AdjustedReferenceData.get(type).size();
                panelManager.GetPanelFromPhenotype(type).Fragments = new HashMap[panelSize];
                
                Map<String,FragmentNode>[] tempFrags = new HashMap[panelSize];
                //Create each panel column
                int totalInsertColumns = 0;
                int panelColumnNo = 0;
                for(int columnNo = 0;columnNo<readManager.InsertionArrays.get(type).length;columnNo++){

                    int insertCount = readManager.InsertionArrays.get(type)[columnNo];
                    int readCountForColumn = 0;
         
                    //Loop through all the samples for the phenotype
                    for(int sampleNo = 0;sampleNo<phenotypes.get(type).size();sampleNo++){

                        //Extract Reads for sample
                        int sampleReadCount = readManager.GetReadsForSample(
                                phenotypes.get(type).get(sampleNo).FileName).size();
                        readCountForColumn+=sampleReadCount;
               
                        //Loop through all the reads for the sample
                        for(int readNo = 0;readNo<sampleReadCount;readNo++){
                            gv15.Read currentRead = readManager.GetReadsForSample(
                                phenotypes.get(type).get(sampleNo).FileName).get(readNo);

                            String[] readBases = currentRead.BaseValues;
                            //Ensure that the read is within the target region
                            
                            int baseIndex = (startCoord - (currentRead.StartPosition+1)) + columnNo; 

                            if(baseIndex >= 0 && baseIndex < currentRead.Length){
                                String baseVal = readBases[baseIndex];
                                FragmentNode tempFragNode = new FragmentNode();  
                                    
                                if(tempFrags[panelColumnNo] == null)
                                    tempFrags[panelColumnNo] = new HashMap();
                                    
                                if(!tempFrags[panelColumnNo].containsKey(baseVal))
                                    tempFrags[panelColumnNo].put(baseVal, tempFragNode);
                                    
                                //Increment the Read Count
                                tempFrags[panelColumnNo].get(baseVal).ReadCount++;

                                //Get the Insert features of the current Read
                                ArrayList<InsertFeature> insertFeatures = readManager.GetInsertsForReadAtPosition(currentRead,
                                    phenotypes.get(type).get(sampleNo).FileName,columnNo,startCoord);
                                
                                //The last fragment does not have any connected fragments
                                if(panelColumnNo < tempFrags.length-1){
                                if(tempFrags[panelColumnNo].get(baseVal).ConnectedFragments == null)
                                    tempFrags[panelColumnNo].get(baseVal).ConnectedFragments = new HashMap();
                               
                                
                                //No inserts for this read therefore connect directly with the next Base
                                if(insertFeatures.isEmpty()){
                                    //Add connected Fragments
                                    if( (baseIndex+1) < currentRead.Length){
                                        String nextBaseVal = readBases[baseIndex+1];

                                        if(!tempFrags[panelColumnNo].get(baseVal).ConnectedFragments.containsKey(nextBaseVal))
                                            tempFrags[panelColumnNo].get(baseVal).ConnectedFragments.put(nextBaseVal, new HashSet());

                                        int connectionColumn = panelColumnNo+readManager.InsertionArrays.get(type)[columnNo]+1;
                                        if(connectionColumn < tempFrags.length)
                                            tempFrags[panelColumnNo].get(baseVal).ConnectedFragments.get(nextBaseVal).
                                                add(connectionColumn);

                                    }                                        
                                }else{
                                    for(InsertFeature insFeature:insertFeatures){
                                        //Add the inserted Fragments
                                        String finalConnectedBase = null;
                                        if( (baseIndex+1) < currentRead.Length)
                                            finalConnectedBase = readBases[baseIndex+1];

                                        AddInsertedBases(tempFrags, panelColumnNo+1, insFeature.InsertedBases,
                                                finalConnectedBase,panelColumnNo+readManager.InsertionArrays.get(type)[columnNo]+1);
                                        
                                        //Connect the current Fragment to the inserted Fragment
                                        String nextBaseVal = insFeature.InsertedBases.get(0);
                                       
                                        if(!tempFrags[panelColumnNo].get(baseVal).ConnectedFragments.containsKey(nextBaseVal))
                                            tempFrags[panelColumnNo].get(baseVal).ConnectedFragments.put(nextBaseVal, new HashSet());

                                        if( (panelColumnNo+1) < tempFrags.length)
                                            tempFrags[panelColumnNo].get(baseVal).ConnectedFragments.get(nextBaseVal).
                                                    add(panelColumnNo+1);
                                    }
                                }
                            }
                            }
                        }//End Read loop
                    }//End sample Loop
                    
                    //if(insertCount > 0)
                        //System.err.println("");
                    panelColumnNo+=(insertCount+1);
                    totalInsertColumns+=insertCount;
                    
                    if(readCountForColumn > maxReadCountForPhenotype)
                        maxReadCountForPhenotype = readCountForColumn;
                    
                }//End Column Loop    
                
                if(maxReadCountForPhenotype > maxReadCount)
                    maxReadCount = maxReadCountForPhenotype;
                
                //Add the fragments to the panel fragments
                panelManager.GetPanelFromPhenotype(type).Fragments = tempFrags;
                panelManager.MaxReadCount = maxReadCount;
               
                //Adjust for insertions
                int adjustedPos = 0;
                int addedVal = 0;
                for(int i = 0;i<flank+1;i++){
                    while(referenceManager.AdjustedReferenceData.get(type).get(adjustedPos).equals("INS")){
                        addedVal++; 
                        adjustedPos++;   
                    }
                    adjustedPos++;
                }
                referenceManager.ShiftVals.put(type, addedVal);
                
            //}//End Type Check            
        }//End Phenotype Loop
    }
    
    public void AddInsertedBases(Map<String,FragmentNode>[] fragments, int index,
            ArrayList<String> insertedBases,String finalConnectedBase,int finalConnectedColumn){
        
        for(int insertIndex = 0;insertIndex<insertedBases.size();insertIndex++){
        
            //Add the inserted Base
            if(fragments[index+insertIndex] == null)
                fragments[index+insertIndex] = new HashMap();

            FragmentNode tempFragNode = new FragmentNode();  
            String insertedBase = insertedBases.get(insertIndex);
            
            if(!fragments[index+insertIndex].containsKey(insertedBase))
                fragments[index+insertIndex].put(insertedBase, tempFragNode);
                                    
            //Increment the Read Count
            fragments[index+insertIndex].get(insertedBase).ReadCount++;
            
            if(fragments[index+insertIndex].get(insertedBase).ConnectedFragments == null)
                fragments[index+insertIndex].get(insertedBase).ConnectedFragments = new HashMap();

            //Add the next Base as the connected Fragment
            if(insertIndex < insertedBases.size()-1){
                String nextInsertedBase = insertedBases.get(insertIndex+1);
                                                                       
                if(!fragments[index+insertIndex].get(insertedBase).ConnectedFragments.containsKey(nextInsertedBase))
                    fragments[index+insertIndex].get(insertedBase).ConnectedFragments.put(nextInsertedBase, new HashSet());
                
                if( (index+insertIndex+1) < fragments.length )
                    fragments[index+insertIndex].get(insertedBase).ConnectedFragments.get(nextInsertedBase).
                        add(index+insertIndex+1);
                
            }else if (insertIndex == insertedBases.size()-1){
                //Add the next non-insert base as the connected fragment of the last Insert
                
                if(!fragments[index+insertIndex].get(insertedBase).ConnectedFragments.containsKey(finalConnectedBase))
                    fragments[index+insertIndex].get(insertedBase).ConnectedFragments.put(finalConnectedBase, new HashSet());

                if( finalConnectedColumn < fragments.length )
                    fragments[index+insertIndex].get(insertedBase).ConnectedFragments.get(finalConnectedBase).
                        add(finalConnectedColumn);                
            }

        }
                  
    }    

    public void FragmentPrinter(Map<String,FragmentNode>[] fragments){
        System.out.println("Printing Fragments\n");
        
        for(int index = 0;index<fragments.length;index++){
            if(fragments[index]!=null){
                for(String baseVal:fragments[index].keySet()){
                    System.out.print(baseVal + "(" + fragments[index].get(baseVal).ReadCount + ") ");
                    
                    for(String connectedFrag:fragments[index].
                            get(baseVal).ConnectedFragments.keySet()){
                        System.out.print(connectedFrag);
                        for(Object connectedIndex:fragments[index].get(baseVal).ConnectedFragments.get(connectedFrag)){
                            System.out.print("["+connectedIndex+"]");
                        }
                    }
                    
                    System.out.println("");
                }
            }
            System.out.println("");
        }
    }
}
