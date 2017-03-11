package main.java.control;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTextField;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import main.java.utility.CurrencyFormatter;
import main.java.utility.Screen;
import main.java.utility.Utility;
import main.java.utility.ValidationUtil;

public class SettingsController extends BaseController implements Initializable {
	// User information fields
	@FXML private JFXTextField firstNameTF;
	@FXML private JFXTextField lastNameTF;
	@FXML private JFXTextField emailTF;
	
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
	
	@FXML private JFXTextField currentBalanceTF;
	@FXML private TextField newBalanceTF;
	@FXML private Label accountName;
	@FXML private Text successfulMessage;
	
	public SettingsController() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Initialize some basic user information when user open [Settings]
	 */
	public void initUserInfo() {
		accountName.setText(user.getAccount().getAccountName());
		currentBalanceTF.setText("$" + Utility.formatCurrencyDouble(user.getAccount().getBalance()));
		firstNameTF.setText(user.getFirstName());
		lastNameTF.setText(user.getLastName());
		emailTF.setText(user.getEmail());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Set balance validation
//		Pattern validDoubleText = Pattern.compile("((\\d*)|(\\d+\\.\\d*))");
//        TextFormatter<Double> textFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
//            change -> {
//                String newText = change.getControlNewText() ;
//                if (validDoubleText.matcher(newText).matches())
//                    return change;
//                else 
//                	return null;
//        });

		newBalanceTF.setTextFormatter(new CurrencyFormatter());
		// Set successful sms invisible
		successfulMessage.setVisible(false);
		
//        textFormatter.valueProperty().addListener((obs, oldValue, newValue) -> {
//            System.out.println("New double value "+newValue);
//        });
		
//		newBalanceTF.textProperty().addListener(new ChangeListener<String>() {
//			@Override
//			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//				if (ValidationUtil.containOnlyNumber(newValue)) {
//					// Add "," to the balance of at least $1,000
////					if (newValue.length() == 4) {
////						newValue = new StringBuilder(newValue).insert(1, ",").toString();
////					}
////					// Add dollar sign
////					newValue = "$" + newValue;
//					newBalanceTF.setText(newValue);
//					return;
//				} else {
//					newBalanceTF.setText(oldValue);
//					return;
//				}
//			}
//			
//		});
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
			if (ValidationUtil.validateLastName(lastNameTF, firstNameError)) {
				user.setLastName(lastNameTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (emailTF.getText().equals("")) {
			// User doesn't want to update email
		} else {
			if (ValidationUtil.validateEmail(emailTF, emailError)) {
				user.setEmail(emailTF.getText());
				anyChange = true;
			} else {
				anyChange = false;
			}
		}
		
		if (currentPasswordPF.getText().equals("") && newPasswordPF.getText().equals("") && confirmPasswordPF.getText().equals("")) {
			// User doesn't want to update password
		} else {
			// Update password
			if (ValidationUtil.validateCurrentPassword(user.getPassword(), currentPasswordPF, currentPasswordError) && ValidationUtil.validateOriginalPassword(newPasswordPF, passwordError) && ValidationUtil.validateConfirmedPassword(newPasswordPF, confirmPasswordPF, passwordError)) {
				user.setPassword(newPasswordPF.getText());
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
				double newBalance = Utility.parseCurrencyDouble(newBalanceTF.getText());
				user.getAccount().setBalance(newBalance);
				anyChange = true;
			} catch (NumberFormatException ex) {
				System.err.println("Invalid balance: " + newBalanceTF.getText());
				anyChange = false;
			}
		}
		
		// Update to database if there is any change and all others are valid
		if (anyChange) {
			userManager.update(user);
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
		} else {
			System.out.println("Something invalid.. Please check again!");
		}
	}
	
	private void updateSettingFields() {
		// Hide all error messages
		passwordError.setVisible(false);
		emailError.setVisible(false);
		firstNameError.setVisible(false);
		lastNameError.setVisible(false);
		currentPasswordError.setVisible(false);
		// Clear passwords
		currentPasswordPF.clear();
		newPasswordPF.clear();
		confirmPasswordPF.clear();
		currentBalanceTF.setText("$" + Utility.formatCurrencyDouble(user.getAccount().getBalance()));
	}

	@Override
	protected void makeNewStage(Screen target, String stageTitle, String url) {
		// TODO Auto-generated method stub
		
	}
}
