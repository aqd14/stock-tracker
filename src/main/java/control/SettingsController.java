package main.java.control;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import main.java.utility.CurrencyFormatter;
import main.java.utility.PhoneNumberFormatter;
import main.java.utility.Utils;
import main.java.utility.ValidationUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingsController extends BaseController implements Initializable, Subject {
	// User information fields
	@FXML private JFXTextField firstNameTF;
	@FXML private JFXTextField lastNameTF;
	@FXML private JFXTextField emailTF;
	@FXML private JFXTextField phoneNumberTF;
	
	// New password
	@FXML private PasswordField currentPasswordPF;
	@FXML private PasswordField newPasswordPF;
	@FXML private PasswordField confirmPasswordPF;
	
	// Error text
	@FXML private Text firstNameError;
	@FXML private Text lastNameError;
	@FXML private Text emailError;
	@FXML private Text currentPasswordError;
	@FXML private Text passwordError;
	@FXML private Text confirmPasswordError;
	@FXML private Text phoneNumberError;
	
	@FXML private JFXTextField currentBalanceTF;
	@FXML private TextField newBalanceTF;
	@FXML private Label accountName;
	@FXML private Text successfulMessage;
	
	@FXML private JFXComboBox<Integer> alertCheckingTime;
	@FXML private JFXComboBox<Integer> stockUpdateTime;
	
	private ArrayList<Observer> observers = new ArrayList<>();
	
	public SettingsController() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Initialize some basic user information when user open [Settings]
	 */
	public void initUserInfo() {
		accountName.setText(user.getAccount().getAccountName());
		currentBalanceTF.setText("$" + Utils.formatCurrencyDouble(user.getAccount().getBalance()));
		firstNameTF.setText(user.getFirstName());
		lastNameTF.setText(user.getLastName());
//		emailTF.setText(user.getEmail());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		newBalanceTF.setTextFormatter(new CurrencyFormatter());
		phoneNumberTF.setTextFormatter(new TextFormatter<>(PhoneNumberFormatter::addPhoneNumberMask));
		// Set successful sms invisible
		successfulMessage.setVisible(false);
		// Initialize options for period settings
		ObservableList<Integer> options = FXCollections.observableArrayList(1, 2, 5, 10, 20, 30, 60, 120);
		alertCheckingTime.setItems(options);
		stockUpdateTime.setItems(options);
	}
	
	@FXML private void saveChanges(ActionEvent e) {
		boolean anyChange = false;
		// Update user information
		if (firstNameTF.getText().equals("")) {
			// User doesn't want to update first name
		} else {
			if (ValidationUtil.validateFirstName(firstNameTF, firstNameError)) {
				user.setFirstName(firstNameTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (lastNameTF.getText().equals("")) {
			// User doesn't want to update last name
		} else {
			 // Only validate next changes if the previous is valid
			if (anyChange && ValidationUtil.validateLastName(lastNameTF, lastNameError)) {
				user.setLastName(lastNameTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (emailTF.getText().equals("")) {
			// User doesn't want to update email
		} else {
			if (anyChange && ValidationUtil.validateEmail(emailTF, emailError)) {
				user.setEmail(emailTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (phoneNumberTF.getText().equals("###-###-####")) {
			// User doesn't want to update phone number
		}
		else {
			if (anyChange && ValidationUtil.validatePhoneNumber(phoneNumberTF, phoneNumberError)) {
				user.setPhoneNumber(phoneNumberTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (currentPasswordPF.getText().equals("") && newPasswordPF.getText().equals("") && confirmPasswordPF.getText().equals("")) {
			// User doesn't want to update password
		} else {
			// Update password
			// Another tab, still need to display error message to user so don't need to check anyChange
			if (ValidationUtil.validateCurrentPassword(user.getPassword(), currentPasswordPF, currentPasswordError)
			        && ValidationUtil.validateOriginalPassword(newPasswordPF, passwordError)
			        && ValidationUtil.validateConfirmedPassword(newPasswordPF, confirmPasswordPF, passwordError)) {
				user.setHashedPassword(newPasswordPF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		// Update balance
		if (newBalanceTF.getText().equals("$0.00")) {
			// User didn't update balance.
			// Do nothing
		} else {
			try {
				double newBalance = Utils.parseCurrencyDouble(newBalanceTF.getText());
				user.getAccount().setBalance(newBalance);
				anyChange = true;
			} catch (NumberFormatException ex) {
				System.err.println("Invalid balance: " + newBalanceTF.getText());
				anyChange = false;
			}
		}
		
		// Update alert checking time
		if (!alertCheckingTime.getSelectionModel().isEmpty()) {
			user.setAlertTime(alertCheckingTime.getSelectionModel().getSelectedItem());
			anyChange = true;
		}
		
		// Update stock update time
		if (!stockUpdateTime.getSelectionModel().isEmpty()) {
			user.setStockUpdateTime(stockUpdateTime.getSelectionModel().getSelectedItem());
			anyChange = true;
		}
		
		
		// Update to database if there is any change and all others are valid
		if (anyChange) {
			userManager.update(user);
			notifyObservers();
			System.out.println("Update succesfully...");
			updateSettingFields();
			// Display successful message for 2s.
			successfulMessage.setVisible(true);
			Task<Void> sleeper = new Task<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} 	finally {
						successfulMessage.setVisible(false);
					}
					return null;
				}
			};
			new Thread(sleeper).start();
		}
	}
	
	private void updateSettingFields() {
		// Hide all error messages
		passwordError.setVisible(false);
		emailError.setVisible(false);
		firstNameError.setVisible(false);
		lastNameError.setVisible(false);
		phoneNumberError.setVisible(false);
		currentPasswordError.setVisible(false);
		// Clear passwords
		currentPasswordPF.clear();
		newPasswordPF.clear();
		confirmPasswordPF.clear();
		currentBalanceTF.setText("$" + Utils.formatCurrencyDouble(user.getAccount().getBalance()));
		// Clear combobox selection
		alertCheckingTime.getSelectionModel().clearSelection();
		stockUpdateTime.getSelectionModel().clearSelection();
	}


	@Override
	public void register(Observer o) {
		if (observers.indexOf(o) == -1) {
			observers.add(o);
		}
	}

	@Override
	public void remove(Observer o) {
		if (observers.indexOf(o) != -1) {
			observers.remove(o);
		}
	}

	@Override
	public void notifyObservers() {
		for (Observer o : observers) {
			o.update();
		}
	}
}
