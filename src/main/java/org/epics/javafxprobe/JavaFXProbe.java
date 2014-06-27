/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.epics.javafxprobe;

import com.sun.javafx.application.PlatformImpl;
import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.Gauge;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
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
import javafx.scene.control.TitledPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.JRootPane;
import static org.epics.pvmanager.ExpressionLanguage.channel;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.PVWriterEvent;
import org.epics.pvmanager.PVWriterListener;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
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
    
    PV<Object , Object> pv;
    PVReader<?> formulaPV;
    private final GridPane grid = new GridPane();
    private final GridPane metaDataGrid = new GridPane();
    private final GridPane generalInfoGrid = new GridPane();
    private final TitledPane metaDataPane = new TitledPane();
    private final TitledPane generalInfoPane = new TitledPane();
    Scene scene = new Scene(grid, 350, 650);
    Stage stage;
    private final Text pvNameLabel = new Text("PV Name: ");
    private final Text pvValueLabel = new Text("Value: ");
    private final Text pvWriteLabel = new Text("Write Value: ");
    private final Text lastErrorLabel = new Text("Last Error: ");
    private final Text metadataLabel = new Text("Meta Data: ");
    private final Text pvTimeLabel = new Text("Time: ");
    private final Text pvTypeLabel = new Text("Type: ");
    private final Text displayLimitsLabel = new Text("Display Limits: ");
    private final Text alarmLimitsLabel = new Text("Alarm Limits: ");
    private final Text warningLimitsLabel = new Text("Warning Limits: ");
    private final Text controlLimitsLabel = new Text("Control Limits: ");
    private final Text unitLabel = new Text("Unit: ");
    private final Text expressionTypeLabel = new Text("Expression Type: ");
    private final Text expressionNameLabel = new Text("Expression Name: ");
    private final Text channelHandlerLabel = new Text("Channel Handler Name: ");
    private final Text usageCountLabel = new Text("Usage Count: ");
    private final Text connectedRWLabel = new Text("Connected (R-W): ");
    private final Text channelPropertiesLabel = new Text("Channel Properties: ");
    private final Text writeConnectedLabel = new Text("Write Connected: ");
    private final Text connectedLabel = new Text("Connected: ");
    private final TextField pvNameField = new TextField();
    private final TextField pvValueField = new TextField();
    private final TextField pvWriteField = new TextField();
    private final TextField lastErrorField = new TextField();
    private final TextField metadataField = new TextField();
    private final TextField pvTimeField = new TextField();
    private final TextField pvTypeField = new TextField();
    private final TextField displayLimitsField = new TextField();
    private final TextField alarmLimitsField = new TextField();
    private final TextField warningLimitsField = new TextField();
    private final TextField controlLimitsField = new TextField();
    private final TextField unitField = new TextField();
    private final TextField expressionTypeField = new TextField();
    private final TextField expressionNameField = new TextField();
    private final TextField channelHandlerField = new TextField();
    private final TextField usageCountField = new TextField();
    private final TextField connectedRWField = new TextField();
    private final TextField channelPropertiesField = new TextField();
    private final TextField writeConnectedField = new TextField();
    private final TextField connectedField = new TextField();
    private final Slider indicator = new Slider();
    private final ValueFormat format = new SimpleValueFormat(3);
    
    private ChoiceBox visualChooser = new ChoiceBox();
    private boolean chooserAdded = false;
    
    private boolean showVisual = false, visualAdded = false;
    
    private boolean showMeter = false, showLineGraph = false, showImage = false, showTable = false
                    , showIntensityGraph = false;
    
    private boolean pvWriteFieldAdded = false;
    
    private boolean channelChanged = false;
    
    private final HBox visualWrapper = new HBox();
    private final Text errorText = new Text();
    private TableView visualTable = new TableView();
    private final ImageView visualImageView = new ImageView();
    private WritableImage visualImage = new WritableImage(100, 100);
    private LineGraphApp lineGraphApp = new LineGraphApp();
    private IntensityGraphApp intensityGraphApp = new IntensityGraphApp();
    private ArrayList<String> visualStringsArray = new ArrayList<String>();
    private ArrayList<BaseGraphApp> visualGraphArray = new ArrayList<BaseGraphApp>();
    private Gauge visualGauge = new Gauge();
    
    private boolean write = false;
    private String writtenValue = "";
    
    private Button visualConfigButton = new Button("Configure");
    
    public void start(){
        this.start(new Stage());
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        this.stage = primaryStage;
        
        //instantiate grid, set actions for fields, add components to grid.
        initComponents();
        
        scene.setFill(Paint.valueOf("lightGray"));
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event){
                if(pv != null){
                    pv.close();
                }
                
                if(formulaPV != null){
                    formulaPV.close();
                }
                
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        PlatformImpl.tkExit();
                        Platform.exit();
                        System.exit(0);
                    }
                });
                
            }
            
        });
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
        Platform.runLater(() -> {
            new JavaFXProbe().start();
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
            final boolean connected1 = connected;
            final String connectedString = connected.toString();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    writeConnectedField.setText(connectedString);
                    if(connected && !pvWriteFieldAdded){
                        grid.addRow(3, pvWriteLabel, pvWriteField);
                        pvWriteFieldAdded = true;
                    }
                    pvWriteField.setEditable(connected);
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
                        if(showMeter) {
                            visualGauge.setMinValue(ValueUtil.displayOf(value1).getLowerDisplayLimit());
                            visualGauge.setMaxValue(ValueUtil.displayOf(value1).getUpperDisplayLimit());
                            visualGauge.setAnimated(false);
                            visualGauge.setAreas(new Section(ValueUtil.displayOf(value1).getLowerCtrlLimit(),
                                                    ValueUtil.displayOf(value1).getLowerAlarmLimit()),
                                                    new Section(ValueUtil.displayOf(value1).getLowerAlarmLimit(),
                                                    ValueUtil.displayOf(value1).getLowerWarningLimit()),
                                                    new Section(ValueUtil.displayOf(value1).getLowerWarningLimit(),
                                                    ValueUtil.displayOf(value1).getUpperWarningLimit()),
                                                    new Section(ValueUtil.displayOf(value1).getUpperWarningLimit(),
                                                    ValueUtil.displayOf(value1).getUpperAlarmLimit()),
                                                    new Section(ValueUtil.displayOf(value1).getUpperAlarmLimit(),
                                                    ValueUtil.displayOf(value1).getUpperCtrlLimit()));
                            visualGauge.setPlainValue(false);
                            visualGauge.setValue((ValueUtil.displayOf(value1).getLowerDisplayLimit() + ValueUtil.displayOf(value1).getUpperDisplayLimit())/2);
                            visualGauge.setAutoScale(true);
                            visualWrapper.getChildren().add(visualGauge);
                            grid.add(visualWrapper, 0, 5, 2, 2);
                            visualAdded = true;
                        }
                        grid.getRowConstraints().get(5).setMaxHeight(Double.MAX_VALUE);
                    }
                    if(showMeter) {
                        visualGauge.setValue(Double.parseDouble(format.format(value1)));
                    }
                }
            });
        }
        
        if(value instanceof VNumberArray){
            
            if(showLineGraph){
                showGraph((VNumberArray) value, lineGraphApp);
            }
            
            if(showIntensityGraph){
                showGraph((VNumberArray) value, intensityGraphApp);
            }
            
        }
        
        if(value instanceof VTable){
            if(showTable){
                final VTable value2 = (VTable)value;
                Platform.runLater(() -> {
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
                        grid.add(visualWrapper, 0, 5, 2, 2);
                        visualAdded = true;  
                        grid.getRowConstraints().get(5).setMaxHeight(Double.MAX_VALUE);
                        grid.getRowConstraints().get(5).setVgrow(Priority.ALWAYS);
                    }
                });    
            }
        }
        
        if(value instanceof VImage){
            if(showImage){
                final VImage value3 = (VImage)value;
                Platform.runLater(() -> {
                    drawByteArray(value3.getData(), value3.getWidth(), value3.getHeight());
                    if(!visualAdded){
                        visualWrapper.getChildren().add(visualImageView);
                        grid.add(visualWrapper, 0, 4, 2, 2);
                        visualAdded = true;
                        grid.getRowConstraints().get(4).setMaxHeight(Double.MAX_VALUE);
                        grid.getRowConstraints().get(4).setVgrow(Priority.ALWAYS);
                    }
                });
            }
        }
         
    }
    
    private void setChoiceBox(Object value){
        
        if(value instanceof VNumber) {
            //check to make sure that none of the limits are NaN because it causes issues when setting up the meter.
            if(!(ValueUtil.displayOf(value).getLowerCtrlLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getLowerAlarmLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getLowerWarningLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getUpperWarningLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getUpperAlarmLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getUpperCtrlLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getLowerDisplayLimit().compareTo(Double.NaN) == 0 ||
                    ValueUtil.displayOf(value).getUpperDisplayLimit().compareTo(Double.NaN) == 0)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run(){
                        visualChooser.setItems(FXCollections.observableArrayList(
                            "Hide", "Meter"));
                        visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                            (ObservableValue<? extends Number> ov,
                                Number oldValue, Number newValue) -> {
                                    switch(newValue.intValue()){
                                        case 0:
                                            hideVisual();
                                            break;
                                        case 1:
                                            swapVisual();
                                            showVisual = true;
                                            showMeter = true;
                                            break;
                                    }
                            });
                        visualChooser.getSelectionModel().selectFirst();
                        grid.add(visualChooser, 0, 4, 2, 1);
                        chooserAdded = true;
                    }
                });
            }
            else {
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        errorText.setText("Meter can not be set when limits are NaN");
                        grid.add(errorText, 0, 4, 2, 1);
                        chooserAdded = true;
                    }
                });
            }
        }
        
        if(value instanceof VNumberArray) {
            visualStringsArray.add("Hide");
            try {
                lineGraphApp.render((VNumberArray)value, 100, 100);
                visualStringsArray.add("Line Graph");
                visualGraphArray.add(lineGraphApp);
            }
            catch(Throwable e){
                
            }
            
            try {
                intensityGraphApp.render((VNumberArray)value, 100, 100);
                visualStringsArray.add("Intensity Graph");
                visualGraphArray.add(intensityGraphApp);
            }
            catch(Throwable e){
                
            }
            
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    visualChooser.setItems(FXCollections.observableArrayList(visualStringsArray));
                    visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                            Number oldValue, Number newValue) -> {
                                switch(newValue.intValue()){
                                    case 0:
                                        hideVisual();
                                        break;
                                    case 1:
                                        setupGraph(visualGraphArray.get(0));
                                        break;
                                    case 2:
                                        setupGraph(visualGraphArray.get(1));
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 4);
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
                                        swapVisual();
                                        showVisual = true;
                                        showTable = true;
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 4, 2, 1);
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
                                        swapVisual();
                                        showVisual = true;
                                        showImage = true;
                                        break;
                                }
                        });
                    visualChooser.getSelectionModel().selectFirst();
                    grid.add(visualChooser, 0, 4, 2, 1);
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
        swapVisual();
        grid.getRowConstraints().get(5).setVgrow(Priority.NEVER);
    }
    
    private void swapVisual() {
        showVisual = visualAdded = false;
        
        showMeter = showLineGraph = showImage = showTable = showIntensityGraph = false;
        
        lineGraphApp = new LineGraphApp();
        intensityGraphApp = new IntensityGraphApp();
        
        
        if(grid.getChildren().contains(visualWrapper)){
            grid.getChildren().remove(visualWrapper);
        }
        
        if(grid.getChildren().contains(errorText)) {
            grid.getChildren().remove(errorText);
        }
        
        if(grid.getChildren().contains(visualConfigButton)){
            grid.getChildren().remove(visualConfigButton);
        }
        
        if(grid.getChildren().contains(pvWriteField) && grid.getChildren().contains(pvWriteLabel)){
            grid.getChildren().removeAll(pvWriteField, pvWriteLabel);
        }
        
        while(visualWrapper.getChildren().size() != 0) {
            visualWrapper.getChildren().remove(visualWrapper.getChildren().size() - 1);
        }
    }
    
    private void clearFields(){
        pvValueField.setText("");
        pvWriteField.setText("");
        lastErrorField.setText("");
        metadataField.setText("");
        pvTimeField.setText("");
        pvTypeField.setText("");
        displayLimitsField.setText("");
        alarmLimitsField.setText("");
        warningLimitsField.setText("");
        controlLimitsField.setText("");
        unitField.setText("");
        expressionTypeField.setText("");
        expressionNameField.setText("");
        channelHandlerField.setText("");
        usageCountField.setText("");
        connectedRWField.setText("");
        channelPropertiesField.setText("");
        writeConnectedField.setText("");
        connectedField.setText("");
        
        if(grid.getChildren().contains(pvWriteField)){
            grid.getChildren().remove(pvWriteField);
        }
        pvWriteFieldAdded = false;
        pvWriteField.setEditable(true);
    }
    
    private void resetChoiceBox(){
        
        if(grid.getChildren().contains(visualChooser)){
            grid.getChildren().remove(visualChooser);
        }
        
        visualStringsArray.clear();
        visualGraphArray.clear();
        
        visualChooser = new ChoiceBox();
        chooserAdded = false;
    }
    
    private void initComponents(){
        pvNameField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                //case when user switches channels while one is still open.
                if (pv != null || formulaPV != null) {
                    
                    if(pv != null){
                        pv.close();
                    }
                    
                    if(formulaPV != null) {
                        formulaPV.close();
                    }
                    
                    channelChanged = true;
                    
                    hideVisual();
                    resetChoiceBox();
                    clearFields();
                }

                //attempt to set up a channel from user input
                try {
                    if(pvNameField.getText().startsWith("=")){
                        formulaPV = PVManager.read(formula(pvNameField.getText()))
                            .timeout(TimeDuration.ofSeconds(5))
                            .readListener(new PVReaderListener<Object>() {
                                @Override
                                public void pvChanged(PVReaderEvent<Object> event) {
                                    if(!channelChanged) {
                                        setLastError(formulaPV.lastException());
                                        Object value = formulaPV.getValue();
                                        setTextValue(format.format(value));
                                        setType(ValueUtil.typeOf(value));
                                        setTime(ValueUtil.timeOf(value));
                                        setIndicator(ValueUtil.normalizedNumericValueOf(value));
                                        setMetadata(ValueUtil.displayOf(value));
                                        setAlarm(ValueUtil.alarmOf(value));
                                        setConnected(formulaPV.isConnected());
                                        if(value != null && !chooserAdded){
                                            setChoiceBox(value);
                                        }
                                        if(showVisual && (value != null)){
                                            setVisual(value);
                                        }
                                    }
                                    else {
                                        channelChanged = false;
                                    }
                                }
                            })
                            .maxRate(ofHertz(10));
                    }
                    else {
                        pv = PVManager.readAndWrite(channel(pvNameField.getText()))
                                .timeout(TimeDuration.ofSeconds(5))
                                .readListener(new PVReaderListener<Object>() {
                                        @Override
                                        public void pvChanged(PVReaderEvent<Object> event) {
                                            if(!channelChanged) {
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
                                            else {
                                                channelChanged = false;
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
                    }
                } 
                catch (RuntimeException ex) { //if the channel does not work, then throw an error.
                    setLastError(ex);
                }
            }
        });
        
        pvWriteField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(pv.isWriteConnected()){
                    pv.write(pvWriteField.getText());
                }
            }
        });
        
        pvValueField.setEditable(false);
        lastErrorField.setEditable(false);
        metadataField.setEditable(false);
        pvTimeField.setEditable(false);
        pvTypeField.setEditable(false);
        displayLimitsField.setEditable(false);
        alarmLimitsField.setEditable(false);
        warningLimitsField.setEditable(false);
        controlLimitsField.setEditable(false);
        unitField.setEditable(false);
        expressionTypeField.setEditable(false);
        expressionNameField.setEditable(false);
        channelHandlerField.setEditable(false);
        usageCountField.setEditable(false);
        connectedRWField.setEditable(false);
        channelPropertiesField.setEditable(false);
        writeConnectedField.setEditable(false);
        connectedField.setEditable(false);
      
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        metaDataGrid.setAlignment(Pos.TOP_LEFT);
        metaDataGrid.setHgap(10);
        metaDataGrid.setVgap(10);
        metaDataGrid.setPadding(new Insets(10, 25, 10, 25));
        
        generalInfoGrid.setAlignment(Pos.TOP_LEFT);
        generalInfoGrid.setHgap(10);
        generalInfoGrid.setVgap(10);
        generalInfoGrid.setPadding(new Insets(10, 25, 10, 25));
        
        Separator seperator1 = new Separator();
        
        visualWrapper.setAlignment(Pos.CENTER);
        
        grid.addRow(0, pvNameLabel, pvNameField);
        grid.add(seperator1, 0, 1, 2, 1);
        grid.addRow(2, pvValueLabel, pvValueField);
        grid.addRow(7, pvTimeLabel, pvTimeField);
        grid.add(metaDataPane, 0, 8, 2, 1);
        grid.add(generalInfoPane, 0, 9, 2, 1);

        
        metaDataGrid.addRow(0, pvTypeLabel, pvTypeField);
        metaDataGrid.addRow(1, displayLimitsLabel, displayLimitsField);
        metaDataGrid.addRow(2, alarmLimitsLabel, alarmLimitsField);
        metaDataGrid.addRow(3, warningLimitsLabel, warningLimitsField);
        metaDataGrid.addRow(4, controlLimitsLabel, controlLimitsField);
        metaDataGrid.addRow(5, unitLabel, unitField);
        
        generalInfoGrid.addRow(0, lastErrorLabel, lastErrorField);
        generalInfoGrid.addRow(1, writeConnectedLabel, writeConnectedField);
        generalInfoGrid.addRow(2, connectedLabel, connectedField);
        
        metaDataPane.setText("Metadata");
        metaDataPane.setContent(metaDataGrid);
        
        generalInfoPane.setText("General Information");
        generalInfoPane.setContent(generalInfoGrid);
        
        ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
        ColumnConstraints column2 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS); //column2 contains textfields, this causes the textfields to grow with the window.
        grid.getColumnConstraints().addAll(column1, column2);
        metaDataGrid.getColumnConstraints().addAll(column1, column2);
        generalInfoGrid.getColumnConstraints().addAll(column1, column2);
        
        for(int i = 0; i < 10; i++){
            grid.getRowConstraints().add(new RowConstraints());
        }
        
    }
    
    private void setupLineGraph() {
        swapVisual();
        visualConfigButton = new Button("Configure");
        visualConfigButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                LineGraphDialogue dialogue = new LineGraphDialogue();
                dialogue.start(lineGraphApp);
            }
        });
        grid.add(visualConfigButton, 1, 4);
        showVisual = true;
        showLineGraph = true;
    }
    
    private void setupIntensityGraph() {
        swapVisual();
        visualConfigButton = new Button("Configure");
        visualConfigButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event){
                IntensityGraphDialogue dialogue = new IntensityGraphDialogue();
                dialogue.start(intensityGraphApp);
            }
        });
        grid.add(visualConfigButton, 1, 4);
        showVisual = true;
        showIntensityGraph = true;
    }
    
    private void setupGraph(BaseGraphApp graph){
        if(graph instanceof LineGraphApp){
            setupLineGraph();
        }
        if (graph instanceof IntensityGraphApp) {
            setupIntensityGraph();
        }
    }
    
    private void showGraph(VNumberArray value, BaseGraphApp graph) {
        
        /**
        *Graph height is calculated by subtracting the total height of the all of the currently visible
        *text fields from the height of the grid (which is equal to the distance between the top and bottom
        *of the window border.)
        */
        int graphHeight = 0;
        if(metaDataPane.isExpanded() && generalInfoPane.isExpanded()){ 
            graphHeight = (int)(grid.getHeight() - (17*10 + 16*pvNameField.getHeight() + 50));
        } 
        else if(metaDataPane.isExpanded()) {
            graphHeight = (int)(grid.getHeight() - (14*10 + 13*pvNameField.getHeight() + 50));
        } 
        else if(generalInfoPane.isExpanded()) {
            graphHeight = (int)(grid.getHeight() - (11*10 + 10*pvNameField.getHeight() + 50));
        } 
        else {
            graphHeight = (int)(grid.getHeight() - (7*10 + 6*pvNameField.getHeight() + 50));
        }
        
        final int graphHeightFinal = Math.max(60, graphHeight);
        
        final int graphWidthFinal = Math.max(60, (int)(grid.getWidth() - 50));
        
        final byte[] pixels = graph.render(value, graphWidthFinal, graphHeightFinal);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                drawByteArray(pixels, graphWidthFinal, graphHeightFinal);
                if(!visualAdded) {
                    visualWrapper.getChildren().add(visualImageView);
                    grid.add(visualWrapper, 0, 5, 2, 2);
                    grid.getRowConstraints().get(5).setMaxHeight(Double.MAX_VALUE);
                    grid.getRowConstraints().get(5).setVgrow(Priority.ALWAYS);
                    visualAdded = true;
                }
            }
        });
    }
}


