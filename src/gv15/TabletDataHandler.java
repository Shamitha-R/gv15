package gv15;

import analysis.PackCreator;
import data.Assembly;
import data.Consensus;
import data.Contig;
import data.IReadManager;
import io.AssemblyFile;
import io.AssemblyFileHandler;
import io.TabletFile;
import io.TabletFileHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ranasi01
 */
public class TabletDataHandler {
    
    private String cachePath;
    private ArrayList<String> loadedReference;
    private IReadManager reads;

    public TabletDataHandler(String cachePath){
        this.cachePath = cachePath;
    }
    
    public void ExtractDataAtCoordinates(String[] fileNames,int startCoordinate,
            int endCoordinate,int contigNumber) throws Exception{
        
        TabletFile tabletFile;
        tabletFile = TabletFileHandler.createFromFileList(fileNames);
        
        AssemblyFile[] files = tabletFile.getFileList();
        File cacheDir = new File(cachePath);
        
        AssemblyFileHandler assemblyFileHandler = new AssemblyFileHandler(files, cacheDir);
        assemblyFileHandler.runJob(0);
        
        TabletFileHandler.addAsMostRecent(tabletFile);
        Assembly assembly = assemblyFileHandler.getAssembly();

        assembly.getAssemblyStatistics().setAssembly(tabletFile.assembly);
        assembly.getAssemblyStatistics().setReference(tabletFile.assembly);   
        
        //Loading data from contigs
        Contig selectedCotig = assembly.getContig(contigNumber);   

        //Set Location
        assembly.getBamBam().setSize(endCoordinate-startCoordinate);
        assembly.getBamBam().setBlockStart(selectedCotig, startCoordinate);

        selectedCotig.clearCigarFeatures();
        assembly.getBamBam().loadDataBlock(selectedCotig);           
        assembly.getBamBam().indexNames();

        //Extracting Reference Data
        Consensus consensus = selectedCotig.getConsensus();            
        byte[] referenceData = consensus.getRange(startCoordinate-1, endCoordinate-1);
        loadedReference = new ArrayList();
        for(int i = 0;i<referenceData.length;i++){
            loadedReference.add(UtilityFunctions.getInstance().GetBaseFromVal(referenceData[i]));
        }    
        
        //Sorting Reads
        selectedCotig.getReads().trimToSize();
        Collections.sort(selectedCotig.getReads());
        selectedCotig.calculateOffsets(assembly);
        
        //Packing Reads
        PackCreator packCreator = new PackCreator(selectedCotig, false);
        packCreator.runJob();      
        
        //Packing Reads                    
        reads = selectedCotig.getPackManager();
    }
    
    public IReadManager getReads(){
        return reads;
    }
    
    public ArrayList<String> getLoadedReference(){
        return loadedReference;
    }
}
