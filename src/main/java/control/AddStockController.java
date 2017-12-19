package main.java.control;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import main.java.common.CommonDefine;
import main.java.utility.AlertFactory;
import main.java.utility.StockUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddStockController extends BaseController implements Initializable {
	@FXML private VBox parentNode;
	@FXML private JFXTextField searchStockTF;
	@FXML private JFXListView<String> stockListView;
	@FXML private JFXButton addStockBt;
	
	private HomeController homeController;
	
	public AddStockController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Disable [Add] button when user doesn't select any item
		addStockBt.setDisable(true);
		// Add event handler when user search for stock
		// Start searching when user typing
		searchStockTF.textProperty().addListener((observable, oldValue, newValue) -> {
			ObservableList<String> results = StockUtils.getMatchedQuery(newValue);
			if (results != null) {
				stockListView.setItems(results);
			} else {
				stockListView.getItems().clear(); // Make the list view empty when user clears search query
			}
		});
		// Allow user to select only one stock in list
		stockListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		stockListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				addStockBt.setDisable(newValue == null); // Disable button when user doesn't select anything
			}
		});
		// Event handler when user add stock
		addStockBt.setOnAction(event->{
			String stock = stockListView.getSelectionModel().getSelectedItem();
			// Extract stock code
			// Normally, stock symbol and company name
			// are separated by a tab. However, in some case,
			// They are separated by a space.
			int separatorIndex = stock.indexOf("\t");
			if (separatorIndex == -1) {
				separatorIndex = stock.indexOf(" ");
			}
			stock = stock.substring(0, separatorIndex);
			ArrayList<String> stockSymbols = homeController.getStocks();
			if (stockSymbols.contains(stock)) {
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.ALREADY_HAD_STOCK_SMS);
				alert.showAndWait();
			} else {
				System.out.println("Add Stock: " + stock);
				// Pass added stock symbol to Home page
				// Download stock data if not exist in table yet
				stockSymbols.add(0, stock);
				homeController.populateData(stock);
				Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, CommonDefine.ADDED_STOCK_SUCCESSFULLY_SMS);
				alert.showAndWait();
				parentNode.getScene().getWindow().hide();
			}
		});
	}

	/**
	 * @return the homeController
	 */
	public HomeController getHomeController() {
		return homeController;
	}

	/**
	 * @param homeController the homeController to set
	 */
	public void setHomeController(HomeController homeController) {
		this.homeController = homeController;
	}
}
