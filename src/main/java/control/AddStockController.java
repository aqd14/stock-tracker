package main.java.control;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import main.java.utility.StockUtils;

public class AddStockController extends BaseController implements Initializable {
	
	@FXML private JFXTextField searchStockTF;
	@FXML private JFXListView<String> stockListView;
	@FXML private JFXButton addStockBt;
	
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
				addStockBt.setDisable(false);			
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
			System.out.println("Add Stock: " + stock);
		});
	}
}
