/**
 * 
 */
package main.java.utility;

import com.jfoenix.controls.JFXTextField;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * @author doquocanh-macbook
 *
 */
/** a node which displays a value on hover, but is otherwise empty */
public class HoveredThresholdNode extends StackPane {
	public HoveredThresholdNode(double priorValue, double value) {
		setPrefSize(5, 5);

//		final Label label = createDataThresholdLabel(priorValue, value);
//	    label.setTranslateY(-40);
		final JFXTextField tf = createDataThresholdTF(value);
		tf.setTranslateY(-30);
		tf.setPrefWidth(tf.getText().length()*10);
		setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				getChildren().setAll(tf);
				setCursor(Cursor.NONE);
				toFront();
			}
		});
		setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				getChildren().clear();
				setCursor(Cursor.CROSSHAIR);
			}
		});
	}
	
	private JFXTextField createDataThresholdTF(double value) {
		final JFXTextField tf = new JFXTextField(String.valueOf(value));
		tf.getStyleClass().addAll("default-color1", "chart-line-symbol", "chart-series-line");
		tf.setStyle("-fx-font: Verdana; -fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: green");
		tf.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		return tf;
	}
}
