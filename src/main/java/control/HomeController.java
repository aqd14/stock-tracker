/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import main.java.model.Stock;
import main.java.utility.Utility;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController implements Initializable {
	@FXML private AnchorPane homeAP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, Double> priceCol;
	@FXML private TreeTableColumn<Stock, Double> lastPriceCol;
	@FXML private TreeTableColumn<Stock, Double> changeCol;
	@FXML private TreeTableColumn<Stock, Double> percentChangeCol;
	@FXML private TreeTableColumn<Stock, Integer> stockBuyCol;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		// List of stocks that will be displayed in Home page
		final String[] stockSymbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT", "TM", "KO", "HPQ"};
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
		// Set Cell Value Factory
		setCellValueStockCode();
		setCellValueCompanyName();
		setCellValueStockPrice();
		setCellFactoryLastPrice();
		setCellFactoryPriceChange();
		setCellFactoryPercentageChange();
		setCellFactoryStockBuy();
	}
	
	private void setCellValueStockCode() {
		stockCodeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockCode()));
	}
	
	private void setCellValueCompanyName() {
		companyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getStockName()));
	}
	
	private void setCellValueStockPrice() {
		priceCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(param.getValue().getValue().getPrice()).asObject());
		// Add Dollar sign to price
//		priceCol.setCellFactory(col -> 
//	    new TreeTableCell<Stock, Double>() {
//	        @Override 
//	        public void updateItem(Double price, boolean empty) {
//	            super.updateItem(price, empty);
//	            if (empty) {
//	                setText(null);
//	            } else {
//	                setText(String.format("US$ %.2f", price));
//	            }
//	        }
//	    });
	}
	
	private void setCellFactoryLastPrice() {
		lastPriceCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(param.getValue().getValue().getPreviousPrice()).asObject());
	}
	
	private void setCellFactoryPriceChange() {
		changeCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(param.getValue().getValue().getPrice() - param.getValue().getValue().getPreviousPrice()).asObject());
		setPriceFormatColumn(changeCol);
	}
	
	private void setCellFactoryPercentageChange() {
		percentChangeCol.setCellValueFactory(param -> new ReadOnlyDoubleWrapper(
		        (param.getValue().getValue().getPrice() - param.getValue().getValue().getPreviousPrice())
		                / param.getValue().getValue().getPrice()).asObject());
		setPriceFormatColumn(percentChangeCol);
	}
	
	private void setCellFactoryStockBuy() {
		stockBuyCol.setCellValueFactory(param -> new ReadOnlyIntegerWrapper(
		        param.getValue().getValue().getStockBuy()).asObject());
		
//		stockBuyCol.setCellFactory((TreeTableColumn<Stock, Integer> param) -> new GenericEditableTreeTableCell<Stock, Integer>(new TextFieldEditorBuilder()));
//		stockBuyCol.setOnEditCommit((CellEditEvent<Stock, Integer> t)->{
//			((Stock) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()).setStockBuy(t.getNewValue());
//		});
	}
	
	private void setPriceFormatColumn(TreeTableColumn<Stock, Double> c) {
		c.setCellFactory(col -> new TreeTableCell<Stock, Double>() {
	        @Override 
	        public void updateItem(Double price, boolean empty) {
	            super.updateItem(price, empty);
	            DecimalFormat doubleFormat = new DecimalFormat("+#,##0.00;-#");
	            if (empty || price <= 0.001) {
	                setText("0.00");
	            } else {
            		setText(doubleFormat.format(price));
	            }
	        }
	    });
	}
}
