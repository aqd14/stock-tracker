/**
 * 
 */
package main.java.control;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.BorderPane;
import main.java.model.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

/**
 * @author doquocanh-macbook
 *
 */
public class HomeController {
	@FXML private BorderPane homeBP;
	@FXML private JFXTreeTableView<Stock> stockTableView;
	
	@FXML private TreeTableColumn<Stock, String> stockCodeCol;
	@FXML private TreeTableColumn<Stock, String> companyCol;
	@FXML private TreeTableColumn<Stock, Double> priceCol;
	@FXML private TreeTableColumn<Stock, Double> lastPriceCol;
	@FXML private TreeTableColumn<Stock, Double> changeCol;
	@FXML private TreeTableColumn<Stock, Double> percentChangeCol;
	/**
	 * @throws IOException 
	 * 
	 */
	
	public void initTableView() throws IOException {
		String[] symbols = new String[] {"INTC", "AAPL", "GOOG", "YHOO", "XOM", "WMT", "TM", "KO", "HPQ"};
		ObservableList<Stock> stocks = getMultipleStockData(symbols);
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
	}
	
	/**
	 * Get list of big stocks
	 * @return stocks
	 * @throws IOException 
	 */
	private ObservableList<Stock> getMultipleStockData(String[] symbols) throws IOException {
		if (symbols == null || symbols.length <= 0) {
			System.err.println("Stock list is incorrect!");
			return null;
		}
		Map<String, yahoofinance.Stock> stocksMap = YahooFinance.get(symbols, true);
		ObservableList<Stock> stocks = extractStockData(stocksMap);
		return stocks;
	}
	
	private ObservableList<Stock> extractStockData(Map<String, yahoofinance.Stock> stocksMap) throws IOException {
		ObservableList<Stock> stocks = FXCollections.observableArrayList();
		for (Map.Entry<String, yahoofinance.Stock> entry : stocksMap.entrySet()) {
			Stock stock = new Stock();
			stock.setStockCode(entry.getKey());
			yahoofinance.Stock s = entry.getValue();
			s.print();
			// Extract stock information
			stock.setStockName(s.getName());
			StockQuote stockQuote = s.getQuote(true);
			if (stockQuote != null) {
				if (stockQuote.getPrice() != null)
					stock.setPrice(stockQuote.getPrice().doubleValue());
				if (stockQuote.getPreviousClose() != null)
					stock.setPreviousPrice(stockQuote.getPreviousClose().doubleValue());
			}
			stocks.add(stock);
		}
		return stocks;
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
	
	private void setPriceFormatColumn(TreeTableColumn<Stock, Double> c) {
		c.setCellFactory(col -> 
	    new TreeTableCell<Stock, Double>() {
	        @Override 
	        public void updateItem(Double price, boolean empty) {
	            super.updateItem(price, empty);
	            DecimalFormat doubleFormat = new DecimalFormat("+#,##0.00;-#");
	            if (empty) {
	                setText(null);
	            } else {
            		setText(doubleFormat.format(price));
	            }
	        }
	    });
	}
}
