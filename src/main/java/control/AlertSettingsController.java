/**
 * 
 */
package main.java.control;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.UserStock;
import main.java.model.UserStockId;
import main.java.utility.AlertFactory;
import main.java.utility.CurrencyFormatter;
import main.java.utility.Utils;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author doquocanh-macbook
 *
 */
public class AlertSettingsController extends BaseController implements Initializable {
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
	
	final String DEFAULT_THRESHOLD = "$0.00";
	
	// Code of selected stock
	private Stock selectedStock;
	/**
	 * 
	 */
	public AlertSettingsController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Text formatting handler for threshold
		valueThreshold.setTextFormatter(new CurrencyFormatter());
		combinedValueThreshold.setTextFormatter(new CurrencyFormatter());
		netProfitThreshold.setTextFormatter(new CurrencyFormatter());
		
		// Save alert settings to database
		saveAlertSettingsButton.setOnAction(eventHandler -> {
			BigDecimal valueTh = new BigDecimal(-1);
			BigDecimal combinedTh = new BigDecimal(-1);
			BigDecimal netProfitTh = new BigDecimal(-1); 
			// Flag to check if user changed any settings
			boolean isSettingsUpdated = false;
			
			// This method doesn't verify user's input. Should be handled whenever user enter something on text field
			System.out.println("User owned stock: " + selectedStock.getStockCode());
			if (!valueAlert.isDisabled() && !valueThreshold.getText().equals(DEFAULT_THRESHOLD)) {
				isSettingsUpdated = true;
				Double value = Utils.parseCurrencyDouble(valueThreshold.getText());
				if (value != null)
					valueTh = new BigDecimal(value);
			}
			
			if (!combinedValueAlert.isDisabled() && !combinedValueThreshold.getText().equals(DEFAULT_THRESHOLD)) {
				isSettingsUpdated = true;
				Double value = Utils.parseCurrencyDouble(combinedValueThreshold.getText());
				if (value != null)
					combinedTh = new BigDecimal(value);
			}
			
			if (!netProfitAlert.isDisabled() && !netProfitThreshold.getText().equals(DEFAULT_THRESHOLD)) {
				isSettingsUpdated = true;
				Double value = Utils.parseCurrencyDouble(netProfitThreshold.getText());
				if (value != null)
					netProfitTh = new BigDecimal(value);
			}
			
			if (isSettingsUpdated) {
				List<UserStock> userStocks = userStockManager.findUserStock(user.getId(), selectedStock.getStockCode());
				if (userStocks != null && !userStocks.isEmpty()) {
					for (UserStock us : userStocks) {
						if (valueTh.compareTo(BigDecimal.ZERO) > 0)
							us.setValueThreshold(valueTh);
						if (combinedTh.compareTo(BigDecimal.ZERO) > 0) 
							us.setCombinedValueThreshold(combinedTh);
						if (netProfitTh.compareTo(BigDecimal.ZERO) > 0)
							us.setNetProfitThreshold(netProfitTh);
						// Update to db
						userStockManager.update(us);
					}
				} else { // Create new UserStock instance in database but user doesn't own this stock
					System.out.println("User doesn't own stock: " + selectedStock.getStockCode());
					stockManager.add(selectedStock);
					UserStockId userStockId = new UserStockId(selectedStock.getId(), user.getId());
					UserStock userStock = new UserStock(userStockId, selectedStock, user, valueTh, combinedTh, netProfitTh);
					userStockManager.add(userStock);
				}
				// Display successful message to user
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.UPDATE_ALERT_SETTINGS_SMS);
				alert.showAndWait();
			}
		});
	}
	
	/**
	 * Initialize status of alert settings. Disable [combined value alert] and [net profit alert]
	 * if user doesn't own selected stock.
	 */
	public void initAlertSettings(Stock stock) {
		selectedStock = stock;
		// Check status of selected stock if it already belongs to user
		// User doesn't have this stock on portfolio
		if(userStockManager.hasStock(user.getId(), selectedStock.getStockCode())) {
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
				valueThreshold.setText(DEFAULT_THRESHOLD);; // Remove threshold when user turns off alert
			}
			valueAlertSwitchOn = !valueAlertSwitchOn;
			valueThreshold.setEditable(valueAlertSwitchOn);
		});
		
		// Event handler when user clicks on [Combined Value Alert] toggle
		combinedValueAlert.setOnAction(eventHandler -> {
			if (combinedValueAlertSwitchOn) {
				combinedValueThreshold.setText(DEFAULT_THRESHOLD);; // Remove threshold when user turns off alert
			}
			combinedValueAlertSwitchOn = !combinedValueAlertSwitchOn;
			combinedValueThreshold.setEditable(combinedValueAlertSwitchOn);
		});
		
		// Event handler when user clicks on [Combined Value Alert] toggle
		netProfitAlert.setOnAction(eventHandler -> {
			if (netProfitAlertSwitchOn) {
				netProfitThreshold.setText(DEFAULT_THRESHOLD);; // Remove threshold when user turns off alert
			}
			netProfitAlertSwitchOn = !netProfitAlertSwitchOn;
			netProfitThreshold.setEditable(netProfitAlertSwitchOn);
		});
	}
}
