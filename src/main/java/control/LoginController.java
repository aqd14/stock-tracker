/*
 * Copyright (c) 2011, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package main.java.control;
 
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXCheckBox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.common.CommonDefine;
import main.java.dao.UserManager;
import main.java.utility.Screen;
import main.java.utility.Utils;
 
public class LoginController extends BaseController implements Initializable, IController {
    @FXML private Text loginError;
	@FXML private TextField usernameTF;
    @FXML private PasswordField passwordPF;
    
    @FXML private JFXCheckBox rememberMeCB;
    @FXML private AnchorPane loginAP;
    
    @FXML protected void login(ActionEvent e) throws IOException {
    	// Check if given username and password matched with records in database
    	if (user == null)
    		user = userManager.findByUsernameOrEmail(usernameTF.getText(), passwordPF.getText());
        if (user != null) {
        	System.out.println("Login successfully!");
        	// Initialize array to pass optional argument
        	switchScreen(Screen.HOME, "Stock Tracker", "../../../main/java/view/Home.fxml");
        	loginError.setVisible(false);
        	// Save current username to file if user selects remember me
        	// Otherwise, empty file's content
        	if (rememberMeCB.isSelected()) {
            	Utils.writeFile(usernameTF.getText());
        	} else {
        		Utils.writeFile("");
        	}
        } else {
        	loginError.setText("Incorrect username or password. Try again.");
        	loginError.setVisible(true);
        }
    }
    
    @FXML private void createNewAccount(MouseEvent e) {
    	System.out.println("Create new account.");
    	// Switch to Registration View
    	switchScreen(Screen.REGISTER, CommonDefine.REGISTRATION_TITLE, "../../../main/java/view/UserRegistration.fxml");
    }
    
    @FXML private void resetPassword(MouseEvent e) {
    	System.out.println("Reset password.");
    	// Switch to Reset Password View
    	switchScreen(Screen.RESET_PASSWORD, CommonDefine.RESET_PASSWORD_TITLE, "../../../main/java/view/ResetPassword.fxml");
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Initialize username and password based on last save
		setupRememberMe();
	}
	
	/**
	 * Check if the last time user checked [Remember Me]. 
	 * If he did, load username and password from config file and database
	 */
	public void setupRememberMe() {
		String identity = Utils.readFile(); // Could be either username or email
		if (identity != null && !identity.equals("")) {
			userManager = new UserManager<>();
			user = userManager.findByUsernameOrEmail(identity);
			if (user != null) {
				usernameTF.setText(identity);
				passwordPF.setText(user.getPassword());
			}
			rememberMeCB.setSelected(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void switchScreen(Screen target, String title, String url) {
        Parent root = null;
		try {
			switch (target) {
				case HOME:
					FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
					root = loader.load(); // Loading before get controller
					HomeController homeController = loader.<HomeController>getController();
					homeController.setUser(user);
					break;
				case REGISTER:
					FXMLLoader ld = new FXMLLoader(getClass().getResource(url));
					root = ld.load();
					RegistrationController controller = ld.<RegistrationController>getController();
					controller.setPhoneNumberFormatter();
					break;
				case RESET_PASSWORD:
					root = new FXMLLoader(getClass().getResource(url)).load();
					break;
				default:
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	  	Stage curStage = (Stage)loginAP.getScene().getWindow();
        curStage.setTitle(title);
        curStage.setScene(new Scene(root));
        curStage.setResizable(false);
        curStage.show();
    }

	@Override
	public void makeNewStage(Screen target, String title, String url) {
		// TODO Auto-generated method stub
		
	}
}
