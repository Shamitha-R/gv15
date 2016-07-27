/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gv15;

import java.util.ArrayList;
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
    
    public void RenderPanels(Group root,ArrayList<String> referenceData,int maxReadcount){
        for(Panel panel:enginePanels){
            panel.RenderPanel(root,referenceData,maxReadcount);
        }    
    }
}
