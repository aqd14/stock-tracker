/**
 * 
 */
package main.java.control;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import main.java.utility.Screen;

/**
 * @author doquocanh-macbook
 *
 */
public class AlertSettingsController extends ParentController implements Initializable {
	// Button ON/OFF
	@FXML private JFXToggleButton valueAlert;
	@FXML private JFXToggleButton combinedValueAlert;
	@FXML private JFXToggleButton netProfitAlert;
	// Threshold
	@FXML private JFXTextField valueThreshold;
	@FXML private JFXTextField combinedValueThreshold;
	@FXML private JFXTextField netProfitThreshold;
	// Save settings button
	@FXML private JFXButton saveAlertSettingsButton;
	// Flag to check toggles switch ON/OFF
	boolean valueAlertSwitchOn = false;
	boolean combinedValueAlertSwitchOn = false;
	boolean netProfitAlertSwitchOn = false;
	// Code of selected stock
	private String selectedStockCode;
	/**
	 * 
	 */
	public AlertSettingsController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Save alert settings to database
		saveAlertSettingsButton.setOnAction(eventHandler -> {
			if (userStockManager.hasStock(user.getId(), selectedStockCode)) {
				if (!valueThreshold.getText().isEmpty()) {
					
				}
				
				if (!combinedValueThreshold.getText().isEmpty()) {
					
				}
				
				if (!netProfitAlert.getText().isEmpty()) {
					
				}
			}
		});
	}
	
	/**
	 * Initialize status of alert settings. Disable [combined value alert] and [net profit alert]
	 * if user doesn't own selected stock.
	 */
	public void initAlertSettings(String stockCode) {
		selectedStockCode = stockCode;
		// Check status of selected stock if it already belongs to user
		// User doesn't have this stock on portfolio
		if(userStockManager.hasStock(user.getId(), selectedStockCode)) {
			// Enable alert settings for [Combined Value Alert] and [Net Profit Alert]
			combinedValueAlert.setDisable(false);
			netProfitAlert.setDisable(false);
		} else {
			// Disable alert settings for [Combined Value Alert] and [Net Profit Alert]
			combinedValueAlert.setDisable(true);
			netProfitAlert.setDisable(true);
		}
		// Set editable for threshold based on current status of toggle button
		valueThreshold.setEditable(valueAlertSwitchOn);
		combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
		netProfitThreshold.setEditable(netProfitAlertSwitchOn);
		
		// Event handler when user clicks on [Value Alert] toggle
		valueAlert.setOnAction(eventHandler -> {
			if (valueAlertSwitchOn) {
				System.out.println("Turning OFF Value Alert.");
				valueThreshold.clear(); // Remove threshold when user turns off alert
			} else {
				System.out.println("Turning ON Value Alert.");
			}
			valueAlertSwitchOn = !valueAlertSwitchOn;
			valueThreshold.setEditable(valueAlertSwitchOn);
		});
		
		// Event handler when user clicks on [Combined Value Alert] toggle
		combinedValueAlert.setOnAction(eventHandler -> {
			if (combinedValueAlertSwitchOn) {
				System.out.println("Turning OFF Combined Value Alert.");
				combinedValueThreshold.clear(); // Remove threshold when user turns off alert
			} else {
				System.out.println("Turning ON Combined Value Alert.");
			}
			combinedValueAlertSwitchOn = !combinedValueAlertSwitchOn;
			combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
		});
		
		// Event handler when user clicks on [Combined Value Alert] toggle
		netProfitAlert.setOnAction(eventHandler -> {
			if (netProfitAlertSwitchOn) {
				System.out.println("Turning OFF Net Profit Alert.");
				netProfitThreshold.clear(); // Remove threshold when user turns off alert
			} else {
				System.out.println("Turning ON Net Profit Alert.");
			}
			netProfitAlertSwitchOn = !netProfitAlertSwitchOn;
			netProfitThreshold.setEditable(netProfitAlertSwitchOn);
		});
	}

	@Override
	protected void makeNewStage(Screen target, String stageTitle, String url) {
		// TODO Auto-generated method stub
		
	}
}
