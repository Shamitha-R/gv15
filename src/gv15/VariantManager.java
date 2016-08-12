package gv15;

import htsjdk.tribble.Feature;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
/**
 *
 * @author ranasi01
 */
public class VariantManager {
    private Feature selectedVariant;
    private ArrayList<Feature> variants;
    
    public int TotalVariantCount = 0;
    
    public VariantManager(File vcfFile){
        
        variants = new ArrayList();        
        String variantCoordinate = UtilityFunctions.getInstance().VariantCoordinate;
        //If a specfic variant was provided, use the given coordinate ingore vcf file
        if(variantCoordinate!=null){
            int contigEnd = variantCoordinate.indexOf(":");
            String contig = variantCoordinate.substring(0,contigEnd);
            int startCoord = Integer.parseInt(variantCoordinate.substring(contigEnd+1));

            ExtendedVariantContext tempContext = new ExtendedVariantContext(contig, startCoord, startCoord);
            variants.add(tempContext);
            TotalVariantCount=1;
        }else{
            VCFFileReader reader = new VCFFileReader(vcfFile, false);
            for(VariantContext context:reader.iterator().toList()){ 
                variants.add(context);
                TotalVariantCount++;
            }            
        }

    }
    
    public Feature getSelectedVariant(){
        return selectedVariant;
    }
    
    public void setVariant(int index){
        selectedVariant = variants.get(index);
    }
}
