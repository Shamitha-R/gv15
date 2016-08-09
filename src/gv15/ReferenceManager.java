package gv15;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author ranasi01
 */
public class ReferenceManager {

    public HashMap<String,ArrayList<String>> ReferenceData;
    public HashMap<String,ArrayList<String>> AdjustedReferenceData;
    public HashMap<String,Integer> ShiftVals;
    
    private String referencePath;
    
    public ReferenceManager(String referencePath){
        ReferenceData = new HashMap();
        AdjustedReferenceData = new HashMap();
        ShiftVals = new HashMap();
        this.referencePath = referencePath;
    }

    public ArrayList<String> CombineReferences(ArrayList<String> ref00,ArrayList<String> ref01){
        ArrayList<String> combineRef = new ArrayList();
                
        int index00 = 0;
        int index01 = 0;
        while(index00<ref00.size() &&
                index01<ref01.size()){

            if(ref00.get(index00).
                    equals(ref01.get(index01))){
                combineRef.add(ref00.get(index00));
                index00++;
                index01++;
            }else if(ref00.get(index00).equals("INS")){
                combineRef.add("INS");
                index00++;
            }else if(ref01.get(index01).equals("INS")){
                combineRef.add("INS");
                index01++;
            }
        }        
        
        return combineRef;
    }
 
    public void AddReference(String type,ArrayList<String> refData){
        if(!ReferenceData.containsKey(type)){
            ReferenceData.put(type, new ArrayList());
            for(String val:refData)
                ReferenceData.get(type).add(val);
        }else{
            ReferenceData.get(type).clear();
            for(String val:refData)
                ReferenceData.get(type).add(val);
        }
    }
    
    public ArrayList<String> GetReferenceForType(String targetType){
        for(String type:AdjustedReferenceData.keySet()){
            if(type.equals(targetType))
                return AdjustedReferenceData.get(type);
        }
        
        return null;
    }
    
    public String getReferencePath(){
        return referencePath;
    }
    
    public void AdjustReferences(HashMap<String,int[]> insertionArray){
        
        for(String type:insertionArray.keySet()){
            
            ArrayList<String> tempRef = new ArrayList();
                    
            for(int i = 0;i<insertionArray.get(type).length;i++){
                int insertionCount = insertionArray.get(type)[i];                
                tempRef.add(ReferenceData.get(type).get(i));
                
                while(insertionCount > 0){
                    tempRef.add("INS");
                    insertionCount--;
                }
            }
            AdjustedReferenceData.put(type, tempRef);
        }        
    }
}
