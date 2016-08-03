package gv15;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ranasi01
 */
public class DataManager {
    
    private String filePath;
    private String variantPath;
    private String phenotypePath;
    private String[] bamFiles;

    public DataManager(String filePath,String variantPath,String phenotypePath){
        this.filePath = filePath;
        this.variantPath = variantPath;
        this.phenotypePath = phenotypePath;
    }
    
    public File ImportVCFFile(){
        return new File(variantPath);
    }
    
    public String[] getBamFiles(){
        
        bamFiles = new String[]{           
            //"samples\\chr1_871234_871434_DA0059011_IonXpress_001_rawlib.bam",
            "samples\\chr1_871234_871434_DA0057131_IonXpress_002_rawlib.bam",
            //"samples\\chr1_871234_871434_DA0057156_IonXpress_003_rawlib.bam",
            //"samples\\chr1_871234_871434_DA0057131_IonXpress_004_rawlib.bam"
            //"samples\\chr1_871234_871434_DA0059025_IonXpress_005_rawlib.bam"
            //"samples\\chr1_871234_871434_DA0057129_IonXpress_012_rawlib.bam",
            //"data00.bam"
        };
        
        return bamFiles;
    }

    public void ImportPhenotypes(HashMap<String,ArrayList<Phenotype>> phenotypes) {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(phenotypePath));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] nextLine;
        try {
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                Phenotype tempPhenotype = new Phenotype();
                tempPhenotype.FID = nextLine[0];
                tempPhenotype.IID = nextLine[1];
                tempPhenotype.FileName = "samples\\"+nextLine[2];
                
                if(!phenotypes.containsKey(nextLine[3]))
                    phenotypes.put(nextLine[3], new ArrayList());

                phenotypes.get(nextLine[3]).add(tempPhenotype); 
            }
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        phenotypes.remove("phenotype");
    }

}
