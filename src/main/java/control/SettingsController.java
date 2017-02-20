package main.java.control;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;

public class SettingsController implements Initializable {
	
	@FXML private TextField newBalanceTF;
	public SettingsController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Pattern validDoubleText = Pattern.compile("((\\d*)|(\\d+\\.\\d*))");

        TextFormatter<Double> textFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
            change -> {
                String newText = change.getControlNewText() ;
                if (validDoubleText.matcher(newText).matches())
                    return change;
                else 
                	return null;
            });


		newBalanceTF.setTextFormatter(textFormatter);

        textFormatter.valueProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("New double value "+newValue);
        });
		
//		newBalanceTF.textProperty().addListener(new ChangeListener<String>() {
//			@Override
//			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//				if (ValidationUtil.containOnlyNumber(newValue)) {
//					// Add "," to the balance of at least $1,000
////					if (newValue.length() == 4) {
////						newValue = new StringBuilder(newValue).insert(1, ",").toString();
////					}
////					// Add dollar sign
////					newValue = "$" + newValue;
//					newBalanceTF.setText(newValue);
//					return;
//				} else {
//					newBalanceTF.setText(oldValue);
//					return;
//				}
//			}
//			
//		});
	}
	
}
