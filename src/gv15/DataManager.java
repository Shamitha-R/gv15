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
    private int phenotypeColumn;

    public DataManager(String filePath,String variantPath,String phenotypePath,int phenotypeColumn){
        this.filePath = filePath;
        this.variantPath = variantPath;
        this.phenotypePath = phenotypePath;
        this.phenotypeColumn = phenotypeColumn;
    }
    
    public File ImportVCFFile(){
        return new File(variantPath);
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
                
                if(!phenotypes.containsKey(nextLine[phenotypeColumn]))
                    phenotypes.put(nextLine[phenotypeColumn], new ArrayList());

                phenotypes.get(nextLine[phenotypeColumn]).add(tempPhenotype); 
            }
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        phenotypes.remove("phenotype");
    }

}
