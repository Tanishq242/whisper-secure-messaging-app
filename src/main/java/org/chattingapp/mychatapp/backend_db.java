package org.chattingapp.mychatapp;

import jbcrypt.*;

import java.sql.*;

public class backend_db {
    private static final String url = "jdbc:mysql://localhost:3306/chatapp";
    private static final String username = "root";
    private static final String password = "";
    private static Connection connection = null;
    String hash;

    backend_db() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to Database Successfully");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();

        }
        startConnectionMonitor();
    }

    public void connect() {
        while (true) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Connected to Database Successfully");
                break;
            } catch (Exception e) {
                System.out.println("Connection failed. Retrying...");
                try { Thread.sleep(3000); } catch (InterruptedException ex) {}
            }
        }
    }

    public boolean isConnectionAlive() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    public void startConnectionMonitor() {
        new Thread(() -> {
            while (true) {
                try {
                    if (!isConnectionAlive()) {
                        System.out.println("❌ Database connection lost. Reconnecting...");
                        connect();
                    } else {
                        System.out.println("✔ Connection OK");
                    }

                    Thread.sleep(5000); // check every 5 seconds

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    public boolean dataInsert(String name, long mobile, String verifiedValue, String pass, String public_key) {
        String hashValue = hashPassword(pass);
        try {
            String query = "INSERT INTO registers(uname, mobile, verified, password, image_path, public_key) VALUES(?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name.toLowerCase());
            preparedStatement.setLong(2, mobile);
            preparedStatement.setString(3, verifiedValue);
            preparedStatement.setString(4, hashValue);
            preparedStatement.setString(5, "none");
            preparedStatement.setString(6, public_key);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int dataExist(String mobile) {
        try {
            String query = "SELECT 1 FROM registers WHERE mobile = ? LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, mobile);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return 1;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public void dataRead(long mobile) {
        try {
            String query = "SELECT * FROM registers WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, mobile);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                hash = resultSet.getString("password");
            } else {
                System.out.println("No data is founded");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet dataRead(String name) {
        try {
            String query = "SELECT * FROM registers WHERE LOWER(uname) LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name.toLowerCase() + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet dataReadByMobile(long mobile) {
        try {
            String query = "SELECT * FROM registers WHERE mobile LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, mobile + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPublicKey(long mobile) {
        try {
            String query = "SELECT public_key FROM registers WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, mobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("public_key");
            } else {
                System.out.println("No data is founded");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int updateName(String newName, String id) {
        try {
            String query = "UPDATE registers SET uname = ? WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, id);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated successfully");
                return 1;
            } else {
                System.out.println("Not Updated");
                return 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 1;
    }

    public int updatePassword(String hash, String id) {
        try {
            String query = "UPDATE registers SET password = ? WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, hash);
            preparedStatement.setString(2, id);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated successfully");
                return 1;
            } else {
                System.out.println("Not Updated");
                return 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public int updatePublicKey(String key, String id) {
        try {
            String query = "UPDATE registers SET public_key = ? WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, id);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated successfully");
                return 1;
            } else {
                System.out.println("Not Updated");
                return 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public boolean updateImgPath(String path, String id) {
        try {
            String query = "UPDATE registers SET image_path = ? WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, path);
            preparedStatement.setString(2, id);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated successfully");
                return true;
            } else {
                System.out.println("Not Updated");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public int removeData(Long id) {
        try {
            String query = "DELETE FROM registers WHERE mobile = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Deleted successfully");
                return 1;
            } else {
                System.out.println("Not Deleted");
                return 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 1;
    }
}