/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.epics.javafxprobe;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.epics.graphene.InterpolationScheme;

/**
 *
 * @author sjdallst
 */
public class LineGraphDialogue extends Application{
    
    private LineGraphApp lineGraphApp;
    private Stage stage;
    private GridPane grid = new GridPane();
    private Scene scene = new Scene(grid, 220, 75);
    private ChoiceBox interpolationChooser = new ChoiceBox();
    
    public void start(LineGraphApp app){
        
        lineGraphApp = app;
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
        interpolationChooser.setItems(FXCollections.observableArrayList(
            "Nearest Neighbor", "Linear", "Cubic"));
        interpolationChooser.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> ov,
            Number oldValue, Number newValue) -> {
                switch(newValue.intValue()){
                    case 0:
                        lineGraphApp.setInterpolationScheme(InterpolationScheme.NEAREST_NEIGHBOR);
                        break;
                    case 1:
                        lineGraphApp.setInterpolationScheme(InterpolationScheme.LINEAR);
                        break;
                    case 2:
                        lineGraphApp.setInterpolationScheme(InterpolationScheme.CUBIC);
                        break;
                }
            });
        interpolationChooser.getSelectionModel().select(0);
        grid.add(interpolationChooser, 0, 0);
        grid.setAlignment(Pos.CENTER);
    }
}
