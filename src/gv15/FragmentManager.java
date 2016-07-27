package gv15;

import analysis.PackCreator;
import data.Assembly;
import data.Consensus;
import data.Contig;
import data.IReadManager;
import data.LineData;
import data.Read;
import data.ReadMetaData;
import data.auxiliary.CigarEvent;
import data.auxiliary.CigarFeature;
import data.auxiliary.CigarInsertEvent;
import data.auxiliary.Feature;
import static gv15.Gv15.VariancePos;
import io.AssemblyFile;
import io.AssemblyFileHandler;
import io.TabletFile;
import io.TabletFileHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
    
    public FragmentManager(){
        
    }
    
    public void ProcessFragments(String[] bamFiles,ReferenceManager referenceManager,
            PanelManager panelManager,int flank) throws Exception{

        ArrayList<ArrayList<String>> ReferenceDataCollection = new ArrayList();
        ArrayList<Map<String,FragmentNode>[]> FragmentsCollection = new ArrayList();
        
        ArrayList<String> tempReference = new ArrayList();
        Map<String,FragmentNode>[] tempFragments;
        
        for(int sampleNo = 0;sampleNo<bamFiles.length;sampleNo++){
            if(ReferenceDataCollection.size() >= 2){
                ReferenceDataCollection.clear();
                FragmentsCollection.clear();
                
                ReferenceDataCollection.add(referenceManager.ReferenceData);
                //TODO
                FragmentsCollection.add(panelManager.enginePanels.get(0).Fragments);
            }
            
            String[] filenames  = new String[]{
                "C:\\Users\\ranasi01\\Documents\\Project\\Data\\" + bamFiles[sampleNo],
                "C:\\Users\\ranasi01\\Documents\\Project\\Data\\ref.fasta"
            };
            
            TabletFile tabletFile;
            tabletFile = TabletFileHandler.createFromFileList(filenames);
            
            AssemblyFile[] files = tabletFile.getFileList();
            File cacheDir = new File("D:\\");

            AssemblyFileHandler assemblyFileHandler = new 
                AssemblyFileHandler(files, cacheDir);

            assemblyFileHandler.runJob(0);

            TabletFileHandler.addAsMostRecent(tabletFile);
            Assembly assembly = assemblyFileHandler.getAssembly();

            assembly.getAssemblyStatistics().setAssembly(tabletFile.assembly);
            assembly.getAssemblyStatistics().setReference(tabletFile.assembly);

            //Loading data from contigs
            Contig selectedCotig = assembly.getContig(0);
            
            //TODO
            int startCoord = VariancePos - flank;
            int endCoord  = VariancePos + flank;

            if(assembly.getBamBam() != null){
                //Set Location
                assembly.getBamBam().setSize(endCoord-startCoord);
                assembly.getBamBam().setBlockStart(selectedCotig, startCoord);

                selectedCotig.clearCigarFeatures();
                assembly.getBamBam().loadDataBlock(selectedCotig);           
                assembly.getBamBam().indexNames();
            }
            //Extracting Referecne Data
            Consensus consensus = selectedCotig.getConsensus();            
            byte[] referenceData = consensus.getRange(startCoord-1, endCoord-1);
            tempReference = new ArrayList();
            for(int i = 0;i<referenceData.length;i++){
                tempReference.add(UtilityFunctions.getInstance().GetBaseFromVal(referenceData[i]));
            }
            
            //Sorting Reads
            selectedCotig.getReads().trimToSize();
            Collections.sort(selectedCotig.getReads());
            selectedCotig.calculateOffsets(assembly);
            
            //Packing Reads
            PackCreator packCreator = new PackCreator(selectedCotig, false);
            packCreator.runJob();
            
            //Packing Reads               
            IReadManager reads = null;       
            reads = selectedCotig.getPackManager();
            
            //Extract Insert Features
            ArrayList<Feature> insertFeatures = new ArrayList();            
            int insertCount = 0;
            for(int i= 0;i<selectedCotig.getFeatures().size();i++){
                Feature currentFeature = selectedCotig.getFeatures().get(i);

                boolean filter = (currentFeature.getDataPS() > VariancePos - 3
                        && currentFeature.getDataPS() < VariancePos);

                if(currentFeature.getGFFType().equals("CIGAR-I") &&
                        currentFeature.getDataPS() >= startCoord-2 && 
                        currentFeature.getDataPE() <= endCoord &&
                        filter){
                    insertFeatures.add(currentFeature);

                    if(currentFeature.getDataPS() > VariancePos - 3
                            && currentFeature.getDataPS() < VariancePos - 1)
                        referenceManager.ShiftVal++;

                    //Add the inserted value to the Reference data
                    //and shift the Referecne values
                    int maxInsertions = -1;
                    CigarFeature cf = (CigarFeature)currentFeature;
                    for(CigarEvent e : cf.getEvents()){
                        CigarInsertEvent insEv = (CigarInsertEvent)e;

                        int insertLength = insEv.getInsertedBases().length();
                        if(insertLength>maxInsertions)
                            maxInsertions = insertLength;
                    }

                    int insertStartPos = (currentFeature.getDataPS()+2)-startCoord;

                    for(int insNum = 0;insNum<maxInsertions;insNum++){
                        tempReference.add(insertStartPos+insNum+insertCount, "INS");
                    }

                    insertCount+=maxInsertions;
                }    
            }
            
            //Extract Reads
            maxReadCount += reads.size();        
            tempFragments = new HashMap[tempReference.size()];
                
            for(int readNo = 0;readNo<reads.size();readNo++){
                LineData lineData = reads.getPixelData(readNo, 0+startCoord-1, 15, 1f, true);

                ReadMetaData[] rmds = lineData.getRMDs();
                int[] indexes = lineData.getIndexes();
                Read[] readArr = lineData.getReads();
                
                int count = 0;
                while(rmds[count] == null && count < rmds.length){
                    count++;
                }
                ReadMetaData testRMD = rmds[count];

                int testIndex = 0;
                int insCount = 0;   
                for(int colIndex = 0;colIndex<indexes.length;colIndex++){
                   while(tempReference.get(testIndex) == "INS"){
                       for(int insNum = 0;insNum<insertFeatures.size();insNum++){
                            Feature event = insertFeatures.get(insNum);
                            CigarFeature cf = (CigarFeature)event; 
                                for(CigarEvent e : cf.getEvents()){
                                    Read read = e.getRead();
                                    CigarInsertEvent insEvent = (CigarInsertEvent)e;
                                    if(read.getID() == readArr[count].getID()){
                                        int insertStartPos = (cf.getDataPS()+2)-startCoord+insCount;

                                        if(insertStartPos == testIndex){

                                            if(tempFragments[testIndex]==null){
                                                tempFragments[testIndex] = new HashMap();
                                                FragmentNode tempNode = new FragmentNode();
                                                tempNode.ReadCount = 1;

                                                if(colIndex < indexes.length - 1){
                                                    int nextIndex = ProbeToNextFragment(testIndex,tempReference);
                                                    if(indexes[colIndex+1] != -1){
                                                        tempNode.ConnectedFragments.put(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                testRMD.getStateAt(indexes[colIndex])), 
                                                            new HashSet<Integer>());
                                                        tempNode.ConnectedFragments.get(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                testRMD.getStateAt(indexes[colIndex]))).
                                                                add(nextIndex);
                                                    }
                                                }
                                                tempFragments[testIndex].put(insEvent.getInsertedBases(), tempNode);
                                            }else if(!tempFragments[testIndex].containsKey(insEvent.getInsertedBases())){
                                                FragmentNode tempNode = new FragmentNode();
                                                tempNode.ReadCount = 1;

                                                if(colIndex < indexes.length - 1){
                                                    int nextIndex = ProbeToNextFragment(testIndex,tempReference);
                                                    if(indexes[colIndex+1] != -1){
                                                        tempNode.ConnectedFragments.put(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                testRMD.getStateAt(indexes[colIndex])), 
                                                            new HashSet<Integer>());
                                                        tempNode.ConnectedFragments.get(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                testRMD.getStateAt(indexes[colIndex]))).
                                                                add(nextIndex);
                                                    }
                                                } 

                                                tempFragments[testIndex].put(insEvent.getInsertedBases(), tempNode);
                                            }else{
                                                if(colIndex < indexes.length - 1){
                                                    int nextIndex = ProbeToNextFragment(testIndex,tempReference);
                                                    if(!tempFragments[testIndex].get(insEvent.getInsertedBases()).ConnectedFragments.
                                                            containsKey(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                    testRMD.getStateAt(indexes[colIndex+1])))){
                                                        if(indexes[colIndex+1] != -1){
                                                            tempFragments[testIndex].get(insEvent.getInsertedBases()).ConnectedFragments.
                                                                    put(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                            testRMD.getStateAt(indexes[colIndex])), 
                                                                    new HashSet<Integer>());
                                                            tempFragments[testIndex].get(insEvent.getInsertedBases()).ConnectedFragments.
                                                                    get(UtilityFunctions.getInstance().GetBaseFromVal(
                                                                            testRMD.getStateAt(indexes[colIndex]))).
                                                                    add(nextIndex);
                                                        }
                                                    }
                                                }
                                                tempFragments[testIndex].get(insEvent.getInsertedBases()).ReadCount++;
                                            }

                                            //Add to previous
                                            if(colIndex!=0){
                                                String prevVal = UtilityFunctions.getInstance().GetBaseFromVal(
                                                        testRMD.getStateAt(indexes[colIndex]-1));

                                                if(tempFragments[testIndex-1].containsKey(prevVal)){
                                                    if(tempFragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                            containsKey(insEvent.getInsertedBases())){
                                                        tempFragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                                get(insEvent.getInsertedBases()).
                                                                add(testIndex);
                                                    }else
                                                    {
                                                        tempFragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                                put(insEvent.getInsertedBases(), new HashSet<Integer>());
                                                        tempFragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                                get(insEvent.getInsertedBases()).add(testIndex);
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                        }                    
                       testIndex++;
                       insCount++;           
                   }//End insert handling loop 
                    if(testRMD == null || indexes[colIndex] >= testRMD.length() || indexes[colIndex] == -1){
                        break;
                    }

                    byte rmdData = testRMD.getStateAt(indexes[colIndex]);
                    String val = UtilityFunctions.getInstance().GetBaseFromVal(rmdData);

                        if(tempFragments[testIndex] == null){
                            tempFragments[testIndex] = new HashMap();
                            FragmentNode tempNode = new FragmentNode();
                            tempNode.ReadCount = 1;

                            if(colIndex < indexes.length - 1){
                                int nextIndex = ProbeToNextFragment(testIndex,tempReference);
                                if(indexes[colIndex+1] != -1){
                                    tempNode.ConnectedFragments.put(UtilityFunctions.getInstance().GetBaseFromVal(
                                            testRMD.getStateAt(indexes[colIndex+1])), 
                                            new HashSet<Integer>());
                                    tempNode.ConnectedFragments.get(UtilityFunctions.getInstance().GetBaseFromVal(
                                            testRMD.getStateAt(indexes[colIndex+1]))).
                                            add(nextIndex);
                                }
                            }
                            tempFragments[testIndex].put(val, tempNode);
                        }else if(!tempFragments[testIndex].containsKey(val)){
                            FragmentNode tempNode = new FragmentNode();
                            tempNode.ReadCount = 1;
                            if(colIndex < indexes.length - 1){
                                int nextIndex = ProbeToNextFragment(testIndex,tempReference);
                                if(indexes[colIndex+1] != -1){
                                    tempNode.ConnectedFragments.put(UtilityFunctions.getInstance().GetBaseFromVal(
                                            testRMD.getStateAt(indexes[colIndex+1])), 
                                            new HashSet<Integer>());
                                    tempNode.ConnectedFragments.get(UtilityFunctions.getInstance().GetBaseFromVal(
                                            testRMD.getStateAt(indexes[colIndex+1]))).
                                            add(nextIndex);                               
                                }
                            }
                            tempFragments[testIndex].put(val, tempNode);
                        }else{
                             if(colIndex < indexes.length - 1){
                                int nextIndex = ProbeToNextFragment(testIndex,tempReference);

                                if(!tempFragments[testIndex].get(val).ConnectedFragments.
                                        containsKey(UtilityFunctions.getInstance().GetBaseFromVal(
                                                testRMD.getStateAt(indexes[colIndex+1])))){
                                    if(indexes[colIndex+1] != -1){
                                        tempFragments[testIndex].get(val).ConnectedFragments.
                                                put(UtilityFunctions.getInstance().GetBaseFromVal(
                                                        testRMD.getStateAt(indexes[colIndex+1])), 
                                                new HashSet<Integer>());
                                        tempFragments[testIndex].get(val).ConnectedFragments.
                                                get(UtilityFunctions.getInstance().GetBaseFromVal(
                                                        testRMD.getStateAt(indexes[colIndex+1]))).
                                                add(nextIndex);
                                    }
                                }else{
    //                                Fragments[testIndex].get(val).ConnectedFragments.
    //                                            get(GetBase(testRMD.getStateAt(indexes[colIndex+1]))).
    //                                        add(nextIndex);  
                                }
                            }
                            tempFragments[testIndex].get(val).ReadCount++;
                        }

                    testIndex++;
                }//end looping columns
            }//end reading reads
            ReferenceDataCollection.add(tempReference);
            FragmentsCollection.add(tempFragments);
            //FragmentPrinter(Fragments);
        
            if(sampleNo>=1){
                //Combine References  
                referenceManager.ReferenceData = referenceManager.
                        CombineReferences(ReferenceDataCollection.get(0), 
                        ReferenceDataCollection.get(1));

                AdjustForInsertions(ReferenceDataCollection.get(1), 
                        FragmentsCollection.get(1), referenceManager.ReferenceData);
                AdjustForInsertions(ReferenceDataCollection.get(0), 
                        FragmentsCollection.get(0), referenceManager.ReferenceData);

                //Combine Fragments
                panelManager.enginePanels.get(0).Fragments = 
                        CombineFragments(referenceManager.ReferenceData,
                        FragmentsCollection.get(0), FragmentsCollection.get(1), 
                        ReferenceDataCollection.get(0), ReferenceDataCollection.get(1),
                        referenceManager.ReferenceData.size());

                //FragmentPrinter(panelManager.enginePanels.get(0).Fragments);                
            }else{
                referenceManager.ReferenceData = ReferenceDataCollection.get(0);
                panelManager.enginePanels.get(0).Fragments = FragmentsCollection.get(0);
                //testPanel2.Fragments = FragmentsCollection.get(0);
            }
        }//End aggregating samples
    }
    
    public int getMaxReadCount(){
        return maxReadCount;
    }
    
    public Map<String,FragmentNode>[] CombineFragments(ArrayList<String> referenceData,
            Map<String,FragmentNode>[] frag00,
            Map<String,FragmentNode>[] frag01,
            ArrayList<String> ref00,ArrayList<String> ref01,
            int size){
        Map<String,FragmentNode>[] combinedFragments = new HashMap[size];
        
        int index00 = 0;
        int index01 = 0;
        for(int indexMaster = 0;indexMaster<referenceData.size();indexMaster++){

            if(ref00.get(index00).
                equals(ref01.get(index01))){

                combinedFragments[indexMaster] = frag00[index00];
                    
                for(String comparedBase:frag01[index01].keySet()){
                    if(combinedFragments[indexMaster].containsKey(comparedBase)){
                        //Update the read count 
                        combinedFragments[indexMaster].get(comparedBase).ReadCount+=
                                frag01[index01].get(comparedBase).ReadCount;
                        //Update connected reads
                        for(String connectedRead:frag01[index01].
                                get(comparedBase).ConnectedFragments.keySet()){
                            //Both Fragments have this connected read
                            if(combinedFragments[indexMaster].get(comparedBase).
                                    ConnectedFragments.containsKey(connectedRead)){
                                //Merge connected columns
                                combinedFragments[indexMaster].get(comparedBase).ConnectedFragments.
                                        get(connectedRead).addAll(
                                             frag01[index01].
                                                    get(comparedBase).ConnectedFragments.get(connectedRead)
                                        );
                            }else{
                                combinedFragments[indexMaster].get(comparedBase).ConnectedFragments.
                                        put(connectedRead, frag01[index01].
                                                    get(comparedBase).ConnectedFragments.get(connectedRead));
                            }
                        }
                    }else{
                        combinedFragments[indexMaster].put(comparedBase, 
                                frag01[index01].get(comparedBase));
                    }
                }

                index00++;
                index01++;
            }else{
                if(ref00.get(index00).equals("INS")){
                    
                    if(combinedFragments[indexMaster] == null)
                        combinedFragments[indexMaster]= new HashMap();
                    
                    combinedFragments[indexMaster].putAll(frag00[index00]);                    
                    index00++;
                }
                else if(ref01.get(index01).equals("INS")){
                    
                    if(combinedFragments[indexMaster] == null)
                        combinedFragments[indexMaster]= new HashMap();
                    
                    combinedFragments[indexMaster].putAll(frag01[index01]); 
                  
                    index01++;
                }
            }     
        }        
        
        return combinedFragments;
    }
    
    public void AdjustForInsertions(ArrayList<String> primaryRef,
            Map<String,FragmentNode>[] fragments,ArrayList<String> combinedRef){

        int primaryIndex = 0;
        int offset = 0;
        for(int combinedIndex = 0;combinedIndex<combinedRef.size()-1;combinedIndex++){
            if(combinedRef.get(combinedIndex+1).equals("INS") &&
                    !primaryRef.get(primaryIndex+1).equals("INS"))
            {
                offset = ProbeToNextFragment(combinedIndex, combinedRef) - combinedIndex - 1;
                combinedIndex = offset + combinedIndex;

            }
            
                            
                for(String baseType:fragments[primaryIndex].keySet()){
                    for(String connectedRead:fragments[primaryIndex].
                        get(baseType).ConnectedFragments.keySet()){
                        
                            Iterator it=fragments[primaryIndex].
                                get(baseType).ConnectedFragments.get(connectedRead).iterator();
                            HashSet testHash = new HashSet();
                            while(it.hasNext())
                            {
                                int val = (int)it.next();
                                testHash.add(val+offset);
                            }

                            fragments[primaryIndex].
                                get(baseType).ConnectedFragments.get(connectedRead).clear();
                            fragments[primaryIndex].
                                get(baseType).ConnectedFragments.get(connectedRead).addAll(testHash);
                            }
                }
            
            primaryIndex++;
        }
    }
    
    public int ProbeToNextFragment(int currentIndex,ArrayList<String> reference){
        currentIndex++;
        while(reference.get(currentIndex)=="INS")
            currentIndex++;
        
        return currentIndex;
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
