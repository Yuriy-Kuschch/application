package com.yuriykusch.TicTacToe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        //Инициализируем музыкальный плеер
        Media media = new Media(getClass().getResource("/music/soundtrack.mp3").toURI().toString());
        MediaPlayer player = new MediaPlayer(media);

        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.play();

        //Инициализируем главное окно
        Parent root = FXMLLoader.load(getClass().getResource("/loginPage.fxml"));
        stage.setTitle("Крестики-нолики");
        stage.setScene(new Scene(root, 720, 680));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
