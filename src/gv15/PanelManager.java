/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gv15;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.Group;

/**
 *
 * @author ranasi01
 */
public class PanelManager {
    
    ArrayList<Panel> enginePanels;
    
    public PanelManager(){
        enginePanels = new ArrayList();
    }
    
    public void AddPanel(String panelName,double startX,double startY,int flank,
            double columnWidth,double rowHeight){
        enginePanels.add(new Panel(panelName, startX, startY,(flank*2)+1,5,
                columnWidth,rowHeight));      
    }
    
    public void RenderPanels(Group root,ReferenceManager referenceManager,int maxReadcount){
        for(Panel panel:enginePanels){
            //if(panel.PanelName.equals("Neg_Control"))
                panel.RenderPanel(root,referenceManager.GetReferenceForType(panel.PanelName),
                        maxReadcount,referenceManager.ShiftVals.get(panel.PanelName));
        }    
    }
    
    public Panel GetPanelFromPhenotype(String type){
        for(Panel panel:enginePanels){
            if(panel.PanelName.equals(type))
                return panel;
        }
        
        return null;
    }
}
