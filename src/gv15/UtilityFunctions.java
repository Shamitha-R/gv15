package gv15;

/**
 *
 * @author ranasi01
 */
public class UtilityFunctions {
    
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
}
