package main.java.control;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import javafx.util.converter.DoubleStringConverter;
import main.java.utility.ValidationUtil;

public class SettingsController extends ParentController implements Initializable {
	// User information fields
	@FXML private JFXTextField firstNameTF;
	@FXML private JFXTextField lastNameTF;
	@FXML private JFXTextField emailTF;
	
	// New password
	@FXML private PasswordField newPasswordPF;
	@FXML private PasswordField confirmPasswordPF;
	
	// Error text
	@FXML private Text firstNameError;
	@FXML private Text lastNameError;
	@FXML private Text emailError;
	@FXML private Text passwordError;
	@FXML private Text confirmPasswordError;
	
	@FXML private TextField newBalanceTF;
	@FXML private Label accountName;
	
	public SettingsController() {
		// TODO Auto-generated constructor stub
	}
	
	public void setAccountName(String accountName) {
		this.accountName.setText(accountName);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Set balance validation
		Pattern validDoubleText = Pattern.compile("((\\d*)|(\\d+\\.\\d*))");
//        TextFormatter<Double> textFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0, 
//            change -> {
//                String newText = change.getControlNewText() ;
//                if (validDoubleText.matcher(newText).matches())
//                    return change;
//                else 
//                	return null;
//        });

		newBalanceTF.setTextFormatter(new CurrencyFormatter());

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
			}
		}
		
		if (lastNameTF.getText().equals("")) {
			// User doesn't want to update last name
		} else {
			if (ValidationUtil.validateLastName(lastNameTF, firstNameError)) {
				user.setLastName(lastNameTF.getText());
				anyChange = true;
			}
		}
		
		if (emailTF.getText().equals("")) {
			// User doesn't want to update email
		} else {
			if (ValidationUtil.validateEmail(emailTF, emailError)) {
				user.setEmail(emailTF.getText());
				anyChange = true;
			}
		}
		
		if (newPasswordPF.getText().equals("") && confirmPasswordPF.getText().equals("")) {
			// User doesn't want to update password
		} else {
			// Update password
			if (ValidationUtil.validateOriginalPassword(newPasswordPF, passwordError) && ValidationUtil.validateConfirmedPassword(newPasswordPF, confirmPasswordPF, confirmPasswordError)) {
				user.setPassword(newPasswordPF.getText());
				anyChange = true;
			}
		}
		
		// Update balance
		if (newBalanceTF.getText().equals("")) {
			// User didn't update balance.
			// Do nothing
		} else {
			try {
				user.getAccount().setBalance(Double.parseDouble(newBalanceTF.getText()));
				anyChange = true;
			} catch (NumberFormatException ex) {
				System.err.println("Invalid balance: " + newBalanceTF.getText());
			}
		}
		
		// Update to database if there is any change
		if (anyChange) {
			userManager.update(user);
		}
	}
	
}
