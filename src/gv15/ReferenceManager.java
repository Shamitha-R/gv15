/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gv15;

import java.util.ArrayList;

/**
 *
 * @author ranasi01
 */
public class ReferenceManager {
    private ArrayList<String> referenceData;
    
    public ReferenceManager(){
        referenceData = new ArrayList();
    }
    
    public ArrayList<String> getReference(){
        return referenceData;
    }
}
