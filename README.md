# stock-tracker
Simple java application that assists users keeping track of stock trends to make decision.

### Setup

1. Clone project by using ```git clone```
```
    git clone https://github.com/aqd14/stock-tracker.git
```
2. Build project with ```maven```
```
mvn jfx:jar
```
3. Run ```jar``` file at $SOURCE_DIR$/target/jfx/app
```
java -jar path_to_jar_file
```

## Screenshots

### Login
![Alt text](screenshots/login.jpg?raw=true "Login Screen")

When user starts the app, Login screen will be opened. User can login either by registered email or username.

Beside the main function is logging user in the app, there are several options. User can:
+ Remember username/password so there is no need to enter them next time login
+ Register Account
+ Reset Password

If the username/password is incorrect, an error message will be displayed to user.

### Reset username/password
![Alt text](screenshots/reset-password.jpg?raw=true "Reset Password")

User can reset password by providing some needed personal information that user has registered earlier.

### Registration
User need to enter some basic information in order to register for the app.
<p align="center">
<img src="screenshots/user-registration.jpg" width=350/>
<img src="screenshots/register-error-1.jpg" width=350/>
</p>

The app restricts some rules on registered information such as:
+ Length of username, password should be sufficiently long
+ Should give a correct email (e.g., someuser@somedomain.com)
+ First name/ last name should not contain special characters (e.g., %@&^%*#^)
+ ...

### Main screen

After succesfully logged in, user will be redirect to the main screen. It includes default 30 stocks and their current price changes compared to the price of last closing day of stock market. User can also add/remove prefered stocks and the configuration will be saved for next log in.

![Alt text](screenshots/main-screen.jpg?raw=true "Main Screen")

