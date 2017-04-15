/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import main.java.model.TransactionWrapper;
import main.java.utility.Screen;
import main.java.utility.Utils;
import yahoofinance.YahooFinance;

/**
 * @author doquocanh-macbook
 *
 */
public class SellStockController extends BaseController implements IController, Initializable {
	@FXML private JFXTextField stockAmountTF;
	@FXML private JFXTextField marketPriceTF;
	@FXML private JFXTextField estimateCostTF;
	@FXML private Label stockSymbolLB;
	@FXML private JFXButton sellStockBT;
	
	TransactionWrapper transaction;
	
	double price;
	/**
	 * 
	 */
	public SellStockController() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see main.java.control.IController#switchScreen(main.java.utility.Screen, java.lang.String, java.lang.String)
	 */
	@Override
	public void switchScreen(Screen target, String title, String url) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see main.java.control.IController#makeNewStage(main.java.utility.Screen, java.lang.String, java.lang.String)
	 */
	@Override
	public void makeNewStage(Screen target, String title, String url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		stockAmountTF.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	        	double tempPrice = price; // Maintain current price of stock after calculation
	        	boolean isDisabled = true;
	            if (!newValue.matches("\\d*")) {
	            	stockAmountTF.setText(newValue.replaceAll("[^\\d]", ""));
	            } else if (!stockAmountTF.getText().isEmpty()){ // Above setText statement will call changed again
	              	// Update estimate price
	            	int amount = Integer.parseInt(stockAmountTF.getText());
	            	// If the entered amount is larger than own amount,
	            	// set price to zero and disable [Sell] button
	            	if (amount > transaction.getStock().getAmount()) {
	            		tempPrice = 0;
	            		isDisabled = true;
	            	} else {
	            		tempPrice = tempPrice*amount;
	            		isDisabled = false;
	            	}
	            	estimateCostTF.setText("$" + Utils.formatCurrencyDouble(tempPrice));
	            }
	        	sellStockBT.setDisable(isDisabled);
	        }
	    });
		marketPriceTF.setEditable(false);
	}
	
	public void init(TransactionWrapper t) {
		transaction = t;
		stockSymbolLB.setText(t.getStockCode());
		try {
			yahoofinance.Stock yahooStock = YahooFinance.get(t.getStockCode(), false);
			// Initialize current price
			price = yahooStock.getQuote().getPrice().setScale(2, RoundingMode.CEILING).doubleValue();
			marketPriceTF.setText("$" + String.valueOf(price));
			estimateCostTF.setText("$0.00");
			// Set button disabled as default
			sellStockBT.setDisable(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
