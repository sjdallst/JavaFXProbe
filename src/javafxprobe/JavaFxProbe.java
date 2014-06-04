/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxprobe;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 *
 * @author sjdallst
 */
public class JavaFxProbe extends Application {
    
    private int count= 0;
    private Button btn = new Button();
    private Text countText = new Text("" + count);
    
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
      
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(grid, 300, 250);
        
        grid.add(hbText, 0, 0);
        grid.add(hbBtn, 0, 1);
        
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
}
