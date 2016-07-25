/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gv15;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import  io.*;
import data.cache.*;
import data.*;
import analysis.PackCreator;
import com.opencsv.CSVReader;
import data.auxiliary.CigarEvent;
import data.auxiliary.CigarFeature;
import data.auxiliary.CigarInsertEvent;
import data.auxiliary.Feature;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 *
 * @author ranasi01
 */
public class Gv15 extends Application {
    
    private static final double WIDTH = 1280, HEIGHT = 720;
    private static ArrayList<String> ReferenceData;
    private static int MAXREADCOUNT;
       
    static int Flank = 7;
    static int ShiftVal = 0;
    static int VariancePos = 871334;
    
    //Render Area Settings
    static float GridStartX = 150;
    static float GridStartY = 200;
    static int ColumnWidth = 65;
    static int RowHeight = 20;
    int FragmentXOffset = 20; 


    private static class Phenotype{
        private enum PhenotypeCat{ NORMAL, CONTROL, CIN3, NEG_CONTROL };
        
        public String FID;
        public String IID;
        public String FileName; 
        public PhenotypeCat Type;
    }
    private static ArrayList<Phenotype> Phenotypes = new ArrayList();
        
    private static Map<String,FragmentNode>[] Fragments;
    
    static ArrayList<String> testReference;
    static VariantContext testContext;
    
    private static Panel testPanel;
      
    @Override public void start(Stage stage) {

        Group root = new Group();         
        testPanel.RenderPanel(root,testReference,MAXREADCOUNT);

        root.getChildren().add(SetupChartDetails((int) (WIDTH/2), 100));

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
    
    private Text SetupChartDetails(int startX,int startY){
        Text details = new Text(startX,startY,"");
        details.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        details.setFill(Color.LIGHTSLATEGREY);
        //details.setA
        
        String displayText = testContext.getContig() +
                ":" + testContext.getStart() + " ";
        
        for(Allele curAllele:testContext.getAlleles()){
            for(byte base:curAllele.getBases()){
                displayText+=Character.toString ((char) base);
            }
            displayText+=">";
        }

        details.setText(displayText.substring(0, displayText.length()-1));

        return details;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Process();

        launch(args);     
    }
    
    private static void Process() throws Exception{

        //Setup Panels
        testPanel = new Panel("Control", GridStartX, GridStartY,(Flank*2)+1,5,
        ColumnWidth,RowHeight);
        
        //Import Variants
        File vcfFile = new File("C:\\Users\\ranasi01\\Documents\\Project\\Data\\variants.vcf");
        VCFFileReader reader = new VCFFileReader(vcfFile, false);
        VCFHeader headerOne = reader.getFileHeader();
        
        List<String> sampleList = new ArrayList<String>();
        sampleList.addAll(headerOne.getSampleNamesInOrder());

        for(VariantContext context:reader.iterator().toList()){
            testContext = context;
            break;
        }
         
        SetPrefsFile();

         //Import phenotypes
        CSVReader csvReader = new CSVReader(new FileReader("C:\\Users\\ranasi01\\Documents\\Project\\Data\\phenotype.csv"));
        String [] nextLine;
        
        csvReader.readNext();
        while ((nextLine = csvReader.readNext()) != null) {
            Phenotype tempPT = new Phenotype();
            tempPT.FID = nextLine[0];
            tempPT.IID = nextLine[1];
            
            String fName = nextLine[2].substring(24);
            tempPT.FileName = fName; 
            
            Phenotypes.add(tempPT);
        }
        
        String[] bamFiles = new String[]{           
            //"samples\\chr1_871234_871434_DA0059011_IonXpress_001_rawlib.bam",
            "samples\\chr1_871234_871434_DA0057131_IonXpress_002_rawlib.bam",
            "samples\\chr1_871234_871434_DA0057156_IonXpress_003_rawlib.bam",
            //"samples\\chr1_871234_871434_DA0057131_IonXpress_004_rawlib.bam"
            //"samples\\chr1_871234_871434_DA0059025_IonXpress_005_rawlib.bam"
            //"samples\\chr1_871234_871434_DA0057129_IonXpress_012_rawlib.bam",
            //"data00.bam"
        };
                
        ArrayList<ArrayList<String>> ReferenceDataCollection = new ArrayList();
        ArrayList<Map<String,FragmentNode>[]> FragmentsCollection = new ArrayList();
        
        for(int sampleNo = 0;sampleNo<bamFiles.length;sampleNo++){
            
            if(ReferenceDataCollection.size() >= 2){
                ReferenceDataCollection.clear();
                FragmentsCollection.clear();
                
                ReferenceDataCollection.add(testReference);
                FragmentsCollection.add(testPanel.Fragments);
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


            //int startCoord = 32890549;
            int startCoord = VariancePos - Flank;
            int endCoord  = VariancePos + Flank;

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
            ReferenceData = new ArrayList();
            for(int i = 0;i<referenceData.length;i++){
                ReferenceData.add(GetBase(referenceData[i]));
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
                        ShiftVal++;

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
                        ReferenceData.add(insertStartPos+insNum+insertCount, "INS");
                    }

                    insertCount+=maxInsertions;
                }         
            }

            //Extract Reads
            MAXREADCOUNT += reads.size();

            Fragments = new HashMap[ReferenceData.size()];

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

                   while(ReferenceData.get(testIndex) == "INS"){

                        for(int insNum = 0;insNum<insertFeatures.size();insNum++){
                            Feature event = insertFeatures.get(insNum);
                            CigarFeature cf = (CigarFeature)event; 
                                for(CigarEvent e : cf.getEvents()){
                                    Read read = e.getRead();
                                    CigarInsertEvent insEvent = (CigarInsertEvent)e;
                                    if(read.getID() == readArr[count].getID()){
                                        int insertStartPos = (cf.getDataPS()+2)-startCoord+insCount;

                                        if(insertStartPos == testIndex){

                                            if(Fragments[testIndex]==null){
                                                Fragments[testIndex] = new HashMap();
                                                FragmentNode tempNode = new FragmentNode();
                                                tempNode.ReadCount = 1;

                                                if(colIndex < indexes.length - 1){
                                                    int nextIndex = ProbeToNextFragment(testIndex,ReferenceData);
                                                    if(indexes[colIndex+1] != -1){
                                                        tempNode.ConnectedFragments.put(GetBase(testRMD.getStateAt(indexes[colIndex])), 
                                                            new HashSet<Integer>());
                                                        tempNode.ConnectedFragments.get(GetBase(testRMD.getStateAt(indexes[colIndex]))).
                                                                add(nextIndex);
                                                    }
                                                }               

                                                Fragments[testIndex].put(insEvent.getInsertedBases(), tempNode);
                                            }else if(!Fragments[testIndex].containsKey(insEvent.getInsertedBases())){
                                                FragmentNode tempNode = new FragmentNode();
                                                tempNode.ReadCount = 1;

                                                if(colIndex < indexes.length - 1){
                                                    int nextIndex = ProbeToNextFragment(testIndex,ReferenceData);
                                                    if(indexes[colIndex+1] != -1){
                                                        tempNode.ConnectedFragments.put(GetBase(testRMD.getStateAt(indexes[colIndex])), 
                                                            new HashSet<Integer>());
                                                        tempNode.ConnectedFragments.get(GetBase(testRMD.getStateAt(indexes[colIndex]))).
                                                                add(nextIndex);
                                                    }
                                                } 

                                                Fragments[testIndex].put(insEvent.getInsertedBases(), tempNode);
                                            }else{
                                                if(colIndex < indexes.length - 1){
                                                    int nextIndex = ProbeToNextFragment(testIndex,ReferenceData);
                                                    if(!Fragments[testIndex].get(insEvent.getInsertedBases()).ConnectedFragments.
                                                            containsKey(GetBase(testRMD.getStateAt(indexes[colIndex+1])))){
                                                        if(indexes[colIndex+1] != -1){
                                                            Fragments[testIndex].get(insEvent.getInsertedBases()).ConnectedFragments.
                                                                    put(GetBase(testRMD.getStateAt(indexes[colIndex])), 
                                                                    new HashSet<Integer>());
                                                            Fragments[testIndex].get(insEvent.getInsertedBases()).ConnectedFragments.
                                                                    get(GetBase(testRMD.getStateAt(indexes[colIndex]))).
                                                                    add(nextIndex);
                                                        }
                                                    }
                                                }
                                                Fragments[testIndex].get(insEvent.getInsertedBases()).ReadCount++;
                                            }

                                            //Add to previous
                                            if(colIndex!=0){
                                                String prevVal = GetBase(testRMD.getStateAt(indexes[colIndex]-1));

                                                if(Fragments[testIndex-1].containsKey(prevVal)){
                                                    if(Fragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                            containsKey(insEvent.getInsertedBases())){
                                                        Fragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                                get(insEvent.getInsertedBases()).
                                                                add(testIndex);
                                                    }else
                                                    {
                                                        Fragments[testIndex-1].get(prevVal).ConnectedFragments.
                                                                put(insEvent.getInsertedBases(), new HashSet<Integer>());
                                                        Fragments[testIndex-1].get(prevVal).ConnectedFragments.
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
                   }

                    if(testRMD == null || indexes[colIndex] >= testRMD.length() || indexes[colIndex] == -1){
                        break;
                    }

                    byte rmdData = testRMD.getStateAt(indexes[colIndex]);
                    String val = GetBase(rmdData);

                        if(Fragments[testIndex] == null){
                            Fragments[testIndex] = new HashMap();
                            FragmentNode tempNode = new FragmentNode();
                            tempNode.ReadCount = 1;

                            if(colIndex < indexes.length - 1){
                                int nextIndex = ProbeToNextFragment(testIndex,ReferenceData);
                                if(indexes[colIndex+1] != -1){
                                    tempNode.ConnectedFragments.put(GetBase(testRMD.getStateAt(indexes[colIndex+1])), 
                                            new HashSet<Integer>());
                                    tempNode.ConnectedFragments.get(GetBase(testRMD.getStateAt(indexes[colIndex+1]))).
                                            add(nextIndex);
                                }
                            }
                            Fragments[testIndex].put(val, tempNode);
                        }else if(!Fragments[testIndex].containsKey(val)){
                            FragmentNode tempNode = new FragmentNode();
                            tempNode.ReadCount = 1;
                            if(colIndex < indexes.length - 1){
                                int nextIndex = ProbeToNextFragment(testIndex,ReferenceData);
                                if(indexes[colIndex+1] != -1){
                                    tempNode.ConnectedFragments.put(GetBase(testRMD.getStateAt(indexes[colIndex+1])), 
                                            new HashSet<Integer>());
                                    tempNode.ConnectedFragments.get(GetBase(testRMD.getStateAt(indexes[colIndex+1]))).
                                            add(nextIndex);                               
                                }
                            }
                            Fragments[testIndex].put(val, tempNode);
                        }else{
                             if(colIndex < indexes.length - 1){
                                int nextIndex = ProbeToNextFragment(testIndex,ReferenceData);

                                if(!Fragments[testIndex].get(val).ConnectedFragments.
                                        containsKey(GetBase(testRMD.getStateAt(indexes[colIndex+1])))){
                                    if(indexes[colIndex+1] != -1){
                                        Fragments[testIndex].get(val).ConnectedFragments.
                                                put(GetBase(testRMD.getStateAt(indexes[colIndex+1])), 
                                                new HashSet<Integer>());
                                        Fragments[testIndex].get(val).ConnectedFragments.
                                                get(GetBase(testRMD.getStateAt(indexes[colIndex+1]))).
                                                add(nextIndex);
                                    }
                                }else{
    //                                Fragments[testIndex].get(val).ConnectedFragments.
    //                                            get(GetBase(testRMD.getStateAt(indexes[colIndex+1]))).
    //                                        add(nextIndex);  
                                }
                            }
                            Fragments[testIndex].get(val).ReadCount++;
                        }

                    testIndex++;
                }
            }
            ReferenceDataCollection.add(ReferenceData);
            FragmentsCollection.add(Fragments);
            FragmentPrinter(Fragments);
        
            if(sampleNo>=1){
                //Combine References  
                testReference = CombineReferences(ReferenceDataCollection.get(0), 
                        ReferenceDataCollection.get(1));

                AdjustForInsertions(ReferenceDataCollection.get(1), FragmentsCollection.get(1), testReference);
                AdjustForInsertions(ReferenceDataCollection.get(0), FragmentsCollection.get(0), testReference);

                //Combine Fragments
                testPanel.Fragments = CombineFragments(FragmentsCollection.get(0), FragmentsCollection.get(1), 
                        ReferenceDataCollection.get(0), ReferenceDataCollection.get(1), testReference.size());

                FragmentPrinter(testPanel.Fragments);                
            }else{
                testReference = ReferenceDataCollection.get(0);
                testPanel.Fragments = FragmentsCollection.get(0);
            }
        }
    }
    
    private static ArrayList<String> CombineReferences(ArrayList<String> ref00,ArrayList<String> ref01){
        ArrayList<String> combineRef = new ArrayList();
                
        int index00 = 0;
        int index01 = 0;
        while(index00<ref00.size() &&
                index01<ref01.size()){

            if(ref00.get(index00).
                    equals(ref01.get(index01))){
                combineRef.add(ref00.get(index00));
                index00++;
                index01++;
            }else if(ref00.get(index00).equals("INS")){
                combineRef.add("INS");
                index00++;
            }else if(ref01.get(index01).equals("INS")){
                combineRef.add("INS");
                index01++;
            }
        }        
        
        return combineRef;
    }
    
    private static Map<String,FragmentNode>[] CombineFragments(Map<String,FragmentNode>[] frag00,
            Map<String,FragmentNode>[] frag01,
            ArrayList<String> ref00,ArrayList<String> ref01,
            int size){
        Map<String,FragmentNode>[] combinedFragments = new HashMap[size];
        
        int index00 = 0;
        int index01 = 0;
        for(int indexMaster = 0;indexMaster<testReference.size();indexMaster++){

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
    
    private static void AdjustForInsertions(ArrayList<String> primaryRef,
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
    
    private static void FragmentPrinter(Map<String,FragmentNode>[] fragments){
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
    
    private static int ProbeToNextFragment(int currentIndex,ArrayList<String> testReference){
        currentIndex++;
        while(testReference.get(currentIndex)=="INS")
            currentIndex++;
        
        return currentIndex;
    }
    
    private static File SetPrefsFile(){
	// Ensure the .scri-bioinf folder exists
	File fldr = new File("C:\\Users\\ranasi01\\Documents\\Project\\gv15", ".scri-bioinf");
	fldr.mkdirs();  
        
        // Color-prefs file
	//ColorPrefs.setFile(new File(fldr, "tablet-colors.xml"));

	// Cached reference file
	ConsensusFileCache.setIndexFile(new File(fldr, "tablet-refs.xml"));
        // This is the file we really want
        File file = new File(fldr, "tablet.xml");
        // So if it exists, just use it
        if (file.exists())
            return file;   
        
        return null;
    }
    
    private static String GetBase(int val){
        switch (val){
            case 6: return "A";
            case 7: return "A";
            case 8: return "C";
            case 9: return "C";
            case 10: return "G";
            case 11: return "G";
            case 12: return "T";
            case 13: return "T";
            
            default: break;
        }
        
        return "N";
    }     
}
