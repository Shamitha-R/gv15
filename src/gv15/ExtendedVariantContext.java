package gv15;

import htsjdk.tribble.Feature;
import htsjdk.variant.variantcontext.VariantContext;

/**
 *
 * @author ranasi01
 */
public class ExtendedVariantContext implements Feature{

    private String contig;
    private int start;
    private int end;
    
    public ExtendedVariantContext(String contig,int start,int end){
        this.contig = contig;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public String getChr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getContig() {
        return contig;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }
    

}
