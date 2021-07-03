package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Item;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

public class ServletUtil {


    /*
     * 复用结构  简化代码
     * refactor
     * */
    public static <T> void writeData(HttpServletResponse response, T data) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        //ObjectMapper可以将任何java built-in的数据结构转化为json格式
        response.getWriter().print(new ObjectMapper().writeValueAsString(data));


    }

    // Help encrypt the user password before save to the database
    public static String encryptPassword(String userId, String password) {
        return DigestUtils.md5Hex(userId + DigestUtils.md5Hex(password)).toLowerCase();
        //万一以后换了库，有可能生成大小写重复的密码 所以统一搞成小写
    }


    /*
     * 复用结构  简化代码
     *refactor
     * */
    public static <T> T readRequestBody(Class<T> cl, HttpServletRequest rq) throws IOException {

        // Read user data from the request body and map to our object

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(rq.getReader(), cl);
        } catch (JsonParseException | JsonMappingException e) {//handle these two exceptions and leave other io exceptions to method throws

            return null;// null object
        }


    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        //因为上面没有reverse的comparator，可以手动reverse下
        Collections.reverse(list);

        //LinkedHashMap会保留插入的顺序，复杂度和无序 的hashmap 是一个数量级
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }




}
