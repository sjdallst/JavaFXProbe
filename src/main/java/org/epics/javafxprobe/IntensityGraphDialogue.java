/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.epics.javafxprobe;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.epics.graphene.InterpolationScheme;
import org.epics.graphene.NumberColorMaps;

/**
 *
 * @author sjdallst
 */
public class IntensityGraphDialogue extends Application {
    private IntensityGraphApp intensityGraphApp;
    private Stage stage;
    private GridPane grid = new GridPane();
    private Scene scene = new Scene(grid, 220, 75);
    private ChoiceBox colorSchemeChooser = new ChoiceBox();
    private CheckBox showLegend = new CheckBox("Show legend");
    
    public void start(IntensityGraphApp app){
        
        intensityGraphApp = app;
        this.start(new Stage());
        
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        this.stage = primaryStage;
        
        //instantiate grid, set actions for fields, add components to grid.
        initComponents();
        
        scene.setFill(Paint.valueOf("lightGray"));
        primaryStage.setTitle("Graph Settings");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void initComponents(){
        colorSchemeChooser.setItems(FXCollections.observableArrayList(
                NumberColorMaps.getRegisteredColorSchemes().keySet()));
        colorSchemeChooser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable,String oldValue, String newValue){
                intensityGraphApp.setColorMap(
                            NumberColorMaps.getRegisteredColorSchemes().get(newValue));
            }
        });
        
        showLegend.selectedProperty().addListener(
            (ObservableValue<? extends Boolean> ov,
                Boolean oldValue, Boolean newValue) -> {
                    intensityGraphApp.setDrawLegend(newValue);                
        });
        
        colorSchemeChooser.getSelectionModel().selectFirst();
        grid.setHgap(10);
        grid.add(colorSchemeChooser, 0, 0);
        grid.add(showLegend, 1, 0);
        grid.setAlignment(Pos.CENTER);
    }
}
