package gv15;

import java.io.File;

/**
 *
 * @author ranasi01
 */
public class DataManager {
    
    private String filePath;
    private String[] bamFiles;
    
    public DataManager(String filePath){
        this.filePath = filePath;
    }
    
    public File ImportVCFFile(){
        return new File(filePath+"\\variants.vcf");
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
}
