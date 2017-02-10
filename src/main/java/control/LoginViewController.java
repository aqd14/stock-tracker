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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.utility.WindowSize;
 
public class LoginViewController extends ParentController{
    @FXML private Text actiontarget;
    @FXML private TextField usernameTF;
    @FXML private PasswordField passwordPF;
    @FXML private Label registerLB;
    @FXML private Label resetPasswordLB;
    
    @FXML private GridPane loginGP;
    
    @FXML protected void handleSubmitButtonAction(ActionEvent e) {
    	boolean matchedUsernamePassword = verifyUserNameOrEmail();
        if (matchedUsernamePassword) {
        	System.out.println("Login successfully!");
        	actiontarget.setVisible(false);
        } else {
        	actiontarget.setText("Incorrect username or password. Try again.");
        	actiontarget.setVisible(true);
        }
    }
    
    // User might enter email in 
    private boolean verifyUserNameOrEmail() {
    	return userManager.findByUsernameOrEmail(usernameTF.getText(), passwordPF.getText()) == null ? false : true;
    }
    
    @FXML protected void createNewAccount(MouseEvent e) {
    	System.out.println("Create new account.");
    	// Switch to Registration View
    	switchLoginToRegister();
    }
    
    @FXML protected void resetPassword(MouseEvent e) {
    	System.out.println("Reset password.");
    	// Switch to Reset Password View
    }
    
    @FXML protected void onMouseEntered(MouseEvent e) {
//    	registerLB.setScaleX(1.5);
//    	registerLB.setScaleY(1.5);
    }
    
    @FXML protected void onMouseExited(MouseEvent e) {
//    	registerLB.setScaleX(1);
//    	registerLB.setScaleY(1);
    }

	public void setStageAndSetupListeners(Stage stage) {
		// TODO Auto-generated method stub
		
	}
	
	private void switchLoginToRegister() {
    	Stage curStage = (Stage)loginGP.getScene().getWindow();
        Parent root;
		try {
			root = new FXMLLoader(getClass().getResource("../../../main/java/view/UserRegistration.fxml")).load();
	        curStage.setTitle("User Registration");
	        curStage.setScene(new Scene(root, WindowSize.REGISTRATION_WIDTH, WindowSize.REGISTRATION_HEIGHT));
	        curStage.setResizable(false);
	        curStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
