package com.laioffer.jupiter.db;

import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.entity.User;

import java.sql.*;
import java.util.*;

public class MySQLConnection implements AutoCloseable{//使用try with：注意，资源的 close 方法与他们创建相反的顺序调用。

    private final Connection conn;

    // Create a connection to the MySQL database.
    // MySQLConnection constructor
    public MySQLConnection() throws MySQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to Database");
        }
    }

    @Override
    public void close() {//这里只有个conn  override AutoCloseable
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // Insert a favorite record to the database
    public void setFavoriteItem(String userId, Item item) throws MySQLException {
        if (conn == null) {
            String errorMessage = "DB connection failed";
            System.err.println(errorMessage);
            throw new MySQLException(errorMessage);
        }
        // Need to make sure item is added to the database first
        // because the foreign key restriction on
        // item_id(favorite_records) -> id(items)　
        saveItem(item);


        // Using ? and preparedStatement to prevent SQL injection
        String sql = "INSERT IGNORE INTO favorite_records (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);// 这个api里面可以检测 string 的合法性
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to save favorite item to Database");
        }

    }


    // Remove a favorite record from the database
    public void unsetFavoriteItem(String userId, String itemId) throws MySQLException {
        if (conn == null) {
            String errorMessage = "DB connection failed";
            System.err.println(errorMessage);
            throw new MySQLException(errorMessage);
        }
        String sql = "DELETE FROM favorite_records WHERE user_id = ? AND item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete favorite item to Database");
        }
    }

    // Insert an item to the database.   private?
    public void saveItem(Item item) throws MySQLException {
        if (conn == null) {
            String errorMessage = "DB connection failed";
            System.err.println(errorMessage);
            throw new MySQLException(errorMessage);
        }
        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getUrl());
            statement.setString(4, item.getThumbnailUrl());
            statement.setString(5, item.getBroadcasterName());
            statement.setString(6, item.getGameId());
            statement.setString(7, item.getType().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to add item to Database");
        }
    }

    // Get favorite item ids for the given user   private??
    public Set<String> getFavoriteItemIds(String userId) throws MySQLException {
        if (conn == null) {
            String errorMessage = "DB connection failed";
            System.err.println(errorMessage);
            throw new MySQLException(errorMessage);
        }

        Set<String> favoriteItems = new HashSet<>();

        String sql = "SELECT item_id FROM favorite_records WHERE user_id = ?";

        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();//注意 这里要返回结果，用executeQuery，不同前面的delete, update, insert
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItems.add(itemId);//不可能 同一个 user 收藏 多次 同一个item
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite item ids from Database");
        }

        return favoriteItems;


    }

    // Get favorite items for the given user. The returned map includes three entries like
    // {"Video": [item1, item2, item3], "Stream": [item4, item5, item6], "Clip": [item7, item8, ...]}
    public Map<String, List<Item>> getFavoriteItems(String userId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<Item>> itemMap = new HashMap<>();

        for (ItemType type : ItemType.values()) {//values()？？？
            itemMap.put(type.toString(), new ArrayList<>());
        }

        //先获取favoriteItem Ids
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);
        String sql = "SELECT * FROM items WHERE id = ?";


        try {

            PreparedStatement statement = conn.prepareStatement(sql);

            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {

                    ItemType itemType = ItemType.valueOf(rs.getString("type"));//string to ItemType

                    Item item = new Item.Builder().id(rs.getString("id"))
                            .title(rs.getString("title"))
                            .url(rs.getString("url"))
                            .thumbnailUrl(rs.getString("thumbnail_url"))
                            .broadcasterName(rs.getString("broadcaster_name"))
                            .gameId(rs.getString("game_id"))
                            .type(itemType).build();


                    itemMap.get(rs.getString("type")).add(item);

                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite items from Database");
        }

        return itemMap;

    }




    // Get favorite game ids for the given user. The returned map includes
    // three entries like {"Video": ["1234", "5678", ...],
    // "Stream": ["abcd", "efgh", ...], "Clip": ["4321", "5678", ...]}

    //favoriteItemIds
    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        String sql = "SELECT game_id, type FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    itemMap.get(rs.getString("type")).add(rs.getString("game_id"));//有可能有多个gameId
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite game ids from Database");
        }
        return itemMap;
    }



    // Verify if the given user Id and password are correct. Returns the user name when it passes
    public String verifyLogin(String userId, String password) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE id = ? AND password = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to verify user id and password from Database");
        }
        return name;
    }



    // Add a new user to the database
    public boolean addUser(User user) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }

        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        //这里也可以删掉IGNORE，这样重复注册就会报 500 错误
        //加上ignore，重复注册下面return 会返回 false， 在registerServlet那里会报409 conflict error

        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, user.getUserId());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());

            return statement.executeUpdate() == 1;//返回1表示 改变了一行， 0 就没改
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to add to Database");

        }
    }













        }






