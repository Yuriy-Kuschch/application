package com.yuriykusch.TicTacToe.controllers;

import com.yuriykusch.TicTacToe.database.DataManager;
import com.yuriykusch.TicTacToe.database.entities.Player;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class RecordsPopupController implements Initializable {
    @FXML
    private GridPane recordsContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Сразу при загрузке страници отображаем в сетке заголовки
        recordsContainer.addRow(
                0,
                new Label("Пользователь"),
                new Label("Рекорд")
        );

        //Стартуем поток получения данных из БД
        new showRecordsThread(recordsContainer).start();
    }

    private static class showRecordsThread extends Thread{
        private GridPane recordsContainer;

        public showRecordsThread(GridPane recordsContainer){
            this.recordsContainer = recordsContainer;
        }

        @Override
        public void run() {
            super.run();

            List<Player> players = null;
            try {
                //Берём всех пользователей, предварительно сортируя их по рейтингу
                players = DataManager.getInstance().getPlayersDao().queryBuilder()
                        .orderBy("record", false)
                        .query();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            if(players == null) return;

            int row = 1;
            for (Player player : players) {
                int finalRow = row;

                //Пробегаемся по всех пользователях
                // и добавляем их данные в сетку в потокке UI
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        recordsContainer.addRow(
                                finalRow,
                                new Label(player.getLogin()),
                                new Label(String.valueOf(player.getRecord()))
                        );
                    }
                });
                row++;
            }

        }
    }
}
