package gv15;

import htsjdk.variant.variantcontext.VariantContext;
/**
 *
 * @author ranasi01
 */
public class VariantManager {
    private VariantContext selectedVariant;
    
    public VariantContext getSelectedVariant(){
        return selectedVariant;
    }
}
