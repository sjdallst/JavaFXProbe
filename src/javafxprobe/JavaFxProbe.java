/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxprobe;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author sjdallst
 */
public class JavaFxProbe extends Application {
    
    private int count= 0;
    private Button btn = new Button();
    private Text countText = new Text("" + count);
    private Text textFieldLabel = new Text("Stuff: ");
    private TextField textField = new TextField();
    private Text textFieldInput = new Text("");
    private final TableView<Thing> table = new TableView<>();
    private ObservableList<Thing> data = FXCollections.observableArrayList(new Thing(), new Thing());
    
    @Override
    public void start(Stage primaryStage) {

        btn.setText("+1");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                increment();
            }
        });
        
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.CENTER);
        hbBtn.getChildren().add(btn);
        
        HBox hbText = new HBox(10);
        hbText.setAlignment(Pos.CENTER);
        hbText.getChildren().add(countText);
        
        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                textFieldInput.setText(textField.getText());
                textField.clear();
            }
        });
        
        HBox hbInputText = new HBox(10);
        hbInputText.setAlignment(Pos.CENTER);
        hbInputText.getChildren().addAll(textFieldLabel, textField, textFieldInput);
        
        TableColumn firstColumn = new TableColumn("First");
        firstColumn.setMinWidth(100);
        firstColumn.setCellValueFactory(
                new PropertyValueFactory<>("first"));
        
        TableColumn secondColumn = new TableColumn("Second");
        secondColumn.setMinWidth(100);
        secondColumn.setCellValueFactory(
                new PropertyValueFactory<>("second"));
        
        TableColumn thirdColumn = new TableColumn("Third");
        thirdColumn.setMinWidth(100);
        thirdColumn.setCellValueFactory(
                new PropertyValueFactory<>("third"));
        
        table.setItems(data);
        table.getColumns().addAll(firstColumn, secondColumn, thirdColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        Button addButton = new Button("Add something");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                data.add(new Thing());
            }
        });
        
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(10, 0, 0, 10));
        vBox.getChildren().addAll(addButton, table);
      
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(grid, 800, 600);
        
        grid.add(hbText, 0, 0);
        grid.add(hbBtn, 0, 1);
        grid.add(hbInputText, 0, 2);
        grid.add(vBox,0,3);
        grid.setMaxWidth(500);
        
        //grid.setGridLinesVisible(true);
        
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
        launch(args);
    }
    
    public void increment(){
        count++;
        countText.setText("" + count);
    }
    
    public static class Thing{
        
        private final SimpleDoubleProperty first, second, third;
        
        public Thing(){
            first = new SimpleDoubleProperty(Math.random());
            second = new SimpleDoubleProperty(Math.random());
            third = new SimpleDoubleProperty(Math.random());
        }
        
        public void setFirst(double f){
            first.set(f);
        }
        
        public void setSecond(double s){
            second.set(s);
        }
        
        public void setThird(double t){
            third.set(t);
        }
        
        public double getFirst(){
            return first.get();
        }
        
        public double getSecond(){
            return second.get();
        }
        
        public double getThird(){
            return third.get();
        }
        
    }
}
