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
    public int ShiftVal;
    
    public ReferenceManager(){
        ReferenceData = new HashMap();
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
        for(String type:ReferenceData.keySet()){
            if(type.equals(targetType))
                return ReferenceData.get(type);
        }
        
        return null;
    }
}
