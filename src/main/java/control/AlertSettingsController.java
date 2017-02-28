/**
 * 
 */
package main.java.control;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import main.java.model.Stock;

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
	// Flag to check toggles switch ON/OFF
	boolean valueAlertSwitchOn = false;
	boolean combinedValueAlertSwitchOn = false;
	boolean netProfitAlertSwitchOn = false;
	
	/**
	 * 
	 */
	public AlertSettingsController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Initialize status of alert settings. Disable [combined value alert] and [net profit alert]
	 * if user doesn't own selected stock.
	 */
	public void initAlertSettings(String stockCode) {
		// Check status of selected stock if it already belongs to user
		List<Stock> stocks = userStockManager.findStocks(user.getId(), stockCode);
		// User doesn't have this stock on portfolio
		if(stocks == null || stocks.isEmpty()) {
			// Disable alert settings for [Combined Value Alert] and [Net Profit Alert]
			combinedValueAlert.setDisable(true);
			netProfitAlert.setDisable(true);
		} else {
			// Enable alert settings for [Combined Value Alert] and [Net Profit Alert]
			combinedValueAlert.setDisable(false);
			netProfitAlert.setDisable(false);
		}
		// Set editable for threshold based on current status of toggle button
		valueThreshold.setEditable(valueAlertSwitchOn);
		combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
		netProfitThreshold.setEditable(netProfitAlertSwitchOn);
		
		// Event handler when user clicks on [Value Alert] toggle
		valueAlert.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (valueAlertSwitchOn) {
					System.out.println("Turning OFF Value Alert.");
					valueThreshold.clear(); // Remove threshold when user turns off alert
				} else {
					System.out.println("Turning ON Value Alert.");
				}
				valueAlertSwitchOn = !valueAlertSwitchOn;
				valueThreshold.setEditable(valueAlertSwitchOn);
			}
		});
		
		// Event handler when user clicks on [Combined Value Alert] toggle
		combinedValueAlert.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (combinedValueAlertSwitchOn) {
					System.out.println("Turning OFF Combined Value Alert.");
					combinedValueThreshold.clear(); // Remove threshold when user turns off alert
				} else {
					System.out.println("Turning ON Combined Value Alert.");
				}
				combinedValueAlertSwitchOn = !combinedValueAlertSwitchOn;
				combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
			}
		});
		
		// Event handler when user clicks on [Combined Value Alert] toggle
		netProfitAlert.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (netProfitAlertSwitchOn) {
					System.out.println("Turning OFF Net Profit Alert.");
					netProfitThreshold.clear(); // Remove threshold when user turns off alert
				} else {
					System.out.println("Turning ON Net Profit Alert.");
				}
				netProfitAlertSwitchOn = !netProfitAlertSwitchOn;
				netProfitThreshold.setEditable(netProfitAlertSwitchOn);
			}
		});
	}
}
