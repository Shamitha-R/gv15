/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gv15;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import  io.*;
import data.cache.*;
import data.*;
import analysis.PackCreator;
import com.opencsv.CSVReader;
import data.auxiliary.CigarEvent;
import data.auxiliary.CigarFeature;
import data.auxiliary.CigarInsertEvent;
import data.auxiliary.Feature;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 *
 * @author ranasi01
 */
public class Gv15  extends Application {
    
   
    static int Flank = 7;
    static int ShiftVal = 0;
    static int VariancePos = 871334;

    private static class Phenotype{
        private enum PhenotypeCat{ NORMAL, CONTROL, CIN3, NEG_CONTROL };
        
        public String FID;
        public String IID;
        public String FileName; 
        public PhenotypeCat Type;
    }
    private static ArrayList<Phenotype> Phenotypes = new ArrayList();

    static Engine engine;
    public static void main(String[] args) throws Exception {
        engine = new Engine(); 
        
        launch(args);
    }
    
    @Override public void start(Stage stage) {
        engine.Render(stage);
    }

}
