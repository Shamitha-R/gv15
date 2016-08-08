package gv15.Filters;

import gv15.FragmentNode;
import gv15.UtilityFunctions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author ranasi01
 */
public class ReadCountFilter implements IFilter{
    /*
    Filter Panel with respect to the provided Minimum read count value
    */
    @Override
    public void FilterPanel(ArrayList<String> referenceData, Map<String, FragmentNode>[] fragments) {
        int minReadCount = UtilityFunctions.getInstance().ReadCountRenderThreshold;
        
        int removedCount = 0;
        for(int columnNum=0;columnNum<fragments.length;columnNum++){
            Iterator fragmentIter = new HashMap(fragments[columnNum]).keySet().iterator();

            while(fragmentIter.hasNext()){
                String baseType = (String)fragmentIter.next();
                
                //Check the read count
                if(fragments[columnNum].get(baseType).ReadCount < minReadCount){
                    //Remove this fragment
                    fragments[columnNum].remove(baseType);
                    //Remove all connections to this fragment
                    RemoveFragmentConnections(baseType, columnNum, fragments);
                }       
            }
            
            if(fragments[columnNum].isEmpty()){
                referenceData.remove(columnNum-removedCount);
                removedCount++;
            }
        }
        System.err.println("");
    }
    
    private void RemoveFragmentConnections(String baseVal,int columnNo,
            Map<String, FragmentNode>[] fragments){
        for(int col = 0;col<columnNo;col++){
            for(String currentBase:fragments[col].keySet()){
                
                if(fragments[col].get(currentBase).ConnectedFragments.containsKey(baseVal)){
                    //The previous base has the low read base as a connection
                    if(fragments[col].get(currentBase).ConnectedFragments.get(baseVal).contains(columnNo)){
                        fragments[col].get(currentBase).ConnectedFragments.get(baseVal).remove(columnNo);
                    }
                }
            }
        }
    }
}
