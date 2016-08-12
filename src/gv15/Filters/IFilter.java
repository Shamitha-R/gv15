package gv15.Filters;

import gv15.FragmentNode;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author ranasi01
 */
public interface IFilter {
    
    //Filters the panel fragments based on the filter type and parameters
    public void FilterPanel(ArrayList<String> referenceData,Map<String,FragmentNode>[] Fragments,int varianceCoord);
}
