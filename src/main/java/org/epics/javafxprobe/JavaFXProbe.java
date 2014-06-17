/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.epics.javafxprobe;

import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.swing.JRootPane;
import static org.epics.pvmanager.ExpressionLanguage.channel;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.PVWriterEvent;
import org.epics.pvmanager.PVWriterListener;
import org.epics.pvmanager.sample.SetupUtil;
import org.epics.util.array.ListNumber;
import org.epics.util.time.TimeDuration;
import static org.epics.util.time.TimeDuration.ofHertz;
import org.epics.vtype.*;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.SimpleValueFormat;
import org.epics.vtype.Time;
import org.epics.vtype.ValueFormat;
import org.epics.vtype.ValueUtil;

/**
 *
 * @author sjdallst
 */
public class JavaFXProbe extends javafx.application.Application {
    
    PV<?,?> pv;
    private GridPane grid = new GridPane();
    Scene scene = new Scene(grid, 310, 675);
    Stage stage;
    private Text pvNameLabel = new Text("PV Name: ");
    private Text pvValueLabel = new Text("Value: ");
    private Text lastErrorLabel = new Text("Last Error: ");
    private Text metadataLabel = new Text("Meta Data: ");
    private Text pvTimeLabel = new Text("Time: ");
    private Text pvTypeLabel = new Text("Type: ");
    private Text displayLimitsLabel = new Text("Display Limits: ");
    private Text alarmLimitsLabel = new Text("Alarm Limits: ");
    private Text warningLimitsLabel = new Text("Warning Limits: ");
    private Text controlLimitsLabel = new Text("Control Limits: ");
    private Text unitLabel = new Text("Unit: ");
    private Text expressionTypeLabel = new Text("Expression Type: ");
    private Text expressionNameLabel = new Text("Expression Name: ");
    private Text channelHandlerLabel = new Text("Channel Handler Name: ");
    private Text usageCountLabel = new Text("Usage Count: ");
    private Text connectedRWLabel = new Text("Connected (R-W): ");
    private Text channelPropertiesLabel = new Text("Channel Properties: ");
    private Text writeConnectedLabel = new Text("Write Connected: ");
    private Text connectedLabel = new Text("Connected: ");
    private TextField pvNameField = new TextField();
    private TextField pvValueField = new TextField();
    private TextField lastErrorField = new TextField();
    private TextField metadataField = new TextField();
    private TextField pvTimeField = new TextField();
    private TextField pvTypeField = new TextField();
    private TextField displayLimitsField = new TextField();
    private TextField alarmLimitsField = new TextField();
    private TextField warningLimitsField = new TextField();
    private TextField controlLimitsField = new TextField();
    private TextField unitField = new TextField();
    private TextField expressionTypeField = new TextField();
    private TextField expressionNameField = new TextField();
    private TextField channelHandlerField = new TextField();
    private TextField usageCountField = new TextField();
    private TextField connectedRWField = new TextField();
    private TextField channelPropertiesField = new TextField();
    private TextField writeConnectedField = new TextField();
    private TextField connectedField = new TextField();
    private Slider indicator = new Slider();
    private ValueFormat format = new SimpleValueFormat(3);
    
    private Button viewTestButton = new Button();
    private ChoiceBox visualChooser = new ChoiceBox();
    private boolean showChooser = false, chooserAdded = false;
    private boolean showVisual = false, visualAdded = false;
    private boolean showText = false, showLineGraph = false, showImage = false, showTable = false;
    private HBox visualWrapper = new HBox();
    private Text visualText = new Text();
    private TableView visualTable = new TableView();
    private ImageView visualImageView = new ImageView();
    private WritableImage visualImage = new WritableImage(100, 100);
    private LineGraphApp lineGraphApp = new LineGraphApp();
    
    public void start(){
        this.start(new Stage());
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        this.stage = primaryStage;
        
        pvNameField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (pv != null) {
                    pv.close();
                    
                    hideVisual();
                    
                    if(grid.getChildren().contains(visualChooser) && chooserAdded){
                        grid.getChildren().remove(visualChooser);
                        showChooser = true;
                        chooserAdded = false;
                    }
                    
                    clearFields();
                    
                }

                try {
                    pv = PVManager.readAndWrite(channel(pvNameField.getText()))
                            .timeout(TimeDuration.ofSeconds(5))
                            .readListener(new PVReaderListener<Object>() {
                                    @Override
                                    public void pvChanged(PVReaderEvent<Object> event) {
                                        setLastError(pv.lastException());
                                        Object value = pv.getValue();
                                        setTextValue(format.format(value));
                                        setType(ValueUtil.typeOf(value));
                                        setTime(ValueUtil.timeOf(value));
                                        setIndicator(ValueUtil.normalizedNumericValueOf(value));
                                        setMetadata(ValueUtil.displayOf(value));
                                        setAlarm(ValueUtil.alarmOf(value));
                                        setConnected(pv.isConnected());
                                        if(value != null && !chooserAdded){
                                            setChoiceBox(value);
                                        }
                                        if(showVisual && (value != null)){
                                            setVisual(value);
                                        }
                                    }
                                })
                            .writeListener(new PVWriterListener<Object>() {

                                @Override
                                public void pvChanged(PVWriterEvent<Object> event) {
                                    setWriteConnected(pv.isWriteConnected());
                                }
                            })
                            .asynchWriteAndMaxReadRate(ofHertz(10));
                } catch (RuntimeException ex) {
                    setLastError(ex);
                }
            }
        });
        
        pvValueField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pvValueField.setEditable(false);
                pvValueField.setFocusTraversable(false);
                pvValueField.setBlendMode(BlendMode.DARKEN);
                pvValueField.clear();
            }
        });
        
        viewTestButton.setText("Test");
        viewTestButton.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent even) {
               if(showVisual && visualAdded) {
                   showVisual = false;
                   visualAdded = false;
                   grid.getChildren().remove(visualWrapper);
                   visualWrapper.getChildren().remove(visualWrapper.getChildren().size() - 1);
               }
               else {
                   showVisual = true;
                   visualAdded = false;
               }
           }
        });
      
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Separator seperator1 = new Separator();
        Separator seperator2 = new Separator();
        Separator seperator3 = new Separator(); 
        
        visualWrapper.setAlignment(Pos.CENTER);
        
        grid.addRow(0, pvNameLabel, pvNameField);
        grid.add(seperator1, 0, 1, 2, 1);
        grid.addRow(2, pvValueLabel, pvValueField);
        grid.addRow(6, pvTimeLabel, pvTimeField);
        grid.add(seperator2, 0, 7, 2, 1);
        grid.addRow(8, pvTypeLabel, pvTypeField);
        grid.addRow(9, displayLimitsLabel, displayLimitsField);
        grid.addRow(10, alarmLimitsLabel, alarmLimitsField);
        grid.addRow(11, warningLimitsLabel, warningLimitsField);
        grid.addRow(12, controlLimitsLabel, controlLimitsField);
        grid.addRow(13, unitLabel, unitField);
        grid.add(seperator3, 0, 14, 2, 1);
        grid.addRow(15, lastErrorLabel, lastErrorField);
        grid.addRow(16, writeConnectedLabel, writeConnectedField);
        grid.addRow(17, connectedLabel, connectedField);
        //grid.add(viewTestButton, 0, 3, 2, 1);
        
        scene.setFill(Paint.valueOf("lightGray"));
        primaryStage.setTitle("Probe");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SetupUtil.defaultCASetup();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                new JavaFXProbe().start();
            }
        }); 
    }
    
    private void setTextValue(String value) {
        if (value == null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pvValueField.setText("");
                }
            });
        } else {
            final String value1 = value;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pvValueField.setText(value1);
                }
            });
        }
    }

    private void setType(Class type) {
        if (type == null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pvTypeField.setText("");
                }
            });
        } else {
            final String simpleName = type.getSimpleName();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pvTypeField.setText(simpleName);
                }
            });
        }
    }

    private void setAlarm(Alarm alarm) {
        
    }

    private void setTime(Time time) {
        if (time == null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pvTimeField.setText("");
                }
            });
        } else {
            final String timeString = time.getTimestamp().toDate().toString();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pvTimeField.setText(timeString);
                }
            });
        }
    }

    private void setMetadata(Display display) {
        if (display == null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    metadataField.setText("");
                }
            });
        } else {
            final String displayLimits = display.getUpperDisplayLimit() + " - " + display.getLowerDisplayLimit();
            final String alarmLimits = display.getUpperAlarmLimit() + " - " + display.getLowerAlarmLimit();
            final String warningLimits = display.getUpperWarningLimit() + " - " + display.getLowerWarningLimit();
            final String controlLimits = display.getUpperCtrlLimit() + " - " + display.getLowerCtrlLimit();
            final String unit = display.getUnits();
            
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    displayLimitsField.setText(displayLimits);
                    alarmLimitsField.setText(alarmLimits);
                    warningLimitsField.setText(warningLimits);
                    controlLimitsField.setText(controlLimits);
                    unitField.setText(unit);
                }
            });
        }
    }

    private void setLastError(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            final String message = ex.getClass().getSimpleName() + " " + ex.getMessage();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lastErrorField.setText(message);
                }
            }); 
        } else {
        }
    }

    private void setConnected(Boolean connected) {
        if (connected != null) {
            final String connectedString = connected.toString();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    connectedField.setText(connectedString);
                }
            });
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    connectedField.setText("");
                }
            });
        }
    }

    private void setWriteConnected(Boolean connected) {
        if (connected != null) {
            final String connectedString = connected.toString();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    writeConnectedField.setText(connectedString);
                }
            });
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    writeConnectedField.setText("");
                }
            });
        }
    }

    private void setIndicator(Double value) {
        double range = (indicator.getMax() - indicator.getMin());
        double position = (indicator.getMin() + range / 2);
        if (value != null) {
            position = (int) (range * value);
        }
        final double position1 = position;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                indicator.setValue(position1);
            }
        });
    }
    
    private void setVisual(Object value){
        
        final Object value1 = value;
        
        if(value instanceof VNumber){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(!visualAdded) {
                        if(showText) {
                            visualWrapper.getChildren().add(visualText);
                            grid.add(visualWrapper, 0, 4, 2, 2);
                            visualAdded = true;
                        }
                    }
                    if(showText) {
                    visualText.setText(ValueUtil.numericValueOf(value1).toString());
                    }
                }
            });
        }
        
        if(value instanceof VNumberArray){
            
            if(showLineGraph){
                final byte[] pixels = lineGraphApp.render((VNumberArray)value, Math.max(100, (int)visualWrapper.getWidth()),
                                                          Math.max(100, (int)visualWrapper.getHeight()));

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        drawByteArray(pixels, Math.max(100, (int)visualWrapper.getWidth()),
                                                          Math.max(100, (int)visualWrapper.getHeight()));
                        if(!visualAdded) {
                            visualWrapper.getChildren().add(visualImageView);
                            grid.add(visualWrapper, 0, 4, 2, 2);
                            visualAdded = true;
                        }
                    }
                });
            }
            
        }
        
        if(value instanceof VTable){
            if(showTable){
                final VTable value2 = (VTable)value;
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        visualTable = new TableView();
                        VTable table = (VTable)value2;
                        TableColumn [] tableColumns = new TableColumn[table.getColumnCount()];        
                        for(int i = 0; i < table.getColumnCount(); i++) {
                            tableColumns[i] = new TableColumn(table.getColumnName(i));
                        }
                        visualTable.getColumns().addAll(tableColumns);
                        visualTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                        ObservableList<ObservableList> csvData = FXCollections.observableArrayList(); 

                        for(int i = 0; i < table.getRowCount(); i++) {
                            ObservableList<String> row = FXCollections.observableArrayList();
                            for(int j = 0; j < table.getColumnCount(); j++) {
                                if(table.getColumnData(j) instanceof List){
                                    if(i < ((List)(table.getColumnData(j))).size()) {
                                        row.add(((List)(table.getColumnData(j))).get(i).toString());
                                    }
                                    else {
                                        row.add("");
                                    }
                                }
                                else {
                                    if(i < ((ListNumber)(table.getColumnData(j))).size()) {
                                        row.add(((ListNumber)(table.getColumnData(j))).getDouble(i) + "");
                                    }
                                    else {
                                        row.add("");
                                    }
                                }
                            }
                            csvData.add(row); // add each row to cvsData
                        }

                        visualTable.setItems(csvData); // finally add data to tableview

                        if(!visualAdded) {
                            visualWrapper.getChildren().add(visualTable);
                            grid.add(visualWrapper, 0, 4, 2, 2);
                            visualAdded = true;
                        }
                    }  
                });    
            }
        }
        
        if(value instanceof VImage){
            final VImage value3 = (VImage)value;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    drawByteArray(value3.getData(), value3.getWidth(), value3.getHeight());
                    if(!visualAdded){
                        visualWrapper.getChildren().add(visualImageView);
                        grid.add(visualWrapper, 0, 4, 2, 2);
                        visualAdded = true;
                    }
                }
            });
        }
         
    }
    
    private void setChoiceBox(Object value){
        
        if(value instanceof VNumber) {
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    visualChooser.setItems(FXCollections.observableArrayList(
                        "Hide", "Value"));
                    visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                            Number oldValue, Number newValue) -> {
                                switch(newValue.intValue()){
                                    case 0:
                                        hideVisual();
                                        break;
                                    case 1:
                                        showVisual = true;
                                        showText = true;
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 3, 2, 1);
                    chooserAdded = true;
                }
            });
        }
        
        if(value instanceof VNumberArray) {
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    visualChooser.setItems(FXCollections.observableArrayList(
                        "Hide", "LineGraph"));
                    visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                            Number oldValue, Number newValue) -> {
                                switch(newValue.intValue()){
                                    case 0:
                                        hideVisual();
                                        break;
                                    case 1:
                                        showVisual = true;
                                        showLineGraph = true;
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 3, 2, 1);
                    chooserAdded = true;
                }
            });
        }
        
        if(value instanceof VTable) {
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    visualChooser.setItems(FXCollections.observableArrayList(
                        "Hide", "Table"));
                    visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                            Number oldValue, Number newValue) -> {
                                switch(newValue.intValue()){
                                    case 0:
                                        hideVisual();
                                        break;
                                    case 1:
                                        showVisual = true;
                                        showTable = true;
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 3, 2, 1);
                    chooserAdded = true;
                }
            });
        }
        
        if(value instanceof VImage) {
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    visualChooser.setItems(FXCollections.observableArrayList(
                        "Hide", "Image"));
                    visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                            Number oldValue, Number newValue) -> {
                                switch(newValue.intValue()){
                                    case 0:
                                        hideVisual();
                                        break;
                                    case 1:
                                        showVisual = true;
                                        showImage = true;
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 3, 2, 1);
                    chooserAdded = true;
                }
            });
        }
        
    }
    
    private void drawByteArray(byte[] pixels, int width, int height) {
        visualImage = new WritableImage(width, height);
        PixelWriter writer = visualImage.getPixelWriter();
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                int argb = 0;
                argb += (pixels[i*width*3 + 3*j + 0] & 0xFF) << 0;
                argb += (pixels[i*width*3 + 3*j + 1] & 0xFF) << 8;
                argb += (pixels[i*width*3 + 3*j + 2] & 0xFF) << 16;
                argb += 0xFF << 24;
                writer.setArgb(j, i, argb);
            }
        }
        visualImageView.setImage(visualImage);
    }
    
    private void hideVisual(){
        showVisual = visualAdded = false;
        
        showText = showLineGraph = showImage = showTable = false;
        
        if(grid.getChildren().contains(visualWrapper)){
            grid.getChildren().remove(visualWrapper);
        }
        
        while(visualWrapper.getChildren().size() != 0) {
            visualWrapper.getChildren().remove(visualWrapper.getChildren().size() - 1);
        }
        
    }
    
    private void clearFields(){
        pvValueField.clear();
        lastErrorField.clear();
        metadataField.clear();
        pvTimeField.clear();
        pvTypeField.clear();
        displayLimitsField.clear();
        alarmLimitsField.clear();
        warningLimitsField.clear();
        controlLimitsField.clear();
        unitField.clear();
        expressionTypeField.clear();
        expressionNameField.clear();
        channelHandlerField.clear();
        usageCountField.clear();
        connectedRWField.clear();
        channelPropertiesField.clear();
        writeConnectedField.clear();
        connectedField.clear();
    }
    
    private void resetChoiceBox(){
        visualChooser = new ChoiceBox();
        if(grid.getChildren().contains(visualChooser)){
            grid.getChildren().remove(visualChooser);
        }
        showChooser = true;
        chooserAdded = false;
    }
}
