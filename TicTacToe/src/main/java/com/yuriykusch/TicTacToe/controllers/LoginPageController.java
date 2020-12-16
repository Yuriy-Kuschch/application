package com.yuriykusch.TicTacToe.controllers;

import com.yuriykusch.TicTacToe.database.DataManager;
import com.yuriykusch.TicTacToe.database.entities.Player;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginPageController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label repeatPasswordLabel;
    @FXML
    private PasswordField repeatPasswordField;
    @FXML
    private Button notRegisteredButton;
    @FXML
    private Button loginRegisterButton;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Text messageText;

    //Определяет текущий режим - "логинится" либо "регистрируется"
    private boolean logging = true;

    //По клику на кнопку "Еще не зарегистрирован"
    public void onNotRegisteredClick(ActionEvent event){
        //Если логинится
        if(logging){
            //Меняем все поля в соответствии с формой входа
            notRegisteredButton.setText("Ещё не зарегистрирован");
            loginRegisterButton.setText("Войти");
            repeatPasswordLabel.setVisible(false);
            repeatPasswordField.setVisible(false);
            repeatPasswordField.clear();
            logging = !logging;
        }else{
            //Меняем все поля в соответствии с формой регистрации
            notRegisteredButton.setText("Уже зарегистрирован");
            loginRegisterButton.setText("Зарегистрироваться");
            repeatPasswordLabel.setVisible(true);
            repeatPasswordField.setVisible(true);
            logging = !logging;
        }
    }

    //По клику на кнопку "Войти/Зарегистрироватся"
    public void onLoginRegisterClick(ActionEvent event){
        //Проверяем не пусты ли поля
        if(loginField.getText().length() == 0 || passwordField.getText().length() == 0){
            //Если да - выводим сообщение
            messageText.setText("Вы не заполнили поля!");
            return;
        }
        if(!logging && passwordField.getText().equals(repeatPasswordField.getText())){
            //Если режим регистрации и поля с паролями не совпадают
            messageText.setText("Пароли в полях не совпадают!");
            return;
        }

        //Стартуем поток для проверки/добавления учетных данных пользователя
        //Передаем ему в конструктор все необходимые поля для работы
        new checkCredentialsThread(
                event,
                logging,
                loginField.getText(),
                passwordField.getText(),
                progressIndicator,
                messageText
        ).start();
    }

    //Определение потока проверки/добавления учетных данных
    private static class checkCredentialsThread extends Thread {
        private Stage callerStage;
        private boolean logging;
        private String login, password;

        private Text messageText;
        private ProgressIndicator progressIndicator;

        private Player player;

        public checkCredentialsThread(ActionEvent event,
                                      boolean logging,
                                      String login,
                                      String password,
                                      ProgressIndicator progressIndicator,
                                      Text messageText
        ){
            //Нужно для передачи данных про текущего пользователя между разными окнами
            callerStage = (Stage)((javafx.scene.Node) event.getSource()).getScene().getWindow();

            this.logging = logging;

            this.login = login;
            this.password = password;

            this.messageText = messageText;
            this.progressIndicator = progressIndicator;
        }

        @Override
        public void run() {
            super.run();
            //На старте делаем видимым индикатор прогресса
            progressIndicator.setVisible(true);

            try {
                //Пытаемся достать из БД пользователя с таким логином
                player = DataManager.getInstance().getPlayersDao().queryBuilder()
                        .where()
                        .eq("login", login)
                        .queryForFirst();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            //Если в БД такого пользователя нет
            if(player == null) {
                //И если он логинится
                if(logging) {
                    //Значит нужно вывести сообщение об ошибке
                    messageText.setText("Такого пользователя не существует!");
                    progressIndicator.setVisible(false);
                    return;
                } else {
                    //Если же он регистрируется, то нужно создать новый объект и поместить его в БД
                    player = new Player(login, password);
                    try {
                        DataManager.getInstance().getPlayersDao().create(player);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    //Все сложные операции, вроде перехода между экранами
                    // должны проводится в потоке отрисовки UI,
                    // что мы, собственно, тут и делаем
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressIndicator.setVisible(false);
                            messageText.setText("Здравствуйте, " + player.getLogin() + '!');
                            loadNextScene();
                        }
                    });
                    return;
                }
            }

            //Если пользователь с таким логином был найден
            // то нужно еще проверить его пароль
            if(player.getPassword().equals(password)){
                //Если все хорошо - переходим на следующий экран
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        callerStage.setUserData(player);
                        progressIndicator.setVisible(false);
                        messageText.setText("Здравствуйте, " + player.getLogin() + '!');
                        loadNextScene();
                    }
                });
            }
        }

        private void loadNextScene(){
            try {
                Parent parent = FXMLLoader.load(getClass().getResource("/gamePage.fxml"));
                Scene gameScene = new Scene(parent);
                callerStage.setScene(gameScene);
                callerStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}