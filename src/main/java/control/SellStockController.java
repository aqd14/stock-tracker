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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.common.CommonDefine;
import main.java.model.TransactionWrapper;
import main.java.utility.AlertFactory;
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
	// Buttons
	@FXML private JFXButton backBT;
	@FXML private JFXButton sellStockBT;
	
	// Flag to check action should do when switch back to Portfolio page
	boolean sellStock = false;
	int[] soldAmount = new int[1];
	
	@FXML private VBox mainVB;
	
	TransactionWrapper transaction;
	
	double price;
	/**
	 * 
	 */
	public SellStockController() {
		// TODO Auto-generated constructor stub
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
	            		soldAmount[0] = amount;
	            	}
	            	estimateCostTF.setText("$" + Utils.formatCurrencyDouble(tempPrice));
	            }
	        	sellStockBT.setDisable(isDisabled);
	        }
	    });
		// Doesn't allow user to edit current market price
		marketPriceTF.setEditable(false);
		
		// Set event handler for buttons
		backBT.setOnAction(event -> {
			sellStock = false;
			switchScreen(Screen.PORFOLIO, CommonDefine.PORTFOLIO_TITLE, "../view/Portfolio.fxml");
		});
		
		sellStockBT.setOnAction(event -> {
			sellStock = true;
			switchScreen(Screen.PORFOLIO, CommonDefine.PORTFOLIO_TITLE, "../view/Portfolio.fxml");
		});
	}
	
	public void init(TransactionWrapper t) {
		transaction = t;
		stockSymbolLB.setText(t.getStockCompany() + " (" + t.getStockCode() + ")");
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
	
	/* (non-Javadoc)
	 * @see main.java.control.IController#switchScreen(main.java.utility.Screen, java.lang.String, java.lang.String)
	 */
	@Override
	public void switchScreen(Screen target, String title, String url) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
		Parent root = null;
        try {
			switch (target) {
				case PORFOLIO:
					root = loader.load();
					PortfolioController portfolioController = loader.<PortfolioController>getController();
					portfolioController.setUser(user);
					if (sellStock) { // Perform transaction when user clicks on [Sell] button
						// Sell stock
						double curBalance = user.getAccount().getBalance();
						curBalance += portfolioController.sellSingleStock(transaction, curBalance, soldAmount);
						user.getAccount().setBalance(curBalance);
						userManager.update(user);
						
						portfolioController.initPortfolio();
						portfolioController.initTransactionHistory();
						portfolioController.initTransactionSummary();
						
						Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.TRANSACTION_SUCCESSFUL_SMS);
						alert.showAndWait();
					} else {
						portfolioController.initPortfolio();
						portfolioController.initTransactionHistory();
						portfolioController.initTransactionSummary();
					}
					break;
				default:
					return;
			}
        } catch (IOException e) {
        	e.printStackTrace();
        	return;
		}
    	Stage curStage = (Stage)mainVB.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
	}

	/* (non-Javadoc)
	 * @see main.java.control.IController#makeNewStage(main.java.utility.Screen, java.lang.String, java.lang.String)
	 */
	@Override
	public void makeNewStage(Screen target, String title, String url) {
		// TODO Auto-generated method stub

	}
}
