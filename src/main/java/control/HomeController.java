/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.java.model.Stock;
import main.java.utility.Utility;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController extends ParentController implements Initializable {
	@FXML private AnchorPane homeAP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, Double> priceCol;
	@FXML private TreeTableColumn<Stock, Double> lastPriceCol;
	@FXML private TreeTableColumn<Stock, Double> changeCol;
	@FXML private TreeTableColumn<Stock, Double> percentChangeCol;
//	@FXML private JFXTreeTableColumn<Integer, String> stockBuyCol;
	
	@FXML private JFXTextField searchTF;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// List of 30 stocks that will be displayed in Home page
		final String[] stockSymbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT",
													"TM", "KO", "HPQ", "FB", "F", "MSFT", 
													"BRK-A", "AMZN", "XOM", "JPM", "WFC", "GE",
													"BAC", "T", "BABA", "PG", "CVX", "V",
													"VZ", "HD", "DIS", "INTC", "ORCL", "HSBC"};
		ObservableList<Stock> stocks = null;
		try {
			stocks = Utility.getMultipleStockData(stockSymbols);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TreeItem<Stock> root = new RecursiveTreeItem<Stock>(stocks, RecursiveTreeObject::getChildren);
		stockTableView.setRoot(root);
		stockTableView.setShowRoot(false);
		stockTableView.setEditable(true);
		// Set Cell Value Factory
		setCellValueStockCode();
		setCellValueCompanyName();
		setCellValueStockPrice();
		setCellFactoryLastPrice();
		setCellFactoryPriceChange();
		setCellFactoryPercentageChange();
		// setCellFactoryStockBuy();
		
		// Add listener for search field
		searchTF.textProperty().addListener((o,oldVal,newVal)->{
			stockTableView.setPredicate(stock -> filterCriteria(stock, newVal));
		});
	}
	
	private boolean filterCriteria(TreeItem<Stock> stock, String value) {
		return stock.getValue().getStockCode().toLowerCase().contains(value.toLowerCase()) ||
				stock.getValue().getStockName().toLowerCase().contains(value.toLowerCase());
	}
	
	private void setCellValueStockCode() {
		stockCodeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockCode()));
	}
	
	private void setCellValueCompanyName() {
		companyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockName()));
	}
	
	private void setCellValueStockPrice() {
		priceCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(param.getValue().getValue().getPrice()).asObject());
	}
	
	private void setCellFactoryLastPrice() {
		lastPriceCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(param.getValue().getValue().getPreviousPrice()).asObject());
	}
	
	private void setCellFactoryPriceChange() {
		changeCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(Utility.round(param.getValue().getValue().getPrice() - param.getValue().getValue().getPreviousPrice(), 2)).asObject());
		//setPriceFormatColumn(changeCol);
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(
		        Utility.round((param.getValue().getValue().getPrice() - param.getValue().getValue().getPreviousPrice())
		                / param.getValue().getValue().getPrice(),2)).asObject());
		//setPriceFormatColumn(percentChangeCol);
	}

	@FXML private void openAccountSettings(ActionEvent event) {
		Stage settingsStage = new Stage();
		settingsStage.setTitle("Account Settings");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/Settings.fxml"));
        try {
			Parent root = (Parent)loader.load();
			// Set current user
			SettingsController settingsController = loader.<SettingsController>getController();
			settingsController.setUser(user);
			settingsController.setAccountName(user.getAccount().getAccountName());
			
			settingsStage.setScene(new Scene(root));
			settingsStage.setResizable(false);
			settingsStage.show();
		} catch (IOException e) {
			System.err.println("Couldn't load FXML file!");
			e.printStackTrace();
		}
	}
	
//	private void setCellFactoryStockBuy() {
//		stockBuyCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<Stock, String> param) ->{
//			if(stockBuyCol.validateValue(param)) 
//				return param.getValue().getValue().getStockBuy();
//			else 
//				return stockBuyCol.getComputedValue(param);
//		});
//		
//		stockBuyCol.setCellFactory((TreeTableColumn<Stock, String> param) -> new GenericEditableTreeTableCell<Stock, String>(new TextFieldEditorBuilder()));
//		stockBuyCol.setOnEditCommit((CellEditEvent<Stock, String> t)->{
//			((Stock) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()).setStockBuy(t.getNewValue());
//		});
//	}
	
	/**
	 * Set format for stock price. Only two floating points displayed
	 * @param c
	 */
//	private void setPriceFormatColumn(TreeTableColumn<Stock, Double> c) {
//		c.setCellFactory(col -> new TreeTableCell<Stock, Double>() {
//	        @Override 
//	        public void updateItem(Double price, boolean empty) {
//	            //super.updateItem(price, empty);
//	            if (empty || price < 0.001) {
//	                setText("0.00");
//	            } else {
//	            	DecimalFormat doubleFormat = new DecimalFormat("+#,##0.000;-#");
//            		setText(doubleFormat.format(price));
//	            }
//	        }
//	    });
//	}
	
	
}
