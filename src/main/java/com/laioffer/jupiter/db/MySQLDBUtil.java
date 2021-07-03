package com.laioffer.jupiter.db;

import com.laioffer.jupiter.entity.Item;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

public class MySQLDBUtil {
    //DB的地址
    private static final String INSTANCE = "laiproject-instance.cnvbcbsxfken.us-east-2.rds.amazonaws.com";
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "jupiter";


    public static String getMySQLAddress() throws IOException {

        //java里面有一套api 专门读 properties 文件
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = Item.class.getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);

        String username = prop.getProperty("user");
        String password = prop.getProperty("password");

        //如果密码有特殊符号，需要转义
        try {

        password= URLEncoder.encode(password, "UTF-8");
        }catch (UnsupportedEncodingException e){

            throw new MySQLException("password encode failed! ");

        }



        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password);
    }

    /*public static void main(String[] args) {
        String propFileName = "config.properties";
        System.out.println(Item.class.getClassLoader().getResource(""));//classpath
        System.out.println(Item.class.getResource(""));//Item.class的位置

        try (InputStream inputStream = Item.class.getClassLoader().getResourceAsStream(propFileName)) {
            // 定义1000个字节大小的缓冲区:
            byte[] buffer = new byte[1000];
            int n;


            while ((n = inputStream.read(buffer)) != -1) { // 读取到缓冲区
                System.out.println((char) n);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }*/


}
