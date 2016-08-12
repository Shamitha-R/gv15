package gv15;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import gv15.Filters.IFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
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
    public int FragmentXOffset;
    public int Flank;
    
    private int columns;
    private int rows;
    private double columnWidth;
    private double rowHeight;
    private int renderColumns;
    private ArrayList<IFilter> panelFilters; 
    
    public Panel(String ID,double positionX,double positionY,
        int columns,int renderColumns,int rows,double columnWidth,double rowHeight,int flank,int xOffset){
        this.PanelName = ID;
        this.PositionX = positionX;
        this.PositionY = positionY;
        this.columns = columns;
        this.rows = rows;
        this.columnWidth = columnWidth;
        this.rowHeight = rowHeight;
        this.Flank = flank;
        this.FragmentXOffset = xOffset;
        this.panelFilters = new ArrayList();
        this.renderColumns = renderColumns;
    }
    
    public Map<String,FragmentNode>[] getFragments(){
        return Fragments;
    }
    
    public void AddFilter(IFilter newFilter){
        panelFilters.add(newFilter);
    }
    
    public void RenderPanel(Group renderGroup,ArrayList<String> refereneceData,
            int maxReadCount,int offset){
        
        ArrayList<String> rawReference = new ArrayList(refereneceData);
        //Exceute all the filters
        for(IFilter filter:panelFilters)
            filter.FilterPanel(refereneceData, Fragments);
        
        //Recalculate Offset
        int totalOffset = Flank+offset;
        for(int colNum=0;colNum<totalOffset;colNum++){
            if(Fragments[colNum].isEmpty())
                offset--;
        }
        
        ArrayList<Shape> renderVariance = SetupVariance(PositionX,PositionY, 
                Flank+offset,columnWidth, rowHeight);
        ArrayList<Shape> renderArea = SetupRenderArea((rows*2), renderColumns, columnWidth, 
                rowHeight, PositionX, PositionY);        
        ArrayList<Shape> referenceRender = SetupReferenceRender(refereneceData,
                PositionX,PositionY,rowHeight,columnWidth,renderColumns);  
        ArrayList<Node> fragmentRenders = SetupFragments(rawReference,
                PositionX, PositionY, rowHeight, columnWidth, maxReadCount);
        ArrayList<Shape> panelTitle = SetupPanelTitle(PanelName, 10, PositionY+10);
       
        renderGroup.getChildren().addAll(renderArea);
        renderGroup.getChildren().addAll(referenceRender);
        renderGroup.getChildren().addAll(renderVariance);
        renderGroup.getChildren().addAll(fragmentRenders);
        renderGroup.getChildren().addAll(panelTitle);
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
            tempLine.setEndX(startX + (cols) * colWidth - (rowHeight/2));
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
    
    private ArrayList<Shape> SetupPanelTitle(String panelName,double startX,
            double startY){
        ArrayList<Shape> renderElements = new ArrayList<Shape>();
        
        Text tempText = new Text(startX, startY, panelName);
        tempText.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        tempText.setFill(Color.LIGHTCORAL);
        renderElements.add(tempText);    
        
        return renderElements;
    }
    
    private ArrayList<Shape> SetupReferenceRender(ArrayList<String> referenceData,
            double startX,double startY,double rowHeight,double colWidth,int cols){
        ArrayList<Shape> renderTexts = new ArrayList<Shape>();
        
        //Reference BasePairs
        for(int refIndex = 0;refIndex<cols;refIndex++){
            if(refIndex < referenceData.size()){
                Text tempText = new Text(startX + (colWidth*refIndex) + (colWidth/2), 
                        startY, referenceData.get(refIndex));
                renderTexts.add(tempText);
            }
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

    private ArrayList<Node> SetupFragments(ArrayList<String> referenceData,
            double gridX, double gridY, double rowHeight, 
            double colWidth,int maxReadCount){
        ArrayList<Node> renderElements = new ArrayList<>();
        
        int XOFFSET = FragmentXOffset;
        int YOFFSET = 4;
        int skippedFragments = 0;   
        int renderedColumns = 0;
        for(int colNum = 0;colNum<Fragments.length;colNum++){
            
            if(colNum >= Fragments.length)
                break;
            
            if(Fragments[colNum].isEmpty()){
                skippedFragments++;
                continue;
            }
            if(renderedColumns >= renderColumns)
                break;
            renderedColumns++;
            
            for(int baseType = 0;baseType<5;baseType++){
                
                if(Fragments[colNum]!=null &&
                        Fragments[colNum].containsKey(UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType)) &&
                        (colNum-skippedFragments) < renderColumns){
                    
                    FragmentNode val = Fragments[colNum].get(UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType));
                    
                    float readSize = 1 + (val.ReadCount/(maxReadCount*1.0f)*((float)rowHeight*0.8f));
                    
                    //Draw primary lines
                    Line tempLine = new Line();
                    tempLine.setStartX(( (colNum-skippedFragments) * colWidth) + gridX + XOFFSET + (readSize/2) );
                    tempLine.setStartY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                    tempLine.setEndX(( (colNum-skippedFragments) * colWidth)+colWidth + gridX - XOFFSET - (readSize/2));
                    tempLine.setEndY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                    tempLine.setStrokeWidth(readSize);
                    
                    if(!referenceData.get(colNum).equals("INS")){
                        if(referenceData.get(colNum).equals(UtilityFunctions.
                            getInstance().RowNumberToBaseType(baseType)))
                            tempLine.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Unvaried));
                        else
                            tempLine.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Varied));
                    }else
                        tempLine.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Insertion));
                    
                    renderElements.add(tempLine); 

                    //Connect the fragments
                    if(colNum < Fragments.length){
                        for(int nextBase = 0;nextBase<5;nextBase++){

                            if(val.ConnectedFragments != null &&
                                    val.ConnectedFragments.containsKey(UtilityFunctions.
                            getInstance().RowNumberToBaseType(nextBase))){
                                
                                String connectedVal = UtilityFunctions.
                                    getInstance().RowNumberToBaseType(nextBase);
                                HashSet<Integer> connectedColumns = val.ConnectedFragments.get(UtilityFunctions.
                                                        getInstance().RowNumberToBaseType(nextBase));

                                for (Integer colVal : connectedColumns) {

                                    if(colVal >= Fragments.length)
                                        System.err.println("");
                                    
                                    if(!Fragments[colVal].containsKey(connectedVal))
                                        System.err.println("");
                                    
                                    if(Fragments[colVal].containsKey(connectedVal) &&
                                            (colVal-skippedFragments) < this.renderColumns){

                                    int connectionEndColumn = GetConnectionEnd(colNum,colVal)-skippedFragments;    
                                    
                                    float nextReadSize = 1 + ((Fragments[colVal].get(connectedVal).ReadCount
                                        /(maxReadCount*1.0f))*((float)rowHeight*0.8f));    
                                
                                    if(nextBase == baseType){
                                        Line connectorLine = new Line();
                                        connectorLine.setStartX(( (colNum-skippedFragments) * colWidth)+colWidth + gridX - XOFFSET - (readSize/2));
                                        connectorLine.setEndX(( (connectionEndColumn) * colWidth) + gridX + XOFFSET + (readSize/2));

                                        if(nextReadSize<readSize){                                      
                                            connectorLine.setStrokeWidth(nextReadSize);
                                            connectorLine.setStartY((baseType * rowHeight * 2) + gridY + YOFFSET + (nextReadSize/2));
                                            connectorLine.setEndY((baseType * rowHeight * 2) + gridY + YOFFSET + (nextReadSize/2));
                                        }else{
                                            connectorLine.setStrokeWidth(readSize);
                                            connectorLine.setStartY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                                            connectorLine.setEndY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                                        }
                                        if(referenceData.get(colNum).equals("INS")  || 
                                                referenceData.get(colVal).equals("INS"))                                        
                                            connectorLine.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Insertion));
                                        else{
                                            if(referenceData.get(colVal).equals(UtilityFunctions.
                                                    getInstance().RowNumberToBaseType(baseType)))
                                                connectorLine.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Unvaried));
                                            else
                                                connectorLine.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Varied));
                                        }

                                        renderElements.add(connectorLine);  
                                    }else{
                                        //Join the fragments
                                        CubicCurve tempCurve = new CubicCurve(
                                            ( (colNum-skippedFragments) * colWidth)+colWidth + gridX - XOFFSET,
                                            (baseType * rowHeight * 2) + gridY + YOFFSET,

                                            ( (colNum-skippedFragments) * colWidth)+colWidth + gridX - XOFFSET + 18,
                                            (baseType * rowHeight * 2) + gridY + YOFFSET,

                                            ( (connectionEndColumn) * colWidth) + gridX + XOFFSET  - 18,
                                            (nextBase * rowHeight * 2) + gridY + YOFFSET,

                                            ( (connectionEndColumn) * colWidth) + gridX + XOFFSET,
                                            (nextBase * rowHeight * 2) + gridY + YOFFSET
                                        );
                                        if(referenceData.get(colNum).equals("INS") 
                                                || referenceData.get(colVal).equals("INS"))
                                            tempCurve.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Insertion));
                                        else{
                                            if(referenceData.get(colNum).equals(UtilityFunctions.
                                                    getInstance().RowNumberToBaseType(baseType)) &&
                                                    referenceData.get(colVal).equals(UtilityFunctions.
                                                    getInstance().RowNumberToBaseType(nextBase)))
                                                tempCurve.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Unvaried));
                                            else
                                                tempCurve.setStroke(Color.web(UtilityFunctions.getInstance().ReadColour_Varied));
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

                    //Add Read count render
                    Text tempText = new Text(0,0,Integer.toString(val.ReadCount ));
                    double width = tempText.getLayoutBounds().getWidth();
                    VBox textBox = new VBox();
                    textBox.getChildren().addAll(tempText);
                    textBox.setAlignment(Pos.BASELINE_CENTER);
                    textBox.setLayoutX(( (colNum-skippedFragments) * colWidth) + gridX + (colWidth/2) - (width/2));
                    textBox.setLayoutY((baseType * rowHeight * 2) + gridY + YOFFSET + (readSize/2));
                    renderElements.add(textBox);  

                }
            }
        }
 
        return renderElements;
    }
    
    private int GetConnectionEnd(int currentColumn,int targetColumn){
        int endColumn = targetColumn;
        
        while(currentColumn<targetColumn-1){
            if(Fragments[currentColumn+1].isEmpty())
                endColumn--;
            
            currentColumn++;
        }
        
        return endColumn;
    }

}

    
 
