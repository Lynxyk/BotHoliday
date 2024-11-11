package db;
import db.entity.Gift;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Postgre {
    private  Connection connection;
    private  Statement statement;
    private  PreparedStatement preparedStatement;
    private  ResultSet resultSet;

    public Postgre(String login, String pass, String network) throws SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://" + network +
                ":5432/holidayDB", login, pass);
    }
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //проверка на наличие пользователя
    public boolean availabilityUser(String username) throws SQLException {
        boolean result = false;
        String query = "SELECT * FROM users WHERE telegram_tag = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()){
                    result = true;
                }
            }
        }
        return result;
    }

    //Добавление пользователя
    public void addUser(String telegramTag, String birthDate) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = LocalDate.parse(birthDate, formatter);
        String query = "INSERT INTO users (telegram_tag, birth_date) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegramTag);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.executeUpdate();
        }
    }

    //показ пользователя
    public String showUser(String telegram_tag) throws SQLException {
        String result = "";
        String query = "SELECT * FROM users WHERE telegram_tag = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegram_tag);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()){
                    result = resultSet.getString("birth_date");
                }
            }
        }
        return result;
    }

    //изменение др пользователя
    public void updateUserBirth(String birthdate, String telegram_tag) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = LocalDate.parse(birthdate, formatter);
        String query = "UPDATE users SET birth_date = ? WHERE telegram_tag = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDate(1, Date.valueOf(date));
            preparedStatement.setString(2, telegram_tag);
            preparedStatement.executeUpdate();
        }
    }

    //Показ друзей
    public List<String> showFriends(String telegram_tag_user) throws SQLException {
        List<String> result = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE telegram_tag_user = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegram_tag_user);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()){
                    result.add(resultSet.getString("telegram_tag_friends"));
                }
            }
        }
        return result;
    }

    //Добавить друга
    public void addFriends(String telegram_tag_user, String telegram_tag_friends) throws SQLException {
        String query = "INSERT INTO friendships (telegram_tag_user, telegram_tag_friends) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegram_tag_user);
            preparedStatement.setString(2, telegram_tag_friends);
            preparedStatement.executeUpdate();
        }
    }

    //Удалить друга
    public void deleteFriends(String telegram_tag_user, String telegram_tag_friends) throws SQLException {
        String query = "DELETE FROM friendships WHERE telegram_tag_user = ? AND telegram_tag_friends = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegram_tag_user);
            preparedStatement.setString(2, telegram_tag_friends);
            preparedStatement.executeUpdate();
        }
    }

    //Показ подарков - доделать
    public List<Gift> showGifts(String username) throws SQLException {
        List<Gift> result = new ArrayList<>();
        String query = "SELECT * FROM gifts WHERE telegram_tag = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()){
                    result.add(new Gift(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)));
                }
            }
        }
        return result;
    }

    //добавить подарок
    public void addGifts(String telegram_tag, String name, String link) throws SQLException {
        String query = "INSERT INTO gifts (telegram_tag, name, link) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegram_tag);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, link);
            preparedStatement.executeUpdate();
        }
    }
    //удалить подарок
    public void deleteGifts(String telegram_tag, int gift_id) throws SQLException {
        String query = "DELETE FROM gifts WHERE telegram_tag = ? AND gift_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, telegram_tag);
            preparedStatement.setInt(2, gift_id);
            preparedStatement.executeUpdate();
        }
    }
}
