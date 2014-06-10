/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.epics.javafxprobe;

import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.swing.border.Border;
import static org.epics.pvmanager.ExpressionLanguage.channel;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.PVWriterEvent;
import org.epics.pvmanager.PVWriterListener;
import org.epics.pvmanager.sample.SetupUtil;
import org.epics.util.time.TimeDuration;
import static org.epics.util.time.TimeDuration.ofHertz;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
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
    private Text pvNameLabel = new Text("PV Name: ");
    private Text pvValueLabel = new Text("Value: ");
    private Text lastErrorLabel = new Text("Last Error: ");
    private Text metadataLabel = new Text("Meta Data: ");
    private Text pvTimeLabel = new Text("Time: ");
    private Text pvTypeLabel = new Text("Type: ");
    private Text writeConnectedLabel = new Text("Write Connected: ");
    private Text connectedLabel = new Text("Connected: ");
    private TextField pvNameField = new TextField();
    private TextField pvValueField = new TextField();
    private TextField lastErrorField = new TextField();
    private TextField metadataField = new TextField();
    private TextField pvTimeField = new TextField();
    private TextField pvTypeField = new TextField();
    private TextField writeConnectedField = new TextField();
    private TextField connectedField = new TextField();
    private Slider indicator = new Slider();
    private ValueFormat format = new SimpleValueFormat(3);
    
    Map<AlarmSeverity, Border> borders = new EnumMap<AlarmSeverity, Border>(AlarmSeverity.class);
    
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
                    pv = PVManager.readAndWrite(channel(pvNameLabel.getText()))
                            .timeout(TimeDuration.ofSeconds(5))
                            .readListener(new PVReaderListener<Object>() {
                                    @Override
                                    public void pvChanged(PVReaderEvent<Object> event) {
                                        setLastError(pv.lastException());
                                        Object value = pv.getValue();
                                        setTextValue(format.format(value));
                                        setType(ValueUtil.typeOf(value));
                                        setAlarm(ValueUtil.alarmOf(value));
                                        setTime(ValueUtil.timeOf(value));
                                        setIndicator(ValueUtil.normalizedNumericValueOf(value));
                                        setMetadata(ValueUtil.displayOf(value));
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
                pvNameField.clear();
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
      
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(grid, 300, 400);
        
        Separator seperator1 = new Separator();
        Separator seperator2 = new Separator();
        
        grid.add(pvNameLabel, 0, 0);
        grid.add(pvNameField, 1, 0);
        grid.add(seperator1, 0, 1, 2, 1);
        grid.add(pvValueLabel, 0, 2);
        grid.add(pvValueField, 1, 2);
        grid.add(seperator2, 0, 3, 2, 1);
        grid.add(lastErrorLabel, 0, 4);
        grid.add(lastErrorField, 1, 4);
        grid.add(metadataLabel, 0, 5);
        grid.add(metadataField, 1, 5);
        grid.add(pvTimeLabel, 0, 6);
        grid.add(pvTimeField, 1, 6);
        grid.add(pvTypeLabel, 0, 7);
        grid.add(pvTypeField, 1, 7);
        grid.add(writeConnectedLabel, 0, 8);
        grid.add(writeConnectedField, 1, 8);
        grid.add(connectedLabel, 0, 9);
        grid.add(connectedField, 1, 9);
        
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
        SetupUtil.defaultCASetupForSwing();
        Platform.runLater(new Runnable() {
            public void run() {
                new JavaFXProbe().start();
            }
        }); 
    }
    
    private void setTextValue(String value) {
        if (value == null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    pvValueField.setText("");
                }
            });
        } else {
            final String value1 = value;
            Platform.runLater(new Runnable() {
                public void run() {
                    pvValueField.setText(value1);
                }
            });
        }
    }

    private void setType(Class type) {
        if (type == null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    pvTypeField.setText("");
                }
            });
        } else {
            final String simpleName = type.getSimpleName();
            Platform.runLater(new Runnable() {
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
                public void run() {
                    pvTypeField.setText("");
                }
            });
        } else {
            final String timeString = time.getTimestamp().toDate().toString();
            Platform.runLater(new Runnable() {
                public void run() {
                    pvTypeField.setText(timeString);
                }
            });
        }
    }

    private void setMetadata(Display display) {
        if (display == null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    metadataField.setText("");
                }
            });
        } else {
            final String metadata = display.getUpperDisplayLimit() + " - " + display.getLowerDisplayLimit();
            Platform.runLater(new Runnable() {
                public void run() {
                    metadataField.setText(metadata);
                }
            });
        }
    }

    private void setLastError(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            final String message = ex.getClass().getSimpleName() + " " + ex.getMessage();
            Platform.runLater(new Runnable() {
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
                public void run() {
                    connectedField.setText(connectedString);
                }
            });
        } else {
            Platform.runLater(new Runnable() {
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
                public void run() {
                    writeConnectedField.setText(connectedString);
                }
            });
        } else {
            Platform.runLater(new Runnable() {
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
            public void run() {
                indicator.setValue(position1);
            }
        });
    }
}
