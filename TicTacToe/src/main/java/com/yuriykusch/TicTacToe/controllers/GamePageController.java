package com.yuriykusch.TicTacToe.controllers;

import com.yuriykusch.TicTacToe.database.DataManager;
import com.yuriykusch.TicTacToe.database.entities.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class GamePageController implements Initializable {
    @FXML
    private GridPane gameFieldContainer;
    @FXML
    private HBox chooserContainer;
    @FXML
    private Label yourTurnLabel;

    @FXML
    private Stage currentStage;

    //Все кнопки игрового поля
    private List<Button> fieldOfButtons;
    //Все метки игрового поля
    private int[] fieldOfMarkers;

    //Все выиграшные комбинации
    private int[][] winCombinations;

    //Игрок "Крестик"? Нужно для определения первого хода
    private boolean playerCross = true;
    private boolean gameStarted = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetGame();
    }

    //По клику на кнопку выбора "Крестик"
    public void onChooserCrossClick(ActionEvent event){
        gameStarted = true;
        playerCross = true;
        //Прячем выбор игровой фигуры
        chooserContainer.setVisible(false);
        //Показываем надпись "Ваш ход"
        yourTurnLabel.setVisible(true);

        currentStage = (Stage)((Node) event.getSource()).getScene().getWindow();
    }

    //По клику на кнопку выбора "Нолик"
    public void onChooserCircleClick(ActionEvent event){
        gameStarted = true;
        playerCross = false;
        chooserContainer.setVisible(false);
        yourTurnLabel.setVisible(false);

        //По скольку компъютер - "Крестик" - он ходит первый
        performComputerTurn();

        currentStage = (Stage)((Node) event.getSource()).getScene().getWindow();
    }

    private void playerTurn(int index){
        if(fieldOfButtons == null || !gameStarted || !getFreeIndexes().contains(index)) return;

        StringBuilder builder = new StringBuilder("-fx-background-image: ");
        if(playerCross){
            builder.append("url('icons/cross.png');");
        }else {
            builder.append("url('icons/circle.png');");
        }
        builder.append("-fx-background-size: 150px");
        fieldOfButtons.get(index).setStyle(builder.toString());
        fieldOfMarkers[index] = 1;

        yourTurnLabel.setVisible(false);
        checkIfSomeoneWin();

        performComputerTurn();
    }

    private void performComputerTurn(){
        if(!gameStarted) return;

        //Получаем список свободных клеток
        List<Integer> free = getFreeIndexes();
        if(free.size() == 0) {
            //Если их нет - ничья
            showDrawPopup();
            resetGame();
            return;
        }

        //Инициализируем генератор случайных чисел
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        int freeSize = free.size();
        int randomIndex = 0;

        if(freeSize > 1) {
            //Проверка нужна, иначе вылетит эксепшн
            randomIndex = random.nextInt(freeSize - 1);
        }

        //Получаем индекс элемента, в который собираемся поставить свою "метку" (число 2)
        int markerIndex = free.get(randomIndex);
        //Получаем также кнопку, соответствующую случайному индексу метки
        Button button = fieldOfButtons.get(markerIndex);

        //Собираем стиль для кнопки, которую выбрал компъютер
        StringBuilder builder = new StringBuilder("-fx-background-image: ");
        //Если игрок - "Крестик"
        if(playerCross){
            //Значит в месте хода компъютера - "Нолик"
            builder.append("url('icons/circle.png');");
        }else {
            builder.append("url('icons/cross.png');");
        }
        //Иконки большие, нужно устанавливать фиксированный размер
        builder.append("-fx-background-size: 150px");
        //Присваеваем построенный стиль кнопке
        button.setStyle(builder.toString());

        //Устанавливаем свою метке на индеке, который соответствует выбранному компъютером
        fieldOfMarkers[markerIndex] = 2;

        //Выключаем кнопку, которую выбрал компъютер во избежание казусов
        button.setDisable(true);
        //Говорим пользователю чтобы ходил
        yourTurnLabel.setVisible(true);
        //Проверяем не победил ли компъютер после данного хода
        checkIfSomeoneWin();
    }

    private void checkIfSomeoneWin(){
        //Перебираем все выиграшные комбинации
        for (int[] winCombination : winCombinations) {
            boolean allMarked = false;

            if(fieldOfMarkers[winCombination[0]] != 0 && fieldOfMarkers[winCombination[0]] == fieldOfMarkers[winCombination[1]] &&
                    fieldOfMarkers[winCombination[1]] == fieldOfMarkers[winCombination[2]]){
                allMarked = true;
                //Проверяем каждую комбинацю, сходятся ли все три метки,
                // на нашем поле данной комбинации
            }

            if(allMarked){
                gameStarted = false;
                //Снова показываем контейнер с кнопками выбора игровой фигуры
                chooserContainer.setVisible(true);

                //Показываем оповещение о результате игры
                // проверяем победил ли игрок, сравнивая одно из полей, которые совпали,
                // с его меткой (число 1)
                showWinPopup(fieldOfMarkers[winCombination[0]] == 1);
                gameStarted = false;
                yourTurnLabel.setVisible(false);
                resetGame();
                break;
            }
        }
    }

    //Устанавливаем все начальные параметры
    private void resetGame(){
        fieldOfButtons = new ArrayList<>();
        fieldOfMarkers = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

        winCombinations = new int[][]{
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, 8},
                {0, 3, 6},
                {1, 4, 7},
                {2, 5, 8},
                {0, 4, 8},
                {2, 4, 6}
        };

        for(int i = 0; i < 9; i++){
            final int index = i;

            Button current = new Button();
            current.setPrefHeight(1.7976931348623157E308);
            current.setPrefWidth(1.7976931348623157E308);
            current.setStyle("-fx-background-color: white");
            //Каждая кнопка передает свой уникальный индекс при клике
            current.setOnAction(event -> {
                playerTurn(index);
            });

            fieldOfButtons.add(current);
        }
        gameFieldContainer.getChildren().clear();
        gameFieldContainer.setStyle("-fx-grid-lines-visible: true;");
        int row = 0;
        for (int i = 0; i < fieldOfButtons.size(); i += 3, row++) {
            gameFieldContainer.addRow(row, fieldOfButtons.get(i), fieldOfButtons.get(i + 1), fieldOfButtons.get(i + 2));
        }
    }

    private void showWinPopup(boolean playerWin){
        //Собираем всплывашку
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Уведомление");
        //Текст и очки отличаются в зависимости от того, выиграл ли игрок
        if (playerWin) {
            alert.setHeaderText("Вы победили! Поздравляем!");
            alert.setContentText("Вы получаете 10 очков рейтинга!");
            //Стартуем наш поток для изменения очков игрока
            new changePointsThread((Player) currentStage.getUserData(), 10).start();
        }else {
            alert.setHeaderText("Мы сожалеем, но вы проиграли!");
            alert.setContentText("Вы теряете 5 очков рейтинга!");
            new changePointsThread((Player) currentStage.getUserData(), -5).start();
        }
        alert.showAndWait();
    }

    private void showDrawPopup() {
        new changePointsThread((Player) currentStage.getUserData(), 3).start();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Уведомление");
        alert.setHeaderText("Ничья!");
        alert.setContentText("Вы получаете 3 очков рейтинга!");
        alert.showAndWait();
    }

    //Создаем новое окно для отображения турнирной таблицы
    public void onShowRecordsClick(ActionEvent event){
        Parent ratingPopup = null;
        try{
            ratingPopup = FXMLLoader.load(getClass().getResource("/recordsPopup.fxml"));
        } catch (IOException e){
            e.printStackTrace();
        }

        Scene ratingPopupScene = new Scene(ratingPopup, 600, 600);
        Stage popupWindow = new Stage();
        popupWindow.initModality(Modality.WINDOW_MODAL);
        popupWindow.initOwner(((Node) event.getSource()).getScene().getWindow());
        popupWindow.setScene(ratingPopupScene);
        popupWindow.setTitle("Турнирная таблица");
        popupWindow.show();
    }

    //Получаем пустые (число 0) индексы из игрового поля
    private List<Integer> getFreeIndexes(){
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < fieldOfMarkers.length; i++) {
            int marker = fieldOfMarkers[i];
            if(marker == 0)
                free.add(i);
        }
        return free;
    }

    //Поток для изменения количества очков игрока
    private static class changePointsThread extends Thread{
        private Player player;
        private int value;
        public changePointsThread(Player player, int value){
            this.player = player;
            this.value = value;
        }
        @Override
        public void run() {
            super.run();

            //Получаем текущее количество очков
            int currentPoints = player.getRecord();
            //Если после добавления (число может быть и негативным)
            // очки игрока не станут негативными
            if(currentPoints + value > 0){
                //То добавляем их
                player.setRecord(currentPoints + value);
            } else {
                //Если же нет, то просто делаем нулевыми
                player.setRecord(0);
            }

            try {
                //Сохраняем изменения объекта в БД
                DataManager.getInstance().getPlayersDao().update(player);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
