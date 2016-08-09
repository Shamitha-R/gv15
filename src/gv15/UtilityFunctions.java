package gv15;

/**
 *
 * @author ranasi01
 */
public class UtilityFunctions {
    
    public int ReadCountRenderThreshold = 50;
    public boolean InsertionsOnlyAtVariant = true;
    
    private static UtilityFunctions instance = new UtilityFunctions();
    
    private UtilityFunctions(){}
        
    public static UtilityFunctions getInstance(){
        return instance;
    }
 
    public String RowNumberToBaseType(int num){
        switch(num){
            case 0: return "A";
            case 1: return "C";
            case 2: return "G";
            case 3: return "T";
            case 4: return "N";
        }        
        return "N";
    }
    
    public String GetBaseFromVal(int val){
        switch (val){
            case 6: return "A";
            case 7: return "A";
            case 8: return "C";
            case 9: return "C";
            case 10: return "G";
            case 11: return "G";
            case 12: return "T";
            case 13: return "T";
            
            default: break;
        }
        
        return "N";
    }  
}
