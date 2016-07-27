package gv15;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
/**
 *
 * @author ranasi01
 */
public class VariantManager {
    private VariantContext selectedVariant;
    private ArrayList<VariantContext> variants;
    
    public VariantManager(File vcfFile){
        variants = new ArrayList();
        VCFFileReader reader = new VCFFileReader(vcfFile, false);
        
        for(VariantContext context:reader.iterator().toList()){
            variants.add(context);
        }
        
        selectedVariant = variants.get(0);
    }
    
    public VariantContext getSelectedVariant(){
        return selectedVariant;
    }
}
