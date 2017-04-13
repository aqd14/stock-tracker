/**
 * 
 */
package main.java.utility;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * @author doquocanh-macbook
 *
 */
/** a node which displays a value on hover, but is otherwise empty */
public class HoveredThresholdNode extends StackPane {
	public HoveredThresholdNode(double priorValue, double value) {
		setPrefSize(5, 5);

		final Label label = createDataThresholdLabel(priorValue, value);
	    label.setTranslateY(-40);
		setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				getChildren().setAll(label);
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

	private Label createDataThresholdLabel(double priorValue, double value) {
		final Label label = new Label(value + "");
		label.getStyleClass().addAll("default-color1", "chart-line-symbol", "chart-series-line");
		label.setStyle("-fx-font: Verdana; -fx-font-size: 15; -fx-font-weight: bold;");
		label.setTextFill(Color.GREEN);
		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		return label;
	}
}
