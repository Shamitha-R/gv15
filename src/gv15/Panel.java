package gv15;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
/**
 *
 * @author ranasi01
 */
public class Panel {
    public String PanelName;
    public Map<String,FragmentNode>[] Fragments;
    public double PositionX;
    public double PositionY;
    public int FragmentXOffset = 20;
    public int Flank;
    
    private int columns;
    private int rows;
    private double columnWidth;
    private double rowHeight;
    
    public Panel(String ID,double positionX,double positionY,
        int columns,int rows,double columnWidth,double rowHeight,int flank){
        this.PanelName = ID;
        this.PositionX = positionX;
        this.PositionY = positionY;
        this.columns = columns;
        this.rows = rows;
        this.columnWidth = columnWidth;
        this.rowHeight = rowHeight;
        this.Flank = flank;
    }
    
    public Map<String,FragmentNode>[] getFragments(){
        return Fragments;
    }
    
    public void RenderPanel(Group renderGroup,ArrayList<String> refereneceData,
            int maxReadCount,int offset){
        ArrayList<Shape> renderVariance = SetupVariance(PositionX,PositionY, 
                Flank+offset,columnWidth, rowHeight);
        ArrayList<Shape> renderArea = SetupRenderArea((rows*2)+1, columns, columnWidth, 
                rowHeight, PositionX, PositionY);        
        ArrayList<Shape> referenceRender = SetupReferenceRender(refereneceData,
                PositionX,PositionY,rowHeight,columnWidth);  
        ArrayList<Shape> fragmentRenders = SetupFragments(refereneceData,
                PositionX, PositionY, rowHeight, columnWidth, columns, maxReadCount);
       
        renderGroup.getChildren().addAll(renderArea);
        renderGroup.getChildren().addAll(referenceRender);
        renderGroup.getChildren().addAll(renderVariance);
        renderGroup.getChildren().addAll(fragmentRenders);
    }
    
    private ArrayList<Shape> SetupVariance(double gridX, double gridY, int varianceCol,
            double colWidth,double rowHeight){
        ArrayList<Shape> renderItems = new ArrayList();
        
        Line tempLine = new Line();
        tempLine.setStartX(gridX + (varianceCol * colWidth));
        tempLine.setStartY(gridY - rowHeight);
        tempLine.setEndX(gridX + (varianceCol * colWidth));
        tempLine.setEndY(gridY + (5 * (rowHeight*2)));
        tempLine.setStrokeWidth(1);
        tempLine.setStroke(Color.RED);
            
        renderItems.add(tempLine);

        Line tempLine2 = new Line();
        tempLine2.setStartX(gridX + ((varianceCol+1) * colWidth));
        tempLine2.setStartY(gridY - rowHeight);
        tempLine2.setEndX(gridX + ((varianceCol+1) * colWidth));
        tempLine2.setEndY(gridY + (5 * (rowHeight*2)));
        tempLine2.setStrokeWidth(1);
        tempLine2.setStroke(Color.RED);
        
        renderItems.add(tempLine2);

        return renderItems;
    }
    
    private ArrayList<Shape> SetupRenderArea(int rows,int cols, double colWidth,double rowHeight
                                            ,double startX, double startY){
        ArrayList<Shape> renderComponents = new ArrayList<>();
        
        //Piano lines
        boolean alter = false;
        for(int row = 0;row<rows;row++){
            Line tempLine = new Line();
            tempLine.setStartX(startX + (rowHeight/2));
            tempLine.setStartY(startY + (row * rowHeight) + (rowHeight/2));
            tempLine.setEndX(startX + (cols+1) * colWidth - (rowHeight/2));
            tempLine.setEndY(startY + (row * rowHeight) + (rowHeight/2));
            tempLine.setStrokeWidth(rowHeight);
            
            if(alter){
                tempLine.setStroke(Color.WHITE);
                alter = false;
                renderComponents.add(tempLine);
                
                Line footerLine = new Line();
                footerLine.setStartX(startX - 25);
                footerLine.setStartY(startY + (row * rowHeight) + (rowHeight));
                footerLine.setEndX(startX );
                footerLine.setEndY(startY + (row * rowHeight) + (rowHeight));
                footerLine.setStrokeWidth(1);    
                footerLine.setStroke(Color.GREY);               
                renderComponents.add(footerLine);
            }else{
                tempLine.setStroke(Color.GHOSTWHITE);
                alter = true;
                renderComponents.add(tempLine);
            }            
           
        }
        //Table borders
        for(int col = 0; col<=cols;col++){
            Line tempLine = new Line();
            tempLine.setStartX(startX + (col * colWidth));
            tempLine.setStartY(startY);
            tempLine.setEndX(startX + (col * colWidth));
            tempLine.setEndY(startY + (rows * rowHeight));
            tempLine.setStrokeWidth(1);
            tempLine.setStroke(Color.LIGHTGREY);
            
            renderComponents.add(tempLine);
        }
        
        return renderComponents;
    }
    
    private ArrayList<Shape> SetupReferenceRender(ArrayList<String> referenceData,
            double startX,double startY,double rowHeight,double colWidth){
        ArrayList<Shape> renderTexts = new ArrayList<Shape>();
        
        //Reference BasePairs
        for(int refIndex = 0;refIndex<referenceData.size();refIndex++){
            Text tempText = new Text(startX + (colWidth*refIndex) + (colWidth/2), 
                    startY, referenceData.get(refIndex));
            renderTexts.add(tempText);
        }
        
        //Read BasePair Types
        for(int baseType = 0;baseType<5;baseType++){
            Text tempText = new Text(startX - 25, 
                    startY + (baseType*rowHeight*2) + 25,UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType));
            tempText.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
            renderTexts.add(tempText);
        }
               
        return renderTexts;
    }

    private ArrayList<Shape> SetupFragments(ArrayList<String> referenceData,
            double gridX, double gridY, double rowHeight, 
            double colWidth,int cols,int maxReadCount){
        ArrayList<Shape> renderElements = new ArrayList<>();
        
        int XOFFSET = FragmentXOffset;
        int YOFFSET = 4;
           
        for(int colNum = 0;colNum<cols;colNum++){
            for(int baseType = 0;baseType<5;baseType++){
                
                if(Fragments[colNum]!=null &&
                        Fragments[colNum].containsKey(UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType))){
                    
                    FragmentNode val = Fragments[colNum].get(UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType));
                    
                    float readSize = 1 + (val.ReadCount/(maxReadCount*1.0f)*13.0f);
                    
                    //Draw primary lines
                    Line tempLine = new Line();
                    tempLine.setStartX((colNum * colWidth) + gridX + XOFFSET + (readSize/2) );
                    tempLine.setStartY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                    tempLine.setEndX((colNum* colWidth)+colWidth + gridX - XOFFSET - (readSize/2));
                    tempLine.setEndY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                    tempLine.setStrokeWidth(readSize);
                    
                    if(referenceData.get(colNum)!="INS"){
                        if(referenceData.get(colNum).equals(UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType)))
                            tempLine.setStroke(Color.GAINSBORO);
                        else
                            tempLine.setStroke(Color.ORANGE);
                    }else
                        tempLine.setStroke(Color.BLUEVIOLET);
                    
                    renderElements.add(tempLine); 
                    
                    //Connect the fragments
                    if(colNum < (cols)){
                        for(int nextBase = 0;nextBase<5;nextBase++){
                            if(val.ConnectedFragments != null &&
                                    val.ConnectedFragments.containsKey(UtilityFunctions.
                            getInstance().RowNumberToBaseType(nextBase))){
                                
                                String connectedVal = UtilityFunctions.
                                    getInstance().RowNumberToBaseType(nextBase);
                                HashSet<Integer> connectedColumns = val.ConnectedFragments.get(UtilityFunctions.
                                                        getInstance().RowNumberToBaseType(nextBase));

                                for (Integer colVal : connectedColumns) {

                                    if(!Fragments[colVal].containsKey(connectedVal))
                                        System.err.println("");
                                    
                                    float nextReadSize = 1 + ((Fragments[colVal].get(connectedVal).ReadCount
                                        /(maxReadCount*1.0f))*13.0f);    
                                
                                    if(nextBase == baseType){
                                        Line connectorLine = new Line();
                                        connectorLine.setStartX((colNum* colWidth)+colWidth + gridX - XOFFSET - (readSize/2));
                                        connectorLine.setEndX(((colVal) * colWidth) + gridX + XOFFSET + (readSize/2));

                                        if(nextReadSize<readSize){                                      
                                            connectorLine.setStrokeWidth(nextReadSize);
                                            connectorLine.setStartY((baseType * rowHeight * 2) + gridY + YOFFSET + (nextReadSize/2));
                                            connectorLine.setEndY((baseType * rowHeight * 2) + gridY + YOFFSET + (nextReadSize/2));
                                        }else{
                                            connectorLine.setStrokeWidth(readSize);
                                            connectorLine.setStartY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                                            connectorLine.setEndY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                                        }
                                        if(referenceData.get(colNum)=="INS"  || referenceData.get(colVal)=="INS")                                        
                                            connectorLine.setStroke(Color.BLUEVIOLET);
                                        else{
                                            if(referenceData.get(colVal).equals(UtilityFunctions.
                                                    getInstance().RowNumberToBaseType(baseType)))
                                                connectorLine.setStroke(Color.GAINSBORO);
                                            else
                                                connectorLine.setStroke(Color.ORANGE);
                                        }

                                        renderElements.add(connectorLine);  
                                    }else{
                                        //Join the fragments
                                        CubicCurve tempCurve = new CubicCurve(
                                            (colNum* colWidth)+colWidth + gridX - XOFFSET,
                                            (baseType * rowHeight * 2) + gridY + YOFFSET,

                                            (colNum* colWidth)+colWidth + gridX - XOFFSET + 18,
                                            (baseType * rowHeight * 2) + gridY + YOFFSET,

                                            ((colVal) * colWidth) + gridX + XOFFSET  - 18,
                                            (nextBase * rowHeight * 2) + gridY + YOFFSET,

                                            ((colVal) * colWidth) + gridX + XOFFSET,
                                            (nextBase * rowHeight * 2) + gridY + YOFFSET
                                        );
                                        if(referenceData.get(colNum)=="INS" || referenceData.get(colVal)=="INS")
                                            tempCurve.setStroke(Color.BLUEVIOLET);
                                        else{
                                            if(referenceData.get(colNum).equals(UtilityFunctions.
                                                    getInstance().RowNumberToBaseType(baseType)) &&
                                                    referenceData.get(colVal).equals(UtilityFunctions.
                                                    getInstance().RowNumberToBaseType(nextBase)))
                                                tempCurve.setStroke(Color.GAINSBORO);
                                            else
                                                tempCurve.setStroke(Color.ORANGE);
                                        }
                                        tempCurve.setFill(null);
                                        renderElements.add(tempCurve);      

                                        Path path = new Path();
                                        for (int i = 0; i <= nextReadSize-1; i++) {
                                            path.getElements().addAll(
                                                    new MoveTo(tempCurve.getStartX(), tempCurve.getStartY()),
                                                    new CubicCurveTo(
                                                            tempCurve.getControlX1() ,
                                                            tempCurve.getControlY1() ,
                                                            tempCurve.getControlX2() ,
                                                            tempCurve.getControlY2() ,
                                                            tempCurve.getEndX() ,
                                                            tempCurve.getEndY() + i 
                                                    )
                                            );            
                                        }
                                        path.setStroke(tempCurve.getStroke());
                                        
                                        Path path2 = new Path();
                                        for (int i = 0; i <= readSize-1; i++) {
                                            path2.getElements().addAll(
                                                    new MoveTo(tempCurve.getStartX(), tempCurve.getStartY() + i),
                                                    new CubicCurveTo(
                                                            tempCurve.getControlX1() ,
                                                            tempCurve.getControlY1() ,
                                                            tempCurve.getControlX2() ,
                                                            tempCurve.getControlY2() ,
                                                            tempCurve.getEndX() ,
                                                            tempCurve.getEndY() 
                                                    )
                                            );            
                                        }
                                        path2.setStroke(tempCurve.getStroke());
                                        
                                        renderElements.add(path);
                                        renderElements.add(path2);
                                    }                                    
                                }          
                            }
                        }
                    }
                }
            }
        }
 
        return renderElements;
    }

}

    
 
