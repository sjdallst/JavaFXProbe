/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.epics.javafxprobe;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.swing.JComponent;
import javax.swing.JFrame;
import static org.epics.pvmanager.ExpressionLanguage.channel;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.PVWriterEvent;
import org.epics.pvmanager.PVWriterListener;
import org.epics.pvmanager.sample.BaseGraphApp;
import org.epics.pvmanager.sample.LineGraphApp;
import org.epics.pvmanager.sample.SetupUtil;
import org.epics.util.time.TimeDuration;
import static org.epics.util.time.TimeDuration.ofHertz;
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
    private Canvas canvas = new Canvas(100, 100);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private boolean showCanvas = false;
    
    public void start(){
        this.start(new Stage());
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        pvNameField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (pv != null) {
                    pv.close();
                    lastErrorField.setText("");
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
               if(showCanvas) {
                   grid.getChildren().remove(canvas);
                   showCanvas = false;
               }
               else {
                   grid.add(canvas, 0, 16, 2, 2);
                   showCanvas = true;
               }
               grid.getChildren().remove(pvValueField);
           }
        });
        
        gc.setFill(Paint.valueOf("white"));
        gc.fillRect(0, 0, 100, 100);
        gc.strokeOval(0, 0, 100, 100);
        
        final SwingNode swingNode = new SwingNode();
        createSwingContent(swingNode);
      
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Scene scene = new Scene(grid, 400, 600);
        
        Separator seperator1 = new Separator();
        Separator seperator2 = new Separator();
        Separator seperator3 = new Separator();
        
        grid.addRow(0, pvNameLabel, pvNameField);
        grid.add(seperator1, 0, 1, 2, 1);
        grid.addRow(2, pvValueLabel, pvValueField);
        grid.addRow(3, pvTimeLabel, pvTimeField);
        grid.add(seperator2, 0, 4, 2, 1);
        grid.addRow(5, pvTypeLabel, pvTypeField);
        grid.addRow(6, displayLimitsLabel, displayLimitsField);
        grid.addRow(7, alarmLimitsLabel, alarmLimitsField);
        grid.addRow(8, warningLimitsLabel, warningLimitsField);
        grid.addRow(9, controlLimitsLabel, controlLimitsField);
        grid.addRow(10, unitLabel, unitField);
        grid.add(seperator3, 0, 11, 2, 1);
        grid.addRow(12, lastErrorLabel, lastErrorField);
        grid.addRow(13, writeConnectedLabel, writeConnectedField);
        grid.addRow(14, connectedLabel, connectedField);
        grid.add(viewTestButton, 0, 15, 2, 1);
        grid.add(swingNode, 0, 17, 2, 6);
        
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
    
    private void createSwingContent(SwingNode swingNode){
        swingNode.setContent(((new LineGraphApp()).getRootPane()));
    }
}
