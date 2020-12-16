package com.yuriykusch.TicTacToe.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.yuriykusch.TicTacToe.database.entities.Player;

import java.sql.SQLException;

//Класс для работы с базой данных
public class DataManager {
    private static volatile DataManager instance;

    private ConnectionSource connectionSource;

    private Dao<Player, Integer> playersDao;

    //Паттерн "Синглтон"
    private DataManager(){
        //Инициализируем подключение к БД
        establishTheConnection();
        //Объявляем объекты доступа к данным
        initDaos();
        //Создаем таблицы (если еще не существуют)
        createTables();
    }

    public static DataManager getInstance(){
        DataManager currentInstance = instance;
        if(currentInstance != null){
            return currentInstance;
        }
        synchronized (DataManager.class){
            if(instance == null){
                instance = new DataManager();
            }
            return instance;
        }
    }

    private void establishTheConnection(){
        try{
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:db.sqlite");
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
    }

    private void initDaos(){
        if(connectionSource == null)
            establishTheConnection();
        try{
            playersDao = DaoManager.createDao(connectionSource, Player.class);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void createTables(){
        try{
            TableUtils.createTableIfNotExists(connectionSource, Player.class);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Dao<Player, Integer> getPlayersDao() {
        return playersDao;
    }
}
