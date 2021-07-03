package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.FavoriteRequestBody;
import com.laioffer.jupiter.entity.User;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", value = "/register")
public class RegisterServlet extends HttpServlet {
/*    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Read user data from the request body
      /*  ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(request.getReader(), User.class);*/

        // Read user data from the request body and map the body to our defined obj
        User user= ServletUtil.readRequestBody(User.class, request);


        if (user == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean isUserAdded = false;

        try(MySQLConnection conn=new MySQLConnection()) {
            user.setPassword(ServletUtil.encryptPassword(user.getUserId(),user.getPassword()));

            isUserAdded=conn.addUser(user);
        }catch (MySQLException e) {
            throw new ServletException(e);


        }

        if (!isUserAdded) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);//重复insert
            //由于和被请求的资源的当前状态之间存在冲突，请求无法完成。一般是update信息有冲突！


        }
    }


}




